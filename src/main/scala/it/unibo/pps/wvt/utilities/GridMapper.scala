package it.unibo.pps.wvt.utilities

import it.unibo.pps.wvt.utilities.ViewConstants.*

object GridMapper:

  type LogicalCoords = (Int, Int)

  lazy val allCells: Seq[Position] =
    for
      row <- 0 until GRID_ROWS
      col <- 0 until GRID_COLS
    yield Position(col * CELL_WIDTH + GRID_OFFSET_X, row * CELL_HEIGHT + GRID_OFFSET_Y)

  def isValidPosition(pos: Position): Boolean =
    pos.x <= GRID_OFFSET_X + GRID_COLS * CELL_WIDTH &&
      pos.y <= GRID_OFFSET_Y + GRID_ROWS * CELL_HEIGHT &&
      pos.x >= GRID_OFFSET_X &&
      pos.y >= GRID_OFFSET_Y

  def isInCell(pos: Position): Boolean =
    val col = ((pos.x - GRID_OFFSET_X) / CELL_WIDTH).toInt
    val row = ((pos.y - GRID_OFFSET_Y) / CELL_HEIGHT).toInt
    col >= 0 && col < GRID_COLS && row >= 0 && row < GRID_ROWS

  def logicalToPhysical(logicalPos: LogicalCoords): Option[Position] =
    val (row, col) = logicalPos
    Some(Position(
      GRID_OFFSET_X + col * CELL_WIDTH + CELL_WIDTH / 2,
      GRID_OFFSET_Y + row * CELL_HEIGHT + CELL_HEIGHT / 2
    ))

  def physicalToLogical(pos: Position): Option[LogicalCoords] =
    if isInCell(pos) then
      val col = ((pos.x - GRID_OFFSET_X) / CELL_WIDTH).toInt
      val row = ((pos.y - GRID_OFFSET_Y) / CELL_HEIGHT).toInt
      Some((row, col))
    else None

  def getCellBounds(row: Int, col: Int): (Double, Double, Double, Double) =
    val left   = GRID_OFFSET_X + col * CELL_WIDTH
    val top    = GRID_OFFSET_Y + row * CELL_HEIGHT
    val right  = left + CELL_WIDTH
    val bottom = top + CELL_HEIGHT
    (left, top, right, bottom)
