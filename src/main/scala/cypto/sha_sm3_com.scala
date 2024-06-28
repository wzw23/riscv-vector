// package cypto
// import chisel3._
// import chisel3.util._
// import utils._
// import darecreek._
// import chisel3._
// import darecreek.exu.vfucore.div._


// class Sha_sm3_com extends Module{
//     val io = IO(new Bundle{
//         //sel = 1:sha else sm3
//         val sel  = Input(Bool)
//         val a_in = Input(UInt(32.W)) 
//         val b_in = Input(UInt(32.W))
//         val c_in = Input(UInt(32.W))
//         val d_in = Input(UInt(32.W))
//         val e_in = Input(UInt(32.W))
//         val f_in = Input(UInt(32.W))
//         val g_in = Input(UInt(32.W))
//         val h_in = Input(UInt(32.W))
//         val round_in = Input(UInt(5.W))
//         val a_out = Output(UInt(32.W))
//         val b_out = Output(UInt(32.W))
//         val c_out = Output(UInt(32.W))
//         val d_out = Output(UInt(32.W))
//         val e_out = Output(UInt(32.W))
//         val f_out = Output(UInt(32.W))
//         val g_out = Output(UInt(32.W))
//         val h_out = Output(UInt(32.W))
//     })

//     def FF1(X: UInt, Y: UInt, Z: UInt): UInt = X ^ Y ^ Z
//     def FF2(X: UInt, Y: UInt, Z: UInt): UInt = (X & Y) | (X & Z) | (Y & Z)
//     def FF_j(X: UInt, Y: UInt, Z: UInt, j: UInt): UInt = Mux(j <= 15.U, FF1(X, Y, Z), FF2(X, Y, Z))

//     def GG1(X: UInt, Y: UInt, Z: UInt): UInt = X ^ Y ^ Z
//     def GG2(X: UInt, Y: UInt, Z: UInt): UInt = (X & Y) | (~X & Z)
//     def GG_j(X: UInt, Y: UInt, Z: UInt, j: UInt): UInt = Mux(j <= 15.U, GG1(X, Y, Z), GG2(X, Y, Z))

//     def T_j(j: UInt): UInt = Mux(j <= 15.U, 0x79CC4519.U(32.W), 0x7A879D8A.U(32.W))
//     def P_0(X: UInt): UInt = X ^ (X.rotateLeft(9)) ^ (X.rotateLeft(17))
//     //SS1 SS2
//     val csa = Module(new CarrySaveAdder)
//     val a_in_rotateLeft = io.a_in.rotateLeft(12)
//     val csa.io.a = a_in_rotateLeft
//     val csa.io.b = io.e_in
//     val csa.io.cin = T_j(io.round_in%32)
//     val ss1 = csa.io.sum + csa.io.cout
//     val ss2 = ss1 ^ (a_in_rotateLeft)

//     // B C D G H F 赋值
//     io.h_out := io.g_in
//     io.g_out := Mux(io.sel,io.f_in,io.f_in.rotateLeft(19))
//     io.f_out := io.e_in
//     io.b_out := io.a_in
// }
