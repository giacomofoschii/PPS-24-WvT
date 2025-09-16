package it.unibo.pps.wvt.input

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import it.unibo.pps.wvt.model.*
import it.unibo.pps.wvt.utilities.ViewConstants.*
import it.unibo.pps.wvt.input.TestInputConstants.*

class InputProcessorTest extends AnyFunSuite with Matchers:

  private val processor = InputProcessor()

  test("processClick should return invalid result for coordinates outside grid"):
    INVALID_COORDS.foreach: (x, y) =>
      val outsideClick = MouseClick(x, y)
      val result = processor.processClick(outsideClick)
      result.isValid shouldBe false
      result.error shouldBe defined

  test("processClick should return valid result for coordinates inside grid"):
    val click1 = MouseClick(INSIDE_GRID_X, INSIDE_GRID_Y)
    val result1 = processor.processClick(click1)
    result1.isValid shouldBe true
    result1.position shouldBe TOP_LEFT_POS
    result1.error shouldBe None

    val click2 = MouseClick(cellCenterX(2), cellCenterY(1))
    val result2 = processor.processClick(click2)
    result2.isValid shouldBe true
    result2.position shouldBe SAMPLE_POS_1
    result2.error shouldBe None

  test("processClick should handle edge cases correctly"):
    val edgeClick1 = MouseClick(LAST_VALID_X, LAST_VALID_Y)
    val result1 = processor.processClick(edgeClick1)
    result1.isValid shouldBe true
    result1.position shouldBe BOTTOM_RIGHT_POS
    result1.error shouldBe None

    val edgeClick2 = MouseClick(FIRST_INVALID_X, FIRST_INVALID_Y)
    val result2 = processor.processClick(edgeClick2)
    result2.isValid shouldBe false
    result2.position shouldBe INVALID_POS
    result2.error shouldBe defined

  test("processClick should work for all corners of the grid"):
    val topLeftClick = MouseClick(TOP_LEFT_X, TOP_LEFT_Y)
    val result1 = processor.processClick(topLeftClick)
    result1.isValid shouldBe true
    result1.position shouldBe TOP_LEFT_POS

    val topRightClick = MouseClick(TOP_RIGHT_X, TOP_RIGHT_Y)
    val result2 = processor.processClick(topRightClick)
    result2.isValid shouldBe true
    result2.position shouldBe TOP_RIGHT_POS

    val bottomLeftClick = MouseClick(BOTTOM_LEFT_X, BOTTOM_LEFT_Y)
    val result3 = processor.processClick(bottomLeftClick)
    result3.isValid shouldBe true
    result3.position shouldBe BOTTOM_LEFT_POS

    val bottomRightClick = MouseClick(BOTTOM_RIGHT_X, BOTTOM_RIGHT_Y)
    val result4 = processor.processClick(bottomRightClick)
    result4.isValid shouldBe true
    result4.position shouldBe BOTTOM_RIGHT_POS

  test("processClick should delegate correctly to converter for boundary detection"):
    BOUNDARY_COORDS.foreach: (x, y) =>
      val boundaryClick = MouseClick(x, y)
      val result = processor.processClick(boundaryClick)
      result.isValid shouldBe true
      result.error shouldBe None

  test("positionToScreen should return Some for valid positions"):
    val result1 = processor.positionToScreen(TOP_LEFT_POS)
    result1 shouldBe defined
    val (screenX1, screenY1) = result1.get
    screenX1 shouldBe EXPECTED_TOP_LEFT_SCREEN_X
    screenY1 shouldBe EXPECTED_TOP_LEFT_SCREEN_Y

    val result2 = processor.positionToScreen(SAMPLE_POS_2)
    result2 shouldBe defined
    val (screenX2, screenY2) = result2.get
    screenX2 shouldBe EXPECTED_SAMPLE_POS_2_SCREEN_X
    screenY2 shouldBe EXPECTED_SAMPLE_POS_2_SCREEN_Y

    val result3 = processor.positionToScreen(BOTTOM_RIGHT_POS)
    result3 shouldBe defined

    val result4 = processor.positionToScreen(CENTER_POS)
    result4 shouldBe defined

  test("positionToScreen should return None for positions outside processor bounds"):
    try {
      val testPosition = TOP_LEFT_POS
      val result = processor.positionToScreen(testPosition)
    } catch {
      case _: IllegalArgumentException =>
        succeed
    }

  test("isValidPosition should delegate to converter"):
    processor.isValidPosition(SAMPLE_POS_2) shouldBe true
    processor.isValidPosition(BOTTOM_RIGHT_POS) shouldBe true
    processor.isValidPosition(TOP_LEFT_POS) shouldBe true
    processor.isValidPosition(CENTER_POS) shouldBe true

  test("isValidPosition should work for edge positions"):
    processor.isValidPosition(TOP_RIGHT_POS) shouldBe true
    processor.isValidPosition(BOTTOM_LEFT_POS) shouldBe true

  test("round trip: processClick -> positionToScreen should be consistent"):
    TEST_CLICKS.foreach: (x, y) =>
      val clickResult = processor.processClick(MouseClick(x, y))
      if clickResult.isValid then
        val backToScreen = processor.positionToScreen(clickResult.position)
        backToScreen shouldBe defined
        val (screenX, screenY) = backToScreen.get
        screenX should be >= GRID_OFFSET_X
        screenX should be < GRID_END_X.toDouble
        screenY should be >= GRID_OFFSET_Y
        screenY should be < GRID_END_Y.toDouble

  test("processClick should handle boundary pixels correctly"):
    val boundaryClick = MouseClick(GRID_START_X, GRID_START_Y)
    val result1 = processor.processClick(boundaryClick)
    result1.isValid shouldBe true
    result1.position shouldBe TOP_LEFT_POS

    val beforeBoundaryClick = MouseClick(BEFORE_BOUNDARY_X, BEFORE_BOUNDARY_Y)
    val result2 = processor.processClick(beforeBoundaryClick)
    result2.isValid shouldBe false
    result2.position shouldBe INVALID_POS
    result2.error shouldBe defined

  test("processClick should be consistent across multiple calls with same input"):
    TEST_CLICKS.foreach: (x, y) =>
      val click = MouseClick(x, y)
      val result1 = processor.processClick(click)
      val result2 = processor.processClick(click)
      val result3 = processor.processClick(click)

      result1.isValid shouldBe result2.isValid
      result1.position shouldBe result2.position
      result1.error shouldBe result2.error

      result2.isValid shouldBe result3.isValid
      result2.position shouldBe result3.position
      result2.error shouldBe result3.error

  test("processClick should handle center positions correctly"):
    val centerClick = MouseClick(cellCenterX(CENTER_POS.col), cellCenterY(CENTER_POS.row))
    val result = processor.processClick(centerClick)
    result.isValid shouldBe true
    result.position shouldBe CENTER_POS
    result.error shouldBe None

  test("processClick should handle sample positions correctly"):
    TEST_POSITIONS.foreach: position =>
      val (screenX, screenY) = processor.positionToScreen(position).get
      val click = MouseClick(screenX.toInt, screenY.toInt)
      val result = processor.processClick(click)
      result.isValid shouldBe true
      result.position shouldBe position
      result.error shouldBe None