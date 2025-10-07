package it.unibo.pps.wvt.input

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import it.unibo.pps.wvt.utilities.ViewConstants

class InputSystemTest extends AnyFlatSpec with Matchers:

  val inputSystem = InputSystem()

  "handleMouseClick" should "return valid ClickResult for valid screen coordinates" in:
    val validX = ViewConstants.GRID_OFFSET_X + 10
    val validY = ViewConstants.GRID_OFFSET_Y + 10

    val result = inputSystem.handleMouseClick(validX, validY)

    result.isValid shouldBe true
    result.error shouldBe None
    result.pos.isInCell shouldBe true

  it should "return invalid ClickResult for invalid screen coordinates" in:
    val result = inputSystem.handleMouseClick(0, 0)

    result.isValid shouldBe false
    result.error shouldBe Some("Invalid Position")
    result.pos.x shouldBe -1
    result.pos.y shouldBe -1

  it should "correctly process multiple valid clicks" in:
    val clicks = Seq(
      (ViewConstants.GRID_OFFSET_X, ViewConstants.GRID_OFFSET_Y),
      (ViewConstants.GRID_OFFSET_X + ViewConstants.CELL_WIDTH, ViewConstants.GRID_OFFSET_Y),
      (ViewConstants.GRID_OFFSET_X, ViewConstants.GRID_OFFSET_Y + ViewConstants.CELL_HEIGHT)
    )

    clicks.foreach: (x, y) =>
      val result = inputSystem.handleMouseClick(x, y)
      result.isValid shouldBe true

  it should "handle edge case coordinates correctly" in:
    val edgeX = ViewConstants.GRID_OFFSET_X + ViewConstants.GRID_COLS * ViewConstants.CELL_WIDTH - 1
    val edgeY = ViewConstants.GRID_OFFSET_Y + ViewConstants.GRID_ROWS * ViewConstants.CELL_HEIGHT - 1

    val result = inputSystem.handleMouseClick(edgeX, edgeY)

    result.isValid shouldBe true
    result.pos.isInCell shouldBe true

  it should "return valid but not in cell for coordinates at exact grid boundary" in:
    val boundaryX = ViewConstants.GRID_OFFSET_X + ViewConstants.GRID_COLS * ViewConstants.CELL_WIDTH
    val boundaryY = ViewConstants.GRID_OFFSET_Y + ViewConstants.GRID_ROWS * ViewConstants.CELL_HEIGHT

    val result = inputSystem.handleMouseClick(boundaryX, boundaryY)

    result.isValid shouldBe true
    result.pos.isValid shouldBe true
    result.pos.isInCell shouldBe false

  it should "return invalid for coordinates beyond grid boundary" in:
    val beyondX = ViewConstants.GRID_OFFSET_X + ViewConstants.GRID_COLS * ViewConstants.CELL_WIDTH + 1
    val beyondY = ViewConstants.GRID_OFFSET_Y + ViewConstants.GRID_ROWS * ViewConstants.CELL_HEIGHT + 1

    val result = inputSystem.handleMouseClick(beyondX, beyondY)

    result.isValid shouldBe false
    result.error shouldBe Some("Invalid Position")

  "isInGridArea" should "return true for coordinates inside grid area" in:
    val insideX = ViewConstants.GRID_OFFSET_X + 10
    val insideY = ViewConstants.GRID_OFFSET_Y + 10

    inputSystem.isInGridArea(insideX, insideY) shouldBe true

  it should "return false for coordinates outside grid area" in:
    val outsideCoordinates = Seq(
      (0.0, 0.0),
      (ViewConstants.GRID_OFFSET_X - 1, ViewConstants.GRID_OFFSET_Y),
      (ViewConstants.GRID_OFFSET_X, ViewConstants.GRID_OFFSET_Y - 1)
    )

    outsideCoordinates.foreach: (x, y) =>
      inputSystem.isInGridArea(x, y) shouldBe false

  it should "return true for coordinates at grid start boundaries" in:
    val boundaryX = ViewConstants.GRID_OFFSET_X
    val boundaryY = ViewConstants.GRID_OFFSET_Y

    inputSystem.isInGridArea(boundaryX, boundaryY) shouldBe true

  it should "return false for coordinates at or beyond far boundaries" in:
    val farBoundaryX = ViewConstants.GRID_OFFSET_X + ViewConstants.GRID_COLS * ViewConstants.CELL_WIDTH
    val farBoundaryY = ViewConstants.GRID_OFFSET_Y + ViewConstants.GRID_ROWS * ViewConstants.CELL_HEIGHT

    inputSystem.isInGridArea(farBoundaryX, farBoundaryY) shouldBe false

  it should "return true for all valid grid cells" in:
    for
      row <- 0 until ViewConstants.GRID_ROWS
      col <- 0 until ViewConstants.GRID_COLS
    do
      val x = ViewConstants.GRID_OFFSET_X + col * ViewConstants.CELL_WIDTH + ViewConstants.CELL_WIDTH / 2
      val y = ViewConstants.GRID_OFFSET_Y + row * ViewConstants.CELL_HEIGHT + ViewConstants.CELL_HEIGHT / 2
      inputSystem.isInGridArea(x, y) shouldBe true