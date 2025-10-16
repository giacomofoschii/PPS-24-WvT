package it.unibo.pps.wvt.utilities

import it.unibo.pps.wvt.utilities.ViewConstants.*
import it.unibo.pps.wvt.utilities._

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class GridMapperTest extends AnyFunSuite with Matchers:
  test("physicalToLogical should convert valid grid position"):
    val centerPos = Position(
      GRID_OFFSET_X + CELL_WIDTH / 2,
      GRID_OFFSET_Y + CELL_HEIGHT / 2
    )
    val result = GridMapper.physicalToLogical(centerPos)
    result shouldBe Some((0, 0))

  test("physicalToLogical should return None for position outside grid"):
    val outsidePos = Position(-100, -100)
    GridMapper.physicalToLogical(outsidePos) shouldBe None

  test("logicalToPhysical should convert valid logical coordinates"):
    val result = GridMapper.logicalToPhysical(0, 0)
    result shouldBe Some(Position(
      GRID_OFFSET_X + CELL_WIDTH / 2,
      GRID_OFFSET_Y + CELL_HEIGHT / 2
    ))

  test("logicalToPhysical should return None for invalid coordinates"):
    GridMapper.logicalToPhysical(-1, 0) shouldBe None
    GridMapper.logicalToPhysical(0, -1) shouldBe None
    GridMapper.logicalToPhysical(GRID_ROWS, 0) shouldBe None
    GridMapper.logicalToPhysical(0, GRID_COLS) shouldBe None

  test("allCells lazy val should contain all grid cells"):
    val cells = GridMapper.allCells
    cells.size shouldBe GRID_ROWS * GRID_COLS

  test("isValidCell should validate positions correctly"):
    val validPos = Position(
      GRID_OFFSET_X + CELL_WIDTH,
      GRID_OFFSET_Y + CELL_HEIGHT
    )
    GridMapper.isInCell(validPos) shouldBe true

    val invalidPos = Position(-100, -100)
    GridMapper.isInCell(invalidPos) shouldBe false

  test("isInCell should check if position is within grid cells"):
    val inCellPos = Position(
      GRID_OFFSET_X + CELL_WIDTH / 2,
      GRID_OFFSET_Y + CELL_HEIGHT / 2
    )
    GridMapper.isInCell(inCellPos) shouldBe true

    val outPos = Position(
      GRID_OFFSET_X + GRID_COLS * CELL_WIDTH + 10,
      GRID_OFFSET_Y
    )
    GridMapper.isInCell(outPos) shouldBe false

  test("getCellBounds should return correct boundaries"):
    val (left, top, right, bottom) = GridMapper.getCellBounds(0, 0)

    left shouldBe GRID_OFFSET_X.toDouble
    top shouldBe GRID_OFFSET_Y.toDouble
    right shouldBe (GRID_OFFSET_X + CELL_WIDTH).toDouble
    bottom shouldBe (GRID_OFFSET_Y + CELL_HEIGHT).toDouble

class PositionTest extends AnyFunSuite with Matchers:
  test("Position should be created with valid coordinates"):
    val pos = Position(100, 200)
    pos.x shouldBe 100
    pos.y shouldBe 200
