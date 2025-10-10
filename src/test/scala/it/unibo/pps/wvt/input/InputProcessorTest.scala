package it.unibo.pps.wvt.input

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import it.unibo.pps.wvt.utilities.ViewConstants

class InputProcessorTest extends AnyFlatSpec with Matchers:
  val processor = InputProcessor()

  "processClick" should "return valid ClickResult for valid grid coordinates" in:
    aClick
      .atOffset(10, 10)
      .whenProcessed
      .shouldBeValid
      .withNoError
      .andShouldBeInCell

  it should "return invalid ClickResult for coordinates outside grid" in:
    aClick
      .at(0, 0)
      .whenProcessed
      .shouldBeInvalid
      .withError("Invalid Position")
      .andPositionShouldBe(-1, -1)

  it should "return invalid ClickResult for coordinates beyond grid bounds" in:
    aClick
      .at(100, 100)
      .whenProcessed
      .shouldBeInvalid
      .withError("Invalid Position")
      .andPositionShouldBe(-1, -1)

  it should "return valid ClickResult but not in cell for coordinates just at grid edge" in:
    aClick
      .atGridEdge
      .whenProcessed
      .shouldBeValid
      .positionShouldBeValid
      .butNotInCell

  it should "correctly accept coordinates at grid start" in:
    aClick
      .atGridStart
      .whenProcessed
      .shouldBeValid
      .andShouldBeInCell

  it should "correctly accept center of first cell" in:
    aClick
      .atFirstCellCenter
      .whenProcessed
      .shouldBeValid
      .andShouldBeInCell

  it should "return invalid ClickResult for coordinates beyond grid edge" in:
    aClick
      .beyondGridEdge
      .whenProcessed
      .shouldBeInvalid
      .withError("Invalid Position")

  it should "correctly accept last valid cell coordinates" in:
    aClick
      .atLastCellWithOffset(10, 10)
      .whenProcessed
      .shouldBeValid
      .andShouldBeInCell

  "isInGridArea" should "return true for coordinates inside grid area" in:
    theProcessor
      .checkingArea
      .atOffset(10, 10)
      .shouldBeInsideGrid

  it should "return false for coordinates outside grid area" in:
    theProcessor
      .checkingArea
      .at(0, 0).shouldBeOutsideGrid
      .at(ViewConstants.GRID_OFFSET_X - 1, ViewConstants.GRID_OFFSET_Y).shouldBeOutsideGrid
      .at(ViewConstants.GRID_OFFSET_X, ViewConstants.GRID_OFFSET_Y - 1).shouldBeOutsideGrid
      .at(ViewConstants.GRID_OFFSET_X + ViewConstants.GRID_COLS * ViewConstants.CELL_WIDTH, ViewConstants.GRID_OFFSET_Y).shouldBeOutsideGrid
      .at(ViewConstants.GRID_OFFSET_X, ViewConstants.GRID_OFFSET_Y + ViewConstants.GRID_ROWS * ViewConstants.CELL_HEIGHT).shouldBeOutsideGrid

  it should "return true for coordinates at grid start boundaries" in:
    theProcessor
      .checkingArea
      .atGridStart
      .shouldBeInsideGrid

  it should "return false for coordinates at or beyond far boundaries" in:
    theProcessor
      .checkingArea
      .atGridEdge
      .shouldBeOutsideGrid

  it should "return true for all corners inside grid" in:
    theProcessor
      .checkingArea
      .atTopLeft.shouldBeInsideGrid
      .atTopRight.shouldBeInsideGrid
      .atBottomLeft.shouldBeInsideGrid
      .atBottomRight.shouldBeInsideGrid

  // DSL Implementation
  private def aClick: ClickBuilder =
    ClickBuilder(processor)

  private def theProcessor: ProcessorDSL =
    ProcessorDSL(processor)

  case class ClickBuilder(processor: InputProcessor, x: Double = 0, y: Double = 0):

    def at(x: Double, y: Double): ClickBuilder =
      copy(x = x, y = y)

    def atOffset(offsetX: Double, offsetY: Double): ClickBuilder =
      copy(
        x = ViewConstants.GRID_OFFSET_X + offsetX,
        y = ViewConstants.GRID_OFFSET_Y + offsetY
      )

    def atGridStart: ClickBuilder =
      copy(
        x = ViewConstants.GRID_OFFSET_X,
        y = ViewConstants.GRID_OFFSET_Y
      )

    def atGridEdge: ClickBuilder =
      copy(
        x = ViewConstants.GRID_OFFSET_X + ViewConstants.GRID_COLS * ViewConstants.CELL_WIDTH,
        y = ViewConstants.GRID_OFFSET_Y + ViewConstants.GRID_ROWS * ViewConstants.CELL_HEIGHT
      )

    def beyondGridEdge: ClickBuilder =
      copy(
        x = ViewConstants.GRID_OFFSET_X + ViewConstants.GRID_COLS * ViewConstants.CELL_WIDTH + 1,
        y = ViewConstants.GRID_OFFSET_Y + ViewConstants.GRID_ROWS * ViewConstants.CELL_HEIGHT + 1
      )

    def atFirstCellCenter: ClickBuilder =
      copy(
        x = ViewConstants.GRID_OFFSET_X + ViewConstants.CELL_WIDTH / 2,
        y = ViewConstants.GRID_OFFSET_Y + ViewConstants.CELL_HEIGHT / 2
      )

    def atLastCellWithOffset(offsetX: Double, offsetY: Double): ClickBuilder =
      copy(
        x = ViewConstants.GRID_OFFSET_X + (ViewConstants.GRID_COLS - 1) * ViewConstants.CELL_WIDTH + offsetX,
        y = ViewConstants.GRID_OFFSET_Y + (ViewConstants.GRID_ROWS - 1) * ViewConstants.CELL_HEIGHT + offsetY
      )

    def whenProcessed: ClickResultAssertions =
      val click = MouseClick(x, y)
      val result = processor.processClick(click)
      ClickResultAssertions(result)

  case class ClickResultAssertions(result: ClickResult):

    def shouldBeValid: ClickResultAssertions =
      result.isValid shouldBe true
      this

    def shouldBeInvalid: ClickResultAssertions =
      result.isValid shouldBe false
      this

    def withNoError: ClickResultAssertions =
      result.error shouldBe None
      this

    def withError(expected: String): ClickResultAssertions =
      result.error shouldBe Some(expected)
      this

    def andShouldBeInCell: ClickResultAssertions =
      result.pos.isInCell shouldBe true
      this

    def butNotInCell: ClickResultAssertions =
      result.pos.isInCell shouldBe false
      this

    def positionShouldBeValid: ClickResultAssertions =
      result.pos.isValid shouldBe true
      this

    def andPositionShouldBe(x: Double, y: Double): ClickResultAssertions =
      result.pos.x shouldBe x
      result.pos.y shouldBe y
      this

  case class ProcessorDSL(processor: InputProcessor):

    def checkingArea: GridAreaChecker =
      GridAreaChecker(processor)

  case class GridAreaChecker(
                              processor: InputProcessor,
                              x: Double = 0,
                              y: Double = 0
                            ):

    def at(x: Double, y: Double): GridAreaChecker =
      copy(x = x, y = y)

    def atOffset(offsetX: Double, offsetY: Double): GridAreaChecker =
      copy(
        x = ViewConstants.GRID_OFFSET_X + offsetX,
        y = ViewConstants.GRID_OFFSET_Y + offsetY
      )

    def atGridStart: GridAreaChecker =
      copy(
        x = ViewConstants.GRID_OFFSET_X,
        y = ViewConstants.GRID_OFFSET_Y
      )

    def atGridEdge: GridAreaChecker =
      copy(
        x = ViewConstants.GRID_OFFSET_X + ViewConstants.GRID_COLS * ViewConstants.CELL_WIDTH,
        y = ViewConstants.GRID_OFFSET_Y + ViewConstants.GRID_ROWS * ViewConstants.CELL_HEIGHT
      )

    def atTopLeft: GridAreaChecker =
      copy(
        x = ViewConstants.GRID_OFFSET_X,
        y = ViewConstants.GRID_OFFSET_Y
      )

    def atTopRight: GridAreaChecker =
      copy(
        x = ViewConstants.GRID_OFFSET_X + ViewConstants.GRID_COLS * ViewConstants.CELL_WIDTH - 1,
        y = ViewConstants.GRID_OFFSET_Y
      )

    def atBottomLeft: GridAreaChecker =
      copy(
        x = ViewConstants.GRID_OFFSET_X,
        y = ViewConstants.GRID_OFFSET_Y + ViewConstants.GRID_ROWS * ViewConstants.CELL_HEIGHT - 1
      )

    def atBottomRight: GridAreaChecker =
      val rightX = ViewConstants.GRID_OFFSET_X + ViewConstants.GRID_COLS * ViewConstants.CELL_WIDTH - 1
      val bottomY = ViewConstants.GRID_OFFSET_Y + ViewConstants.GRID_ROWS * ViewConstants.CELL_HEIGHT - 1
      copy(x = rightX, y = bottomY)

    def shouldBeInsideGrid: GridAreaChecker =
      processor.isInGridArea(x, y) shouldBe true
      this

    def shouldBeOutsideGrid: GridAreaChecker =
      processor.isInGridArea(x, y) shouldBe false
      this