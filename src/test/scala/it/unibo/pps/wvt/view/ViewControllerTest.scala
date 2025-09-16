package it.unibo.pps.wvt.view

import it.unibo.pps.wvt.model.Grid
import it.unibo.pps.wvt.utilities.ViewConstants._

import org.scalatest.funsuite.AnyFunSuite

class GridInitializationTest extends AnyFunSuite {
  test("Grid should initialize with correct dimensions and empty cells") {
    val grid = Grid.empty

    assert(grid.cells.length == GRID_ROWS)
    assert(grid.cells.forall(_.length == GRID_COLS))
    assert(grid.cells.flatten.forall(_.isEmpty))
  }
}