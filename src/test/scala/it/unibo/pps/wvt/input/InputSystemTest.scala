package it.unibo.pps.wvt.input

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import it.unibo.pps.wvt.utilities.{Position, ViewConstants}

class InputSystemTest extends AnyFlatSpec with Matchers {

  val inputSystem = InputSystem()

  "handleMouseClick" should "return valid ClickResult for valid screen coordinates" in {
    val validX = (ViewConstants.GRID_OFFSET_X + 10).toInt
    val validY = (ViewConstants.GRID_OFFSET_Y + 10).toInt

    val result = inputSystem.handleMouseClick(validX, validY)

    result.isValid shouldBe true
    result.error shouldBe None
    result.position.isValid shouldBe true
  }

  it should "return invalid ClickResult for invalid screen coordinates" in {
    val result = inputSystem.handleMouseClick(0, 0)

    result.isValid shouldBe false
    result.error shouldBe Some("Posizione invalida")
    result.position.row shouldBe -1
    result.position.col shouldBe -1
  }

  it should "correctly process multiple valid clicks" in {
    val clicks = Seq(
      (ViewConstants.GRID_OFFSET_X.toInt, ViewConstants.GRID_OFFSET_Y.toInt),
      ((ViewConstants.GRID_OFFSET_X + ViewConstants.CELL_WIDTH).toInt, ViewConstants.GRID_OFFSET_Y.toInt),
      (ViewConstants.GRID_OFFSET_X.toInt, (ViewConstants.GRID_OFFSET_Y + ViewConstants.CELL_HEIGHT).toInt)
    )

    clicks.foreach { case (x, y) =>
      val result = inputSystem.handleMouseClick(x, y)
      result.isValid shouldBe true
    }
  }

  it should "handle edge case coordinates correctly" in {
    // Test coordinate al limite della griglia
    val edgeX = (ViewConstants.GRID_OFFSET_X + ViewConstants.GRID_COLS * ViewConstants.CELL_WIDTH - 1).toInt
    val edgeY = (ViewConstants.GRID_OFFSET_Y + ViewConstants.GRID_ROWS * ViewConstants.CELL_HEIGHT - 1).toInt

    val result = inputSystem.handleMouseClick(edgeX, edgeY)

    result.isValid shouldBe true
    result.position.row shouldBe ViewConstants.GRID_ROWS - 1
    result.position.col shouldBe ViewConstants.GRID_COLS - 1
  }

  "positionToScreen" should "delegate correctly to processor" in {
    val position = Position(1, 1)

    val systemResult = inputSystem.positionToScreen(position)
    val processorResult = InputProcessor().positionToScreen(position)

    systemResult shouldBe processorResult
    systemResult shouldBe defined
  }

  it should "return None for invalid positions" in {
    val invalidPosition = Position(-1, -1, allowInvalid = true)

    val result = inputSystem.positionToScreen(invalidPosition)

    result shouldBe None
  }

  "isValidPosition" should "delegate correctly to processor" in {
    val validPosition = Position(2, 3)
    val invalidPosition = Position(-1, -1, allowInvalid = true)

    inputSystem.isValidPosition(validPosition) shouldBe true
    inputSystem.isValidPosition(invalidPosition) shouldBe false
  }

  it should "handle boundary positions correctly" in {
    val boundaryPositions = Seq(
      Position(0, 0),
      Position(ViewConstants.GRID_ROWS - 1, ViewConstants.GRID_COLS - 1),
      Position(0, ViewConstants.GRID_COLS - 1),
      Position(ViewConstants.GRID_ROWS - 1, 0)
    )

    boundaryPositions.foreach { pos =>
      inputSystem.isValidPosition(pos) shouldBe true
    }
  }

  "integration test" should "correctly convert screen coordinates to position and back" in {
    val screenX = (ViewConstants.GRID_OFFSET_X + ViewConstants.CELL_WIDTH * 2.5).toInt
    val screenY = (ViewConstants.GRID_OFFSET_Y + ViewConstants.CELL_HEIGHT * 1.5).toInt

    // Da schermo a posizione
    val clickResult = inputSystem.handleMouseClick(screenX, screenY)
    clickResult.isValid shouldBe true

    // Da posizione a schermo
    val screenCoords = inputSystem.positionToScreen(clickResult.position)
    screenCoords shouldBe defined

    val (convertedX, convertedY) = screenCoords.get
    // Le coordinate convertite dovrebbero corrispondere all'angolo della cella
    convertedX shouldBe ViewConstants.GRID_OFFSET_X + 2 * ViewConstants.CELL_WIDTH
    convertedY shouldBe ViewConstants.GRID_OFFSET_Y + 1 * ViewConstants.CELL_HEIGHT
  }

  it should "handle the complete workflow for all grid cells" in {
    for {
      row <- 0 until ViewConstants.GRID_ROWS
      col <- 0 until ViewConstants.GRID_COLS
    } {
      val position = Position(row, col)

      // Verifica che la posizione sia valida
      inputSystem.isValidPosition(position) shouldBe true

      // Converti in coordinate schermo
      val screenCoords = inputSystem.positionToScreen(position)
      screenCoords shouldBe defined

      val (x, y) = screenCoords.get

      // Converti di nuovo in posizione tramite click
      val clickResult = inputSystem.handleMouseClick(x.toInt, y.toInt)
      clickResult.isValid shouldBe true
      clickResult.position.row shouldBe position.row
      clickResult.position.col shouldBe position.col
    }
  }
}