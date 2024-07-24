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
import vcix._
import chisel3.util.experimental.decode.TruthTable


class CustomOutput extends Bundle{
    val rfWriteEn = Bool() 
    val rfWriteIdx = UInt(5.W)
    val data = UInt(VLEN.W)
    val muopEnd = Bool()
}
class SVcustomIO(implicit p: Parameters) extends ParameterizedBundle()(p) {
   val mUop            = Input(ValidIO(new Muop()(p)))
   val mUopMergeAttr   = Input(ValidIO(new MuopMergeAttr))
   val customout       = Output(ValidIO(new CustomOutput))
   val vcix            = new(VcixIO)
   val customReady     = Output(Bool())
}
class SVcustom(implicit p: Parameters) extends Module {
    val io = IO(new SVcustomIO);
    val q = Module(new Queue(new SaveCustomMessage ,entries = 4))
    io.vcix.req.valid := io.mUop.valid && io.mUop.bits.uop.ctrl.custom
    io.customReady := io.vcix.req.ready && q.io.enq.ready
    //需要根据译码信息对data1 data2 data3的值进行选择
    //data1=>funct3:100->xs1 011->simm 101->fs1 000->vs1
    io.vcix.req.bits.data1 := MuxLookup(io.mUop.bits.uop.ctrl.funct3,io.mUop.bits.uopRegInfo.vs1,Seq(
                                        "b000".U -> io.mUop.bits.uopRegInfo.vs1,
                                        "b100".U -> io.mUop.bits.scalar_opnd_1,
                                        "b101".U -> io.mUop.bits.scalar_opnd_1,
                                        "b011".U -> io.mUop.bits.uop.ctrl.vs1_imm
    ))
    //data2=>vs2
    io.vcix.req.bits.data2 := io.mUop.bits.uopRegInfo.vs2
    //data3=>old_vd
    io.vcix.req.bits.data3 := io.mUop.bits.uopRegInfo.old_vd

    io.vcix.req.bits.funct7 := Cat(io.mUop.bits.uop.ctrl.funct6,io.mUop.bits.uop.ctrl.vm)
    io.vcix.req.bits.funct3 := io.mUop.bits.uop.ctrl.funct3
    io.vcix.req.bits.rs1 := io.mUop.bits.uop.ctrl.vs1_imm
    io.vcix.req.bits.rs2 := io.mUop.bits.uop.ctrl.vs2
    io.vcix.req.bits.rd := io.mUopMergeAttr.bits.ldest
    val shift_vl = MuxLookup(io.mUop.bits.uop.info.vsew,0.U,Seq(
        0.U -> 3.U(3.W),
        1.U -> 4.U(3.W),
        2.U -> 5.U(3.W),
        3.U -> 6.U(3.W)
    ))
    io.vcix.req.bits.vl_in_bytpes_minus_1 := (io.mUop.bits.uop.info.vl >> shift_vl -1.U)
    io.vcix.req.bits.vlmul := io.mUop.bits.uop.info.vlmul
    io.vcix.req.bits.vsew := io.mUop.bits.uop.info.vsew
    //建立一个fifo,用来传递写回的地址 以及通过vm存储是否进行写回
    // io.customout.bits.
    class SaveCustomMessage extends Bundle{
        val rfWriteIdx= UInt(5.W)
        val muopEnd = Bool()
        val vm = Bool()
    }
    //写入queue队列的肯定是写回向量寄存器的
    q.io.enq.valid := io.vcix.req.fire && (~io.mUop.bits.uop.ctrl.vm.asBool)
    q.io.enq.bits.rfWriteIdx := io.mUopMergeAttr.bits.regDstIdx
    q.io.enq.bits.muopEnd := io.mUopMergeAttr.bits.muopEnd
    q.io.enq.bits.vm := io.mUop.bits.uop.ctrl.vm.asBool
    //由于vmerge不能并行接收多条指令，所以ready信号一直拉高
    io.vcix.response.ready := true.B
    //当vcix返回数据时或不写回时直接出队
    q.io.deq.ready := io.vcix.response.fire || (q.io.deq.bits.vm)
    //当不写回数据时或者custom完成写回时写回向量寄存器
    io.customout.valid := q.io.deq.fire
    io.customout.bits.muopEnd := q.io.deq.bits.muopEnd
    io.customout.bits.rfWriteEn := ~q.io.deq.bits.vm
    io.customout.bits.data := io.vcix.response.bits.resp_bits_data
    io.customout.bits.rfWriteIdx := q.io.deq.bits.rfWriteIdx
}