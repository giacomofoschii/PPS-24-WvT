package it.unibo.pps.wvt.utilities

import it.unibo.pps.wvt.utilities.ViewConstants.{GRID_COLS, GRID_ROWS}

case class Position(row: Int, col: Int, allowInvalid: Boolean = false):
  if !allowInvalid then
    require(row >= 0 && row < GRID_ROWS, s"Row must be between 0 and ${GRID_ROWS - 1}")
    require(col >= 0 && col < GRID_COLS, s"Col must be between 0 and ${GRID_COLS - 1}")

  def isValid: Boolean = GridMapper.isValidPosition(this)