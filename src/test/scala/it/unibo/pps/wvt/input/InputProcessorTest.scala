package it.unibo.pps.wvt.input

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import it.unibo.pps.wvt.input.{InputProcessor, MouseClick}
import it.unibo.pps.wvt.utilities.{Position, ViewConstants}

class InputProcessorTest extends AnyFlatSpec with Matchers {

  val processor = InputProcessor()

  "processClick" should "return valid ClickResult for valid grid coordinates" in {
    // Coordinate che dovrebbero mappare alla posizione (0, 0)
    val validX = ViewConstants.GRID_OFFSET_X + 10
    val validY = ViewConstants.GRID_OFFSET_Y + 10
    val click = MouseClick(validX.toInt, validY.toInt)

    val result = processor.processClick(click)

    result.isValid shouldBe true
    result.error shouldBe None
    result.position.isValid shouldBe true
  }

  it should "return invalid ClickResult for coordinates outside grid" in {
    // Coordinate fuori dalla griglia
    val invalidClick = MouseClick(0, 0)

    val result = processor.processClick(invalidClick)

    result.isValid shouldBe false
    result.error shouldBe Some("Posizione invalida")
    result.position.row shouldBe -1
    result.position.col shouldBe -1
  }

  it should "return invalid ClickResult for coordinates beyond grid bounds" in {
    // Coordinate che non rispettano le condizioni iniziali di physicalToLogical 
    // (fuori dall'area di offset)
    val invalidClick = MouseClick(100, 100) // Coordinate sicuramente fuori dall'area della griglia

    val result = processor.processClick(invalidClick)

    result.isValid shouldBe false
    result.error shouldBe Some("Posizione invalida")
    result.position.row shouldBe -1
    result.position.col shouldBe -1
  }

  it should "return invalid ClickResult for coordinates just at grid edge" in {
    // Test coordinate esattamente al limite esterno della griglia
    val edgeX = (ViewConstants.GRID_OFFSET_X + ViewConstants.GRID_COLS * ViewConstants.CELL_WIDTH).toInt
    val edgeY = (ViewConstants.GRID_OFFSET_Y + ViewConstants.GRID_ROWS * ViewConstants.CELL_HEIGHT).toInt
    val edgeClick = MouseClick(edgeX, edgeY)

    val result = processor.processClick(edgeClick)

    result.isValid shouldBe false
    result.error shouldBe Some("Posizione invalida")
    result.position.row shouldBe -1
    result.position.col shouldBe -1
  }

  it should "correctly map corner coordinates" in {
    // Test angolo in alto a sinistra della griglia
    val topLeftX = ViewConstants.GRID_OFFSET_X.toInt
    val topLeftY = ViewConstants.GRID_OFFSET_Y.toInt
    val click = MouseClick(topLeftX, topLeftY)

    val result = processor.processClick(click)

    result.isValid shouldBe true
    result.position.row shouldBe 0
    result.position.col shouldBe 0
  }

  it should "correctly map center of first cell" in {
    val centerX = (ViewConstants.GRID_OFFSET_X + ViewConstants.CELL_WIDTH / 2).toInt
    val centerY = (ViewConstants.GRID_OFFSET_Y + ViewConstants.CELL_HEIGHT / 2).toInt
    val click = MouseClick(centerX, centerY)

    val result = processor.processClick(click)

    result.isValid shouldBe true
    result.position.row shouldBe 0
    result.position.col shouldBe 0
  }

  "positionToScreen" should "return Some coordinates for valid position" in {
    val validPosition = Position(0, 0)

    val result = processor.positionToScreen(validPosition)

    result shouldBe defined
    val (x, y) = result.get
    x shouldBe ViewConstants.GRID_OFFSET_X
    y shouldBe ViewConstants.GRID_OFFSET_Y
  }

  it should "return None for invalid position" in {
    val invalidPosition = Position(-1, -1, allowInvalid = true)

    val result = processor.positionToScreen(invalidPosition)

    result shouldBe None
  }

  it should "return correct coordinates for different valid positions" in {
    val position = Position(1, 2)

    val result = processor.positionToScreen(position)

    result shouldBe defined
    val (x, y) = result.get
    x shouldBe ViewConstants.GRID_OFFSET_X + 2 * ViewConstants.CELL_WIDTH
    y shouldBe ViewConstants.GRID_OFFSET_Y + 1 * ViewConstants.CELL_HEIGHT
  }

  "isValidPosition" should "return true for valid positions" in {
    val validPositions = Seq(
      Position(0, 0),
      Position(ViewConstants.GRID_ROWS - 1, ViewConstants.GRID_COLS - 1),
      Position(2, 4)
    )

    validPositions.foreach { pos =>
      processor.isValidPosition(pos) shouldBe true
    }
  }

  it should "return false for invalid positions" in {
    val invalidPositions = Seq(
      Position(-1, -1, allowInvalid = true),
      Position(ViewConstants.GRID_ROWS, 0, allowInvalid = true),
      Position(0, ViewConstants.GRID_COLS, allowInvalid = true)
    )

    invalidPositions.foreach { pos =>
      processor.isValidPosition(pos) shouldBe false
    }
  }

  "isInGridArea" should "return true for coordinates inside grid area" in {
    val insideX = ViewConstants.GRID_OFFSET_X.toInt + 10
    val insideY = ViewConstants.GRID_OFFSET_Y.toInt + 10

    processor.isInGridArea(insideX, insideY) shouldBe true
  }

  it should "return false for coordinates outside grid area" in {
    val outsideCoordinates = Seq(
      (0, 0),
      (ViewConstants.GRID_OFFSET_X.toInt - 1, ViewConstants.GRID_OFFSET_Y.toInt),
      (ViewConstants.GRID_OFFSET_X.toInt, ViewConstants.GRID_OFFSET_Y.toInt - 1),
      ((ViewConstants.GRID_OFFSET_X + ViewConstants.GRID_COLS * ViewConstants.CELL_WIDTH).toInt, ViewConstants.GRID_OFFSET_Y.toInt),
      (ViewConstants.GRID_OFFSET_X.toInt, (ViewConstants.GRID_OFFSET_Y + ViewConstants.GRID_ROWS * ViewConstants.CELL_HEIGHT).toInt)
    )

    outsideCoordinates.foreach { case (x, y) =>
      processor.isInGridArea(x, y) shouldBe false
    }
  }

  it should "return true for coordinates at grid boundaries" in {
    val boundaryX = ViewConstants.GRID_OFFSET_X.toInt
    val boundaryY = ViewConstants.GRID_OFFSET_Y.toInt

    processor.isInGridArea(boundaryX, boundaryY) shouldBe true
  }

  it should "return false for coordinates at or beyond far boundaries" in {
    val farBoundaryX = (ViewConstants.GRID_OFFSET_X + ViewConstants.GRID_COLS * ViewConstants.CELL_WIDTH).toInt
    val farBoundaryY = (ViewConstants.GRID_OFFSET_Y + ViewConstants.GRID_ROWS * ViewConstants.CELL_HEIGHT).toInt

    processor.isInGridArea(farBoundaryX, farBoundaryY) shouldBe false
  }
}