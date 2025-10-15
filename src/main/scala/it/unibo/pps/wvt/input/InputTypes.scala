package it.unibo.pps.wvt.input

import it.unibo.pps.wvt.utilities.Position

/**
 * Represents a mouse click event with x and y coordinates.
 * Provides a method to convert the click coordinates to a Position object.
 * @param x The x-coordinate of the mouse click.
 * @param y The y-coordinate of the mouse click.
 */
case class MouseClick(x: Double, y: Double):
  def toPosition: Position = Position(x, y)

/**
 * Represents the result of processing a mouse click.
 * Contains the position of the click, a validity flag, and an optional error message.
 * @param pos The Position corresponding to the mouse click.
 * @param isValid flag indicating whether the click is valid.
 * @param error An optional error message if the click is invalid.
 */
case class ClickResult(pos: Position, isValid: Boolean, error: Option[String] = None):
  /** It maps the position if the click is valid, otherwise returns the same ClickResult
   * @param f the function to apply to the position if valid
   * @return a new ClickResult with the mapped position if valid, otherwise the same ClickResult
   */
  def map(f: Position => Position): ClickResult =
    if isValid then copy(pos = f(pos)) else this

  /** It flatMaps the position if the click is valid, otherwise returns the same ClickResult
   * @param f the function to apply to the position if valid, returning a ClickResult
   * @return a new ClickResult returned by the function if valid, otherwise the same ClickResult
   */
  def flatMap(f: Position => ClickResult): ClickResult =
    if isValid then f(pos) else this

  /** It filters the ClickResult based on a predicate applied to the position if the click is valid
   * @param p the predicate to apply to the position
   * @param errorMsg the error message to set if the filter fails
   * @return the same ClickResult if valid and the predicate holds, otherwise an invalid ClickResult with the provided error message
   */
  def filter(p: Position => Boolean, errorMsg: String = "Filter failed"): ClickResult =
    if isValid && p(pos) then this
    else ClickResult.invalid(errorMsg)
/**
 * Companion object for ClickResult providing utility methods to create and validate ClickResult instances.
 */
object ClickResult:

  /** Creates a valid ClickResult with the given position.
   * @param pos The Position corresponding to the mouse click.
   * @return A ClickResult marked as valid with the provided position.
   */
  def valid(pos: Position): ClickResult =
    ClickResult(pos, isValid = true, None)
  /** Creates an invalid ClickResult with the given error message.
   * @param errorMsg The error message explaining why the click is invalid.
   * @return A ClickResult marked as invalid with a default position and the provided error message.
   */
  def invalid(errorMsg: String): ClickResult =
    ClickResult(Position(-1, -1), isValid = false, Some(errorMsg))
  /** Validates a position against multiple validation functions.
   * Each validation function is a tuple containing a predicate and an associated error message.
   * The position is checked against each predicate in sequence, and if any predicate fails,
   * an invalid ClickResult is returned with the corresponding error message.
   * If all predicates pass, a valid ClickResult is returned.
   * @param pos The Position to validate.
   * @param validations A variable number of tuples, each containing a predicate function and an error message.
   * @return A ClickResult indicating whether the position is valid or invalid based on the provided validations.
   */
  def validate(pos: Position)(validations: (Position => Boolean, String)*): ClickResult =
    validations.foldLeft(valid(pos)): (result, validation) =>
      result.filter(validation._1, validation._2)