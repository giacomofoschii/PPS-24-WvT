package it.unibo.pps.wvt.input

import it.unibo.pps.wvt.model.Position
import it.unibo.pps.wvt.utilities.ViewConstants.*

/** Converts between screen coordinates (pixels) and grid positions (rows and columns) */
case class CoordinateConverter():

  def screenToCell(x: Int, y: Int): Option[Position] = {
    if (x < GRID_OFFSET_X || y < GRID_OFFSET_Y) return None
    val gridX = (x - GRID_OFFSET_X) / CELL_SIZE  
    val gridY = (y - GRID_OFFSET_Y) / CELL_SIZE  
    if (gridX >= 0 && gridX < GRID_WIDTH && gridY >= 0 && gridY < GRID_HEIGHT)
      Some(Position(gridY, gridX)) 
    else None
  }

  def cellToScreen(position: Position): (Int, Int) =
    val screenX = GRID_OFFSET_X + (position.col * CELL_SIZE) + (CELL_SIZE / 2)
    val screenY = GRID_OFFSET_Y + (position.row * CELL_SIZE) + (CELL_SIZE / 2)
    (screenX, screenY)

  private def isValidCell(cellX: Int, cellY: Int): Boolean =
    cellX >= 0 && cellX < GRID_WIDTH && cellY >= 0 && cellY < GRID_HEIGHT

  def isValidPosition(position: Position): Boolean =
    position.row >= 0 && position.row < GRID_HEIGHT && position.col >= 0 && position.col < GRID_WIDTH

  def isInGridArea(screenX: Int, screenY: Int): Boolean =
    val gridEndX = GRID_OFFSET_X + (GRID_WIDTH * CELL_SIZE)
    val gridEndY = GRID_OFFSET_Y + (GRID_HEIGHT * CELL_SIZE)

    screenX >= GRID_OFFSET_X && screenX < gridEndX &&
      screenY >= GRID_OFFSET_Y && screenY < gridEndY