package darecreek.vfuAutotest.alu

import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import chisel3._
import chiseltest.WriteVcdAnnotation
import scala.reflect.io.File
import scala.reflect.runtime.universe._

import darecreek.exu.vfu._
import darecreek.exu.vfu.alu._
import darecreek.exu.vfu.vmask._
import darecreek.exu.vfu.reduction._
import darecreek.exu.vfu.VInstructions._

class VredsumvsTestBehavior extends Vred("vredsum.vs.data", ctrlBundles.vredsum_vs, "u", "vredsum_vs") {}
class VredmaxuvsTestBehavior extends Vred("vredmaxu.vs.data", ctrlBundles.vredmaxu_vs, "u", "vredmaxu_vs") {}
class VredmaxvsTestBehavior extends Vred("vredmax.vs.data", ctrlBundles.vredmax_vs, "s", "vredmax_vs") {}
class VredminuvsTestBehavior extends Vred("vredminu.vs.data", ctrlBundles.vredminu_vs, "u", "vredminu_vs") {}
class VredminvsTestBehavior extends Vred("vredmin.vs.data", ctrlBundles.vredmin_vs, "s", "vredmin_vs") {}
class VredandvsTestBehavior extends Vred("vredand.vs.data", ctrlBundles.vredand_vs, "u", "vredand_vs") {}
class VredorvsTestBehavior extends Vred("vredor.vs.data", ctrlBundles.vredor_vs, "u", "vredor_vs") {}
class VredxorvsTestBehavior extends Vred("vredxor.vs.data", ctrlBundles.vredxor_vs, "u", "vredxor_vs") {}

class VwredsumuvsTestBehavior extends Vred("vwredsumu.vs.data", ctrlBundles.vwredsumu_vs, "u", "vwredsumu_vs", true) {}
class VwredsumvsTestBehavior extends Vred("vwredsum.vs.data", ctrlBundles.vwredsum_vs, "s", "vwredsum_vs", true) {}

class Vred(fn : String, cb : CtrlBundle, s : String, instid : String, widen : Boolean = false) extends TestBehavior(fn, cb, s, instid) {
    
    override def getDut() : Module               = {
        val dut = new Reduction
        return dut
    }

    override def testMultiple(simi:Map[String,String],ctrl:CtrlBundle,s:String, dut:Reduction) : Unit = {
        val vs2data = UtilFuncs.multilmuldatahandle(simi.get("VS2").get)
        val vs1data = UtilFuncs.multilmuldatahandle(simi.get("VS1").get)
        val oldvddata = UtilFuncs.multilmuldatahandle(simi.get("OLD_VD").get)
        val mask = UtilFuncs.multilmuldatahandle(simi.get("MASK").get)
        val vflmul = simi.get("vflmul").get
        val vxsat = simi.get("vxsat").get.toInt == 1
        val expectvd = UtilFuncs.multilmuldatahandle(simi.get("VD").get)
        val vxrm = simi.get("vxrm").get.toInt
        // println("lmel > 1, id", i)

        val vsew = UtilFuncs.vsewconvert(simi.get("vsew").get)

        var n_inputs = 1
        if(vflmul == "2.000000") n_inputs = 2
        if(vflmul == "4.000000") n_inputs = 4
        if(vflmul == "8.000000") n_inputs = 8
        
        var vd : BigInt = 0
        var vdres = false
        // var prevVds : Seq[String] = Seq()

        var robIdxValid = false
            
        // println("1111")
        var vs1 = vs1data(n_inputs - 1)
        var vs2 = vs2data(n_inputs - 1)
        var oldvd = oldvddata(n_inputs - 1)
        for(j <- 0 until n_inputs){
            vs2 = vs2data(n_inputs - 1 - j)

            var ctrlBundle = ctrl.copy(
                vsew=vsew,
                // widen2=widen,
                vl=simi.get("vl").get.toInt,
                vlmul = UtilFuncs.lmulconvert(vflmul).toInt, 
                ma = (simi.get("ma").get.toInt == 1),
                ta = (simi.get("ta").get.toInt == 1),
                vm = (simi.get("vm").get.toInt == 1),
                uopIdx=j,
                uopEnd = (j == n_inputs - 1),
                vxrm = vxrm,
                vstart = getVstart(simi)
            )

            // var robIdx = (false, 0)
            robIdxValid = randomFlush()
            /*if (robIdxValid) {
                robIdx = (true, 1)
            }
            ctrlBundle.robIdx = robIdx*/

            if (!robIdxValid) {
                dut.io.in.valid.poke(true.B)
                dut.io.in.bits.poke(genVFuInput(
                    SrcBundle(
                        vs2=vs2, vs1=vs1,
                        old_vd=oldvd,mask=mask(0)), 
                    ctrlBundle
                ))
                // dut.io.redirect.poke(genFSMRedirect((robIdxValid, robIdxValid, 0)))

                dut.clock.step(1)
            } else {
                println("flushed")
                return
            }

            // sprevVds = prevVds :+ f"h$vd%032x"
        }

        dut.io.in.valid.poke(false.B)

        while (dut.io.out.valid.peek().litValue != 1) {
            dut.clock.step(1) // 10.19
        }

        vd = dut.io.out.bits.vd.peek().litValue
        
        var vdidx = n_inputs - 1
        if (widen && (
            vflmul != "0.125000" && 
            vflmul != "0.250000" && 
            vflmul != "0.500000"
        )) vdidx = n_inputs * 2 - 1
        vdres = f"h$vd%032x".equals(expectvd(vdidx))
        if (!vdres) dump(simi, f"h$vd%032x", expectvd(vdidx))
        Logger.printvds(f"h$vd%032x", expectvd(vdidx))
        assert(vdres)
    }

    override def testSingle(simi:Map[String,String],ctrl:CtrlBundle,s:String, dut:Reduction) : Unit = {
        testMultiple(simi, ctrl, s, dut)
    }
}