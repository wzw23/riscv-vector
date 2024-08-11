package cypto
import chisel3._
import chisel3.util._
import utils._
import darecreek._
import chisel3._
import darecreek.exu.vfucore.div._

class aes_sm4_wrapper extends BlackBox with HasBlackBoxResource{
  val io = IO(new Bundle{
    val is_vsm4k = Input(Bool())
    val is_vsm4r = Input(Bool())
    val is_vaesem = Input(Bool())
    val is_vaesef = Input(Bool())
    val is_vaesdm = Input(Bool())
    val is_vaesdf = Input(Bool())
    val data1= Input(UInt(128.W))
    val data2= Input(UInt(128.W))
    val rs1= Input(UInt(5.W))
    val result = Output(UInt(128.W))
  })
  addResource("vsrc/aes_sm4_wrapper.v")
  addResource("vsrc/aes_en_de.v")
  addResource("vsrc/sbox4_new.v")
  addResource("vsrc/aes_sbox_new.v")
  addResource("vsrc/sm4_ende_wrapper.v")
  addResource("vsrc/sm4_ende_round.v")
  addResource("vsrc/sm4_key_round.v")
  addResource("vsrc/sm4_key_wrapper.v")
}

class AES_SM4 extends Module{
  val io = IO(new Bundle{
    val is_vsm4k = Input(Bool())
    val is_vsm4r = Input(Bool())
    val is_vaesem = Input(Bool())
    val is_vaesef = Input(Bool())
    val is_vaesdm = Input(Bool())
    val is_vaesdf = Input(Bool())
    val data1= Input(UInt(128.W))
    val data2= Input(UInt(128.W))
    val rs1= Input(UInt(5.W))
    val result = Output(UInt(128.W))
  })
  val aes_sm4_wrapper  = Module(new aes_sm4_wrapper)
  aes_sm4_wrapper .io <> io
  }