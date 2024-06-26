/***************************************************************************************
*Copyright (c) 2023-2024 Intel Corporation
*Vector Acceleration IP core for RISC-V* is licensed under Mulan PSL v2.
*You can use this software according to the terms and conditions of the Mulan PSL v2.
*You may obtain a copy of Mulan PSL v2 at:
*        http://license.coscl.org.cn/MulanPSL2
*THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
*EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
*MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*See the Mulan PSL v2 for more details.
***************************************************************************************/

package darecreek.lsu

import chisel3._
import darecreek.{VRobPtr, VExpdUOp}
import darecreek.DarecreekParam._

class VLdInput extends Bundle {
  val uop = new VExpdUOp
  val rs2 = UInt(xLen.W)
  val vs2 = UInt(VLEN.W)
  val oldVd = UInt(VLEN.W)
  val vmask = UInt(VLEN.W)
}

class VStInput extends Bundle {
  val uop = new VExpdUOp
  val rs2 = UInt(xLen.W)
  val vs2 = UInt(VLEN.W)
  val vs3 = UInt(VLEN.W)
  val vmask = UInt(VLEN.W)
}

class VLdOutput extends Bundle {
  val uop = new VExpdUOp
  val vd = UInt(VLEN.W)
}
class VStOutput extends Bundle {
  val uop = new VExpdUOp
}

class SeqId extends Bundle {
  val sb_id = UInt(5.W)
  val el_count = UInt(7.W)
  val el_off = UInt(6.W)
  val el_id = UInt(11.W)
  val v_reg = UInt(5.W)
}

class LdstCtrl extends Bundle {
  val unitStride = Bool()
  val mask = Bool()
  val strided = Bool()
  val indexed = Bool()
  val fof = Bool()
  val segment = Bool()
  val wholeReg = Bool()
  def idx_noSeg = indexed && !segment
}

object LdstDecoder {
  def apply(funct6: UInt, vs2: UInt): LdstCtrl = {
    val nf = funct6(5, 3)
    val mop = funct6(1, 0)
    val lumop = vs2
    val ctrl = Wire(new LdstCtrl)
    ctrl.unitStride := mop === 0.U
    ctrl.mask := lumop === "b01011".U && ctrl.unitStride
    ctrl.strided := mop === 2.U
    ctrl.indexed := mop(0)
    ctrl.fof := lumop === "b10000".U && ctrl.unitStride
    ctrl.segment := nf =/= 0.U && !ctrl.wholeReg
    ctrl.wholeReg := lumop === "b01000".U && ctrl.unitStride
    ctrl
  }
}