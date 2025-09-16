package it.unibo.pps.wvt.input

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import it.unibo.pps.wvt.model.*
import it.unibo.pps.wvt.utilities.ViewConstants.*
import it.unibo.pps.wvt.input.TestInputConstants.*
import java.io.{ByteArrayOutputStream, PrintStream}

class InputSystemTest extends AnyFunSuite with Matchers:
  private val inputSystem = InputSystem()

  test("handleMouseClick should return invalid result for coordinates outside grid"):
    INVALID_COORDS.foreach: (x, y) =>
      val result = inputSystem.handleMouseClick(x, y)
      result.isValid shouldBe false
      result.error shouldBe defined

  test("handleMouseClick should return valid result for coordinates inside grid"):
    val result1 = inputSystem.handleMouseClick(INSIDE_GRID_X, INSIDE_GRID_Y)
    result1.isValid shouldBe true
    result1.position shouldBe TOP_LEFT_POS
    result1.error shouldBe None

    val result2 = inputSystem.handleMouseClick(cellCenterX(2), cellCenterY(1))
    result2.isValid shouldBe true
    result2.position shouldBe SAMPLE_POS_1
    result2.error shouldBe None

  test("handleMouseClick should handle edge cases correctly"):
    val result1 = inputSystem.handleMouseClick(LAST_VALID_X, LAST_VALID_Y)
    result1.isValid shouldBe true
    result1.position shouldBe BOTTOM_RIGHT_POS
    result1.error shouldBe None

    val result2 = inputSystem.handleMouseClick(FIRST_INVALID_X, FIRST_INVALID_Y)
    result2.isValid shouldBe false
    result2.position shouldBe INVALID_POS
    result2.error shouldBe defined

  test("handleMouseClick should work for all corners of the grid"):
    val result1 = inputSystem.handleMouseClick(TOP_LEFT_X, TOP_LEFT_Y)
    result1.isValid shouldBe true
    result1.position shouldBe TOP_LEFT_POS

    val result2 = inputSystem.handleMouseClick(TOP_RIGHT_X, TOP_RIGHT_Y)
    result2.isValid shouldBe true
    result2.position shouldBe TOP_RIGHT_POS

    val result3 = inputSystem.handleMouseClick(BOTTOM_LEFT_X, BOTTOM_LEFT_Y)
    result3.isValid shouldBe true
    result3.position shouldBe BOTTOM_LEFT_POS

    val result4 = inputSystem.handleMouseClick(BOTTOM_RIGHT_X, BOTTOM_RIGHT_Y)
    result4.isValid shouldBe true
    result4.position shouldBe BOTTOM_RIGHT_POS

  test("handleMouseClick should delegate correctly to InputProcessor"):
    val processor = InputProcessor()
    TEST_CLICKS.foreach: (x, y) =>
      val systemResult = inputSystem.handleMouseClick(x, y)
      val processorResult = processor.processClick(MouseClick(x, y))
      systemResult.isValid shouldBe processorResult.isValid
      systemResult.position shouldBe processorResult.position
      systemResult.error shouldBe processorResult.error

  test("positionToScreen should delegate to InputProcessor"):
    val systemResult = inputSystem.positionToScreen(SAMPLE_POS_2)
    val processor = InputProcessor()
    val processorResult = processor.positionToScreen(SAMPLE_POS_2)
    systemResult shouldBe processorResult

  test("positionToScreen should return Some for valid positions"):
    val result1 = inputSystem.positionToScreen(TOP_LEFT_POS)
    result1 shouldBe defined
    val (screenX1, screenY1) = result1.get
    screenX1 shouldBe EXPECTED_TOP_LEFT_SCREEN_X
    screenY1 shouldBe EXPECTED_TOP_LEFT_SCREEN_Y

    val result2 = inputSystem.positionToScreen(SAMPLE_POS_2)
    result2 shouldBe defined
    val (screenX2, screenY2) = result2.get
    screenX2 shouldBe EXPECTED_SAMPLE_POS_2_SCREEN_X
    screenY2 shouldBe EXPECTED_SAMPLE_POS_2_SCREEN_Y

  test("isValidPosition should delegate to InputProcessor"):
    val systemResult = inputSystem.isValidPosition(SAMPLE_POS_2)
    val processor = InputProcessor()
    val processorResult = processor.isValidPosition(SAMPLE_POS_2)
    systemResult shouldBe processorResult
    systemResult shouldBe true

  test("isValidPosition should work for edge positions"):
    inputSystem.isValidPosition(TOP_LEFT_POS) shouldBe true
    inputSystem.isValidPosition(BOTTOM_RIGHT_POS) shouldBe true



  test("round trip: handleMouseClick -> positionToScreen should be consistent"):
    TEST_CLICKS.foreach: (x, y) =>
      val clickResult = inputSystem.handleMouseClick(x, y)
      if clickResult.isValid then
        val backToScreen = inputSystem.positionToScreen(clickResult.position)
        backToScreen shouldBe defined
        val (screenX, screenY) = backToScreen.get
        val maxX = GRID_END_X
        val maxY = GRID_END_Y
        screenX should be >= GRID_OFFSET_X
        screenX should be < maxX.toDouble
        screenY should be >= GRID_OFFSET_Y
        screenY should be < maxY.toDouble

  test("handleMouseClick should handle boundary pixels correctly"):
    val result1 = inputSystem.handleMouseClick(GRID_START_X, GRID_START_Y)
    result1.isValid shouldBe true
    result1.position shouldBe TOP_LEFT_POS

    val result2 = inputSystem.handleMouseClick(BEFORE_BOUNDARY_X, BEFORE_BOUNDARY_Y)
    result2.isValid shouldBe false
    result2.position shouldBe INVALID_POS
    result2.error shouldBe defined

  test("InputSystem should be stateless"):
    val (x, y) = TEST_CLICKS.head
    val result1 = inputSystem.handleMouseClick(x, y)
    val result2 = inputSystem.handleMouseClick(x, y)
    val result3 = inputSystem.handleMouseClick(x, y)
    result1 shouldBe result2
    result2 shouldBe result3
    result1.isValid shouldBe true
    result2.isValid shouldBe true
    result3.isValid shouldBe true
    result1.position shouldBe result2.position
    result2.position shouldBe result3.position