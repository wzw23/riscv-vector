package darecreek.vfuAutotest.alu

import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import chisel3._
import chiseltest.WriteVcdAnnotation
import scala.reflect.io.File
import scala.reflect.runtime.universe._
import scala.util.control.Breaks._

import darecreek.exu.vfu._
import darecreek.exu.vfu.alu._
import darecreek.exu.vfu.fp._
import darecreek.exu.vfu.VInstructions._

class DivResult(n_res : Int, dump : (Map[String,String], String, String) => Unit) extends BundleGenHelper {

    var cur_res = 0
    var fflags : Int = 0

    def randomBlock() : Boolean = {
        return RandomGen.rand.nextInt(100) > 80
    }

    def vCompare(dut : VDivWrapper, simi : Map[String, String], 
            uopIdx : Int, expectvd : Array[String]) = {
        var vd = dut.io.out.bits.vd.peek().litValue
        var vdres = f"h$vd%032x".equals(expectvd(n_res - 1 - uopIdx))
        Logger.printvds(f"h$vd%032x", expectvd(n_res - 1 - uopIdx))
        if (!vdres) dump(simi, f"h$vd%032x", expectvd(n_res - 1 - uopIdx))
        assert(vdres)
    }

    def checkAndCompare(dut : VDivWrapper, simi : Map[String, String], 
            ctrlBundles : Map[Int, CtrlBundle], expectvd : Array[String], 
            compFunc : (VDivWrapper, Map[String, String], 
                Int, Array[String]) => Unit = vCompare) = {
        val block = randomBlock()
        dut.io.out.ready.poke((!block).B) // TODO randomly block
        // println(s"dut.io.out.valid.peek().litValue ${dut.io.out.valid.peek().litValue}")
        if ((!block) && dut.io.out.valid.peek().litValue == 1) {

            var uopIdx = dut.io.out.bits.uop.uopIdx.peek().litValue.toInt
            var ctrlBundle = ctrlBundles(uopIdx)
            var uop = genVFuUop(ctrlBundle)
            
            println(s"checking for result of uop ${uopIdx}")
            dut.io.out.bits.uop.expect(uop) // TODO check uop

            compFunc(dut, simi, uopIdx, expectvd)

            fflags = fflags | dut.io.out.bits.fflags.peek().litValue.toInt
            cur_res += 1
        }
    }

    def finished() : Boolean = {
        return cur_res == n_res
    }

    def getFflags() : Int = {
        return fflags
    }
}