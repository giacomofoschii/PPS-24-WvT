package it.unibo.pps.wvt.input

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import it.unibo.pps.wvt.utilities.ViewConstants

class InputSystemTest extends AnyFlatSpec with Matchers:
  val inputSystem = InputSystem()

  "handleMouseClick" should "return valid ClickResult for valid screen coordinates" in:
    anInputSystem
      .handlingClick
      .atOffset(10, 10)
      .shouldBeValid
      .withNoError
      .andShouldBeInCell

  it should "return invalid ClickResult for invalid screen coordinates" in:
    anInputSystem
      .handlingClick
      .at(0, 0)
      .shouldBeInvalid
      .withError("Invalid Position")
      .andPositionShouldBe(-1, -1)

  it should "correctly process multiple valid clicks" in:
    anInputSystem
      .handlingMultipleClicks
      .atGridStart
      .atGridStartPlusCell(1, 0)
      .atGridStartPlusCell(0, 1)
      .allShouldBeValid

  it should "handle edge case coordinates correctly" in:
    anInputSystem
      .handlingClick
      .atGridEdgeMinus(1, 1)
      .shouldBeValid
      .andShouldBeInCell

  it should "return valid but not in cell for coordinates at exact grid boundary" in:
    anInputSystem
      .handlingClick
      .atGridBoundary
      .shouldBeValid
      .positionShouldBeValid
      .butNotInCell

  it should "return invalid for coordinates beyond grid boundary" in:
    anInputSystem
      .handlingClick
      .beyondGridBoundary
      .shouldBeInvalid
      .withError("Invalid Position")

  "isInGridArea" should "return true for coordinates inside grid area" in:
    anInputSystem
      .checkingGridArea
      .atOffset(10, 10)
      .shouldBeInside

  it should "return false for coordinates outside grid area" in:
    anInputSystem
      .checkingGridArea
      .at(0, 0).shouldBeOutside
      .at(ViewConstants.GRID_OFFSET_X - 1, ViewConstants.GRID_OFFSET_Y).shouldBeOutside
      .at(ViewConstants.GRID_OFFSET_X, ViewConstants.GRID_OFFSET_Y - 1).shouldBeOutside

  it should "return true for coordinates at grid start boundaries" in:
    anInputSystem
      .checkingGridArea
      .atGridStart
      .shouldBeInside

  it should "return false for coordinates at or beyond far boundaries" in:
    anInputSystem
      .checkingGridArea
      .atGridBoundary
      .shouldBeOutside

  it should "return true for all valid grid cells" in:
    anInputSystem
      .checkingGridArea
      .forAllCellCenters
      .allShouldBeInside

  // DSL Implementation
  private def anInputSystem: InputSystemDSL =
    InputSystemDSL(inputSystem)

  case class InputSystemDSL(system: InputSystem):

    def handlingClick: ClickHandler =
      ClickHandler(system)

    def handlingMultipleClicks: MultiClickHandler =
      MultiClickHandler(system, List.empty)

    def checkingGridArea: GridAreaChecker =
      GridAreaChecker(system)

  case class ClickHandler(
      system: InputSystem,
      x: Double = 0,
      y: Double = 0,
      result: Option[ClickResult] = None
  ):

    def at(x: Double, y: Double): ClickHandler =
      val res = system.handleMouseClick(x, y)
      copy(x = x, y = y, result = Some(res))

    def atOffset(offsetX: Double, offsetY: Double): ClickHandler =
      at(
        ViewConstants.GRID_OFFSET_X + offsetX,
        ViewConstants.GRID_OFFSET_Y + offsetY
      )

    def atGridStart: ClickHandler =
      at(ViewConstants.GRID_OFFSET_X, ViewConstants.GRID_OFFSET_Y)

    def atGridBoundary: ClickHandler =
      at(
        ViewConstants.GRID_OFFSET_X + ViewConstants.GRID_COLS * ViewConstants.CELL_WIDTH,
        ViewConstants.GRID_OFFSET_Y + ViewConstants.GRID_ROWS * ViewConstants.CELL_HEIGHT
      )

    def beyondGridBoundary: ClickHandler =
      at(
        ViewConstants.GRID_OFFSET_X + ViewConstants.GRID_COLS * ViewConstants.CELL_WIDTH + 1,
        ViewConstants.GRID_OFFSET_Y + ViewConstants.GRID_ROWS * ViewConstants.CELL_HEIGHT + 1
      )

    def atGridEdgeMinus(minusX: Double, minusY: Double): ClickHandler =
      at(
        ViewConstants.GRID_OFFSET_X + ViewConstants.GRID_COLS * ViewConstants.CELL_WIDTH - minusX,
        ViewConstants.GRID_OFFSET_Y + ViewConstants.GRID_ROWS * ViewConstants.CELL_HEIGHT - minusY
      )

    // Assertions
    def shouldBeValid: ClickHandler =
      result.foreach(_.isValid shouldBe true)
      this

    def shouldBeInvalid: ClickHandler =
      result.foreach(_.isValid shouldBe false)
      this

    def withNoError: ClickHandler =
      result.foreach(_.error shouldBe None)
      this

    def withError(expected: String): ClickHandler =
      result.foreach(_.error shouldBe Some(expected))
      this

    def andShouldBeInCell: ClickHandler =
      result.foreach(_.pos.isInCell shouldBe true)
      this

    def butNotInCell: ClickHandler =
      result.foreach(_.pos.isInCell shouldBe false)
      this

    def positionShouldBeValid: ClickHandler =
      result.foreach(_.pos.isValid shouldBe true)
      this

    def andPositionShouldBe(x: Double, y: Double): ClickHandler =
      result.foreach: r =>
        r.pos.x shouldBe x
        r.pos.y shouldBe y
      this

  case class MultiClickHandler(
      system: InputSystem,
      clicks: List[(Double, Double)]
  ):

    def atGridStart: MultiClickHandler =
      copy(clicks = clicks :+ (ViewConstants.GRID_OFFSET_X, ViewConstants.GRID_OFFSET_Y))

    def atGridStartPlusCell(colOffset: Int, rowOffset: Int): MultiClickHandler =
      copy(clicks =
        clicks :+ (
          ViewConstants.GRID_OFFSET_X + colOffset * ViewConstants.CELL_WIDTH,
          ViewConstants.GRID_OFFSET_Y + rowOffset * ViewConstants.CELL_HEIGHT
        )
      )

    def at(x: Double, y: Double): MultiClickHandler =
      copy(clicks = clicks :+ (x, y))

    def allShouldBeValid: MultiClickHandler =
      clicks.foreach: (x, y) =>
        val result = system.handleMouseClick(x, y)
        result.isValid shouldBe true
      this

    def allShouldBeInvalid: MultiClickHandler =
      clicks.foreach: (x, y) =>
        val result = system.handleMouseClick(x, y)
        result.isValid shouldBe false
      this

  case class GridAreaChecker(
      system: InputSystem,
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

    def atGridBoundary: GridAreaChecker =
      copy(
        x = ViewConstants.GRID_OFFSET_X + ViewConstants.GRID_COLS * ViewConstants.CELL_WIDTH,
        y = ViewConstants.GRID_OFFSET_Y + ViewConstants.GRID_ROWS * ViewConstants.CELL_HEIGHT
      )

    def forAllCellCenters: GridAreaChecker =
      for
        row <- 0 until ViewConstants.GRID_ROWS
        col <- 0 until ViewConstants.GRID_COLS
      do
        val cellX = ViewConstants.GRID_OFFSET_X + col * ViewConstants.CELL_WIDTH + ViewConstants.CELL_WIDTH / 2
        val cellY = ViewConstants.GRID_OFFSET_Y + row * ViewConstants.CELL_HEIGHT + ViewConstants.CELL_HEIGHT / 2
        system.isInGridArea(cellX, cellY) shouldBe true
      this

    def shouldBeInside: GridAreaChecker =
      system.isInGridArea(x, y) shouldBe true
      this

    def shouldBeOutside: GridAreaChecker =
      system.isInGridArea(x, y) shouldBe false
      this

    def allShouldBeInside: GridAreaChecker =
      this // Already verified in forAllCellCenters
