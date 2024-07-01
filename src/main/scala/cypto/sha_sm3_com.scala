package cypto
import chisel3._
import chisel3.util._
import utils._
import darecreek._
import chisel3._
import darecreek.exu.vfucore.div._


class Sha_sm3_com extends Module{
    val io = IO(new Bundle{
        //sel = 1:sha else sm3
        val sel  = Input(Bool())
        val a_in = Input(UInt(32.W)) 
        val b_in = Input(UInt(32.W))
        val c_in = Input(UInt(32.W))
        val d_in = Input(UInt(32.W))
        val e_in = Input(UInt(32.W))
        val f_in = Input(UInt(32.W))
        val g_in = Input(UInt(32.W))
        val h_in = Input(UInt(32.W))
        val W_in = Input(UInt(32.W))
        val Wx_in = Input(UInt(32.W))//sha:wx_in = k[i] sm3:wx_in = w*
        val round_in = Input(UInt(5.W))
        val a_out = Output(UInt(32.W))
        val b_out = Output(UInt(32.W))
        val c_out = Output(UInt(32.W))
        val d_out = Output(UInt(32.W))
        val e_out = Output(UInt(32.W))
        val f_out = Output(UInt(32.W))
        val g_out = Output(UInt(32.W))
        val h_out = Output(UInt(32.W))
    })
    //sm
    def FF1(X: UInt, Y: UInt, Z: UInt): UInt = X ^ Y ^ Z
    def FF2(X: UInt, Y: UInt, Z: UInt): UInt = (X & Y) | (X & Z) | (Y & Z)
    def FF_j(X: UInt, Y: UInt, Z: UInt, j: UInt): UInt = Mux(j <= 15.U, FF1(X, Y, Z), FF2(X, Y, Z))

    def GG1(X: UInt, Y: UInt, Z: UInt): UInt = X ^ Y ^ Z
    def GG2(X: UInt, Y: UInt, Z: UInt): UInt = (X & Y) | (~X & Z)
    def GG_j(X: UInt, Y: UInt, Z: UInt, j: UInt): UInt = Mux(j <= 15.U, GG1(X, Y, Z), GG2(X, Y, Z))

    def T_j(j: UInt): UInt = Mux(j <= 15.U, 0x79CC4519.U(32.W), 0x7A879D8A.U(32.W))
    def P_0(X: UInt): UInt = X ^ (X.rotateLeft(9)) ^ (X.rotateLeft(17))
    //sha
    def ROTR(x: UInt, n: Int): UInt = (x >> n) | (x << (32 - n))
    def MAJ(x: UInt, y: UInt, z: UInt): UInt = (x & y) ^ (x & z) ^ (y & z)
    def CH(x: UInt, y: UInt, z: UInt): UInt = (x & y) ^ (~x & z)
    def SUM0(x:UInt) : UInt = ROTR(x, 2) ^ ROTR(x, 13) ^ ROTR(x, 22)
    def SUM1(x:UInt) : UInt = ROTR(x, 6) ^ ROTR(x, 11) ^ ROTR(x, 25)
    //SS1 SS2
    val csa = Module(new C32_32())
    val a_in_rotateLeft = io.a_in.rotateLeft(12)
    csa.io.in(0) := a_in_rotateLeft
    csa.io.in(1) := io.e_in
    csa.io.in(2) := T_j(io.round_in%(32.U))
    val ss1 = csa.io.out(0) + (csa.io.out(1) << 1)
    val ss2 = ss1 ^ (a_in_rotateLeft)

    // B C D G H F 赋值
    io.h_out := io.g_in
    io.g_out := Mux(io.sel,io.f_in,io.f_in.rotateLeft(19))
    io.f_out := io.e_in
    io.b_out := io.a_in
    io.c_out := Mux(io.sel,io.b_in,io.b_in.rotateLeft(19))
    io.d_out := io.c_in


    //A E 赋值
    val c52_32 = Module(new C52_32())
    val c42_32 = Module(new C42_32())
    val c32_32 = Module(new C32_32())
    //sha2:a = T1 + T2 ;T1 = h + sum1(e) + ch(e,f,g) + W1; T2 = sum0(a) + maj(a,b,c)
    //a = sum0(a) + maj(a,b,c) + (h + sum1(e) + ch(e,f,g) + W1 + K).out
    //sm3: A = tt1 = FF_j(ABCj) + D + ss2 + x0 x0 = w0 ^ w4
    //sm3: A= FF_j(ABCj) + D + ss2 + w*

    //sha2: E = d + T1
    //sm3: E = p_0(TT2) TT2=GG_j(E,F,G,j) + H + SS1 + W + 0
    c52_32.io.in(0) := io.h_in
    c52_32.io.in(1) := Mux(io.sel,SUM1(io.e_in),ss1)
    c52_32.io.in(2) := Mux(io.sel,CH(io.e_in,io.f_in,io.g_in),GG_j(io.e_in,io.f_in,io.g_in,io.round_in))
    c52_32.io.in(3) := io.W_in
    c52_32.io.in(4) := Mux(io.sel,io.Wx_in,0.U)

    c42_32.io.in(0) := Mux(io.sel,SUM0(io.a_in),FF_j(io.a_in,io.b_in,io.c_in,io.round_in))
    c42_32.io.in(1) := Mux(io.sel,MAJ(io.a_in,io.b_in,io.c_in),io.d_in)
    c42_32.io.in(2) := Mux(io.sel,c52_32.io.out(0),ss2)
    c42_32.io.in(3) := Mux(io.sel,c52_32.io.out(1) << 1,io.Wx_in)

    c32_32.io.in(0) := c52_32.io.out(0)
    c32_32.io.in(1) := c52_32.io.out(1)
    c32_32.io.in(2) := Mux(io.sel,io.d_in,0.U)

    io.a_out := c42_32.io.out(0) + (c42_32.io.out(1)<<1)
    val c32_out = c32_32.io.out(0) + (c32_32.io.out(1)<<1)
    io.e_out := Mux(io.sel , c32_out , P_0(c32_out))

}
