import scala.collection.Seq

import VirtualMachine._
import org.scalajs.dom.raw.{CanvasRenderingContext2D, HTMLCanvasElement, HTMLElement, MouseEvent}

final class Visualizer(canvas: HTMLCanvasElement, debugger: HTMLElement, inspector: HTMLElement, warriors: List[Warrior])(implicit mem: Memory with DirtyMemory) {
  private[this] val gridWidth     = 125
  private[this] val cellInnerSize = 6
  private[this] val cellOuterSize = 7
  private[this] val padLeft       = cellOuterSize
  private[this] val padTop        = cellOuterSize
  private[this] val cellColor     = "rgb(30, 30, 30)"
  private[this] val playerColors  = List("rgb(30, 200, 200)", "rgb(200, 200, 30)", "rgb(200, 30, 200)", "rgb(30, 200, 30)")
  private[this] val renderer      = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
  private[this] var inspectedAddr = 0

  canvas.onmousemove = onMouseMove _

  private[this] def onMouseMove(e: MouseEvent) = {
    val pos = canvasMousePosition(e)
    inspectedAddr = (pos._2 - padTop) / cellOuterSize * gridWidth + (pos._1 - padLeft) / cellOuterSize
  }

  private[this] def canvasMousePosition(e: MouseEvent): (Int, Int) = {
    val rect = canvas.getBoundingClientRect()
    ((e.clientX - rect.left).toInt, (e.clientY - rect.top).toInt)
  }

  def drawDirtyMemory() {
    drawCells(mem.dirty.toSeq, addr => mem.player(addr) match {
      case None => cellColor
      case w: Warrior => playerColors(w.id)
    })

    mem.dirty.clear()
  }

  def highlightWarriorsPc() {
    renderer.save()
    renderer.globalAlpha = 0.5
    drawCells(warriors.map(_.pc), _ => "rgb(255, 255, 255)")
    renderer.restore()
  }

  def clearWarriorsPc() {
    warriors foreach { w => drawCells(Seq(w.pc), _ => playerColors(w.id)) }
  }

  def outputDebug() {
    debugger.innerHTML = warriors.map(w => f"<b style='color: ${playerColors(w.id)}%s;'>${w.id}%s</b> ${w.pc}%05d ${mem(w.pc)}%s").mkString("<br>")
    inspector.innerHTML = f"$inspectedAddr%05d ${mem(inspectedAddr)}%s"
  }

  private[this] def drawCells(addrs: Seq[Int], style: Int => String) {
    addrs foreach { addr =>
      val px = (addr % gridWidth) * cellOuterSize + padLeft
      val py = (addr / gridWidth) * cellOuterSize + padTop

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