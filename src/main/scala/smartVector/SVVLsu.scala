package smartVector
import chisel3._
import chisel3.util._
import darecreek.VDecode
import utils._
import freechips.rocketchip.rocket._
import freechips.rocketchip.util._
import chipsalliance.rocketchip.config.{Config, Field, Parameters}
import xiangshan.MicroOp
import SmartParam._

class SVVLsu(implicit p: Parameters) extends Module {
    val io = IO(new LdstIO())

    // * BEGIN
    // * signal define

    // save last addr
    val addrReg = RegInit(0.U(addrWidth.W))

    // ldQueue
    val canEnqueue      = WireInit(false.B)
    val ldstEnqPtr      = RegInit(0.U(vLdstUopQueueWidth.W))
    val issueLdstPtr    = RegInit(0.U(vLdstUopQueueWidth.W))
    val commitPtr       = RegInit(0.U(vLdstUopQueueWidth.W))
    val ldstUopQueue    = RegInit(VecInit(Seq.fill(vLdstUopQueueSize)(0.U.asTypeOf(new SegLdstUop))))

    // * signal define
    // * END

    val ldstQueueFull = ldstUopQueue.zipWithIndex.map { case (uop, i) => uop.valid }.reduce(_ && _)
    io.lsuReady := !ldstQueueFull

    // decode nfield / indexed / unit-stride / strided
    val (vstart, vl)     = (io.mUop.bits.uop.info.vstart, io.mUop.bits.uop.info.vl)
    val (uopIdx, uopEnd) = (io.mUop.bits.uop.uopIdx, io.mUop.bits.uop.uopEnd)
    
    val ldstCtrl = LSULdstDecoder(io.mUop.bits, io.mUopMergeAttr.bits)
    val mUopInfo = mUopInfoSelecter(io.mUop.bits, io.mUopMergeAttr.bits)

    // * BEGIN
    // * Calculate Addr
    val addr        = WireInit(0.U(addrWidth.W))
    val addrMask    = WireInit(0.U(addrWidth.W))
    val alignedAddr = WireInit(0.U(addrWidth.W))
    val offset      = WireInit(0.U(log2Ceil(addrWidth / 8).W))

    val curVl       = uopIdx
    val baseAddr    = mUopInfo.rs1Val

    // indexed addr
    val idxVal      = WireInit(0.U(XLEN.W))
    val idxMask     = WireInit(0.U(XLEN.W))
    val eew         = ldstCtrl.eewb << 3.U // change eew byte to eew bit
    val beginIdx    = (curVl  - ((curVl >> ldstCtrl.log2Elen) << ldstCtrl.log2Elen)) << (ldstCtrl.log2Eewb +& 3.U)
    idxMask        := (("h1".asUInt(addrWidth.W) << eew) - 1.U)
    idxVal         := (mUopInfo.vs2 >> beginIdx) & idxMask

    // strided addr
    val stride      = WireInit(0.S(XLEN.W))
    val negStride   = stride < 0.S
    val strideAbs   = Mux(negStride, (-stride).asUInt, stride.asUInt)

    stride         := Mux(ldstCtrl.ldstType === Mop.constant_stride, 
                            mUopInfo.rs2Val.asSInt, 11111.S)

    val validLdstSegReq = io.mUop.valid && io.mUop.bits.uop.ctrl.isLdst && ldstCtrl.nfield > 1.U

    when (validLdstSegReq) {
        when (ldstCtrl.ldstType === Mop.unit_stride) {
            when (mUopInfo.segIdx === 0.U && mUopInfo.uopIdx === 0.U) {
                addr := baseAddr
            }.otherwise {
                addr := addrReg + ldstCtrl.memwb
            }
            addrReg := addr
        }.elsewhen (ldstCtrl.ldstType === Mop.constant_stride) {
            when (mUopInfo.segIdx === 0.U && mUopInfo.uopIdx === 0.U) {
                addr := baseAddr
                addrReg := addr
            }.elsewhen (mUopInfo.segIdx === 0.U) {
                addr := Mux(negStride, addrReg - strideAbs, addrReg + strideAbs)
                addrReg := addr
            }.otherwise {
                addr := addrReg + (mUopInfo.segIdx << ldstCtrl.log2Memwb)
            }
        }.elsewhen (ldstCtrl.ldstType === Mop.index_ordered || ldstCtrl.ldstType === Mop.index_unodered) {
            when (mUopInfo.segIdx === 0.U) {
                addr := baseAddr + idxVal
                addrReg := addr
            }.otherwise {
                addr := addrReg + ldstCtrl.memwb
            }
        }.otherwise {
            addr := 0.U
        }
    }.otherwise {
        addr := 0.U
    }

