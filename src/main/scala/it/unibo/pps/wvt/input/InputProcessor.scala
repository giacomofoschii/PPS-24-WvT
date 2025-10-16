package it.unibo.pps.wvt.input

import it.unibo.pps.wvt.utilities.Position
import it.unibo.pps.wvt.utilities.ViewConstants.*

/** Processes mouse click inputs and validates their positions within a defined grid area. */
case class InputProcessor():
  /** Processes a mouse click and returns a ClickResult indicating whether the click is valid.
    * A click is considered valid if it falls within the grid bounds and corresponds to a valid position.
    *
    * @param click The MouseClick event to process.
    * @return A ClickResult indicating the validity of the click and the corresponding position if valid.
    */
  def processClick(click: MouseClick): ClickResult =
    val position = click.toPosition

    (for
      validPos <- Option.when(position.isValid)(position)
      _        <- Option.when(isWithinGridBounds(click.x, click.y))(())
    yield ClickResult.valid(validPos))
      .getOrElse(ClickResult.invalid("Invalid Position"))

  /** Processes a mouse click with additional validation checks.
    * Validates that the position is valid and that the click is within the grid area.
    * @param click The MouseClick event to process.
    * @return A ClickResult indicating the validity of the click and the corresponding position if valid.
    */
  def processClickWithValidation(click: MouseClick): ClickResult =
    val position = click.toPosition
    ClickResult.validate(position)(
      (_.isValid, "Position is not valid"),
      (_ => isInGridArea(click.x, click.y), "Click outside grid area")
    )

  /** Checks if the given (x, y) coordinates are within the defined grid area.
    * @param x represents the x coordinate of the click
    * @param y represents the y coordinate of the click
    * @return true if the coordinates are within the grid area, false otherwise
    */
  def isInGridArea(x: Double, y: Double): Boolean =
    gridBounds match
      case (xMin, xMax, yMin, yMax) =>
        isInRange(x, xMin, xMax) && isInRange(y, yMin, yMax)

  /** Checks if the given (x, y) coordinates are within the exact grid bounds.
    * @param x represents the x coordinate of the click
    * @param y represents the y coordinate of the click
    * @return true if the coordinates are within the grid bounds, false otherwise
    */
  private def isWithinGridBounds(x: Double, y: Double): Boolean =
    gridBounds match
      case (xMin, xMax, yMin, yMax) =>
        x >= xMin && x <= xMax && y >= yMin && y <= yMax

  private lazy val gridBounds: (Double, Double, Double, Double) =
    (
      GRID_OFFSET_X,
      GRID_OFFSET_X + (GRID_COLS * CELL_WIDTH),
      GRID_OFFSET_Y,
      GRID_OFFSET_Y + (GRID_ROWS * CELL_HEIGHT)
    )

  /** Checks if a value is within a specified range [min, max).
    * @param value the value to check
    * @param min the minimum bound (inclusive)
    * @param max the maximum bound (exclusive)
    * @return true if the value is within the range, false otherwise
    */
  private def isInRange(value: Double, min: Double, max: Double): Boolean =
    value >= min && value < max

  /** Function to check if a given x coordinate is within the grid's x bounds.
    * @param x the x coordinate to check
    * @return true if the x coordinate is within the grid's x bounds, false otherwise
    */
  def isInGridX: Double => Boolean = x => isInRange(x, gridBounds._1, gridBounds._2)

  /** Function to check if a given y coordinate is within the grid's y bounds.
    * @param y the y coordinate to check
    * @return true if the y coordinate is within the grid's y bounds, false otherwise
    */
  def isInGridY: Double => Boolean = y => isInRange(y, gridBounds._3, gridBounds._4)

extension (click: MouseClick)
  /** Validates the mouse click using the provided InputProcessor.
    * @param processor The InputProcessor to use for validation.
    * @return A ClickResult indicating the validity of the click.
    */
  def validate(processor: InputProcessor): ClickResult =
    processor.processClick(click)

  /** Checks if the mouse click is within the grid area using the provided InputProcessor.
    * @param processor The InputProcessor to use for the check.
    * @return true if the click is within the grid area, false otherwise.
    */
  def isInGrid(processor: InputProcessor): Boolean =
    processor.isInGridArea(click.x, click.y)

  /** Validates the mouse click with a custom error message using the provided InputProcessor.
    * @param processor The InputProcessor to use for validation.
    * @param errorMsg The custom error message to return if the click is invalid.
    * @return A ClickResult indicating the validity of the click, with the custom error message if invalid.
    */
  def validateWith(processor: InputProcessor)(errorMsg: String): ClickResult =
    processor.processClick(click) match
      case result if result.isValid => result
      case _                        => ClickResult.invalid(errorMsg)
