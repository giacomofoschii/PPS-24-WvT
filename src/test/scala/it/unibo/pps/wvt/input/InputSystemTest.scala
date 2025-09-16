package it.unibo.pps.wvt.input

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import it.unibo.pps.wvt.model.*
import it.unibo.pps.wvt.utilities.ViewConstants.*
import java.io.{ByteArrayOutputStream, PrintStream}

class InputSystemTest extends AnyFunSuite with Matchers:

  private val inputSystem = InputSystem()

  test("handleMouseClick should return invalid result for coordinates outside grid"):
    val result1 = inputSystem.handleMouseClick(10, 10)
    result1.isValid shouldBe false
    result1.error shouldBe defined
    val result2 = inputSystem.handleMouseClick(-5, -5)
    result2.isValid shouldBe false
    result2.error shouldBe defined
    val tooFarX = GRID_OFFSET_X + (GRID_WIDTH * CELL_SIZE) + 10
    val tooFarY = GRID_OFFSET_Y + (GRID_HEIGHT * CELL_SIZE) + 10
    val result3 = inputSystem.handleMouseClick(tooFarX, tooFarY)
    result3.isValid shouldBe false
    result3.error shouldBe defined

  test("handleMouseClick should return valid result for coordinates inside grid"):
    val firstCellX = GRID_OFFSET_X + 10
    val firstCellY = GRID_OFFSET_Y + 10
    val result1 = inputSystem.handleMouseClick(firstCellX, firstCellY)
    result1.isValid shouldBe true
    result1.position shouldBe Position(0, 0)
    result1.error shouldBe None
    val centerX = GRID_OFFSET_X + (2 * CELL_SIZE) + (CELL_SIZE / 2)  // Centro cella colonna 2
    val centerY = GRID_OFFSET_Y + (1 * CELL_SIZE) + (CELL_SIZE / 2)  // Centro cella riga 1
    val result2 = inputSystem.handleMouseClick(centerX, centerY)
    result2.isValid shouldBe true
    result2.position shouldBe Position(1, 2)
    result2.error shouldBe None

  test("handleMouseClick should handle edge cases correctly"):
    val lastValidX = GRID_OFFSET_X + (GRID_WIDTH * CELL_SIZE) - 1
    val lastValidY = GRID_OFFSET_Y + (GRID_HEIGHT * CELL_SIZE) - 1
    val result1 = inputSystem.handleMouseClick(lastValidX, lastValidY)
    result1.isValid shouldBe true
    result1.position shouldBe Position(GRID_HEIGHT - 1, GRID_WIDTH - 1)
    result1.error shouldBe None
    val firstInvalidX = GRID_OFFSET_X + (GRID_WIDTH * CELL_SIZE)
    val firstInvalidY = GRID_OFFSET_Y + (GRID_HEIGHT * CELL_SIZE)
    val result2 = inputSystem.handleMouseClick(firstInvalidX, firstInvalidY)
    result2.isValid shouldBe false
    result2.position shouldBe Position(-1, -1, allowInvalid = true)
    result2.error shouldBe defined

  test("handleMouseClick should work for all corners of the grid"):
    val result1 = inputSystem.handleMouseClick(GRID_OFFSET_X + 1, GRID_OFFSET_Y + 1)
    result1.isValid shouldBe true
    result1.position shouldBe Position(0, 0)
    val topRightX = GRID_OFFSET_X + ((GRID_WIDTH - 1) * CELL_SIZE) + 10
    val topRightY = GRID_OFFSET_Y + 10
    val result2 = inputSystem.handleMouseClick(topRightX, topRightY)
    result2.isValid shouldBe true
    result2.position shouldBe Position(0, GRID_WIDTH - 1)
    val bottomLeftX = GRID_OFFSET_X + 10
    val bottomLeftY = GRID_OFFSET_Y + ((GRID_HEIGHT - 1) * CELL_SIZE) + 10
    val result3 = inputSystem.handleMouseClick(bottomLeftX, bottomLeftY)
    result3.isValid shouldBe true
    result3.position shouldBe Position(GRID_HEIGHT - 1, 0)
    val bottomRightX = GRID_OFFSET_X + ((GRID_WIDTH - 1) * CELL_SIZE) + 10
    val bottomRightY = GRID_OFFSET_Y + ((GRID_HEIGHT - 1) * CELL_SIZE) + 10
    val result4 = inputSystem.handleMouseClick(bottomRightX, bottomRightY)
    result4.isValid shouldBe true
    result4.position shouldBe Position(GRID_HEIGHT - 1, GRID_WIDTH - 1)

  test("handleMouseClick should delegate correctly to InputProcessor"):
    val processor = InputProcessor()

    val testCases = List(
      (GRID_OFFSET_X + 50, GRID_OFFSET_Y + 50),
      (GRID_OFFSET_X + 250, GRID_OFFSET_Y + 150),
      (10, 10)
    )
    testCases.foreach: (x, y) =>
      val systemResult = inputSystem.handleMouseClick(x, y)
      val processorResult = processor.processClick(MouseClick(x, y))
      systemResult.isValid shouldBe processorResult.isValid
      systemResult.position shouldBe processorResult.position
      systemResult.error shouldBe processorResult.error

  test("positionToScreen should delegate to InputProcessor"):
    val validPos = Position(2, 3)
    val systemResult = inputSystem.positionToScreen(validPos)
    val processor = InputProcessor()
    val processorResult = processor.positionToScreen(validPos)
    systemResult shouldBe processorResult

  test("positionToScreen should return Some for valid positions"):
    val validPos1 = Position(0, 0)
    val result1 = inputSystem.positionToScreen(validPos1)
    result1 shouldBe defined
    val (screenX1, screenY1) = result1.get
    screenX1 shouldBe (GRID_OFFSET_X + CELL_SIZE / 2)
    screenY1 shouldBe (GRID_OFFSET_Y + CELL_SIZE / 2)
    val validPos2 = Position(2, 3)
    val result2 = inputSystem.positionToScreen(validPos2)
    result2 shouldBe defined
    val (screenX2, screenY2) = result2.get
    screenX2 shouldBe (GRID_OFFSET_X + 3 * CELL_SIZE + CELL_SIZE / 2)
    screenY2 shouldBe (GRID_OFFSET_Y + 2 * CELL_SIZE + CELL_SIZE / 2)

  test("isValidPosition should delegate to InputProcessor"):
    val validPos = Position(2, 3)
    val systemResult = inputSystem.isValidPosition(validPos)
    val processor = InputProcessor()
    val processorResult = processor.isValidPosition(validPos)
    systemResult shouldBe processorResult
    systemResult shouldBe true

  test("isValidPosition should work for edge positions"):
    val topLeft = Position(0, 0)
    inputSystem.isValidPosition(topLeft) shouldBe true
    val bottomRight = Position(GRID_HEIGHT - 1, GRID_WIDTH - 1)
    inputSystem.isValidPosition(bottomRight) shouldBe true

  test("round trip: handleMouseClick -> positionToScreen should be consistent"):
    val testClicks = List(
      (GRID_OFFSET_X + 50, GRID_OFFSET_Y + 50),
      (GRID_OFFSET_X + 250, GRID_OFFSET_Y + 150),
      (GRID_OFFSET_X + 450, GRID_OFFSET_Y + 350)
    )

    testClicks.foreach: (x, y) =>
      val clickResult = inputSystem.handleMouseClick(x, y)
      if clickResult.isValid then
        val backToScreen = inputSystem.positionToScreen(clickResult.position)
        backToScreen shouldBe defined
        val (screenX, screenY) = backToScreen.get
        screenX should be >= GRID_OFFSET_X
        screenX should be < (GRID_OFFSET_X + GRID_WIDTH * CELL_SIZE)
        screenY should be >= GRID_OFFSET_Y
        screenY should be < (GRID_OFFSET_Y + GRID_HEIGHT * CELL_SIZE)

  test("handleMouseClick should handle boundary pixels correctly"):
    val result1 = inputSystem.handleMouseClick(GRID_OFFSET_X, GRID_OFFSET_Y)
    result1.isValid shouldBe true
    result1.position shouldBe Position(0, 0)
    val result2 = inputSystem.handleMouseClick(GRID_OFFSET_X - 1, GRID_OFFSET_Y - 1)
    result2.isValid shouldBe false
    result2.position shouldBe Position(-1, -1, allowInvalid = true)
    result2.error shouldBe defined

  test("InputSystem should be stateless"):
    val x = GRID_OFFSET_X + 100
    val y = GRID_OFFSET_Y + 100
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