package it.unibo.pps.wvt.utilities

import it.unibo.pps.wvt.utilities.ViewConstants.{GRID_COLS, GRID_ROWS}

sealed trait Position:
  def x: Double
  def y: Double
  def isValid: Boolean
  def toGrid: GridPosition
  def toPixel: PixelPosition
  def row: Int
  def col: Int

case class GridPosition(row: Int, col: Int, allowInvalid: Boolean = false) extends Position:
  if !allowInvalid then
    require(row >= 0 && row < GRID_ROWS, s"Row must be between 0 and ${GRID_ROWS - 1}")
    require(col >= 0 && col < GRID_COLS, s"Column must be between 0 and ${GRID_COLS - 1}")

  val x: Double = col.toDouble
  val y: Double = row.toDouble
  def isValid: Boolean = GridMapper.isValidPosition(this)
  def toGrid: GridPosition = this
  def toPixel: PixelPosition = GridMapper.gridToPixel(this)

case class PixelPosition(x: Double, y: Double) extends Position:
  def isValid: Boolean =
    val grid = toGrid
    grid.row >= 0 && grid.row < GRID_ROWS && grid.col >= 0 && grid.col < GRID_COLS

  def toGrid: GridPosition = GridMapper.pixelToGrid(this)
  def toPixel: PixelPosition = this
  def row: Int = toGrid.row
  def col: Int = toGrid.col

  //Functional method for the transformations
  def translate(dx: Double, dy: Double): PixelPosition =
    PixelPosition(x + dx, y + dy)

  def clamp(minX: Double, maxX: Double, minY: Double, maxY:Double): PixelPosition =
    PixelPosition(
      x.max(minX).min(maxX),
      y.max(minY).min(maxY)
    )

  def distanceTo(other: PixelPosition): Double =
    math.sqrt(math.pow(x - other.x, 2) + math.pow(y - other.y, 2))

object Position:
  def apply(row: Int, col: Int, allowInvalid: Boolean = false): GridPosition =
    GridPosition(row, col, allowInvalid)