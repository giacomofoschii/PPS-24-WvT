package it.unibo.pps.wvt.input

import it.unibo.pps.wvt.utilities.{GridMapper, Position}
import it.unibo.pps.wvt.utilities.ViewConstants._

case class InputProcessor():

  def processClick(click: MouseClick): ClickResult =
    GridMapper.physicalToLogical(click.x.toDouble, click.y.toDouble) match
      case Some(position) if position.isValid =>
        ClickResult(
          position = position,
          isValid = true,
          error = None
        )
      case _ =>
        ClickResult(
          position = Position(-1, -1, allowInvalid=true),
          isValid = false,
          error = Some(s"Posizione invalida")
        )

  def positionToScreen(position: Position): Option[(Double, Double)] =
    if position.isValid then
      val coords = GridMapper.logicalToPhysical(position)
      Some(coords)
    else
      None

  def isValidPosition(position: Position): Boolean =
    position.isValid

  def isInGridArea(x: Int, y: Int): Boolean =
    val gridEndx = GRID_OFFSET_X + (GRID_COLS * CELL_WIDTH)
    val gridEndY = GRID_OFFSET_Y + (GRID_ROWS * CELL_HEIGHT)

    x >= GRID_OFFSET_X && x < gridEndx && y >= GRID_OFFSET_Y && y < gridEndY