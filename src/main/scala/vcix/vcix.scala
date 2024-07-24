package vcix
import chisel3._
import chisel3.util._
import utils._
import darecreek._
import chisel3._

class RequestIO extends Bundle {
    val data1 = UInt(128.W)
    val data2 = UInt(128.W)
    val data3 = UInt(256.W)
    val funct7 = UInt(7.W)
    val rs2 = UInt(5.W)
    val rs1 = UInt(5.W)
    val funct3 = UInt(3.W)
    val rd = UInt(5.W)
    val vsew = UInt(3.W)
    val vlmul = UInt(3.W)
    val vl_in_bytpes_minus_1 = UInt(7.W)
}

class ResponseIO extends Bundle{
    val resp_bits_data = UInt(256.W)
}

class VcixIO extends Bundle{
    val req = (Decoupled(new RequestIO()))
    val response = Flipped(Decoupled(new ResponseIO))
}