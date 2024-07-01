package cypto
import chisel3._
import chisel3.util._
import utils._
import darecreek._
import chisel3._
import darecreek.exu.vfucore.div._

class aes_en_de extends BlackBox with HasBlackBoxResource{
  val io = IO(new Bundle{
    val en_de = Input(Bool())
    val last = Input(Bool())

    val block = Input(UInt(128.W))
    val round_key= Output(UInt(128.W))
    val new_block = Output(UInt(128.W))
  })

  addResource("/aes_en_de.v")
  addResource("/aes_4sbox.v")
  addResource("/aes_4inv_sbox.v")
}

class Aes extends Module{
  val io = IO(new Bundle{
    val en_de = Input(Bool())
    val last = Input(Bool())

    val block = Input(UInt(128.W))
    val round_key= Output(UInt(128.W))
    val new_block = Output(UInt(128.W))
  })
  val aes_core_inst = Module(new aes_en_de)
  aes_core_inst.io <> io
  }

