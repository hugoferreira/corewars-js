import org.scalajs.dom
import org.scalajs.dom.{CanvasRenderingContext2D, HTMLCanvasElement, HTMLElement}
import scala.scalajs.js.{Dynamic, JSApp}
import VirtualMachine._

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
  	val playground: HTMLCanvasElement = document.getElementById("board").asInstanceOf[HTMLCanvasElement]
    val debugger: HTMLElement = document.getElementById("debugger").asInstanceOf[HTMLElement]
  	implicit val renderer: CanvasRenderingContext2D = playground.getContext("2d").asInstanceOf[CanvasRenderingContext2D]

    implicit val mem = new Memory(8000) with DirtyMemory

    val p1 = new Assembler("MOV 0, 1").compile

    val p2 = new Assembler(
      """ADD #4, 3
        |MOV 2, @2
        |JMP -2, 0
        |DAT #0, #0""".stripMargin).compile

    val p3 = new Assembler(
      """DAT #0, #0
        |JMP 0, <-1""".stripMargin).compile

    val p4 = new Assembler(
      """MOV <2, 3
        |ADD 3, -1
        |JMP -1, 0
        |DAT #0, #0
        |DAT #-5084, #5084""".stripMargin).compile

    val w1 = new Warrior(0, 0, p1)
    val w2 = new Warrior(1, 0, p2)
    val w3 = new Warrior(2, 1, p3)
    val w4 = new Warrior(3, 0, p4)

    val warriors = w1 :: w2 :: w3 :: w4 :: Nil

    val v = new Visualizer(renderer, debugger, warriors)

    dom.setInterval(() => {
      v.clearWarriorsPc()
      warriors foreach { _.step() }
      v.drawDirtyMemory()
      v.highlightWarriorsPc()
      v.outputDebug()
    }, 100)
  }
}