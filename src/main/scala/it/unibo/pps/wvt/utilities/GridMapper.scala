package it.unibo.pps.wvt.utilities

import it.unibo.pps.wvt.utilities.ViewConstants._

object GridMapper:

  type PhysicalCoords = (Double, Double)
  
  def allCells: Seq[PhysicalCoords] =
    for  
      row <- 0 until GRID_ROWS
      col <- 0 until GRID_COLS
    yield logicalToPhysical(Position(row, col))

  def isValidPosition(pos: Position): Boolean =
    pos.row >= 0 && pos.row < GRID_ROWS && pos.col >= 0 && pos.col < GRID_COLS
    
  def logicalToPhysical(position: Position): PhysicalCoords =
    (GRID_OFFSET_X + position.col * CELL_WIDTH, GRID_OFFSET_Y + position.row * CELL_HEIGHT)

  def physicalToLogical(x: Double, y: Double): Option[Position] =
    Option.when(x >= GRID_OFFSET_X && y >= GRID_OFFSET_Y):
      val col = ((x - GRID_OFFSET_X) / CELL_WIDTH).toInt
      val row = ((y - GRID_OFFSET_Y) / CELL_HEIGHT).toInt
      Position(row, col, allowInvalid = true)
    .filter(isValidPosition)

  def screenToCell(x: Int, y: Int): Option[Position] =
    physicalToLogical(x.toDouble, y.toDouble)

  def cellToScreen(position: Position): PhysicalCoords =
    getCellCenter(position)

  private def getCellCenter(position: Position): PhysicalCoords =
    val (x, y) = logicalToPhysical(position)
    (x + CELL_WIDTH / 2.0, y + CELL_HEIGHT / 2.0)
