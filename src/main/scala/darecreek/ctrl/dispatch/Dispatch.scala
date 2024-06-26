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

/**
  * Constraint: so far all inputs of io.in share one 'ready'.
  *             All outputs of one Iq share one 'ready'.
  *             So far there is only one arithmetic issue queue (NArithIQs = 1).
  */

package darecreek

import chisel3._
import chisel3.util._
import utils._

class VDispatch extends Module {
  val io = IO(new Bundle {
    val in = Vec(VRenameWidth, Flipped(Decoupled(new VExpdUOp)))
    // to Issue Queues
    val out = new Bundle {
      val toArithIQ = Vec(VRenameWidth, Decoupled(new VExpdUOp))
      val toLsIQ = Vec(VRenameWidth, Decoupled(new VExpdUOp))
    }
    val toRob = Vec(VRenameWidth, ValidIO(new VExpdUOp))
    // To read the busy table
    val readBusyTable = Vec(VRenameWidth, Vec(4, Output(UInt(VPRegIdxWidth.W))))
    val flush = Input(Bool())
  })

  val inputHasValid = Cat(io.in.map(_.valid)).orR
  val rdyArith = io.out.toArithIQ(0).ready
  val rdyLs = io.out.toLsIQ(0).ready
  for (i <- 0 until VRenameWidth) {
    val canOut = rdyArith && rdyLs
    io.in(i).ready := !inputHasValid || canOut
    
    val isArith = io.in(i).bits.ctrl.arith
    val isLs = io.in(i).bits.ctrl.load || io.in(i).bits.ctrl.store
    
    // -- NOTE: so far there is only one arithmetic issue queue (NArithIQs = 1)
    io.out.toArithIQ(i).valid := io.in(i).valid && isArith && rdyLs
    io.out.toLsIQ(i).valid := io.in(i).valid && isLs && rdyArith
    io.toRob(i).valid := io.in(i).valid && rdyArith && rdyLs
    io.out.toArithIQ(i).bits := io.in(i).bits
    io.out.toLsIQ(i).bits := io.in(i).bits
    io.toRob(i).bits := io.in(i).bits

    // // To write busy table 
    // io.allocPregs(i).valid := io.in(i).valid && io.in(i).bits.pdestVal && !io.flush
    // io.allocPregs(i).bits := io.in(i).bits.pdest
    // To read busy table
    io.readBusyTable(i) := io.in(i).bits.psrc
  }

}