package it.unibo.pps.wvt.input

import it.unibo.pps.wvt.utilities.Position

case class MouseClick(x: Double, y: Double):
  def toPosition: Position = Position(x, y)

case class ClickResult(pos: Position, isValid: Boolean, error: Option[String] = None):
  def map(f: Position => Position): ClickResult =
    if isValid then copy(pos = f(pos)) else this

  def flatMap(f: Position => ClickResult): ClickResult =
    if isValid then f(pos) else this

  def filter(p: Position => Boolean, errorMsg: String = "Filter failed"): ClickResult =
    if isValid && p(pos) then this
    else ClickResult.invalid(errorMsg)

object ClickResult:
  def valid(pos: Position): ClickResult =
    ClickResult(pos, isValid = true, None)

  def invalid(errorMsg: String): ClickResult =
    ClickResult(Position(-1, -1), isValid = false, Some(errorMsg))
  
  def validate(pos: Position)(validations: (Position => Boolean, String)*): ClickResult =
    validations.foldLeft(valid(pos)): (result, validation) =>
      result.filter(validation._1, validation._2)