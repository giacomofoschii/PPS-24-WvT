package it.unibo.pps.wvt.input

import it.unibo.pps.wvt.utilities.Position

/**
 * The InputSystem class handles mouse click inputs and validates their positions within a defined grid area.
 * It utilizes an InputProcessor to process and validate clicks, providing various methods to interact with
 * and analyze click data.
 */
case class InputSystem():

  private val processor = InputProcessor()
  /**
   * Handles a mouse click at the specified screen coordinates.
   * @param screenX The x-coordinate of the mouse click.
   * @param screenY The y-coordinate of the mouse click.
   * @return A ClickResult indicating whether the click is valid and the corresponding position if valid.
   */
  def handleMouseClick(screenX: Double, screenY: Double): ClickResult =
    MouseClick(screenX, screenY).validate(processor)
  /**
   * Checks if the given (x, y) coordinates are within the defined grid area.
   * @param x represents the x coordinate of the click
   * @param y represents the y coordinate of the click
   * @return true if the coordinates are within the grid area, false otherwise
   */
  def isInGridArea(x: Double, y: Double): Boolean =
    processor.isInGridArea(x, y)
  /**
   * Processes a sequence of mouse clicks and returns a sequence of ClickResults.
   * @param clicks A sequence of tuples representing the (x, y) coordinates of mouse clicks.
   * @return A sequence of ClickResults indicating the validity of each click and the corresponding positions if valid.
   */
  def processClicks(clicks: Seq[(Double, Double)]): Seq[ClickResult] =
    clicks.map((x, y) => handleMouseClick(x, y))
    
  /**
   * Filters and returns only the valid positions from a sequence of mouse clicks.
   * @param clicks A sequence of tuples representing the (x, y) coordinates of mouse clicks.
   * @return A sequence of valid Positions corresponding to the valid clicks.
   */
  def validPositions(clicks: Seq[(Double, Double)]): Seq[Position] =
    clicks
      .map((x, y) => handleMouseClick(x, y))
      .collect { case result if result.isValid => result.pos }
  /**
   * Partitions a sequence of mouse clicks into valid positions and error messages for invalid clicks.
   * @param clicks A sequence of tuples representing the (x, y) coordinates of mouse clicks.
   * @return A tuple containing a sequence of valid Positions and a sequence of error messages for invalid clicks.
   */
  def partitionClicks(clicks: Seq[(Double, Double)]): (Seq[Position], Seq[String]) =
    clicks
      .map((x, y) => handleMouseClick(x, y))
      .partition(_.isValid) match
      case (valid, invalid) =>
        (valid.map(_.pos), invalid.flatMap(_.error))
        
  /**
   * Counts the number of valid clicks within the grid area from a sequence of mouse clicks.
   * @param clicks A sequence of tuples representing the (x, y) coordinates of mouse clicks.
   * @return The count of valid clicks within the grid area.
   */
  def countValidClicks(clicks: Seq[(Double, Double)]): Int =
    clicks.count((x, y) => processor.isInGridArea(x, y))
  
  /** Finds and returns the first valid click position from a sequence of mouse clicks.
   * @param clicks A sequence of tuples representing the (x, y) coordinates of mouse clicks.
   * @return An Option containing the first valid Position if found, or None if no valid clicks exist.
   */
  def firstValidClick(clicks: Seq[(Double, Double)]): Option[Position] =
    clicks
      .map((x, y) => handleMouseClick(x, y))
      .find(_.isValid)
      .map(_.pos)
  /**
   * Validates a mouse click at the specified screen coordinates with an additional custom validation function.
   * @param screenX The x-coordinate of the mouse click.
   * @param screenY The y-coordinate of the mouse click.
   * @param extraValidation A custom validation function that takes a Position and returns a Boolean.
   * @return A ClickResult indicating whether the click is valid based on both standard and custom validations.
   */
  def validateClick(screenX: Double, screenY: Double)
                   (extraValidation: Position => Boolean): ClickResult =
    handleMouseClick(screenX, screenY)
      .filter(extraValidation, "Custom validation failed")

/**
 * Companion object for InputSystem providing utility methods for handling ClickResults.
 */
object InputSystem:
  /**
   * Extracts the Position from a ClickResult if it is valid.
   * @param result The ClickResult to extract the position from.
   * @return An Option containing the Position if the ClickResult is valid, or None if it is invalid.
   */
  def extractPosition(result: ClickResult): Option[Position] =
    result match
      case ClickResult(pos, true, _) => Some(pos)
      case _ => None
  /**
   * Sequences a collection of ClickResults into an Option containing a sequence of Positions.
   * If all ClickResults are valid, returns Some with the sequence of Positions; otherwise, returns None.
   * @param results A sequence of ClickResults to process.
   * @return An Option containing a sequence of Positions if all ClickResults are valid, or None if any are invalid.
   */
  def sequenceResults(results: Seq[ClickResult]): Option[Seq[Position]] =
    if results.forall(_.isValid) then
      Some(results.map(_.pos))
    else
      None