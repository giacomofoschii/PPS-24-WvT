
package it.unibo.pps.wvt.input

import it.unibo.pps.wvt.model.Position
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import it.unibo.pps.wvt.utilities.ViewConstants.*

class CoordinateConverterTest extends AnyFunSuite with Matchers:

  val converter = CoordinateConverter()

  test("screenToCell should convert screen coordinates to cell positions correctly"):
    val topLeft = converter.screenToCell(GRID_OFFSET_X, GRID_OFFSET_Y)
    topLeft shouldBe Some(Position(0, 0))
    val centerFirstCell = converter.screenToCell(GRID_OFFSET_X + CELL_SIZE/2, GRID_OFFSET_Y + CELL_SIZE/2)
    centerFirstCell shouldBe Some(Position(0, 0))
    val secondCell = converter.screenToCell(GRID_OFFSET_X + CELL_SIZE, GRID_OFFSET_Y + CELL_SIZE)
    secondCell shouldBe Some(Position(1, 1))

  test("screenToCell should return None for coordinates outside grid"):
    converter.screenToCell(40, 40) shouldBe None
    converter.screenToCell(100, 600) shouldBe None
    val tooFarX = GRID_OFFSET_X + (GRID_WIDTH * CELL_SIZE) + 10
    val tooFarY = GRID_OFFSET_Y + (GRID_HEIGHT * CELL_SIZE) + 10
    converter.screenToCell(tooFarX, tooFarY) shouldBe None
    converter.screenToCell(-10, -10) shouldBe None

  test("screenToCell should handle edge cases correctly"):
    val lastValidX = GRID_OFFSET_X + (GRID_WIDTH * CELL_SIZE) - 1  // 949
    val lastValidY = GRID_OFFSET_Y + (GRID_HEIGHT * CELL_SIZE) - 1  // 549
    val lastCell = converter.screenToCell(lastValidX, lastValidY)
    lastCell shouldBe Some(Position(GRID_HEIGHT - 1, GRID_WIDTH - 1))  // Position(4, 8)
    val firstInvalidX = GRID_OFFSET_X + (GRID_WIDTH * CELL_SIZE)   // 950
    val firstInvalidY = GRID_OFFSET_Y + (GRID_HEIGHT * CELL_SIZE)  // 550
    converter.screenToCell(firstInvalidX, firstInvalidY) shouldBe None

  test("cellToScreen should convert cell positions to screen coordinates correctly"):
    val topLeftCenter = converter.cellToScreen(Position(0, 0))
    val expectedX = GRID_OFFSET_X + CELL_SIZE/2
    val expectedY = GRID_OFFSET_Y + CELL_SIZE/2
    topLeftCenter shouldBe (expectedX, expectedY)
    val secondCellCenter = converter.cellToScreen(Position(1, 1))
    val expected1X = GRID_OFFSET_X + CELL_SIZE + CELL_SIZE/2
    val expected1Y = GRID_OFFSET_Y + CELL_SIZE + CELL_SIZE/2
    secondCellCenter shouldBe (expected1X, expected1Y)

  test("cellToScreen should work for all valid positions"):
    val topLeft = converter.cellToScreen(Position(0, 0))                           // ✓ Corretto
    val topRight = converter.cellToScreen(Position(0, GRID_WIDTH - 1))            // row=0 (top), col=8 (right)
    val bottomLeft = converter.cellToScreen(Position(GRID_HEIGHT - 1, 0))         // row=4 (bottom), col=0 (left)
    val bottomRight = converter.cellToScreen(Position(GRID_HEIGHT - 1, GRID_WIDTH - 1)) // row=4 (bottom), col=8 (right)
    topLeft._1 should be < topRight._1      // x di topLeft < x di topRight ✓
    topLeft._2 should be < bottomLeft._2    // y di topLeft < y di bottomLeft ✓
    bottomRight._1 should be > bottomLeft._1 // x di bottomRight > x di bottomLeft ✓
    bottomRight._2 should be > topRight._2   // y di bottomRight > y di topRight ✓


  test("isInGridArea should correctly identify grid area"):
    converter.isInGridArea(GRID_OFFSET_X + 10, GRID_OFFSET_Y + 10) shouldBe true
    converter.isInGridArea(GRID_OFFSET_X + 100, GRID_OFFSET_Y + 100) shouldBe true
    converter.isInGridArea(GRID_OFFSET_X - 10, GRID_OFFSET_Y) shouldBe false
    converter.isInGridArea(GRID_OFFSET_X, GRID_OFFSET_Y - 10) shouldBe false
    val endX = GRID_OFFSET_X + (GRID_WIDTH * CELL_SIZE)
    val endY = GRID_OFFSET_Y + (GRID_HEIGHT * CELL_SIZE)
    converter.isInGridArea(endX + 10, endY + 10) shouldBe false

  test("isInGridArea should handle boundary cases correctly"):
    converter.isInGridArea(GRID_OFFSET_X, GRID_OFFSET_Y) shouldBe true
    val lastValidX = GRID_OFFSET_X + (GRID_WIDTH * CELL_SIZE) - 1
    val lastValidY = GRID_OFFSET_Y + (GRID_HEIGHT * CELL_SIZE) - 1
    converter.isInGridArea(lastValidX, lastValidY) shouldBe true
    val firstInvalidX = GRID_OFFSET_X + (GRID_WIDTH * CELL_SIZE)
    val firstInvalidY = GRID_OFFSET_Y + (GRID_HEIGHT * CELL_SIZE)
    converter.isInGridArea(firstInvalidX, firstInvalidY) shouldBe false

  test("round trip conversion should be consistent"):
    val testPositions = List(
      Position(0, 0),                                        // ✓ Valido
      Position(2, 3),                                        // ✓ Valido (invece di 5,5)
      Position(GRID_HEIGHT - 1, GRID_WIDTH - 1),           // Position(4, 8) - CORRETTO
      Position(3, 7)                                         // ✓ Valido (invece di 8,3)
    )

    testPositions.foreach: pos =>
      val (screenX, screenY) = converter.cellToScreen(pos)
      val convertedBack = converter.screenToCell(screenX, screenY)
      convertedBack shouldBe Some(pos)

  test("converter should be immutable"):
    val converter1 = CoordinateConverter()
    val converter2 = CoordinateConverter()
    converter1 shouldBe converter2
    val testResult1 = converter1.screenToCell(150, 250)
    val testResult2 = converter2.screenToCell(150, 250)
    testResult1 shouldBe testResult2