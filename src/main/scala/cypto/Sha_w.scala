package cypto
import chisel3._
import chisel3.util._
import utils._
import darecreek._
import chisel3._


class Sha_w extends Module{
    val io = IO(new Bundle{
        val vs1_in = Input(UInt(128.W)) 
        val vs2_in = Input(UInt(128.W))
        val vd_in  = Input(UInt(128.W))
        val vd_out = Output(UInt(128.W))
    })

  def ROTR(x: UInt, n: Int): UInt = (x >> n) | (x << (32 - n))
  def SHR(x: UInt, n: Int): UInt = x >> n

  def sig0(x: UInt): UInt = ROTR(x, 7) ^ ROTR(x, 18) ^ SHR(x, 3)
  def sig1(x: UInt): UInt = ROTR(x, 17) ^ ROTR(x, 19) ^ SHR(x, 10)

  // SHA operations
  val W_0  = io.vd_in(31,0)
  val W_1  = io.vd_in(63,32)
  val W_2  = io.vd_in(95,64)
  val W_3  = io.vd_in(127,96)
  val W_4  = io.vs1_in(31,0)
  val W_9  = io.vs1_in(63,32)
  val W_10 = io.vs1_in(95,64)
  val W_11 = io.vs1_in(127,96)
  val W_12 = io.vs2_in(31,0)
  val W_13 = io.vs2_in(63,32)
  val W_14 = io.vs2_in(95,64)
  val W_15 = io.vs2_in(127,96)

  // Compute new values and store them
  val W_16 = sig1(W_14) + W_9 +  sig0(W_1) + W_0
  val W_17 = sig1(W_15) + W_10 + sig0(W_2) + W_1
  val W_18 = sig1(W_16) + W_11 + sig0(W_3) + W_2
  val W_19 = sig1(W_17) + W_12 + sig0(W_4) + W_3

  io.vd_out := Cat(W_19, W_18, W_17, W_16).asUInt
}

