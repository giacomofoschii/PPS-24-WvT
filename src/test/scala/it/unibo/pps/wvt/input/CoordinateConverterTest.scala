package it.unibo.pps.wvt.input

import it.unibo.pps.wvt.model.Position
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import it.unibo.pps.wvt.utilities.ViewConstants.*
import it.unibo.pps.wvt.input.TestInputConstants.*

class CoordinateConverterTest extends AnyFunSuite with Matchers:

  val converter = CoordinateConverter()

  test("screenToCell should convert screen coordinates to cell positions correctly"):
    val topLeft = converter.screenToCell(GRID_START_X, GRID_START_Y)
    topLeft shouldBe Some(TOP_LEFT_POS)
    val centerFirstCell = converter.screenToCell(FIRST_CELL_CENTER_X, FIRST_CELL_CENTER_Y)
    centerFirstCell shouldBe Some(TOP_LEFT_POS)
    val secondCell = converter.screenToCell(cellCornerX(1), cellCornerY(1))
    secondCell shouldBe Some(Position(1, 1))

  test("screenToCell should return None for coordinates outside grid"):
    converter.screenToCell(40, 40) shouldBe None
    converter.screenToCell(100, 600) shouldBe None
    val tooFarX = GRID_OFFSET_X + (GRID_COLS * CELL_WIDTH) + 10
    val tooFarY = GRID_OFFSET_Y + (GRID_ROWS * CELL_HEIGHT) + 10
    converter.screenToCell(tooFarX.toInt, tooFarY.toInt) shouldBe None
    converter.screenToCell(-10, -10) shouldBe None

  test("screenToCell should handle edge cases correctly"):
    val lastValidX = GRID_OFFSET_X + (GRID_COLS * CELL_WIDTH) - 1
    val lastValidY = GRID_OFFSET_Y + (GRID_ROWS * CELL_HEIGHT) - 1
    val lastCell = converter.screenToCell(lastValidX.toInt, lastValidY.toInt)
    lastCell shouldBe Some(Position(GRID_ROWS - 1, GRID_COLS - 1))
    val firstInvalidX = GRID_OFFSET_X + (GRID_COLS * CELL_WIDTH)
    val firstInvalidY = GRID_OFFSET_Y + (GRID_ROWS * CELL_HEIGHT)
    converter.screenToCell(firstInvalidX.toInt, firstInvalidY.toInt) shouldBe None

  test("cellToScreen should convert cell positions to screen coordinates correctly"):
    val topLeftCenter = converter.cellToScreen(Position(0, 0))
    val expectedX = GRID_OFFSET_X + CELL_WIDTH/2
    val expectedY = GRID_OFFSET_Y + CELL_HEIGHT/2
    topLeftCenter shouldBe (expectedX, expectedY)
    val secondCellCenter = converter.cellToScreen(Position(1, 1))
    val expected1X = GRID_OFFSET_X + CELL_WIDTH + CELL_WIDTH/2
    val expected1Y = GRID_OFFSET_Y + CELL_HEIGHT + CELL_HEIGHT/2
    secondCellCenter shouldBe (expected1X, expected1Y)

  test("cellToScreen should work for all valid positions"):
    val topLeft = converter.cellToScreen(Position(0, 0))
    val topRight = converter.cellToScreen(Position(0, GRID_COLS - 1))
    val bottomLeft = converter.cellToScreen(Position(GRID_ROWS - 1, 0))
    val bottomRight = converter.cellToScreen(Position(GRID_ROWS - 1, GRID_COLS - 1))
    topLeft._1 should be < topRight._1
    topLeft._2 should be < bottomLeft._2
    bottomRight._1 should be > bottomLeft._1
    bottomRight._2 should be > topRight._2

  test("isInGridArea should correctly identify grid area"):
    converter.isInGridArea((GRID_OFFSET_X + 10).toInt, (GRID_OFFSET_Y + 10).toInt) shouldBe true
    converter.isInGridArea((GRID_OFFSET_X + 100).toInt, (GRID_OFFSET_Y + 100).toInt) shouldBe true
    converter.isInGridArea((GRID_OFFSET_X - 10).toInt, GRID_OFFSET_Y.toInt) shouldBe false
    converter.isInGridArea(GRID_OFFSET_X.toInt, (GRID_OFFSET_Y - 10).toInt) shouldBe false
    val endX = GRID_OFFSET_X + (GRID_COLS * CELL_WIDTH)
    val endY = GRID_OFFSET_Y + (GRID_ROWS * CELL_HEIGHT)
    converter.isInGridArea((endX + 10).toInt, (endY + 10).toInt) shouldBe false

  test("isInGridArea should handle boundary cases correctly"):
    converter.isInGridArea(GRID_OFFSET_X.toInt, GRID_OFFSET_Y.toInt) shouldBe true
    val lastValidX = GRID_OFFSET_X + (GRID_COLS * CELL_WIDTH) - 1
    val lastValidY = GRID_OFFSET_Y + (GRID_ROWS * CELL_HEIGHT) - 1
    converter.isInGridArea(lastValidX.toInt, lastValidY.toInt) shouldBe true
    val firstInvalidX = GRID_OFFSET_X + (GRID_COLS * CELL_WIDTH)
    val firstInvalidY = GRID_OFFSET_Y + (GRID_ROWS * CELL_HEIGHT)
    converter.isInGridArea(firstInvalidX.toInt, firstInvalidY.toInt) shouldBe false

  test("round trip conversion should be consistent"):
    val testPositions = List(
      Position(0, 0),
      Position(2, 3),
      Position(GRID_ROWS - 1, GRID_COLS - 1),
      Position(3, 7)
    )
    testPositions.foreach: pos =>
      val (screenX, screenY) = converter.cellToScreen(pos)
      val convertedBack = converter.screenToCell(screenX.toInt, screenY.toInt)
      convertedBack shouldBe Some(pos)

  test("converter should be immutable"):
    val converter1 = CoordinateConverter()
    val converter2 = CoordinateConverter()
    converter1 shouldBe converter2
    val testResult1 = converter1.screenToCell(150, 250)
    val testResult2 = converter2.screenToCell(150, 250)
    testResult1 shouldBe testResult2