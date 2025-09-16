package it.unibo.pps.wvt.utilities

import it.unibo.pps.wvt.model.{Position, Grid}
import it.unibo.pps.wvt.utilities.ViewConstants._

object GridMapper {

  type PhysicalCoords = (Double, Double)

  def logicalToPhysical(position: Position): PhysicalCoords =
    (GRID_OFFSET_X + position.col * CELL_WIDTH, GRID_OFFSET_Y + position.row * CELL_HEIGHT)

  def physicalToLogical(x: Double, y: Double): Option[Position] =
    Option.when(x >= GRID_OFFSET_X && y >= GRID_OFFSET_Y):
      val col = ((x - GRID_OFFSET_X) / CELL_WIDTH).toInt
      val row = ((y - GRID_OFFSET_Y) / CELL_HEIGHT).toInt
      Position(row, col)
    .filter(isValidPosition)

  def getCellCenter(position: Position): PhysicalCoords = {
    val (x, y) = logicalToPhysical(position)
    (x + CELL_WIDTH / 2.0, y + CELL_HEIGHT / 2.0)
  }

  private def isValidPosition(pos: Position): Boolean =
    pos.row >= 0 && pos.row < GRID_ROWS && pos.col >= 0 && pos.col < GRID_COLS
}
