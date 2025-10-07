package it.unibo.pps.wvt.input

import it.unibo.pps.wvt.utilities.Position
import it.unibo.pps.wvt.utilities.ViewConstants._

case class InputProcessor():

  def processClick(click: MouseClick): ClickResult =
    val position = Position(click.x, click.y)
    if position.isValid then
      ClickResult(
        pos = position,
        isValid = true,
        error = None
      )
    else
      ClickResult(
        pos = Position(-1, -1),
        isValid = false,
        error = Some("Invalid Position")
      )

  def isInGridArea(x: Double, y: Double): Boolean =
    val gridEndX = GRID_OFFSET_X + (GRID_COLS * CELL_WIDTH)
    val gridEndY = GRID_OFFSET_Y + (GRID_ROWS * CELL_HEIGHT)

    x >= GRID_OFFSET_X && x < gridEndX && y >= GRID_OFFSET_Y && y < gridEndY