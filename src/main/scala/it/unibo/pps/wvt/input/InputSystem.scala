package it.unibo.pps.wvt.input

import it.unibo.pps.wvt.utilities.Position

case class InputSystem():

  private val processor = InputProcessor()
  
  def handleMouseClick(screenX: Double, screenY: Double): ClickResult =
    val click = MouseClick(screenX, screenY)
    val result = processor.processClick(click)
    result

  def isInGridArea(x: Double, y: Double): Boolean =
    processor.isInGridArea(x, y)

  