package cypto
import vcix._
import chisel3._
import chisel3.util._
import utils._
import darecreek._
import chisel3._
import darecreek.exu.vfucore.div._

class Crypto extends Module {
  val io = IO(new Bundle {
    val vcix = new VcixIO
  })
  val is_vaesem = (io.vcix.req.bits.funct7(6,1) === "b001000".U && io.vcix.req.bits.funct3 === "b000".U)
  val is_vaesef = (io.vcix.req.bits.funct7(6,1) === "b001001".U && io.vcix.req.bits.funct3 === "b000".U)
  val is_vaesdm = (io.vcix.req.bits.funct7(6,1) === "b001010".U && io.vcix.req.bits.funct3 === "b000".U)
  val is_vaesdf = (io.vcix.req.bits.funct7(6,1) === "b001011".U && io.vcix.req.bits.funct3 === "b000".U)
  val is_vaeskf1 =(io.vcix.req.bits.funct7(6,1) === "b001000".U && io.vcix.req.bits.funct3 === "b011".U)
  val is_vsm3c   =(io.vcix.req.bits.funct7(6,1) === "b001001".U && io.vcix.req.bits.funct3 === "b011".U)
  val is_vsha2c_l=(io.vcix.req.bits.funct7(6,1) === "b101000".U && io.vcix.req.bits.funct3 === "b000".U)
  val is_vsha2c_h=(io.vcix.req.bits.funct7(6,1) === "b101011".U && io.vcix.req.bits.funct3 === "b000".U)
  val is_vsm3me  =(io.vcix.req.bits.funct7(6,1) === "b101001".U && io.vcix.req.bits.funct3 === "b000".U)
  val is_vsha2me =(io.vcix.req.bits.funct7(6,1) === "b101010".U && io.vcix.req.bits.funct3 === "b000".U)
  //128 ->128 vaesem vaesef vaesdf vaeskf1 vsha2c vsha2me
  //256 -> 256 vsm3c vsm3me
  //vaesem vaesef vaesdf vaeskf1
  val aes_en_de = Module(new Aes)
  io.vcix.req.ready := true.B
  aes_en_de.io.en_de := is_vaesem || is_vaesem
  aes_en_de.io.last := is_vaesdf || is_vaesef
  aes_en_de.io.block := io.vcix.req.bits.data1
  aes_en_de.io.round_key := io.vcix.req.bits.data2
  // val vdAesc = RegEnable(aes_en_de.io.new_block,io.vcix.req.fire &&(is_vaesdf || is_vaesdm || is_vaesef || is_vaesem))

  //is_vaeskf1
  val aes_key_w = Module(new aes_key_w)
  aes_key_w.io.key := io.vcix.req.bits.rs1
  aes_key_w.io.round := io.vcix.req.bits.data2
  // val vdAesk = RegEnable(aes_key_w.io.round_key,io.vcix.req.fire &&(is_vaeskf1))

    //is_vsha_w
  val sha_w = Module(new Sha_w)
  sha_w.io.vd_in := io.vcix.req.bits.data3
  sha_w.io.vs1_in := io.vcix.req.bits.data1
  sha_w.io.vs2_in := io.vcix.req.bits.data2
  // val vdSha_w = RegEnable(sha_w.io.vd_out,io.vcix.req.fire &&(is_vsha2me))

  //vsm3c_p vsm3e_p
  val count_sm = RegInit(0.U(1.W))
  val vs1_r = RegEnable(io.vcix.req.bits.data1,io.vcix.req.fire &&(is_vsm3c||((is_vsm3me || is_vsm3c) && (count_sm === 0.U))))
  val vs2_r = RegEnable(io.vcix.req.bits.data2,io.vcix.req.fire &&(is_vsm3c||((is_vsm3me || is_vsm3c) && (count_sm === 0.U))))
  val uimm_r= RegEnable(io.vcix.req.bits.rs1,io.vcix.req.fire &&(is_vsm3c||((is_vsm3me || is_vsm3c) && (count_sm === 0.U))))
  val vd_r  = RegEnable(io.vcix.req.bits.data3,io.vcix.req.fire &&(is_vsm3c||((is_vsm3me || is_vsm3c) && (count_sm === 0.U))))
  when(io.vcix.req.fire && (is_vsm3c||is_vsm3me)){
    count_sm := count_sm + 1.U;
  }

