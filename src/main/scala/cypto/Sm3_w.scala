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
    (P_1(M16 ^ M9 ^ ROL32(M3, 15)(31,0)) ^ ROL32(M13, 7)(31,0)(31,0) ^ M6)(31,0)
  }
  io.w_out(0) := ZVKSH_W(io.w_in(0), io.w_in(7), io.w_in(13), io.w_in(3), io.w_in(10)) //16
  io.w_out(1) := ZVKSH_W(io.w_in(1), io.w_in(8), io.w_in(14), io.w_in(4), io.w_in(11)) //17
  io.w_out(2) := ZVKSH_W(io.w_in(2), io.w_in(9), io.w_in(15), io.w_in(5), io.w_in(12)) //18
  io.w_out(3) := ZVKSH_W(io.w_in(3), io.w_in(10), io.w_out(0), io.w_in(6), io.w_in(13)) //19
  io.w_out(4) := ZVKSH_W(io.w_in(4), io.w_in(11), io.w_out(1), io.w_in(7), io.w_in(14)) //20
  io.w_out(5) := ZVKSH_W(io.w_in(5), io.w_in(12), io.w_out(2), io.w_in(8), io.w_in(15)) //21
  io.w_out(6) := ZVKSH_W(io.w_in(6), io.w_in(13), io.w_out(3), io.w_in(9), io.w_out(0)) //22
  io.w_out(7) := ZVKSH_W(io.w_in(7), io.w_in(14), io.w_out(4), io.w_in(10), io.w_out(1)) //23
}

