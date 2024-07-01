package cypto
import chisel3._
import chisel3.util._
import utils._
import darecreek._
import chisel3._
import darecreek.exu.vfucore.div._

class aes_key_w extends BlackBox with HasBlackBoxResource{
  val io = IO(new Bundle{
    val key = Input(UInt(128.W))
    val round = Input(UInt(3.W))
    val round_key = Output(UInt(128.W))
  })

  addResource("/aes_key_w.v")
}

class Aes_key_wa extends Module{

  val io = IO(new Bundle{
    val key = Input(UInt(128.W))
    val round = Input(UInt(3.W))
    val round_key = Output(UInt(128.W))
  })

  val aes_key = Module(new aes_key_w)
  aes_key.io <> io
  }

object Main extends App {
  println("Generating the Sha_w hardware")
  emitVerilog(new Aes_key_wa(), Array("--target-dir", "generated"))
}