    addrMask     := (("h1".asUInt(addrWidth.W) << ldstCtrl.log2Memwb) - 1.U)
    alignedAddr  := (addr >> (log2Ceil(dataWidth / 8)).U) << (log2Ceil(dataWidth / 8)).U // align addr to 64 bits
    offset       := addr - alignedAddr
   
    val addrMisalign = (addr & addrMask).orR  // align addr to memwb ?
    
    // * Calculate Addr
    // * END


    // store data to queue
    val memw     = ldstCtrl.memwb << 3.U
    val destElem = (curVl  - ((curVl >> ldstCtrl.log2Mlen) << ldstCtrl.log2Mlen))
    val destMask = (1.U << memw) - 1.U
    val destData = (io.mUop.bits.uopRegInfo.old_vd >> (destElem << (ldstCtrl.log2Memwb +& 3.U))) & destMask
    
    // push request to queue
    
    // * if vm=1 => not masked
    // * if vm=0 =>
    // *          v0(i) = 1 => not masked
    // *          v0(i) = 0 => masked
    val isMasked = Mux(ldstCtrl.vm, false.B, !mUopInfo.mask(curVl))
    canEnqueue  := validLdstSegReq && !isMasked && curVl >= vstart && curVl < vl
    
    val misalignXcpt    = 0.U.asTypeOf(new HellaCacheExceptions)
    misalignXcpt.ma.ld := ldstCtrl.isLoad  && addrMisalign
    misalignXcpt.ma.st := ldstCtrl.isStore && addrMisalign

    when (canEnqueue) {
        ldstUopQueue(ldstEnqPtr).valid                  := true.B
        ldstUopQueue(ldstEnqPtr).status                 := Mux(addrMisalign, LdstUopStatus.ready, LdstUopStatus.notReady)
        ldstUopQueue(ldstEnqPtr).memOp                  := ldstCtrl.isStore
        ldstUopQueue(ldstEnqPtr).addr                   := alignedAddr
        ldstUopQueue(ldstEnqPtr).pos                    := curVl
        ldstUopQueue(ldstEnqPtr).offset                 := offset
        ldstUopQueue(ldstEnqPtr).size                   := ldstCtrl.log2Memwb
        ldstUopQueue(ldstEnqPtr).destElem               := destElem
        ldstUopQueue(ldstEnqPtr).data                   := Mux(ldstCtrl.isStore, destData, ldstUopQueue(ldstEnqPtr).data)
        ldstUopQueue(ldstEnqPtr).commitInfo.muopEnd     := uopEnd
        ldstUopQueue(ldstEnqPtr).commitInfo.rfWriteEn   := mUopInfo.rfWriteEn
        ldstUopQueue(ldstEnqPtr).commitInfo.rfWriteIdx  := mUopInfo.ldest
        ldstUopQueue(ldstEnqPtr).commitInfo.isFof       := ldstCtrl.unitSMop === UnitStrideMop.fault_only_first
        ldstUopQueue(ldstEnqPtr).commitInfo.xcpt        := misalignXcpt

        ldstEnqPtr  := ldstEnqPtr + 1.U
    }


    // * BEGIN
    // * Issue LdstUop

    val isNoXcptUop = ldstUopQueue(issueLdstPtr).valid && (ldstUopQueue(issueLdstPtr).commitInfo.xcpt.asUInt.orR === 0.U)
    
    when (io.dataExchange.resp.bits.nack && io.dataExchange.resp.bits.idx(3, 0) <= issueLdstPtr) {
        issueLdstPtr := io.dataExchange.resp.bits.idx(3, 0)
    }.elsewhen (isNoXcptUop) {
        issueLdstPtr := issueLdstPtr + 1.U // NOTE: exsits multiple issues for the same uop
    }

    // TODO: store waiting resp
    when (isNoXcptUop) {
        val data    = ldstUopQueue(issueLdstPtr).data
        val dataSz  = (1.U << ldstUopQueue(issueLdstPtr).size)
        val offset  = ldstUopQueue(issueLdstPtr).offset

        val wData = data << (offset << 3.U)
        val wMask = VecInit(Seq.fill(8)(0.U(1.W)))

        for (i <- 0 until 8) {
            // 1 to write, 0 to skip
            wMask(i) := Mux(i.U >= offset && i.U < offset + dataSz, 1.U, 0.U)
        }

        val memOp = ldstUopQueue(issueLdstPtr).memOp
        io.dataExchange.req.valid       := true.B
        io.dataExchange.req.bits.addr   := ldstUopQueue(issueLdstPtr).addr
        io.dataExchange.req.bits.cmd    := memOp
        io.dataExchange.req.bits.idx    := (1 << 4).U | issueLdstPtr // to figure out hlsu or vlsu
        io.dataExchange.req.bits.data   := Mux(memOp, wData, DontCare)
        io.dataExchange.req.bits.mask   := Mux(memOp, wMask.asUInt, DontCare)
    }.otherwise {
        io.dataExchange.req.valid       := false.B
        io.dataExchange.req.bits        := DontCare
    }
    // * Issue LdstUop
    // * END


