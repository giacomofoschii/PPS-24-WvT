package it.unibo.pps.wvt.utilities

import it.unibo.pps.wvt.utilities.ViewConstants.{GRID_COLS, GRID_ROWS}

/**
 * Represents a position in a 2D space, a pixel in the game window.
 * 
 * @param x the x-coordinate
 * @param y the y-coordinate
 */
case class Position(x: Double, y: Double):
  def isInCell: Boolean = GridMapper.isInCell(this)
  def isValid: Boolean  = GridMapper.isValidPosition(this)
