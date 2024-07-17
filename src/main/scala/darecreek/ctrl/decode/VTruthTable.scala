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
  * Split up truth table into several parts to deal with the NP-hard problem
  *       Split up into: vClass0, vClass1, vClass2, ...
  */
package darecreek.ctrl.decode

import chisel3._
import chisel3.util._
import chisel3.util.experimental.decode._
import VInstructions._

abstract trait VecDecode {
  val default: BitPat
  val table: Seq[(BitPat, BitPat)]
}

class VecDecode0 extends VecDecode {
                       //   lsrcVal(2,1,0)       crossLane          perm  narrowTo1
                       //            | ldestVal     |      fp     mask|  narrow
                       //            |  |rdVal   arith   mul|div redu | widen2 custom
                       //    vClass0 |  ||  ld st  ||  alu| | |fixP | |widen|| |
                       //         |  |  ||    |    ||   | | | | | | | |  |||| |
  val default =          BitPat("b0 ??? ??   ??    ??   ? ? ? ? ? ? ? ?  ?????")
  val table = Seq(
    // Vector Load/Store instructions
    VLE8_V            -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VLE16_V           -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VLE32_V           -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VLE64_V           -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VSE8_V            -> BitPat("b1 100 00   01    00   0 0 0 0 0 0 0 0  00000"),
    VSE16_V           -> BitPat("b1 100 00   01    00   0 0 0 0 0 0 0 0  00000"),
    VSE32_V           -> BitPat("b1 100 00   01    00   0 0 0 0 0 0 0 0  00000"),
    VSE64_V           -> BitPat("b1 100 00   01    00   0 0 0 0 0 0 0 0  00000"),
    VLM_V             -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VSM_V             -> BitPat("b1 100 00   01    00   0 0 0 0 0 0 0 0  00000"),
    VLSE8_V           -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VLSE16_V          -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VLSE32_V          -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VLSE64_V          -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VSSE8_V           -> BitPat("b1 100 00   01    00   0 0 0 0 0 0 0 0  00000"),
    VSSE16_V          -> BitPat("b1 100 00   01    00   0 0 0 0 0 0 0 0  00000"),
    VSSE32_V          -> BitPat("b1 100 00   01    00   0 0 0 0 0 0 0 0  00000"),
    VSSE64_V          -> BitPat("b1 100 00   01    00   0 0 0 0 0 0 0 0  00000"),
    VLUXEI8_V         -> BitPat("b1 010 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VLUXEI16_V        -> BitPat("b1 010 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VLUXEI32_V        -> BitPat("b1 010 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VLUXEI64_V        -> BitPat("b1 010 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VLOXEI8_V         -> BitPat("b1 010 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VLOXEI16_V        -> BitPat("b1 010 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VLOXEI32_V        -> BitPat("b1 010 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VLOXEI64_V        -> BitPat("b1 010 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VSUXEI8_V         -> BitPat("b1 110 00   01    00   0 0 0 0 0 0 0 0  00000"),
    VSUXEI16_V        -> BitPat("b1 110 00   01    00   0 0 0 0 0 0 0 0  00000"),
    VSUXEI32_V        -> BitPat("b1 110 00   01    00   0 0 0 0 0 0 0 0  00000"),
    VSUXEI64_V        -> BitPat("b1 110 00   01    00   0 0 0 0 0 0 0 0  00000"),
    VSOXEI8_V         -> BitPat("b1 110 00   01    00   0 0 0 0 0 0 0 0  00000"),
    VSOXEI16_V        -> BitPat("b1 110 00   01    00   0 0 0 0 0 0 0 0  00000"),
    VSOXEI32_V        -> BitPat("b1 110 00   01    00   0 0 0 0 0 0 0 0  00000"),
    VSOXEI64_V        -> BitPat("b1 110 00   01    00   0 0 0 0 0 0 0 0  00000"),
    VLE8FF_V          -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VLE16FF_V         -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VLE32FF_V         -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VLE64FF_V         -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VL1RE8_V          -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VL1RE16_V         -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VL1RE32_V         -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VL1RE64_V         -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VL2RE8_V          -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VL2RE16_V         -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VL2RE32_V         -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VL2RE64_V         -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VL4RE8_V          -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VL4RE16_V         -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VL4RE32_V         -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VL4RE64_V         -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VL8RE8_V          -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VL8RE16_V         -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VL8RE32_V         -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VL8RE64_V         -> BitPat("b1 000 10   10    00   0 0 0 0 0 0 0 0  00000"),
    VS1R_V            -> BitPat("b1 100 00   01    00   0 0 0 0 0 0 0 0  00000"),
    VS2R_V            -> BitPat("b1 100 00   01    00   0 0 0 0 0 0 0 0  00000"),
    VS4R_V            -> BitPat("b1 100 00   01    00   0 0 0 0 0 0 0 0  00000"),
    VS8R_V            -> BitPat("b1 100 00   01    00   0 0 0 0 0 0 0 0  00000"),
  )
}

class VecDecode1 extends VecDecode {
                       //   lsrcVal(2,1,0)       crossLane          perm  narrowTo1
                       //            | ldestVal     |      fp     mask|  narrow
                       //            |  |rdVal   arith   mul|div redu | widen2 custom
                       //    vClass0 |  ||  ld st  ||  alu| | |fixP | |widen|| |
                       //         |  |  ||    |    ||   | | | | | | | |  |||| |
  val default =          BitPat("b0 ??? ??   ??    ??   ? ? ? ? ? ? ? ?  ?????")
  val table = Seq(
    // Some of vector Integer Arithmetic Instructions
    VADD_VV           -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VADD_VX           -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VADD_VI           -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VSUB_VV           -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VSUB_VX           -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VRSUB_VX          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VRSUB_VI          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VWADDU_VV         -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  10000"),
    VWADDU_VX         -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  10000"),
    VWSUBU_VV         -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  10000"),
    VWSUBU_VX         -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  10000"),
    VWADD_VV          -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  10000"),
    VWADD_VX          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  10000"),
    VWSUB_VV          -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  10000"),
    VWSUB_VX          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  10000"),
    VWADDU_WV         -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  01000"),
    VWADDU_WX         -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  01000"),
    VWSUBU_WV         -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  01000"),
    VWSUBU_WX         -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  01000"),
    VWADD_WV          -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  01000"),
    VWADD_WX          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  01000"),
    VWSUB_WV          -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  01000"),
    VWSUB_WX          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  01000"),
    VZEXT_VF2         -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VSEXT_VF2         -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VZEXT_VF4         -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VSEXT_VF4         -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VZEXT_VF8         -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VSEXT_VF8         -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VADC_VVM          -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VADC_VXM          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VADC_VIM          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VMADC_VVM         -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMADC_VXM         -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMADC_VIM         -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMADC_VV          -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMADC_VX          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMADC_VI          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VSBC_VVM          -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VSBC_VXM          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VMSBC_VVM         -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMSBC_VXM         -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMSBC_VV          -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMSBC_VX          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VAND_VV           -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VAND_VX           -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VAND_VI           -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VOR_VV            -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VOR_VX            -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VOR_VI            -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VXOR_VV           -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VXOR_VX           -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VXOR_VI           -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VSLL_VV           -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VSLL_VX           -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VSLL_VI           -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VSRL_VV           -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VSRL_VX           -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VSRL_VI           -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VSRA_VV           -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VSRA_VX           -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VSRA_VI           -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VNSRL_WV          -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00100"),
    VNSRL_WX          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00100"),
    VNSRL_WI          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00100"),
    VNSRA_WV          -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00100"),
    VNSRA_WX          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00100"),
    VNSRA_WI          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00100"),
    VMSEQ_VV          -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMSEQ_VX          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMSEQ_VI          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMSNE_VV          -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMSNE_VX          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMSNE_VI          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMSLTU_VV         -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMSLTU_VX         -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMSLT_VV          -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMSLT_VX          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMSLEU_VV         -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMSLEU_VX         -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMSLEU_VI         -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMSLE_VV          -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMSLE_VX          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMSLE_VI          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMSGTU_VX         -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMSGTU_VI         -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMSGT_VX          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMSGT_VI          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00010"),
    VMINU_VV          -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VMINU_VX          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VMIN_VV           -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VMIN_VX           -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VMAXU_VV          -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VMAXU_VX          -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VMAX_VV           -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VMAX_VX           -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VMERGE_VVM        -> BitPat("b1 011 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VMERGE_VXM        -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VMERGE_VIM        -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VMV_V_V           -> BitPat("b1 001 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VMV_V_X           -> BitPat("b1 000 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VMV_V_I           -> BitPat("b1 000 10   00    10   1 0 0 0 0 0 0 0  00000"),
  )
}

class VecDecode2 extends VecDecode {
                       //   lsrcVal(2,1,0)       crossLane          perm  narrowTo1
                       //            | ldestVal     |      fp     mask|  narrow
                       //            |  |rdVal   arith   mul|div redu | widen2 custom
                       //    vClass0 |  ||  ld st  ||  alu| | |fixP | |widen|| |
                       //         |  |  ||    |    ||   | | | | | | | |  |||| |
  val default =          BitPat("b0 ??? ??   ??    ??   ? ? ? ? ? ? ? ?  ?????")
  val table = Seq(
    // Vector multiply instructions
    VMUL_VV           -> BitPat("b1 011 10   00    10   0 1 0 0 0 0 0 0  00000"),
    VMUL_VX           -> BitPat("b1 010 10   00    10   0 1 0 0 0 0 0 0  00000"),
    VMULH_VV          -> BitPat("b1 011 10   00    10   0 1 0 0 0 0 0 0  00000"),
    VMULH_VX          -> BitPat("b1 010 10   00    10   0 1 0 0 0 0 0 0  00000"),
    VMULHU_VV         -> BitPat("b1 011 10   00    10   0 1 0 0 0 0 0 0  00000"),
    VMULHU_VX         -> BitPat("b1 010 10   00    10   0 1 0 0 0 0 0 0  00000"),
    VMULHSU_VV        -> BitPat("b1 011 10   00    10   0 1 0 0 0 0 0 0  00000"),
    VMULHSU_VX        -> BitPat("b1 010 10   00    10   0 1 0 0 0 0 0 0  00000"),
    VWMUL_VV          -> BitPat("b1 011 10   00    10   0 1 0 0 0 0 0 0  10000"),
    VWMUL_VX          -> BitPat("b1 010 10   00    10   0 1 0 0 0 0 0 0  10000"),
    VWMULU_VV         -> BitPat("b1 011 10   00    10   0 1 0 0 0 0 0 0  10000"),
    VWMULU_VX         -> BitPat("b1 010 10   00    10   0 1 0 0 0 0 0 0  10000"),
    VWMULSU_VV        -> BitPat("b1 011 10   00    10   0 1 0 0 0 0 0 0  10000"),
    VWMULSU_VX        -> BitPat("b1 010 10   00    10   0 1 0 0 0 0 0 0  10000"),
    VMACC_VV          -> BitPat("b1 111 10   00    10   0 1 0 0 0 0 0 0  00000"),
    VMACC_VX          -> BitPat("b1 110 10   00    10   0 1 0 0 0 0 0 0  00000"),
    VNMSAC_VV         -> BitPat("b1 111 10   00    10   0 1 0 0 0 0 0 0  00000"),
    VNMSAC_VX         -> BitPat("b1 110 10   00    10   0 1 0 0 0 0 0 0  00000"),
    VMADD_VV          -> BitPat("b1 111 10   00    10   0 1 0 0 0 0 0 0  00000"),
    VMADD_VX          -> BitPat("b1 110 10   00    10   0 1 0 0 0 0 0 0  00000"),
    VNMSUB_VV         -> BitPat("b1 111 10   00    10   0 1 0 0 0 0 0 0  00000"),
    VNMSUB_VX         -> BitPat("b1 110 10   00    10   0 1 0 0 0 0 0 0  00000"),
    VWMACCU_VV        -> BitPat("b1 111 10   00    10   0 1 0 0 0 0 0 0  10000"),
    VWMACCU_VX        -> BitPat("b1 110 10   00    10   0 1 0 0 0 0 0 0  10000"),
    VWMACC_VV         -> BitPat("b1 111 10   00    10   0 1 0 0 0 0 0 0  10000"),
    VWMACC_VX         -> BitPat("b1 110 10   00    10   0 1 0 0 0 0 0 0  10000"),
    VWMACCSU_VV       -> BitPat("b1 111 10   00    10   0 1 0 0 0 0 0 0  10000"),
    VWMACCSU_VX       -> BitPat("b1 110 10   00    10   0 1 0 0 0 0 0 0  10000"),
    VWMACCUS_VX       -> BitPat("b1 110 10   00    10   0 1 0 0 0 0 0 0  10000"),
    VDIVU_VV          -> BitPat("b1 011 10   00    11   0 0 0 1 0 0 0 0  00000"),
    VDIVU_VX          -> BitPat("b1 010 10   00    11   0 0 0 1 0 0 0 0  00000"),
    VDIV_VV           -> BitPat("b1 011 10   00    11   0 0 0 1 0 0 0 0  00000"),
    VDIV_VX           -> BitPat("b1 010 10   00    11   0 0 0 1 0 0 0 0  00000"),
    VREMU_VV          -> BitPat("b1 011 10   00    11   0 0 0 1 0 0 0 0  00000"),
    VREMU_VX          -> BitPat("b1 010 10   00    11   0 0 0 1 0 0 0 0  00000"),
    VREM_VV           -> BitPat("b1 011 10   00    11   0 0 0 1 0 0 0 0  00000"),
    VREM_VX           -> BitPat("b1 010 10   00    11   0 0 0 1 0 0 0 0  00000"),
    VFDIV_VV          -> BitPat("b1 011 10   00    11   0 0 0 1 0 0 0 0  00000"),
    VFDIV_VF          -> BitPat("b1 010 10   00    11   0 0 0 1 0 0 0 0  00000"),
    VFRDIV_VF         -> BitPat("b1 010 10   00    11   0 0 0 1 0 0 0 0  00000"),
    VFSQRT_V          -> BitPat("b1 010 10   00    11   0 0 0 1 0 0 0 0  00000"),
  )
}

class VecDecode3 extends VecDecode {
                       //   lsrcVal(2,1,0)       crossLane          perm  narrowTo1
                       //            | ldestVal     |      fp     mask|  narrow
                       //            |  |rdVal   arith   mul|div redu | widen2 custom
                       //    vClass0 |  ||  ld st  ||  alu| | |fixP | |widen|| |
                       //         |  |  ||    |    ||   | | | | | | | |  |||| |
  val default =          BitPat("b0 ??? ??   ??    ??   ? ? ? ? ? ? ? ?  ?????")
  val table = Seq(  
    // Vector Fixed-Point instructions
    VSADDU_VV         -> BitPat("b1 011 10   00    10   1 0 0 0 1 0 0 0  00000"),
    VSADDU_VX         -> BitPat("b1 010 10   00    10   1 0 0 0 1 0 0 0  00000"),
    VSADDU_VI         -> BitPat("b1 010 10   00    10   1 0 0 0 1 0 0 0  00000"),
    VSADD_VV          -> BitPat("b1 011 10   00    10   1 0 0 0 1 0 0 0  00000"),
    VSADD_VX          -> BitPat("b1 010 10   00    10   1 0 0 0 1 0 0 0  00000"),
    VSADD_VI          -> BitPat("b1 010 10   00    10   1 0 0 0 1 0 0 0  00000"),
    VSSUBU_VV         -> BitPat("b1 011 10   00    10   1 0 0 0 1 0 0 0  00000"),
    VSSUBU_VX         -> BitPat("b1 010 10   00    10   1 0 0 0 1 0 0 0  00000"),
    VSSUB_VV          -> BitPat("b1 011 10   00    10   1 0 0 0 1 0 0 0  00000"),
    VSSUB_VX          -> BitPat("b1 010 10   00    10   1 0 0 0 1 0 0 0  00000"),
    VAADDU_VV         -> BitPat("b1 011 10   00    10   1 0 0 0 1 0 0 0  00000"),
    VAADDU_VX         -> BitPat("b1 010 10   00    10   1 0 0 0 1 0 0 0  00000"),
    VAADD_VV          -> BitPat("b1 011 10   00    10   1 0 0 0 1 0 0 0  00000"),
    VAADD_VX          -> BitPat("b1 010 10   00    10   1 0 0 0 1 0 0 0  00000"),
    VASUBU_VV         -> BitPat("b1 011 10   00    10   1 0 0 0 1 0 0 0  00000"),
    VASUBU_VX         -> BitPat("b1 010 10   00    10   1 0 0 0 1 0 0 0  00000"),
    VASUB_VV          -> BitPat("b1 011 10   00    10   1 0 0 0 1 0 0 0  00000"),
    VASUB_VX          -> BitPat("b1 010 10   00    10   1 0 0 0 1 0 0 0  00000"),
    VSMUL_VV          -> BitPat("b1 011 10   00    10   0 1 0 0 1 0 0 0  00000"),
    VSMUL_VX          -> BitPat("b1 010 10   00    10   0 1 0 0 1 0 0 0  00000"),
    VSSRL_VV          -> BitPat("b1 011 10   00    10   1 0 0 0 1 0 0 0  00000"),
    VSSRL_VX          -> BitPat("b1 010 10   00    10   1 0 0 0 1 0 0 0  00000"),
    VSSRL_VI          -> BitPat("b1 010 10   00    10   1 0 0 0 1 0 0 0  00000"),
    VSSRA_VV          -> BitPat("b1 011 10   00    10   1 0 0 0 1 0 0 0  00000"),
    VSSRA_VX          -> BitPat("b1 010 10   00    10   1 0 0 0 1 0 0 0  00000"),
    VSSRA_VI          -> BitPat("b1 010 10   00    10   1 0 0 0 1 0 0 0  00000"),
    VNCLIPU_WV        -> BitPat("b1 011 10   00    10   1 0 0 0 1 0 0 0  00100"),
    VNCLIPU_WX        -> BitPat("b1 010 10   00    10   1 0 0 0 1 0 0 0  00100"),
    VNCLIPU_WI        -> BitPat("b1 010 10   00    10   1 0 0 0 1 0 0 0  00100"),
    VNCLIP_WV         -> BitPat("b1 011 10   00    10   1 0 0 0 1 0 0 0  00100"),
    VNCLIP_WX         -> BitPat("b1 010 10   00    10   1 0 0 0 1 0 0 0  00100"),
    VNCLIP_WI         -> BitPat("b1 010 10   00    10   1 0 0 0 1 0 0 0  00100"),
  )
}

class VecDecode4 extends VecDecode {
                       //   lsrcVal(2,1,0)       crossLane          perm  narrowTo1
                       //            | ldestVal     |      fp     mask|  narrow
                       //            |  |rdVal   arith   mul|div redu | widen2 custom
                       //    vClass0 |  ||  ld st  ||  alu| | |fixP | |widen|| |
                       //         |  |  ||    |    ||   | | | | | | | |  |||| |
  val default =          BitPat("b0 ??? ??   ??    ??   ? ? ? ? ? ? ? ?  ?????")
  val table = Seq(  
    // Vector Floating-point instructions
    VFADD_VV          -> BitPat("b1 011 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFADD_VF          -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFSUB_VV          -> BitPat("b1 011 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFSUB_VF          -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFRSUB_VF         -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFWADD_VV         -> BitPat("b1 011 10   00    10   0 0 1 0 0 0 0 0  10000"),
    VFWADD_VF         -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  10000"),
    VFWSUB_VV         -> BitPat("b1 011 10   00    10   0 0 1 0 0 0 0 0  10000"),
    VFWSUB_VF         -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  10000"),
    VFWADD_WV         -> BitPat("b1 011 10   00    10   0 0 1 0 0 0 0 0  01000"),
    VFWADD_WF         -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  01000"),
    VFWSUB_WV         -> BitPat("b1 011 10   00    10   0 0 1 0 0 0 0 0  01000"),
    VFWSUB_WF         -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  01000"),
    VFMUL_VV          -> BitPat("b1 011 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFMUL_VF          -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFWMUL_VV         -> BitPat("b1 011 10   00    10   0 0 1 0 0 0 0 0  10000"),
    VFWMUL_VF         -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  10000"),
    VFMACC_VV         -> BitPat("b1 111 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFMACC_VF         -> BitPat("b1 110 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFNMACC_VV        -> BitPat("b1 111 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFNMACC_VF        -> BitPat("b1 110 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFMSAC_VV         -> BitPat("b1 111 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFMSAC_VF         -> BitPat("b1 110 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFNMSAC_VV        -> BitPat("b1 111 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFNMSAC_VF        -> BitPat("b1 110 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFMADD_VV         -> BitPat("b1 111 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFMADD_VF         -> BitPat("b1 110 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFNMADD_VV        -> BitPat("b1 111 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFNMADD_VF        -> BitPat("b1 110 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFMSUB_VV         -> BitPat("b1 111 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFMSUB_VF         -> BitPat("b1 110 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFNMSUB_VV        -> BitPat("b1 111 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFNMSUB_VF        -> BitPat("b1 110 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFWMACC_VV        -> BitPat("b1 111 10   00    10   0 0 1 0 0 0 0 0  10000"),
    VFWMACC_VF        -> BitPat("b1 110 10   00    10   0 0 1 0 0 0 0 0  10000"),
    VFWNMACC_VV       -> BitPat("b1 111 10   00    10   0 0 1 0 0 0 0 0  10000"),
    VFWNMACC_VF       -> BitPat("b1 110 10   00    10   0 0 1 0 0 0 0 0  10000"),
    VFWMSAC_VV        -> BitPat("b1 111 10   00    10   0 0 1 0 0 0 0 0  10000"),
    VFWMSAC_VF        -> BitPat("b1 110 10   00    10   0 0 1 0 0 0 0 0  10000"),
    VFWNMSAC_VV       -> BitPat("b1 111 10   00    10   0 0 1 0 0 0 0 0  10000"),
    VFWNMSAC_VF       -> BitPat("b1 110 10   00    10   0 0 1 0 0 0 0 0  10000"),
    VFRSQRT7_V        -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFREC7_V          -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFMIN_VV          -> BitPat("b1 011 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFMIN_VF          -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFMAX_VV          -> BitPat("b1 011 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFMAX_VF          -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFSGNJ_VV         -> BitPat("b1 011 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFSGNJ_VF         -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFSGNJN_VV        -> BitPat("b1 011 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFSGNJN_VF        -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFSGNJX_VV        -> BitPat("b1 011 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFSGNJX_VF        -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VMFEQ_VV          -> BitPat("b1 011 10   00    10   0 0 1 0 0 0 0 0  00010"),
    VMFEQ_VF          -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00010"),
    VMFNE_VV          -> BitPat("b1 011 10   00    10   0 0 1 0 0 0 0 0  00010"),
    VMFNE_VF          -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00010"),
    VMFLT_VV          -> BitPat("b1 011 10   00    10   0 0 1 0 0 0 0 0  00010"),
    VMFLT_VF          -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00010"),
    VMFLE_VV          -> BitPat("b1 011 10   00    10   0 0 1 0 0 0 0 0  00010"),
    VMFLE_VF          -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00010"),
    VMFGT_VF          -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00010"),
    VMFGE_VF          -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00010"),
    VFCLASS_V         -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFMERGE_VFM       -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFMV_V_F          -> BitPat("b1 000 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFCVT_XU_F_V      -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFCVT_X_F_V       -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFCVT_RTZ_XU_F_V  -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFCVT_RTZ_X_F_V   -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFCVT_F_XU_V      -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFCVT_F_X_V       -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00000"),
    VFWCVT_XU_F_V     -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  10000"),
    VFWCVT_X_F_V      -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  10000"),
    VFWCVT_RTZ_XU_F_V -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  10000"),
    VFWCVT_RTZ_X_F_V  -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  10000"),
    VFWCVT_F_XU_V     -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  10000"),
    VFWCVT_F_X_V      -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  10000"),
    VFWCVT_F_F_V      -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  10000"),
    VFNCVT_XU_F_W     -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00100"),
    VFNCVT_X_F_W      -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00100"),
    VFNCVT_RTZ_XU_F_W -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00100"),
    VFNCVT_RTZ_X_F_W  -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00100"),
    VFNCVT_F_XU_W     -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00100"),
    VFNCVT_F_X_W      -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00100"),
    VFNCVT_F_F_W      -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00100"),
    VFNCVT_ROD_F_F_W  -> BitPat("b1 010 10   00    10   0 0 1 0 0 0 0 0  00100"),
  )
}

class VecDecode5 extends VecDecode {
                       //   lsrcVal(2,1,0)       crossLane          perm  narrowTo1
                       //            | ldestVal     |      fp     mask|  narrow
                       //            |  |rdVal   arith   mul|div redu | widen2 custom
                       //    vClass0 |  ||  ld st  ||  alu| | |fixP | |widen|| |
                       //         |  |  ||    |    ||   | | | | | | | |  |||| |
  val default =          BitPat("b0 ??? ??   ??    ??   ? ? ? ? ? ? ? ?  ?????")
  val table = Seq(
    // Vector reduction instructions
    VREDSUM_VS        -> BitPat("b1 011 10   00    11   0 0 0 0 0 1 0 0  00000"),
    VREDMAX_VS        -> BitPat("b1 011 10   00    11   0 0 0 0 0 1 0 0  00000"),
    VREDMAXU_VS       -> BitPat("b1 011 10   00    11   0 0 0 0 0 1 0 0  00000"),
    VREDMIN_VS        -> BitPat("b1 011 10   00    11   0 0 0 0 0 1 0 0  00000"),
    VREDMINU_VS       -> BitPat("b1 011 10   00    11   0 0 0 0 0 1 0 0  00000"),
    VREDAND_VS        -> BitPat("b1 011 10   00    11   0 0 0 0 0 1 0 0  00000"),
    VREDOR_VS         -> BitPat("b1 011 10   00    11   0 0 0 0 0 1 0 0  00000"),
    VREDXOR_VS        -> BitPat("b1 011 10   00    11   0 0 0 0 0 1 0 0  00000"),
    VWREDSUMU_VS      -> BitPat("b1 011 10   00    11   0 0 0 0 0 1 0 0  10000"),
    VWREDSUM_VS       -> BitPat("b1 011 10   00    11   0 0 0 0 0 1 0 0  10000"),
    VFREDOSUM_VS      -> BitPat("b1 011 10   00    11   0 0 0 0 0 1 0 0  00000"),
    VFREDUSUM_VS      -> BitPat("b1 011 10   00    11   0 0 0 0 0 1 0 0  00000"),
    VFREDMIN_VS       -> BitPat("b1 011 10   00    11   0 0 0 0 0 1 0 0  00000"),
    VFREDMAX_VS       -> BitPat("b1 011 10   00    11   0 0 0 0 0 1 0 0  00000"),
    VFWREDUSUM_VS     -> BitPat("b1 011 10   00    11   0 0 0 0 0 1 0 0  10000"),
    VFWREDOSUM_VS     -> BitPat("b1 011 10   00    11   0 0 0 0 0 1 0 0  10000"),

    // Vector mask instructions
    VMAND_MM          -> BitPat("b1 011 10   00    11   0 0 0 0 0 0 1 0  00000"),
    VMNAND_MM         -> BitPat("b1 011 10   00    11   0 0 0 0 0 0 1 0  00000"),
    VMANDN_MM         -> BitPat("b1 011 10   00    11   0 0 0 0 0 0 1 0  00000"),
    VMXOR_MM          -> BitPat("b1 011 10   00    11   0 0 0 0 0 0 1 0  00000"),
    VMOR_MM           -> BitPat("b1 011 10   00    11   0 0 0 0 0 0 1 0  00000"),
    VMNOR_MM          -> BitPat("b1 011 10   00    11   0 0 0 0 0 0 1 0  00000"),
    VMORN_MM          -> BitPat("b1 011 10   00    11   0 0 0 0 0 0 1 0  00000"),
    VMXNOR_MM         -> BitPat("b1 011 10   00    11   0 0 0 0 0 0 1 0  00000"),
    VCPOP_M           -> BitPat("b1 010 01   00    11   0 0 0 0 0 0 1 0  00000"),
    VFIRST_M          -> BitPat("b1 010 01   00    11   0 0 0 0 0 0 1 0  00000"),
    VMSBF_M           -> BitPat("b1 010 10   00    11   0 0 0 0 0 0 1 0  00000"),
    VMSIF_M           -> BitPat("b1 010 10   00    11   0 0 0 0 0 0 1 0  00000"),
    VMSOF_M           -> BitPat("b1 010 10   00    11   0 0 0 0 0 0 1 0  00000"),
    VIOTA_M           -> BitPat("b1 010 10   00    11   0 0 0 0 0 0 1 0  00000"),
    VID_V             -> BitPat("b1 000 10   00    11   0 0 0 0 0 0 1 0  00000"),

    // Vector permutation instructions
    VMV_X_S           -> BitPat("b1 010 01   00    10   1 0 0 0 0 0 0 0  00000"),
    VMV_S_X           -> BitPat("b1 000 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VFMV_F_S          -> BitPat("b1 010 01   00    10   1 0 0 0 0 0 0 0  00000"),
    VFMV_S_F          -> BitPat("b1 000 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VSLIDEUP_VX       -> BitPat("b1 010 10   00    11   0 0 0 0 0 0 0 1  00000"),
    VSLIDEUP_VI       -> BitPat("b1 010 10   00    11   0 0 0 0 0 0 0 1  00000"),
    VSLIDEDOWN_VX     -> BitPat("b1 010 10   00    11   0 0 0 0 0 0 0 1  00000"),
    VSLIDEDOWN_VI     -> BitPat("b1 010 10   00    11   0 0 0 0 0 0 0 1  00000"),
    VSLIDE1UP_VX      -> BitPat("b1 010 10   00    11   0 0 0 0 0 0 0 1  00000"),
    VFSLIDE1UP_VF     -> BitPat("b1 010 10   00    11   0 0 0 0 0 0 0 1  00000"),
    VSLIDE1DOWN_VX    -> BitPat("b1 010 10   00    11   0 0 0 0 0 0 0 1  00000"),
    VFSLIDE1DOWN_VF   -> BitPat("b1 010 10   00    11   0 0 0 0 0 0 0 1  00000"),
    VRGATHER_VV       -> BitPat("b1 011 10   00    11   0 0 0 0 0 0 0 1  00000"),
    VRGATHEREI16_VV   -> BitPat("b1 011 10   00    11   0 0 0 0 0 0 0 1  00000"),
    VRGATHER_VX       -> BitPat("b1 010 10   00    11   0 0 0 0 0 0 0 1  00000"),
    VRGATHER_VI       -> BitPat("b1 010 10   00    11   0 0 0 0 0 0 0 1  00000"),
    VCOMPRESS_VM      -> BitPat("b1 011 10   00    11   0 0 0 0 0 0 0 1  00000"),
    VMV1R_V           -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VMV2R_V           -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VMV4R_V           -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
    VMV8R_V           -> BitPat("b1 010 10   00    10   1 0 0 0 0 0 0 0  00000"),
  )
}

class VecDecode6 extends VecDecode {
                      //   lsrcVal(2,1,0)       crossLane          perm  narrowTo1
                       //            | ldestVal     |      fp     mask|  narrow
                       //            |  |rdVal   arith   mul|div redu | widen2 custom
                       //    vClass0 |  ||  ld st  ||  alu| | |fixP | |widen|| |
                       //         |  |  ||    |    ||   | | | | | | | |  |||| |
  val default =          BitPat("b0 ??? ??   ??    ??   ? ? ? ? ? ? ? ?  ?????")
  val table = Seq(
    // Vector reduction instructions
    CUSTOM0_VS1       -> BitPat("b1 001 00   00    10   0 0 0 0 0 0 0 0  00001"),
    CUSTOM0_VS1_VS2   -> BitPat("b1 011 00   00    10   0 0 0 0 0 0 0 0  00001"),
    CUSTOM0_VD        -> BitPat("b1 000 10   00    10   0 0 0 0 0 0 0 0  00001"),
    CUSTOM0_VD_VS1    -> BitPat("b1 001 10   00    10   0 0 0 0 0 0 0 0  00001"),
    CUSTOM0_VD_VS1_VS2-> BitPat("b1 011 10   00    10   0 0 0 0 0 0 0 0  00001"),
     )
}