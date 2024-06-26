/**
  * Just for debug
  */

package darecreek

import chisel3._
import chisel3.util._
import utils._

class VRvfiBlock extends Module {
  val io = IO(new Bundle {
    // from ROB
    val commits = Flipped(new VRobCommitIO)
    val sb_id = Input(UInt(5.W))
    val commitEnd = Input(Bool())
    // for rd
    val ovi_issue_valid = Input(Bool())
    val ovi_issue_sb_id = Input(UInt(5.W))
    val ovi_issue_inst = Input(UInt(32.W))
    val ovi_completed = Flipped(new OVIcompleted)
    // read RF
    val rfRd = Vec(VCommitWidth, Flipped(new VRFReadPort(LaneWidth)))
    // output RVFI signals
    val rvfi = Output(new VRvfi)
  })

  val validIn = io.commits.valid(0)
  val commitEnd = io.commitEnd
  // val rvfi = RegInit(0.U.asTypeOf(new VRvfi))
  io.rvfi.valid := RegNext(validIn && commitEnd)
  io.rvfi.sb_id := RegNext(io.sb_id)

  val expdCnt = RegInit(0.U(4.W))
  when (validIn) {
    when (commitEnd) {
      expdCnt := 0.U
    }.otherwise {
      expdCnt := expdCnt + PopCount(io.commits.valid)
    }
  }

  // read RF
  io.rfRd zip io.commits.info map {case (r, c) => r.addr := c.pdest}
  val rfRdData = io.rfRd.map(x => Cat(x.data.reverse))
  // compose data out
  val dout = RegInit(VecInit(Seq.fill(8)(0.U(VLEN.W))))
  when (validIn && io.commits.info(0).pdestVal) {
    dout(expdCnt) := rfRdData(0)
    when (io.commits.valid(1) && io.commits.info(1).pdestVal) {
      dout(expdCnt + 1.U) := rfRdData(1)
    }
  }
  io.rvfi.vd_wdata := Cat(dout.reverse)

  // ldest
  val ldest = Reg(UInt(5.W))
  when (expdCnt === 0.U && validIn) {
    ldest := io.commits.info(0).ldest
  }
  io.rvfi.vd_addr := ldest

  // emul (some instructions may fail)
  val emul = Reg(UInt(3.W))
  when (validIn && commitEnd) {
    emul := MuxCase(4.U, Seq(
      (expdCnt === 0.U) -> Mux(io.commits.valid(1) && io.commits.info(1).pdestVal, 1.U, 0.U),
      (expdCnt === 2.U) -> 2.U,
      (expdCnt === 6.U) -> 3.U,
    ))
  }.otherwise {
    emul := 0.U
  }
  assert(emul =/= 4.U, "Error: Emul of RVFI interface is not a valid value! (RvfiBlock.scala)" )
  io.rvfi.vd_emul := emul

  //---- rvif rd handling ----
  // A buffer to store sb_id and rd
  class rdBufEntry extends Bundle {
    val rdAddr = UInt(5.W)
    val rd = UInt(XLEN.W)
    val fflags = UInt(5.W)
    val vxsat = Bool()
  }
  val rdBuf = Reg(Vec(VQSize, new rdBufEntry))
  when (io.ovi_issue_valid) {
    rdBuf(io.ovi_issue_sb_id).rdAddr := io.ovi_issue_inst(11, 7)
  }
  when (io.ovi_completed.valid) {
    rdBuf(io.ovi_completed.sb_id).rd := io.ovi_completed.dest_reg
    rdBuf(io.ovi_completed.sb_id).vxsat := io.ovi_completed.vxsat
    rdBuf(io.ovi_completed.sb_id).fflags := io.ovi_completed.fflags
  } 
  io.rvfi.rd_addr := rdBuf(io.rvfi.sb_id).rdAddr
  io.rvfi.rd_wdata := rdBuf(io.rvfi.sb_id).rd
  io.rvfi.vxsat := rdBuf(io.rvfi.sb_id).vxsat
  io.rvfi.fflags := rdBuf(io.rvfi.sb_id).fflags
}