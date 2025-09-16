
package it.unibo.pps.wvt.input

import it.unibo.pps.wvt.model.Position

case class InputProcessor():

  private val converter = CoordinateConverter()

  def processClick(click: MouseClick): ClickResult =
    if !converter.isInGridArea(click.x, click.y) then
      ClickResult(
        position = Position(-1, -1, true),
        isValid = false,
        error = Some(s"Click fuori dalla griglia di gioco (${click.x}, ${click.y})")
      )
    else
      converter.screenToCell(click.x, click.y) match
        case Some(position) =>
          ClickResult(
            position = position,
            isValid = true
          )
        case None =>
          ClickResult(
            position = Position(-1, -1, true),  
            isValid = false,
            error = Some(s"Impossibile convertire coordinate (${click.x}, ${click.y}) in Position valida")
          )

  def positionToScreen(position: Position): Option[(Int, Int)] =
    if converter.isValidPosition(position) then
      Some(converter.cellToScreen(position))
    else
      None

  def isValidPosition(position: Position): Boolean =
    converter.isValidPosition(position)