    // * BEGIN
    // * Recv Resp
    val (respLdstPtr, respData) = (io.dataExchange.resp.bits.idx(3, 0), io.dataExchange.resp.bits.data)

    when (io.dataExchange.resp.valid) {
        val isLoadResp = ldstUopQueue(respLdstPtr).memOp === VMemCmd.read
        val isLoadRespDataValid = io.dataExchange.resp.bits.has_data
        val loadComplete  = isLoadResp && isLoadRespDataValid

        val dataSz = (1.U << ldstUopQueue(respLdstPtr).size)
        val ldData = WireInit(0.U(64.W))
        val offset = ldstUopQueue(respLdstPtr).offset
        // ldData := io.dataExchange.resp.bits.data((offset + dataSz) << 3.U - 1.U, offset << 3.U)
        ldData := (respData >> (offset << 3.U)) & ((1.U << (dataSz << 3.U)) - 1.U)

        ldstUopQueue(respLdstPtr).data   := Mux(loadComplete, ldData, ldstUopQueue(respLdstPtr).data)
        ldstUopQueue(respLdstPtr).status := LdstUopStatus.ready
        ldstUopQueue(respLdstPtr).commitInfo.xcpt := io.dataExchange.xcpt
    }

    // * Recv Resp
    // * END


    // * BEGIN
    // * Commit
    val canCommit  = ldstUopQueue(commitPtr).valid && ldstUopQueue(commitPtr).status === LdstUopStatus.ready
    val commitXcpt = canCommit && ldstUopQueue(commitPtr).commitInfo.xcpt.asUInt.orR
    

    when (commitXcpt) {
        val xcptVl = ldstUopQueue(commitPtr).pos
        when(ldstUopQueue(commitPtr).commitInfo.isFof && xcptVl > 0.U) {
            io.xcpt.exception_vld   := false.B
            io.xcpt.xcpt_cause      := 0.U.asTypeOf(new HellaCacheExceptions)
            io.xcpt.update_vl       := true.B
            io.xcpt.update_data     := 0.U
        }.otherwise {
            io.xcpt.exception_vld   := true.B
            io.xcpt.xcpt_cause      := ldstUopQueue(commitPtr).commitInfo.xcpt
            io.xcpt.update_vl       := false.B
            io.xcpt.update_data     := 0.U
        }

        for (i <- 0 until vLdstUopQueueSize) {
            ldstUopQueue(i) := 0.U.asTypeOf(new SegLdstUop)
        }
    }.otherwise {
        io.xcpt.exception_vld   := false.B
        io.xcpt.xcpt_cause      := 0.U.asTypeOf(new HellaCacheExceptions)
        io.xcpt.update_vl       := false.B
        io.xcpt.update_data     := 0.U
    }

    when (canCommit && !commitXcpt) {
        val destElem     = ldstUopQueue(commitPtr).destElem
        val data        = ldstUopQueue(commitPtr).data
        val dataSz      = (1.U << ldstUopQueue(commitPtr).size)
        val log2DataSz  = ldstUopQueue(commitPtr).size
    
        val wData       = data << ((destElem << log2DataSz) << 3.U)
        val wMask       = VecInit(Seq.fill(vlenb)(0.U(1.W)))

        for (i <- 0 until vlenb) {
            wMask(i) := ~(i.U >= (destElem << log2DataSz) && i.U < (destElem << log2DataSz) + dataSz)
        }

        io.lsuOut.valid             := true.B
        io.lsuOut.bits.muopEnd      := ldstUopQueue(commitPtr).commitInfo.muopEnd
        io.lsuOut.bits.rfWriteEn    := ldstUopQueue(commitPtr).commitInfo.rfWriteEn
        io.lsuOut.bits.rfWriteIdx   := ldstUopQueue(commitPtr).commitInfo.rfWriteIdx
        io.lsuOut.bits.data         := wData
        io.lsuOut.bits.rfWriteMask  := wMask.asUInt
    }.otherwise {
        io.lsuOut.valid             := false.B
        io.lsuOut.bits              := DontCare
        io.lsuOut.bits.rfWriteEn    := false.B
    }

    when (io.lsuOut.fire) {
        commitPtr                      := commitPtr + 1.U
        ldstUopQueue(commitPtr).status := LdstUopStatus.notReady
        ldstUopQueue(commitPtr).valid  := false.B
    }
    // * Commit
    // * END

}