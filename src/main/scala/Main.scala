import scala.scalajs.js.{JSApp, Dynamic}
import org.scalajs.dom
import org.scalajs.dom.{HTMLElement, HTMLCanvasElement, CanvasRenderingContext2D}

object CoreWars extends JSApp {
  def main() = {
  	val document = Dynamic.global.document
  	val playground: HTMLCanvasElement = document.getElementById("board").asInstanceOf[HTMLCanvasElement]
    val debugger: HTMLElement = document.getElementById("debugger").asInstanceOf[HTMLElement]
  	implicit val renderer: CanvasRenderingContext2D = playground.getContext("2d").asInstanceOf[CanvasRenderingContext2D]

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