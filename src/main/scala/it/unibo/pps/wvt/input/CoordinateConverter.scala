package it.unibo.pps.wvt.input

import it.unibo.pps.wvt.model.Position
import it.unibo.pps.wvt.utilities.ViewConstants.*

/** Converts between screen coordinates (pixels) and grid positions (rows and columns) */
case class CoordinateConverter():

  def screenToCell(x: Int, y: Int): Option[Position] = {
    if (x < GRID_OFFSET_X || y < GRID_OFFSET_Y) return None
    val gridX = ((x - GRID_OFFSET_X) / CELL_WIDTH).toInt
    val gridY = ((y - GRID_OFFSET_Y) / CELL_HEIGHT).toInt  
    if (gridX >= 0 && gridX < GRID_ROWS && gridY >= 0 && gridY < GRID_COLS)
      Some(Position(gridY, gridX)) 
    else None
  }

  def cellToScreen(position: Position): (Double, Double) =
    val screenX = GRID_OFFSET_X + (position.col * CELL_WIDTH) + (CELL_WIDTH / 2)
    val screenY = GRID_OFFSET_Y + (position.row * CELL_HEIGHT) + (CELL_HEIGHT / 2)
    (screenX, screenY)

  private def isValidCell(cellX: Int, cellY: Int): Boolean =
    cellX >= 0 && cellX < GRID_ROWS && cellY >= 0 && cellY < GRID_COLS

  def isValidPosition(position: Position): Boolean =
    position.row >= 0 && position.row < GRID_COLS && position.col >= 0 && position.col < GRID_ROWS

  def isInGridArea(screenX: Int, screenY: Int): Boolean =
    val gridEndX = GRID_OFFSET_X + (GRID_ROWS * CELL_WIDTH)
    val gridEndY = GRID_OFFSET_Y + (GRID_COLS * CELL_HEIGHT)

    screenX >= GRID_OFFSET_X && screenX < gridEndX &&
      screenY >= GRID_OFFSET_Y && screenY < gridEndY