  //vsm3me
  val sm3_w = Module(new Sm3_w)
  for(i<-0 until 4){
    sm3_w.io.w_in(i) := vs1_r((i+1)*32-1,i*32)
  } 
  for(i<-0 until 4){
    sm3_w.io.w_in(i+4):= vs2_r((i+1)*32-1,i*32)
  }
  for(i<-0 until(4)){
    sm3_w.io.w_in(i+8) := io.vcix.req.bits.data1((i+1)*32-1,i*32) 
  }
  for(i<-0 until(4)){
    sm3_w.io.w_in(i+12):= io.vcix.req.bits.data2((i+1)*32-1,i*32) 
  }
  // val vSm3me_l = RegEnable(Cat(sm3_w.io.w_out(3),sm3_w.io.w_out(2),sm3_w.io.w_out(1),sm3_w.io.w_out(0)),(is_vsm3me && (count_sm === 1.U)))
  val vSm3me_h = RegEnable(Cat(sm3_w.io.w_out(3),sm3_w.io.w_out(2),sm3_w.io.w_out(1),sm3_w.io.w_out(0)),(is_vsm3me && (count_sm === 1.U)))

  //is_vsha2c
  val sha_sm3_com_0 = Module(new Sha_sm3_com)
  val sha_sm3_com_1 = Module(new Sha_sm3_com)
  val is_sha2 = is_vsha2c_h || is_vsha2c_l
  sha_sm3_com_0.io.sel :=  is_sha2
  sha_sm3_com_0.io.a_in := Mux(is_sha2,io.vcix.req.bits.data2(31,0),vs1_r(31,0))
  sha_sm3_com_0.io.b_in := Mux(is_sha2,io.vcix.req.bits.data2(63,32),vs1_r(63,32))
  sha_sm3_com_0.io.e_in := Mux(is_sha2,io.vcix.req.bits.data2(95,64),vs1_r(95,64))
  sha_sm3_com_0.io.f_in := Mux(is_sha2,io.vcix.req.bits.data2(127,96),vs1_r(127,96))
  sha_sm3_com_0.io.c_in := io.vcix.req.bits.data3(31,0)
  sha_sm3_com_0.io.d_in := io.vcix.req.bits.data3(63,32)
  sha_sm3_com_0.io.g_in := io.vcix.req.bits.data3(95,64)
  sha_sm3_com_0.io.h_in := io.vcix.req.bits.data3(127,96)
  sha_sm3_com_0.io.W_in := Mux(is_sha2,Mux(is_vsha2c_l,io.vcix.req.bits.data1(31,0),io.vcix.req.bits.data1(95,64)),io.vcix.req.bits.data2(31,0))
  sha_sm3_com_0.io.Wx_in:= Mux(is_sha2,0.U,io.vcix.req.bits.data2(31,0)^vs2_r(31,0))
  sha_sm3_com_0.io.round_in := uimm_r

