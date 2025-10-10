package it.unibo.pps.wvt.input

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import it.unibo.pps.wvt.utilities.ViewConstants

class InputProcessorTest extends AnyFlatSpec with Matchers:
  val processor = InputProcessor()

  "processClick" should "return valid ClickResult for valid grid coordinates" in:
    val validX = ViewConstants.GRID_OFFSET_X + 10
    val validY = ViewConstants.GRID_OFFSET_Y + 10
    val click = MouseClick(validX, validY)
    val result = processor.processClick(click)
    result.isValid shouldBe true
    result.error shouldBe None
    result.pos.isInCell shouldBe true

  it should "return invalid ClickResult for coordinates outside grid" in:
    val invalidClick = MouseClick(0, 0)
    val result = processor.processClick(invalidClick)
    result.isValid shouldBe false
    result.error shouldBe Some("Invalid Position")
    result.pos.x shouldBe -1
    result.pos.y shouldBe -1

  it should "return invalid ClickResult for coordinates beyond grid bounds" in:
    val invalidClick = MouseClick(100, 100)
    val result = processor.processClick(invalidClick)
    result.isValid shouldBe false
    result.error shouldBe Some("Invalid Position")
    result.pos.x shouldBe -1
    result.pos.y shouldBe -1

  it should "return valid ClickResult but not in cell for coordinates just at grid edge" in:
    val edgeX = ViewConstants.GRID_OFFSET_X + ViewConstants.GRID_COLS * ViewConstants.CELL_WIDTH
    val edgeY = ViewConstants.GRID_OFFSET_Y + ViewConstants.GRID_ROWS * ViewConstants.CELL_HEIGHT
    val edgeClick = MouseClick(edgeX, edgeY)
    val result = processor.processClick(edgeClick)
    result.isValid shouldBe true
    result.pos.isValid shouldBe true
    result.pos.isInCell shouldBe false

  it should "correctly accept coordinates at grid start" in:
    val topLeftX = ViewConstants.GRID_OFFSET_X
    val topLeftY = ViewConstants.GRID_OFFSET_Y
    val click = MouseClick(topLeftX, topLeftY)
    val result = processor.processClick(click)
    result.isValid shouldBe true
    result.pos.isInCell shouldBe true

  it should "correctly accept center of first cell" in:
    val centerX = ViewConstants.GRID_OFFSET_X + ViewConstants.CELL_WIDTH / 2
    val centerY = ViewConstants.GRID_OFFSET_Y + ViewConstants.CELL_HEIGHT / 2
    val click = MouseClick(centerX, centerY)
    val result = processor.processClick(click)
    result.isValid shouldBe true
    result.pos.isInCell shouldBe true

  it should "return invalid ClickResult for coordinates beyond grid edge" in:
    val beyondX = ViewConstants.GRID_OFFSET_X + ViewConstants.GRID_COLS * ViewConstants.CELL_WIDTH + 1
    val beyondY = ViewConstants.GRID_OFFSET_Y + ViewConstants.GRID_ROWS * ViewConstants.CELL_HEIGHT + 1
    val beyondClick = MouseClick(beyondX, beyondY)
    val result = processor.processClick(beyondClick)
    result.isValid shouldBe false
    result.error shouldBe Some("Invalid Position")

  it should "correctly accept last valid cell coordinates" in:
    val lastCellX = ViewConstants.GRID_OFFSET_X + (ViewConstants.GRID_COLS - 1) * ViewConstants.CELL_WIDTH + 10
    val lastCellY = ViewConstants.GRID_OFFSET_Y + (ViewConstants.GRID_ROWS - 1) * ViewConstants.CELL_HEIGHT + 10
    val click = MouseClick(lastCellX, lastCellY)
    val result = processor.processClick(click)
    result.isValid shouldBe true
    result.pos.isInCell shouldBe true

  "isInGridArea" should "return true for coordinates inside grid area" in:
    val insideX = ViewConstants.GRID_OFFSET_X + 10
    val insideY = ViewConstants.GRID_OFFSET_Y + 10
    processor.isInGridArea(insideX, insideY) shouldBe true

  it should "return false for coordinates outside grid area" in:
    val outsideCoordinates = Seq(
      (0.0, 0.0),
      (ViewConstants.GRID_OFFSET_X - 1, ViewConstants.GRID_OFFSET_Y),
      (ViewConstants.GRID_OFFSET_X, ViewConstants.GRID_OFFSET_Y - 1),
      (ViewConstants.GRID_OFFSET_X + ViewConstants.GRID_COLS * ViewConstants.CELL_WIDTH, ViewConstants.GRID_OFFSET_Y),
      (ViewConstants.GRID_OFFSET_X, ViewConstants.GRID_OFFSET_Y + ViewConstants.GRID_ROWS * ViewConstants.CELL_HEIGHT)
    )
    outsideCoordinates.foreach { case (x, y) =>
      processor.isInGridArea(x, y) shouldBe false
    }

  it should "return true for coordinates at grid start boundaries" in:
    val boundaryX = ViewConstants.GRID_OFFSET_X
    val boundaryY = ViewConstants.GRID_OFFSET_Y
    processor.isInGridArea(boundaryX, boundaryY) shouldBe true

  it should "return false for coordinates at or beyond far boundaries" in:
    val farBoundaryX = ViewConstants.GRID_OFFSET_X + ViewConstants.GRID_COLS * ViewConstants.CELL_WIDTH
    val farBoundaryY = ViewConstants.GRID_OFFSET_Y + ViewConstants.GRID_ROWS * ViewConstants.CELL_HEIGHT
    processor.isInGridArea(farBoundaryX, farBoundaryY) shouldBe false

  it should "return true for all corners inside grid" in:
    processor.isInGridArea(ViewConstants.GRID_OFFSET_X, ViewConstants.GRID_OFFSET_Y) shouldBe true
    val topRightX = ViewConstants.GRID_OFFSET_X + ViewConstants.GRID_COLS * ViewConstants.CELL_WIDTH - 1
    processor.isInGridArea(topRightX, ViewConstants.GRID_OFFSET_Y) shouldBe true
    val bottomLeftY = ViewConstants.GRID_OFFSET_Y + ViewConstants.GRID_ROWS * ViewConstants.CELL_HEIGHT - 1
    processor.isInGridArea(ViewConstants.GRID_OFFSET_X, bottomLeftY) shouldBe true
    processor.isInGridArea(topRightX, bottomLeftY) shouldBe true