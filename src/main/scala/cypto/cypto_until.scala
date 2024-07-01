package cypto
import chisel3._
import chisel3.util._
import utils._
import darecreek._
import chisel3._
import darecreek.exu.vfucore.div._

class C32_32 extends CSA3_2(32)

class C52_32 extends CarrySaveAdderMToN(5,2)(32) {
  val c32_32_1 = Module(new C32_32)
  c32_32_1.io.in(0) := io.in(0)
  c32_32_1.io.in(1) := io.in(1)
  c32_32_1.io.in(2) := io.in(2)
  val c32_32_2 = Module(new C32_32)
  c32_32_2.io.in(0) := c32_32_1.io.out(0)
  c32_32_2.io.in(1) := c32_32_1.io.out(1)<<1
  c32_32_2.io.in(2) := io.in(3)
  val c32_32_3 = Module(new C32_32)
  c32_32_3.io.in(0) := c32_32_2.io.out(0)
  c32_32_3.io.in(1) := c32_32_2.io.out(1)<<1
  c32_32_3.io.in(2) := io.in(4)
  io.out(0) := c32_32_3.io.out(0)
  io.out(1) := c32_32_3.io.out(1)
 }

class C42_32 extends CarrySaveAdderMToN(4,2)(32) {
  val c32_32_1 = Module(new C32_32)
  c32_32_1.io.in(0) := io.in(0)
  c32_32_1.io.in(1) := io.in(1)
  c32_32_1.io.in(2) := io.in(2)
  val c32_32_2 = Module(new C32_32)
  c32_32_2.io.in(0) := c32_32_1.io.out(0)
  c32_32_2.io.in(1) := c32_32_1.io.out(1)<<1
  println("out0 %d\n",c32_32_1.io.out(0))
  printf("out0 %d\n",c32_32_1.io.out(0))
  c32_32_2.io.in(2) := io.in(3)
  io.out(0) := c32_32_2.io.out(0)
  io.out(1) := c32_32_2.io.out(1)
 }
class C52_32_sum extends CarrySaveAdderMToN(5,1)(32) {
   val c52_32 = Module(new C52_32 )
   c52_32.io.in <> io.in
   val sum = c52_32.io.out(0)
   val cout = c52_32.io.out(1)
   io.out(0):= sum + (cout << 1)
 }

class C42_32_sum extends CarrySaveAdderMToN(4,1)(32) {
   val c42_32 = Module(new C42_32 )
   c42_32.io.in <> io.in
   val sum = c42_32.io.out(0)
   val cout = c42_32.io.out(1)
   io.out(0):= sum + (cout << 1)
 }
