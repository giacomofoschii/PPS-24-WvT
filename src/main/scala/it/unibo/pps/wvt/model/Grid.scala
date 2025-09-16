package it.unibo.pps.wvt.model

import it.unibo.pps.wvt.model.CellType.Troll
import it.unibo.pps.wvt.utilities.ViewConstants.*

enum CellType:
  case Empty
  case Wizard
  case Troll

case class Cell(position: Position, cellType: CellType):
  def isEmpty: Boolean = cellType == CellType.Empty
  def isWizard: Boolean = cellType == CellType.Wizard
  def isTroll: Boolean = cellType == CellType.Troll

case class Grid(cells: Array[Array[Cell]]):
  def get(pos:Position): Cell = cells(pos.row)(pos.col)

  def set(pos:Position, cell: Cell): Grid = {
    val newCells = cells.map(_.clone())
    newCells(pos.row)(pos.col) = cell
    Grid(newCells)
  }

  def emptyCell(position: Position): Grid =
    set(position, Cell(position, CellType.Empty))

  def isValidPosition(pos: Position): Boolean =
    pos.row >= 0 && pos.row < GRID_ROWS && pos.col >= 0 && pos.col < GRID_COLS
  
  private lazy val allCells: Seq[Cell] =
    for
      row <- 0 until GRID_ROWS
      col <- 0 until GRID_COLS
    yield cells(row)(col)

  private lazy val allPositions: Seq[Position] = allCells.map(_.position)

  def getAvailablePositions: Seq[Position] =
    allCells.filter(_.cellType != Troll).map(_.position)
    
  def getCellsByType(cellType: CellType): Seq[Cell] =
    allCells.filter(_.cellType == cellType)
    
object Grid:
  def empty: Grid =
    val data = Array.tabulate(GRID_ROWS, GRID_COLS) { (row, col) =>
      Cell(Position(row, col), CellType.Empty)
    }
    Grid(data)