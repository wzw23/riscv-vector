package smartVector
import chisel3._
import chisel3.util._
import darecreek.VDecode
import utils._
import freechips.rocketchip.rocket._
import freechips.rocketchip.util._
import chipsalliance.rocketchip.config.{Config, Field, Parameters}
import xiangshan.MicroOp
import SmartParam._

class CustomOutput extends Bundle{
    val vd = UInt(128.W)
}
class SVcustomIO(implicit p: Parameters) extends ParameterizedBundle()(p) {
    val mUop            = Input(ValidIO(new Muop()(p)))
    val mUopMergeAttr   = Input(ValidIO(new MuopMergeAttr))
    val customout       = Output(ValidIO(new CustomOutput))
    val dataExchange    = new RVUCustom()
    val customReady     = Output(Bool())
}
class SVcustom(implicit p: Parameters) extends Module {
    val io = IO(new SVcustomIO);

    //wire
    val insn_only_read = io.mUop.bits.uop.ctrl.lsrcVal(0) || io.mUop.bits.uop.ctrl.lsrcVal(1)

    val idle :: issue_m :: wait_m :: Nil = Enum(3)
    val state = RegInit(idle);
    val customReady = (state === idle);

    //interface
    io.dataExchange.req.valid := state === issue_m
    io.dataExchange.req.bits.vs1 := io.mUop.bits.uopRegInfo.vs1
    io.dataExchange.req.bits.vs2 := io.mUop.bits.uopRegInfo.vs2
    io.dataExchange.req.bits.custom_vector_inst := io.mUop.bits.inst

    io.customReady := state === idle

    io.customout.valid := WireDefault(false.B)
    io.customout.bits.vd := io.dataExchange.resp.bits.vd


    //FSM
    switch(state){
        is(idle){
            when(io.mUop.valid && io.mUop.bits.uop.ctrl.custom){
                    state := issue_m 
            }
        }
        is(issue_m){
            when(io.dataExchange.req.fire){
                when(insn_only_read){
                    state := idle
                }.otherwise{
                    state := wait_m
                }
            }
        }
        is(wait_m){
            when(io.dataExchange.resp.valid){
                state := idle
                io.customout.valid := true.B
            }
        }
    }
}