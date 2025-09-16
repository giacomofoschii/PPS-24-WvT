package it.unibo.pps.wvt.input

import it.unibo.pps.wvt.model.Position
import it.unibo.pps.wvt.utilities.ViewConstants.*

/** Converts between screen coordinates (pixels) and grid positions (rows and columns) */
case class CoordinateConverter():

  def screenToCell(x: Int, y: Int): Option[Position] = {
    if (x < GRID_OFFSET_X || y < GRID_OFFSET_Y) return None
    val gridX = ((x - GRID_OFFSET_X) / CELL_WIDTH).toInt   // colonna
    val gridY = ((y - GRID_OFFSET_Y) / CELL_HEIGHT).toInt  // riga
    // CORREZIONE: gridX < GRID_COLS, gridY < GRID_ROWS
    if (gridX >= 0 && gridX < GRID_COLS && gridY >= 0 && gridY < GRID_ROWS)
      Some(Position(gridY, gridX))  // Position(row, col)
    else None
  }

  def cellToScreen(position: Position): (Double, Double) =
    // position.col = X (orizzontale), position.row = Y (verticale)
    val screenX = GRID_OFFSET_X + (position.col * CELL_WIDTH) + (CELL_WIDTH / 2)
    val screenY = GRID_OFFSET_Y + (position.row * CELL_HEIGHT) + (CELL_HEIGHT / 2)
    (screenX, screenY)

  private def isValidCell(cellX: Int, cellY: Int): Boolean =
    // cellX = col, cellY = row
    cellX >= 0 && cellX < GRID_COLS && cellY >= 0 && cellY < GRID_ROWS

  def isValidPosition(position: Position): Boolean =
    // position.row < GRID_ROWS, position.col < GRID_COLS
    position.row >= 0 && position.row < GRID_ROWS && position.col >= 0 && position.col < GRID_COLS

  def isInGridArea(screenX: Int, screenY: Int): Boolean =
    // CORREZIONE: X -> COLS * WIDTH, Y -> ROWS * HEIGHT
    val gridEndX = GRID_OFFSET_X + (GRID_COLS * CELL_WIDTH)
    val gridEndY = GRID_OFFSET_Y + (GRID_ROWS * CELL_HEIGHT)

    screenX >= GRID_OFFSET_X && screenX < gridEndX &&
      screenY >= GRID_OFFSET_Y && screenY < gridEndY