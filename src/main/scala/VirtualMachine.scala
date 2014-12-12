package VirtualMachine

import scala.collection.mutable

sealed trait Opcode
object Mov extends Opcode { override def toString = "MOV" }
object Add extends Opcode { override def toString = "ADD" }
object Sub extends Opcode { override def toString = "SUB" }
object Cmp extends Opcode { override def toString = "CMP" }
object Slt extends Opcode { override def toString = "SLT" }
object Jmp extends Opcode { override def toString = "JMP" }
object Jmz extends Opcode { override def toString = "JMZ" }
object Jmn extends Opcode { override def toString = "JMN" }
object Djn extends Opcode { override def toString = "DJN" }
object Spl extends Opcode { override def toString = "SPL" }
object Dat extends Opcode { override def toString = "DAT" }

sealed trait Mode
object Immediate extends Mode     { override def toString = "#" }
object Direct extends Mode        { override def toString = " " }
object Indirect extends Mode      { override def toString = "@" }
object PreDecrement extends Mode  { override def toString = "<" }

object Instruction {
  def apply(op: Opcode, operandA: Operand, operandB: Operand): Instruction =
    Instruction(op, operandA.mode, operandA.value, operandB.mode, operandB.value)
}

final case class Operand(mode: Mode, value: Int)

sealed case class Instruction(op: Opcode, amode: Mode, a: Int, bmode: Mode, b: Int) {
  def update(f: Int => Int, g: Int => Int) = this.copy(a = f(a), b = g(b))

  override def toString = s"$op  $amode$a $bmode$b"
}

object Dat00 extends Instruction(Dat, Immediate, 0, Immediate, 0)

class Memory(val size: Int) {
  private[this] val contents = Array.fill[(Instruction, Player)](size)((Dat00, None))
  protected def modulo(i: Int) = ((i % size) + size) % size // negative numbers should also wrap around

  def player(index: Int) = contents(modulo(index))._2
  def apply(index: Int) = contents(modulo(index))._1

  def update(index: Int, v: Instruction)(implicit player: Player = None) {
    contents(modulo(index)) = (v, player)
  }

  def updated(index: Int, fA: Int => Int, fB: Int => Int)(implicit player: Player = None) {
    update(index, this(index).update(fA, fB))
  }
}

trait DirtyMemory extends Memory {
  val dirty = mutable.BitSet((0 until size).toArray : _*)

  override def update(index: Int, v: Instruction)(implicit player: Player = None) {
    super.update(index, v)
    dirty += modulo(index)
  }
}

sealed trait Player
object None extends Player

class Warrior(val id: Int, base: Int, code: List[Instruction])(implicit mem: Memory) extends Player {
  private[this] implicit val warrior = this
  private[this] val initAddr = util.Random.nextInt(mem.size)
  private[this] var alive = true

  var pc = initAddr + base

  code.zipWithIndex.foreach { case (i, idx) => mem(idx + initAddr) = i }

  def step() {
    if (alive) {
      val decodeA = mem(pc) match {
        case Instruction(_, Immediate, a, _, _)    => a
        case Instruction(_, Direct, a, _, _)       => pc + a
        case Instruction(_, Indirect, a, _, _)     => pc + a + mem(pc + a).a
        case Instruction(_, PreDecrement, a, _, _) =>
          mem.updated(pc + a, identity, _ - 1)
          pc + a + mem(pc + a).b
      }

      val decodeB = mem(pc) match {
        case Instruction(_, _, _, Immediate, b)    => b
        case Instruction(_, _, _, Direct, b)       => pc + b
        case Instruction(_, _, _, Indirect, b)     => pc + b + mem(pc + b).b
        case Instruction(_, _, _, PreDecrement, b) =>
          mem.updated(pc + b, identity, _ - 1)
          pc + b + mem(pc + b).b
      }

      mem(pc) match {
        // ==== DAT ====
        case Instruction(Dat, _, _, _, _) =>
          alive = false
          println(s"Player $id has lost!")
          pc -= 1

        // ==== ADD ====
        case Instruction(Add, Immediate, _, _, _) =>
          mem.updated(decodeB, identity, _ + decodeA)
        case Instruction(Add, _, _, _, _) =>
          mem.updated(decodeB, _ + mem(decodeA).a, _ + mem(decodeA).b)

        // ==== MOV ====
        case Instruction(Mov, Immediate, _, _, _) =>
          mem.updated(decodeB, identity, _ => decodeA)
        case Instruction(Mov, _, _, _, _) =>
          mem(decodeB) = mem(decodeA)

        // ==== JMP ====
        case Instruction(Jmp, _, _, _, _) =>
          pc = decodeA - 1

        case _ => pc -= 1
      }

      pc = (pc + 1) % mem.size
    }
  }
}
