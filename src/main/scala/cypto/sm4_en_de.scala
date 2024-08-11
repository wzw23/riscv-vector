package cypto
import chisel3._
import chisel3.util._
import utils._
import darecreek._
import chisel3._
import darecreek.exu.vfucore.div._

class four_round_for_encdec extends BlackBox with HasBlackBoxResource{
  val io = IO(new Bundle{
    val data_in = Input(UInt(128.W))
    val round_key_in= Input(UInt(128.W))
    val result_out = Output(UInt(128.W))
  })

  addResource("vsrc/sm4_ende_wrapper.v")
  addResource("vsrc/sm4_ende_round.v")
  addResource("vsrc/aes_sbox_new.v")
}

class SM4_EN_DE extends Module{
  val io = IO(new Bundle{
    val data_in = Input(UInt(128.W))
    val round_key_in= Input(UInt(128.W))
    val result_out = Output(UInt(128.W))
  })
  val sm4_core_inst = Module(new four_round_for_encdec)
  sm4_core_inst .io <> io
  }

