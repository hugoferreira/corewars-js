/**
 * Created by Hugo Sereno Ferreira on 15/06/14.
 */

import scala.scalajs.js.{JSApp, Dynamic}
import org.scalajs.dom
import org.scalajs.dom.{HTMLCanvasElement, CanvasRenderingContext2D}
import scala.collection._

sealed trait Opcode 
object Mov extends Opcode { override def toString = "MOV" }
object Add extends Opcode { override def toString = "ADD" }
object Sub extends Opcode { override def toString = "SUB" }
object Cmp extends Opcode { override def toString = "CMP" }
object Slt extends Opcode { override def toString = "SLT" }
object Jmp extends Opcode { override def toString = "JMP" }
object Jmz extends Opcode
object Jmn extends Opcode
object Djn extends Opcode
object Spl extends Opcode
object Dat extends Opcode { override def toString = "DAT" }

sealed trait Mode
object Immediate extends Mode     { override def toString = "#" }
object Direct extends Mode        { override def toString = " " }
object Indirect extends Mode      { override def toString = "@" }
object PreDecrement extends Mode  { override def toString = "<" }

sealed case class Instruction(op: Opcode, amode: Mode, a: Int, bmode: Mode, b: Int) {
  def update(f: Int => Int, g: Int => Int) = this.copy(a = f(a), b = g(b))
  
  override def toString = s"$op  $amode$a $bmode$b" 
}

object Dat00 extends Instruction(Dat, Immediate, 0, Immediate, 0)

final class Visualizer(warriors: List[Warrior])(implicit mem: Memory with DirtyMemory, renderer: CanvasRenderingContext2D) {
  private[this] val gridWidth     = 100
  private[this] val gridHeight    = 80
  private[this] val cellInnerSize = 6
  private[this] val cellOuterSize = 7
  private[this] val padLeft       = cellOuterSize
  private[this] val padTop        = cellOuterSize
  private[this] val cellColor     = "rgb(30, 30, 30)"
  private[this] val playerColors  = List("rgb(30, 200, 200)", "rgb(200, 200, 30)", "rgb(200, 30, 200)", "rgb(30, 200, 30)")
  
  def drawDirtyMemory() {
    drawCells(mem.dirty.toSeq, addr => mem.player(addr) match {
      case None => cellColor
      case w: Warrior => playerColors(w.id)
    })
      
    mem.dirty.clear()
  }
  
  def highlightWarriorsPc() {
    drawCells(warriors.map(_.pc), _ => "rgb(255, 255, 255)")
  }

  def clearWarriorsPc() {
    warriors foreach { w => drawCells(Seq(w.pc), _ => playerColors(w.id)) }
  }
  
  private[this] def drawCells(addrs: Seq[Int], style: Int => String) {
    addrs foreach { addr =>
      val x = addr % gridWidth
      val y = addr / gridWidth
      val px = x * cellOuterSize + padLeft
      val py = y * cellOuterSize + padTop
      
      renderer.fillStyle = style(addr)
      drawCircle(px, py)
    }
  }
  
  private[this] def drawCircle(px: Int, py: Int) {
    renderer.beginPath()
    renderer.arc(px, py, Math.floor(cellInnerSize / 2), 0, Math.PI*2, true) 
    renderer.closePath()
    renderer.fill()  
  }
}

sealed class Memory(val size: Int) {
  private[this] var contents = Array.fill[(Instruction, Player)](size)((Dat00, None))
  protected def modulo(i: Int) = (((i % size) + size) % size)
  
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
  val size: Int
  val dirty = mutable.BitSet((0 until size).toArray : _*) 

  override def update(index: Int, v: Instruction)(implicit player: Player = None) {
    super.update(index, v)
    dirty += modulo(index)
  }
}

sealed trait Player
object None extends Player

class Warrior(val id: Int, base: Int, code: List[Instruction])(implicit mem: Memory) extends Player {
  implicit val warrior = this
  val initAddr = util.Random.nextInt(mem.size)
  var pc = initAddr + base
  var alive = true
  
  code.zipWithIndex.foreach { case (i, idx) => mem(idx + initAddr) = i }
  
  def step() {
    if (alive) {
      val decodeA = mem(pc) match {
        case Instruction(_, Immediate, a, _, _)    => a
        case Instruction(_, Direct, a, _, _)       => pc + a
        case Instruction(_, Indirect, a, _, _)     => pc + a + mem(pc + a).a
        case Instruction(_, PreDecrement, a, _, _) => {
          mem.updated(pc + a, identity, _ - 1)
          pc + a + mem(pc + a).b 
        }
      }
  
      val decodeB = mem(pc) match {
        case Instruction(_, _, _, Immediate, b)    => b
        case Instruction(_, _, _, Direct, b)       => pc + b
        case Instruction(_, _, _, Indirect, b)     => pc + b + mem(pc + b).b
        case Instruction(_, _, _, PreDecrement, b) => { 
          mem.updated(pc + b, identity, _ - 1) 
          pc + b + mem(pc + b).b 
        }
      }
      
      mem(pc) match {
        case Instruction(Dat, _, _, _, _) => { 
          alive = false; 
          println(s"Player $id has lost!")
          pc -= 1 
        }
    
        case Instruction(Add, Immediate, _, _, _) => mem.updated(decodeB, identity, _ + decodeA) 
        
        case Instruction(Add, _, _, _, _) => mem.updated(decodeB, _ + mem(decodeA).a, _ + mem(decodeA).b)
      
        case Instruction(Mov, Immediate, _, _, _) => mem.updated(decodeB, identity, _ => decodeA)
        case Instruction(Mov, _,         _, _, _) => mem(decodeB) = mem(decodeA)
        
        case Instruction(Jmp, _, _, _, _) => pc = decodeA - 1
        
        case _ => pc -= 1
      }
      
      pc = (pc + 1) % mem.size
    } 
  }
}

object CoreWars extends JSApp {
  def main() = {
  	val document = Dynamic.global.document
  	val playground = document.getElementById("board").asInstanceOf[HTMLCanvasElement]
  	implicit val renderer = playground.getContext("2d").asInstanceOf[CanvasRenderingContext2D]

    implicit val mem = new Memory(8000) with DirtyMemory
    
    val w1 = new Warrior(0, 0, Instruction(Mov, Direct, 0, Direct, 1) :: Nil)

    val w2 = new Warrior(1, 0, Instruction(Add, Immediate, 4, Direct, 3) ::
                               Instruction(Mov, Direct, 2, Indirect, 2) ::
                               Instruction(Jmp, Direct, -2, Direct, 0) ::
                               Instruction(Dat, Immediate, 0, Immediate, 0) :: Nil) 

    val w3 = new Warrior(2, 1, Instruction(Dat, Immediate, 0, Immediate, 0) ::
                               Instruction(Jmp, Direct, 0, PreDecrement, -1) :: Nil)
    
    val w4 = new Warrior(3, 0, Instruction(Mov, PreDecrement, 2, Direct, 3) ::
                               Instruction(Add, Direct, 3, Direct, -1) :: 
                               Instruction(Jmp, Direct, -2, Direct, 0) :: 
                               Instruction(Dat, Immediate, 0, Immediate, 0) ::
                               Instruction(Dat, Immediate, -5084, Immediate, 5084) :: Nil)

    val warriors = w1 :: w2 :: w3 :: w4 :: Nil
    
    val v = new Visualizer(warriors)
                    
    dom.setInterval(() => { 
      v.clearWarriorsPc()
      warriors foreach { _.step() }
      v.drawDirtyMemory()
      v.highlightWarriorsPc()
    }, 1000)
  }
}