  sha_sm3_com_1.io.sel :=  is_sha2
  sha_sm3_com_1.io.a_in := sha_sm3_com_0.io.a_out
  sha_sm3_com_1.io.b_in := sha_sm3_com_0.io.b_out
  sha_sm3_com_1.io.e_in := sha_sm3_com_0.io.e_out
  sha_sm3_com_1.io.f_in := sha_sm3_com_0.io.f_out
  sha_sm3_com_1.io.c_in := sha_sm3_com_0.io.c_out
  sha_sm3_com_1.io.d_in := sha_sm3_com_0.io.d_out
  sha_sm3_com_1.io.g_in := sha_sm3_com_0.io.g_out
  sha_sm3_com_1.io.h_in := sha_sm3_com_0.io.h_out
  sha_sm3_com_1.io.W_in := Mux(is_sha2,Mux(is_vsha2c_l,io.vcix.req.bits.data1(63,32),io.vcix.req.bits.data1(127,96)),io.vcix.req.bits.data2(63,32))
  sha_sm3_com_1.io.Wx_in:= Mux(is_sha2,0.U,io.vcix.req.bits.data2(63,32)^vs2_r(63,32))
  sha_sm3_com_1.io.round_in := io.vcix.req.bits.rs1
  val sha2_sm3l_output = Cat(sha_sm3_com_1.io.a_out,sha_sm3_com_1.io.b_out,sha_sm3_com_1.io.e_out,sha_sm3_com_1.io.f_out)
  val sm3h_output = Cat(sha_sm3_com_1.io.c_out,sha_sm3_com_1.io.d_out,sha_sm3_com_1.io.g_out,sha_sm3_com_1.io.h_out)
  // val vdSha2c = RegEnable(sha2_sm3l_output,io.vcix.req.fire &&(is_sha2))
  // val vdSm3l = RegEnable(sha2_sm3l_output,(is_vsm3c && (count_sm === 1.U)))
  val vdSm3h = RegEnable(sm3h_output,(is_vsm3c && (count_sm === 1.U)))

  

  class CryptoMessage extends Bundle{
    val vd_data = UInt(128.W)
  }
  class CryptoQueue extends Module {
    val io = IO(new Bundle {
      val in = Flipped(Decoupled(new CryptoMessage ))
      val out = Decoupled(new CryptoMessage )
      val cnt = Output(UInt(4.W))
    })
    val q = Module(new Queue(new CryptoMessage ,entries = 12))
    q.io.enq <> io.in
    io.out <> q.io.deq
    io.cnt <> q.io.count
  }
   val crypto_q = Module(new CryptoQueue)
    crypto_q.io.in.valid := io.vcix.req.fire &&(is_vaesdf || is_vaesdm || is_vaesef || is_vaesem)||
    (io.vcix.req.fire &&(is_vaeskf1))||
    (io.vcix.req.fire &&(is_vsha2me))||
    (is_vsm3me && (count_sm === 1.U))||
    RegNext(is_vsm3me && (count_sm === 1.U))||
    (io.vcix.req.fire &&(is_sha2))||
    (is_vsm3c && (count_sm === 1.U))||
    RegNext(is_vsm3c && (count_sm === 1.U))

    crypto_q.io.in.bits.vd_data := Mux1H(Seq(
    (io.vcix.req.fire &&(is_vaesdf || is_vaesdm || is_vaesef || is_vaesem)) -> aes_en_de.io.new_block,
    (io.vcix.req.fire &&(is_vaeskf1)) -> aes_key_w.io.round_key,
    (io.vcix.req.fire &&(is_vsha2me)) -> sha_w.io.vd_out,
    (is_vsm3me && (count_sm === 1.U)) -> Cat(sm3_w.io.w_out(3),sm3_w.io.w_out(2),sm3_w.io.w_out(1),sm3_w.io.w_out(0)),
    RegNext(is_vsm3me && (count_sm === 1.U)) -> vSm3me_h,
    (io.vcix.req.fire &&(is_sha2)) -> sha2_sm3l_output,
    (is_vsm3c && (count_sm === 1.U)) -> sha2_sm3l_output,
    RegNext(is_vsm3c && (count_sm === 1.U)) -> vdSm3h
  ))

    io.vcix.response.bits.resp_bits_data := crypto_q.io.out.bits.vd_data
    io.vcix.response.valid := crypto_q.io.out.valid
    crypto_q.io.out.ready := io.vcix.response.ready
}

object Main extends App {
  println("Generating the Sha_w hardware")
  emitVerilog(new Crypto(), Array("--target-dir", "generated"))
}