package it.unibo.pps.wvt.input

import it.unibo.pps.wvt.utilities.Position

/** Sistema di input principale - interfaccia per il GameLoop */
case class InputSystem():

  private val processor = InputProcessor()

  /** It takes screen coordinates of a mouse click and returns the result of processing that click.
   *
   * @param screenX
   *   The x-coordinate of the mouse click on the screen.
   * @param screenY
   *   The y-coordinate of the mouse click on the screen.
   * @return
   *   A ClickResult object containing information about the processed click,
   *   including the corresponding Position if valid, and any error messages if
   *   applicable.
   */
  def handleMouseClick(screenX: Int, screenY: Int): ClickResult =
    val click = MouseClick(screenX, screenY)
    val result = processor.processClick(click)
    logClick(click, result)
    result

  def positionToScreen(position: Position): Option[(Double, Double)] =
    processor.positionToScreen(position)

  def isValidPosition(position: Position): Boolean =
    processor.isValidPosition(position)

  private def logClick(click: MouseClick, result: ClickResult): Unit =
    if result.isValid then
      println(s"[INPUT] Click (${click.x}, ${click.y}) -> Position(${result.position.row}, ${result.position.col})")
    else
      println(s"[INPUT] Click non valido")