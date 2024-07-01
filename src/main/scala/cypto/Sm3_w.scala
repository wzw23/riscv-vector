package cypto
import chisel3._
import chisel3.util._
import utils._
import darecreek._
import chisel3._


class Sm3_w extends Module{
    val io = IO(new Bundle{
      val w_in = Input(Vec(16,UInt(32.W)))
      val w_out = Output(Vec(8,UInt(32.W)))
    })

  def ROL32(x: UInt, n: Int): UInt = {
    (x << n) | (x >> (32 - n))
  }

  def P_1(x: UInt): UInt = {
    x ^ ROL32(x, 15) ^ ROL32(x, 23)
  }

  def ZVKSH_W(M16: UInt, M9: UInt, M3: UInt, M13: UInt, M6: UInt): UInt = {
    P_1(M16 ^ M9 ^ ROL32(M3, 15)) ^ ROL32(M13, 7) ^ M6
  }
  for (i <- 0 until 8) {
    io.w_out(i) := ZVKSH_W(io.w_in(i - 16), io.w_in(i - 9), io.w_in(i - 3), io.w_in(i - 13), io.w_in(i - 6))
  }
}

