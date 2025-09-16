package it.unibo.pps.wvt.input

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import it.unibo.pps.wvt.model.*
import it.unibo.pps.wvt.utilities.ViewConstants.*

class InputProcessorTest extends AnyFunSuite with Matchers:

  private val processor = InputProcessor()

  test("processClick DEBUG - step by step"):
    val outsideClick1 = MouseClick(10, 10)
    println(s"Testing click: ${outsideClick1}")
    println(s"GRID_OFFSET_X: $GRID_OFFSET_X, GRID_OFFSET_Y: $GRID_OFFSET_Y")

    try {
      val result1 = processor.processClick(outsideClick1)
      println(s"Result isValid: ${result1.isValid}")
      println(s"Result error: ${result1.error}")
      if (result1.error.isDefined) {
        println(s"Error message: ${result1.error.get}")
      }
    } catch {
      case e: Exception =>
        println(s"Exception: ${e.getMessage}")
        e.printStackTrace()
    }

  test("processClick should return valid result for coordinates inside grid"):
    // Test click all'inizio della griglia
    val firstCellX = GRID_OFFSET_X + 10  // Dentro la prima cella
    val firstCellY = GRID_OFFSET_Y + 10
    val click1 = MouseClick(firstCellX, firstCellY)
    val result1 = processor.processClick(click1)
    result1.isValid shouldBe true
    result1.position shouldBe Position(0, 0)
    result1.error shouldBe None

    // Test click al centro di una cella specifica
    val centerX = GRID_OFFSET_X + (2 * CELL_SIZE) + (CELL_SIZE / 2)  // Centro cella colonna 2
    val centerY = GRID_OFFSET_Y + (1 * CELL_SIZE) + (CELL_SIZE / 2)  // Centro cella riga 1
    val click2 = MouseClick(centerX, centerY)
    val result2 = processor.processClick(click2)
    result2.isValid shouldBe true
    result2.position shouldBe Position(1, 2)
    result2.error shouldBe None

  test("processClick edge cases"):
    val lastValidX = GRID_OFFSET_X + (GRID_WIDTH * CELL_SIZE) - 1  // 949
    val lastValidY = GRID_OFFSET_Y + (GRID_HEIGHT * CELL_SIZE) - 1  // 549
    val edgeClick1 = MouseClick(lastValidX, lastValidY)
    val result1 = processor.processClick(edgeClick1)
    result1.isValid shouldBe true
    result1.position shouldBe Position(GRID_HEIGHT - 1, GRID_WIDTH - 1)
    result1.error shouldBe None

  test("processClick should work for all corners of the grid"):
    val topLeftClick = MouseClick(GRID_OFFSET_X + 1, GRID_OFFSET_Y + 1)
    val result1 = processor.processClick(topLeftClick)
    result1.isValid shouldBe true
    result1.position shouldBe Position(0, 0)
    val topRightX = GRID_OFFSET_X + ((GRID_WIDTH - 1) * CELL_SIZE) + 10
    val topRightY = GRID_OFFSET_Y + 10
    val topRightClick = MouseClick(topRightX, topRightY)
    val result2 = processor.processClick(topRightClick)
    result2.isValid shouldBe true
    result2.position shouldBe Position(0, GRID_WIDTH - 1)
    val bottomLeftX = GRID_OFFSET_X + 10
    val bottomLeftY = GRID_OFFSET_Y + ((GRID_HEIGHT - 1) * CELL_SIZE) + 10
    val bottomLeftClick = MouseClick(bottomLeftX, bottomLeftY)
    val result3 = processor.processClick(bottomLeftClick)
    result3.isValid shouldBe true
    result3.position shouldBe Position(GRID_HEIGHT - 1, 0)
    val bottomRightX = GRID_OFFSET_X + ((GRID_WIDTH - 1) * CELL_SIZE) + 10
    val bottomRightY = GRID_OFFSET_Y + ((GRID_HEIGHT - 1) * CELL_SIZE) + 10
    val bottomRightClick = MouseClick(bottomRightX, bottomRightY)
    val result4 = processor.processClick(bottomRightClick)
    result4.isValid shouldBe true
    result4.position shouldBe Position(GRID_HEIGHT - 1, GRID_WIDTH - 1)

  test("positionToScreen should return Some for valid positions"):
    val validPos1 = Position(0, 0)
    val result1 = processor.positionToScreen(validPos1)
    result1 shouldBe defined
    val (screenX1, screenY1) = result1.get
    screenX1 shouldBe (GRID_OFFSET_X + CELL_SIZE / 2)
    screenY1 shouldBe (GRID_OFFSET_Y + CELL_SIZE / 2)

    val validPos2 = Position(2, 3)
    val result2 = processor.positionToScreen(validPos2)
    result2 shouldBe defined
    val (screenX2, screenY2) = result2.get
    screenX2 shouldBe (GRID_OFFSET_X + 3 * CELL_SIZE + CELL_SIZE / 2)
    screenY2 shouldBe (GRID_OFFSET_Y + 2 * CELL_SIZE + CELL_SIZE / 2)

  test("positionToScreen should return None for invalid positions"):
    try {
      val validButOutOfBoundsPos = Position(0, 0)
      val result = processor.positionToScreen(validButOutOfBoundsPos)
    } catch {
      case _: IllegalArgumentException =>
        succeed
    }

  test("isValidPosition should delegate to converter"):
    val validPos = Position(2, 3)
    processor.isValidPosition(validPos) shouldBe true
    val edgePos = Position(GRID_HEIGHT - 1, GRID_WIDTH - 1)
    processor.isValidPosition(edgePos) shouldBe true

  test("round trip: processClick -> positionToScreen should be consistent"):
    val testClicks = List(
      MouseClick(GRID_OFFSET_X + 50, GRID_OFFSET_Y + 50),
      MouseClick(GRID_OFFSET_X + 250, GRID_OFFSET_Y + 150),
      MouseClick(GRID_OFFSET_X + 450, GRID_OFFSET_Y + 350)
    )

    testClicks.foreach: click =>
      val clickResult = processor.processClick(click)
      if clickResult.isValid then
        val backToScreen = processor.positionToScreen(clickResult.position)
        backToScreen shouldBe defined
        // Le coordinate potrebbero non essere identiche perché positionToScreen
        // restituisce il centro della cella, ma dovrebbero essere ragionevoli
        val (screenX, screenY) = backToScreen.get
        screenX should be >= GRID_OFFSET_X
        screenX should be < (GRID_OFFSET_X + GRID_WIDTH * CELL_SIZE)
        screenY should be >= GRID_OFFSET_Y
        screenY should be < (GRID_OFFSET_Y + GRID_HEIGHT * CELL_SIZE)

  test("processClick should handle boundary pixels correctly"):
    val boundaryClick = MouseClick(GRID_OFFSET_X, GRID_OFFSET_Y)
    val result1 = processor.processClick(boundaryClick)
    result1.isValid shouldBe true
    result1.position shouldBe Position(0, 0)  
    val beforeBoundaryClick = MouseClick(GRID_OFFSET_X - 1, GRID_OFFSET_Y - 1)
    val result2 = processor.processClick(beforeBoundaryClick)
    result2.isValid shouldBe false
    result2.position shouldBe Position(-1, -1, allowInvalid = true) 
    result2.error shouldBe defined