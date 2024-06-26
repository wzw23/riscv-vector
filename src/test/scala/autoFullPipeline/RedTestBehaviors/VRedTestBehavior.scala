package darecreek.vfuAutotest.fullPipeline

import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import chisel3._
import chiseltest.WriteVcdAnnotation
import scala.reflect.io.File
import scala.reflect.runtime.universe._
import scala.collection.mutable.Map

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

    override def isOrdered() = true

    override def getTargetTestEngine() = TestEngine.RED_TEST_ENGINE

    override def _getNextTestCase(simi:Map[String,String]) : TestCase = {
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

        val resultChecker = new RedResultChecker(
            n_inputs, widen, vflmul, expectvd,
            (a, b) => this.dump(simi, a, b)
        )

        var srcBundles : Seq[SrcBundle] = Seq()
        var ctrlBundles : Seq[CtrlBundle] = Seq()
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

            val srcBundle = SrcBundle(
                vs2=vs2, vs1=vs1,
                old_vd=oldvd,mask=mask(0))

            srcBundles :+= srcBundle
            ctrlBundles :+= ctrlBundle
        }

        return TestCase.newNormalCase(
            this.instid,
            srcBundles,
            ctrlBundles,
            resultChecker
        )
    }
}