package it.unibo.pps.wvt.utilities

import it.unibo.pps.wvt.utilities.ViewConstants.{GRID_COLS, GRID_ROWS}

case class Position(x: Double, y: Double):
  def isInCell: Boolean = GridMapper.isInCell(this)
  def isValid: Boolean  = GridMapper.isValidPosition(this)
