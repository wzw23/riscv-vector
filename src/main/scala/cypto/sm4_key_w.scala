package cypto
import chisel3._
import chisel3.util._
import utils._
import darecreek._
import chisel3._
import darecreek.exu.vfucore.div._

class four_round_for_key_exp extends BlackBox with HasBlackBoxResource{
  val io = IO(new Bundle{
    val count_round_in = Input(UInt(5.W))
    val data_in = Input(UInt(128.W))
    val result_out = Output(UInt(128.W))
  })

  addResource("vsrc/sm4_key_wrapper.v")
  addResource("vsrc/aes_sbox_new.v")
}

class SM4_KEY extends Module{
  val io = IO(new Bundle{
    val data_in = Input(UInt(128.W))
    val count_round_in = Input(UInt(5.W))
    val result_out = Output(UInt(128.W))
  })

  val sm4_key = Module(new four_round_for_key_exp)
  sm4_key.io <> io
}

