package it.unibo.pps.wvt.input

import it.unibo.pps.wvt.utilities.Position
import it.unibo.pps.wvt.utilities.ViewConstants.*

case class InputProcessor():

  def processClick(click: MouseClick): ClickResult =
    val position = click.toPosition

    (for
      validPos <- Option.when(position.isValid)(position)
      _ <- Option.when(isInGridArea(click.x, click.y))(())
    yield ClickResult.valid(validPos))
      .getOrElse(ClickResult.invalid("Invalid Position"))

  def processClickWithValidation(click: MouseClick): ClickResult =
    val position = click.toPosition
    ClickResult.validate(position)(
      (_.isValid, "Position is not valid"),
      (_ => isInGridArea(click.x, click.y), "Click outside grid area")
    )

  def isInGridArea(x: Double, y: Double): Boolean =
    gridBounds match
      case (xMin, xMax, yMin, yMax) =>
        isInRange(x, xMin, xMax) && isInRange(y, yMin, yMax)

  private lazy val gridBounds: (Double, Double, Double, Double) =
    (
      GRID_OFFSET_X,
      GRID_OFFSET_X + (GRID_COLS * CELL_WIDTH),
      GRID_OFFSET_Y,
      GRID_OFFSET_Y + (GRID_ROWS * CELL_HEIGHT)
    )

  private def isInRange(value: Double, min: Double, max: Double): Boolean =
    value >= min && value < max

  def isInGridX: Double => Boolean = x => isInRange(x, gridBounds._1, gridBounds._2)
  def isInGridY: Double => Boolean = y => isInRange(y, gridBounds._3, gridBounds._4)

extension (click: MouseClick)
  def validate(processor: InputProcessor): ClickResult =
    processor.processClick(click)

  def isInGrid(processor: InputProcessor): Boolean =
    processor.isInGridArea(click.x, click.y)

  def validateWith(processor: InputProcessor)(errorMsg: String): ClickResult =
    processor.processClick(click) match
      case result if result.isValid => result
      case _ => ClickResult.invalid(errorMsg)