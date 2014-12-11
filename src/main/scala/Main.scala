import scala.scalajs.js.{Dynamic, JSApp}

import VirtualMachine._
import org.scalajs.dom
import org.scalajs.dom.{HTMLCanvasElement, HTMLElement}

object Test extends App {
  val p2 = new Assembler(
    """ADD #4, 3
      |MOV 2, @2
      |JMP -2, 0
      |DAT #0, #0
    """.stripMargin).compile

  println(p2)
}

object CoreWars extends JSApp {
  def main() = {
  	val document = Dynamic.global.document
  	val canvas = document.getElementById("board").asInstanceOf[HTMLCanvasElement]
    val debugger = document.getElementById("debugger").asInstanceOf[HTMLElement]
    val inspector = document.getElementById("inspector").asInstanceOf[HTMLElement]

    implicit val mem = new Memory(8000) with DirtyMemory

    val p1 = RedCode("MOV 0, 1")

    val p2 = RedCode(
      """ADD #4, 3
        |MOV 2, @2
        |JMP -2, 0
        |DAT #0, #0""")

    val p3 = RedCode(
      """DAT #0, #0
        |JMP 0, <-1""")

    val p4 = RedCode(
      """MOV <2, 3
        |ADD 3, -1
        |JMP -1, 0
        |DAT #0, #0
        |DAT #-5084, #5084""")

    val w1 = new Warrior(0, 0, p1)
    val w2 = new Warrior(1, 0, p2)
    val w3 = new Warrior(2, 1, p3)
    val w4 = new Warrior(3, 0, p4)

    val warriors = w1 :: w2 :: w3 :: w4 :: Nil

    val v = new Visualizer(canvas, debugger, inspector, warriors)

    dom.setInterval(() => {
      v.clearWarriorsPc()
      warriors foreach { _.step() }
      v.drawDirtyMemory()
      v.highlightWarriorsPc()
      v.outputDebug()
    }, 100)
  }
}