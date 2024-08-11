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
    val vcix = Flipped(new VcixIO)
  })
  val is_vaesem = (io.vcix.req.bits.funct7(6,1) === "b001000".U && io.vcix.req.bits.funct3 === "b000".U)
  val is_vaesef = (io.vcix.req.bits.funct7(6,1) === "b001001".U && io.vcix.req.bits.funct3 === "b000".U)
  val is_vaesdm = (io.vcix.req.bits.funct7(6,1) === "b001010".U && io.vcix.req.bits.funct3 === "b000".U)
  val is_vaesdf = (io.vcix.req.bits.funct7(6,1) === "b001011".U && io.vcix.req.bits.funct3 === "b000".U)
  val is_vaeskf1 =(io.vcix.req.bits.funct7(6,1) === "b001000".U && io.vcix.req.bits.funct3 === "b011".U)
  val is_vsm3c_l =(io.vcix.req.bits.funct7(6,1) === "b001001".U && io.vcix.req.bits.funct3 === "b011".U)
  val is_vsm3c_h =(io.vcix.req.bits.funct7(6,1) === "b001010".U && io.vcix.req.bits.funct3 === "b011".U)
  val is_vsha2c_l=(io.vcix.req.bits.funct7(6,1) === "b101000".U && io.vcix.req.bits.funct3 === "b000".U)
  val is_vsha2c_h=(io.vcix.req.bits.funct7(6,1) === "b101011".U && io.vcix.req.bits.funct3 === "b000".U)
  val is_vsm3me  =(io.vcix.req.bits.funct7(6,1) === "b101001".U && io.vcix.req.bits.funct3 === "b000".U)
  val is_vsha2me =(io.vcix.req.bits.funct7(6,1) === "b101010".U && io.vcix.req.bits.funct3 === "b000".U)
  val is_vsm4k   =(io.vcix.req.bits.funct7(6,1) === "b001011".U && io.vcix.req.bits.funct3 === "b011".U)
  val is_vsm4r   =(io.vcix.req.bits.funct7(6,1) === "b001111".U && io.vcix.req.bits.funct3 === "b000".U)
  val is_vrev    =(io.vcix.req.bits.funct7(6,1) === "b001000".U && io.vcix.req.bits.funct3 === "b111".U)
  val is_vsm3c = is_vsm3c_h || is_vsm3c_l;
  //sm4
  val sm4_ende = Module(new SM4_EN_DE)
  val sm4_key = Module(new SM4_KEY)
  sm4_ende.io.data_in := io.vcix.req.bits.data2
  sm4_ende.io.round_key_in := io.vcix.req.bits.data1
  sm4_key.io.count_round_in := io.vcix.req.bits.rs1
  sm4_key.io.data_in := io.vcix.req.bits.data2

  //128 ->128 vaesem vaesef vaesdf vaeskf1 vsha2c vsha2me
  //256 -> 256 vsm3c vsm3me
  //vaesem vaesef vaesdf vaeskf1
  val aes_en_de = Module(new Aes)
  io.vcix.req.ready := true.B
  aes_en_de.io.en_de := is_vaesem || is_vaesef
  aes_en_de.io.last := is_vaesdf || is_vaesef
  aes_en_de.io.block := io.vcix.req.bits.data1
  aes_en_de.io.round_key := io.vcix.req.bits.data2
  // val vdAesc = RegEnable(aes_en_de.io.new_block,io.vcix.req.fire &&(is_vaesdf || is_vaesdm || is_vaesef || is_vaesem))

  //is_vaeskf1
  val aes_key_w = Module(new aes_key_w)
  aes_key_w.io.key := io.vcix.req.bits.data2

  aes_key_w.io.round := io.vcix.req.bits.rs1
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
  for(i<-0 until(4)){
    sm3_w.io.w_in(i+4) := io.vcix.req.bits.data1((i+1)*32-1,i*32) 
  }
  for(i<-0 until 4){
    sm3_w.io.w_in(i+8):= vs2_r((i+1)*32-1,i*32)
  }
  for(i<-0 until(4)){
    sm3_w.io.w_in(i+12):= io.vcix.req.bits.data2((i+1)*32-1,i*32) 
  }
  // val vSm3me_l = RegEnable(Cat(sm3_w.io.w_out(3),sm3_w.io.w_out(2),sm3_w.io.w_out(1),sm3_w.io.w_out(0)),(is_vsm3me && (count_sm === 1.U)))
  val vSm3me_h = RegEnable(Cat(sm3_w.io.w_out(7),sm3_w.io.w_out(6),sm3_w.io.w_out(5),sm3_w.io.w_out(4)),(is_vsm3me && (count_sm === 1.U)))

  //is_vsha2c
  val k = VecInit(Seq(
    "h428a2f98".U, "h71374491".U, "hb5c0fbcf".U, "he9b5dba5".U, 
    "h3956c25b".U, "h59f111f1".U, "h923f82a4".U, "hab1c5ed5".U,
    "hd807aa98".U, "h12835b01".U, "h243185be".U, "h550c7dc3".U,
    "h72be5d74".U, "h80deb1fe".U, "h9bdc06a7".U, "hc19bf174".U,
    "he49b69c1".U, "hefbe4786".U, "h0fc19dc6".U, "h240ca1cc".U,
    "h2de92c6f".U, "h4a7484aa".U, "h5cb0a9dc".U, "h76f988da".U,
    "h983e5152".U, "ha831c66d".U, "hb00327c8".U, "hbf597fc7".U,
    "hc6e00bf3".U, "hd5a79147".U, "h06ca6351".U, "h14292967".U,
    "h27b70a85".U, "h2e1b2138".U, "h4d2c6dfc".U, "h53380d13".U,
    "h650a7354".U, "h766a0abb".U, "h81c2c92e".U, "h92722c85".U,
    "ha2bfe8a1".U, "ha81a664b".U, "hc24b8b70".U, "hc76c51a3".U,
    "hd192e819".U, "hd6990624".U, "hf40e3585".U, "h106aa070".U,
    "h19a4c116".U, "h1e376c08".U, "h2748774c".U, "h34b0bcb5".U,
    "h391c0cb3".U, "h4ed8aa4a".U, "h5b9cca4f".U, "h682e6ff3".U,
    "h748f82ee".U, "h78a5636f".U, "h84c87814".U, "h8cc70208".U,
    "h90befffa".U, "ha4506ceb".U, "hbef9a3f7".U, "hc67178f2".U
  ))
  
  val sha_sm3_com_0 = Module(new Sha_sm3_com)
  val sha_sm3_com_1 = Module(new Sha_sm3_com)
  val is_sha2 = is_vsha2c_h || is_vsha2c_l

  val count = RegInit(0.U(5.W))
  when(io.vcix.req.fire && is_sha2){
    count := count + 1.U;
  }
  sha_sm3_com_0.io.sel :=  is_sha2
  sha_sm3_com_0.io.a_in := Mux(is_sha2,io.vcix.req.bits.data2(31,0),vd_r(31,0))
  sha_sm3_com_0.io.b_in := Mux(is_sha2,io.vcix.req.bits.data2(63,32),vd_r(63,32))
  sha_sm3_com_0.io.e_in := Mux(is_sha2,io.vcix.req.bits.data2(95,64),vd_r(95,64))
  sha_sm3_com_0.io.f_in := Mux(is_sha2,io.vcix.req.bits.data2(127,96),vd_r(127,96))
  sha_sm3_com_0.io.c_in := io.vcix.req.bits.data3(31,0)
  sha_sm3_com_0.io.d_in := io.vcix.req.bits.data3(63,32)
  sha_sm3_com_0.io.g_in := io.vcix.req.bits.data3(95,64)
  sha_sm3_com_0.io.h_in := io.vcix.req.bits.data3(127,96)
  sha_sm3_com_0.io.W_in := Mux(is_vsha2c_l||is_vsm3c_l,io.vcix.req.bits.data1(31,0),io.vcix.req.bits.data1(95,64))
  sha_sm3_com_0.io.W_in := Mux1H(Seq(
    is_vsha2c_l ->io.vcix.req.bits.data1(31,0),
    is_vsha2c_h ->io.vcix.req.bits.data1(95,64),
    is_vsm3c_l -> vs2_r(31,0),
    is_vsm3c_h -> vs2_r(95,64)
  ))
  sha_sm3_com_0.io.Wx_in:= Mux(is_sha2,k(count * 2.U),Mux(is_vsm3c_l,io.vcix.req.bits.data2(31,0)^vs2_r(31,0),io.vcix.req.bits.data2(95,64)^vs2_r(95,64)))
  sha_sm3_com_0.io.round_in := uimm_r << 1

  sha_sm3_com_1.io.sel :=  is_sha2
  sha_sm3_com_1.io.a_in := sha_sm3_com_0.io.a_out
  sha_sm3_com_1.io.b_in := sha_sm3_com_0.io.b_out
  sha_sm3_com_1.io.e_in := sha_sm3_com_0.io.e_out
  sha_sm3_com_1.io.f_in := sha_sm3_com_0.io.f_out
  sha_sm3_com_1.io.c_in := sha_sm3_com_0.io.c_out
  sha_sm3_com_1.io.d_in := sha_sm3_com_0.io.d_out
  sha_sm3_com_1.io.g_in := sha_sm3_com_0.io.g_out
  sha_sm3_com_1.io.h_in := sha_sm3_com_0.io.h_out
  sha_sm3_com_1.io.W_in := Mux(is_vsha2c_l||is_vsm3c_l,io.vcix.req.bits.data1(63,32),io.vcix.req.bits.data1(127,96))
  sha_sm3_com_1.io.W_in := Mux1H(Seq(
    is_vsha2c_l ->io.vcix.req.bits.data1(63,32),
    is_vsha2c_h ->io.vcix.req.bits.data1(127,96),
    is_vsm3c_l -> vs2_r(63,32),
    is_vsm3c_h -> vs2_r(127,96)
  ))
  sha_sm3_com_1.io.Wx_in:= Mux(is_sha2,k(count *2.U+1.U),Mux(is_vsm3c_l,io.vcix.req.bits.data2(63,32)^vs2_r(63,32),io.vcix.req.bits.data2(127,96)^vs2_r(127,96)))
  sha_sm3_com_1.io.round_in := (io.vcix.req.bits.rs1 << 1) + 1.U
  val sha2_sm3l_output = Cat(sha_sm3_com_1.io.f_out,sha_sm3_com_1.io.e_out,sha_sm3_com_1.io.b_out,sha_sm3_com_1.io.a_out)
  val sm3h_output = Cat(sha_sm3_com_1.io.h_out,sha_sm3_com_1.io.g_out,sha_sm3_com_1.io.d_out,sha_sm3_com_1.io.c_out)
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
    RegNext(is_vsm3c && (count_sm === 1.U)) ||
    (io.vcix.req.fire && is_vsm4k ) ||
    (io.vcix.req.fire && is_vsm4r ) ||
    is_vrev

    crypto_q.io.in.bits.vd_data := Mux1H(Seq(
    (io.vcix.req.fire &&(is_vaesdf || is_vaesdm || is_vaesef || is_vaesem)) -> aes_en_de.io.new_block,
    (io.vcix.req.fire &&(is_vaeskf1)) -> aes_key_w.io.round_key,
    (io.vcix.req.fire &&(is_vsha2me)) -> sha_w.io.vd_out,
    (is_vsm3me && (count_sm === 1.U)) -> Cat(sm3_w.io.w_out(3),sm3_w.io.w_out(2),sm3_w.io.w_out(1),sm3_w.io.w_out(0)),
    RegNext(is_vsm3me && (count_sm === 1.U)) -> vSm3me_h,
    (io.vcix.req.fire &&(is_sha2)) -> sha2_sm3l_output,
    (is_vsm3c && (count_sm === 1.U)) -> sha2_sm3l_output,
    RegNext(is_vsm3c && (count_sm === 1.U)) -> vdSm3h,
    (io.vcix.req.fire && is_vsm4k ) -> sm4_key.io.result_out,
    (io.vcix.req.fire && is_vsm4r) -> sm4_ende.io.result_out,
    is_vrev -> Cat(io.vcix.req.bits.data2(31,0),io.vcix.req.bits.data2(63,32),io.vcix.req.bits.data2(95,64),io.vcix.req.bits.data2(127,96))
  ))

    io.vcix.response.bits.resp_bits_data := crypto_q.io.out.bits.vd_data
    io.vcix.response.valid := crypto_q.io.out.valid
    crypto_q.io.out.ready := io.vcix.response.ready
}

object Main extends App {
  println("Generating the Sha_w hardware")
  emitVerilog(new Crypto(), Array("--target-dir", "generated"))
}