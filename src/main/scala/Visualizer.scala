import org.scalajs.dom.{CanvasRenderingContext2D, HTMLElement}

import scala.collection.Seq

final class Visualizer(renderer: CanvasRenderingContext2D, debugger: HTMLElement, warriors: List[Warrior])(implicit mem: Memory with DirtyMemory) {
  private[this] val gridWidth     = 125
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

  def outputDebug() {
    debugger.innerHTML = warriors.map(w => f"<b style='color: ${playerColors(w.id)}%s;'>${w.id}%s</b> ${w.pc}%05d ${mem(w.pc)}%s").mkString("<br>")
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
    renderer.arc(px, py, Math.floor(cellInnerSize / 2), 0, Math.PI*2, anticlockwise = true)
    renderer.closePath()
    renderer.fill()
  }
}