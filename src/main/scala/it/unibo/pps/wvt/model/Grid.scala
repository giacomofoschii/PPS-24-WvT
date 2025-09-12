package it.unibo.pps.wvt.model

sealed trait CellType
object CellType {
  case object Empty extends CellType
  case object Wizard extends CellType
  case object Troll extends CellType
}

case class Grid(cells: Array[Array[CellType]] = Array.fill(5, 9)(CellType.Empty)) {
  def get(pos:Position): CellType = cells(pos.row)(pos.col)

  def set(pos:Position, cellType: CellType): Grid = {
    val newCells = cells.map(_.clone())
    newCells(pos.row)(pos.col) = cellType
    Grid(newCells)
  }

  def isValidPosition(pos: Position): Boolean =
    pos.row >= 0 && pos.row < 5 && pos.col >= 0 && pos.col < 9

  def isEmpty(pos: Position): Boolean = get(pos) == CellType.Empty
}
