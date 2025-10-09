package it.unibo.pps.wvt.input

import it.unibo.pps.wvt.utilities.Position

case class InputSystem():

  private val processor = InputProcessor()
  
  def handleMouseClick(screenX: Double, screenY: Double): ClickResult =
    MouseClick(screenX, screenY).validate(processor)

  def isInGridArea(x: Double, y: Double): Boolean =
    processor.isInGridArea(x, y)

  def processClicks(clicks: Seq[(Double, Double)]): Seq[ClickResult] =
    clicks.map((x, y) => handleMouseClick(x, y))
  
  def validPositions(clicks: Seq[(Double, Double)]): Seq[Position] =
    clicks
      .map((x, y) => handleMouseClick(x, y))
      .collect { case result if result.isValid => result.pos }
  
  def partitionClicks(clicks: Seq[(Double, Double)]): (Seq[Position], Seq[String]) =
    clicks
      .map((x, y) => handleMouseClick(x, y))
      .partition(_.isValid) match
      case (valid, invalid) =>
        (valid.map(_.pos), invalid.flatMap(_.error))
  
  def countValidClicks(clicks: Seq[(Double, Double)]): Int =
    clicks.count((x, y) => processor.isInGridArea(x, y))
  
  def firstValidClick(clicks: Seq[(Double, Double)]): Option[Position] =
    clicks
      .map((x, y) => handleMouseClick(x, y))
      .find(_.isValid)
      .map(_.pos)
  
  def validateClick(screenX: Double, screenY: Double)
                   (extraValidation: Position => Boolean): ClickResult =
    handleMouseClick(screenX, screenY)
      .filter(extraValidation, "Custom validation failed")


object InputSystem:
  def extractPosition(result: ClickResult): Option[Position] =
    result match
      case ClickResult(pos, true, _) => Some(pos)
      case _ => None
  
  def sequenceResults(results: Seq[ClickResult]): Option[Seq[Position]] =
    if results.forall(_.isValid) then
      Some(results.map(_.pos))
    else
      None