package it.unibo.pps.wvt.utilities

import it.unibo.pps.wvt.utilities.ViewConstants.*

import scala.annotation.tailrec

object GridMapper:

  type PhysicalCoords = (Double, Double)

  def gridToPixel(pos: GridPosition): PixelPosition =
    PixelPosition(
      GRID_OFFSET_X + pos.col * CELL_WIDTH + CELL_WIDTH / 2.0,
      GRID_OFFSET_Y + pos.row * CELL_HEIGHT + CELL_HEIGHT / 2.0
    )

  def pixelToGrid(pos: PixelPosition): GridPosition =
    GridPosition(
      ((pos.y - GRID_OFFSET_Y) / CELL_HEIGHT).toInt.max(0).min(GRID_ROWS - 1),
      ((pos.x - GRID_OFFSET_X) / CELL_WIDTH).toInt.max(0).min(GRID_COLS - 1),
      allowInvalid = true
    )
  
  def allCells: Seq[PhysicalCoords] =
    for  
      row <- 0 until GRID_ROWS
      col <- 0 until GRID_COLS
    yield
      val gridPos = GridPosition(row, col)
      (GRID_OFFSET_X + gridPos.col * CELL_WIDTH, GRID_OFFSET_Y + gridPos.row * CELL_HEIGHT)

  @tailrec
  def isValidPosition(pos: Position): Boolean = pos match
    case GridPosition(row, col, _) =>
      row >=0 && row < GRID_ROWS && col >= 0 && col < GRID_COLS
    case pixel: PixelPosition =>
      isValidPosition(pixel.toGrid)

  def logicalToPhysical(pos: Position): PhysicalCoords = pos match
    case grid: GridPosition =>
      val pixel = gridToPixel(grid)
      (pixel.x, pixel.y)
    case pixel: PixelPosition =>
      (pixel.x, pixel.y)

  def physicalToLogical(x: Double, y: Double): Option[Position] =
    val col = ((x - GRID_OFFSET_X) / CELL_WIDTH).toInt
    val row = ((y - GRID_OFFSET_Y) / CELL_HEIGHT).toInt

    Option.when(row >= 0 && row < GRID_ROWS && col >= 0 && col < GRID_COLS)(
      GridPosition(row, col)
    )

  def screenToCell(x: Int, y: Int): Option[Position] =
    physicalToLogical(x.toDouble, y.toDouble)

  def cellToScreen(pos: Position): PhysicalCoords =
    logicalToPhysical(pos)

  def getCellBounds(pos: GridPosition): (Double, Double, Double, Double) =
    val x = GRID_OFFSET_X + pos.col * CELL_WIDTH
    val y = GRID_OFFSET_Y + pos.row * CELL_HEIGHT
    (x, y, x + CELL_WIDTH.toDouble, y + CELL_HEIGHT.toDouble)