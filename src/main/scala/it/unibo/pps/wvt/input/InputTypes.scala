
package it.unibo.pps.wvt.input

import it.unibo.pps.wvt.utilities.GridMapper.LogicalCoords
import it.unibo.pps.wvt.utilities.Position

case class MouseClick(x: Double, y: Double)
case class ClickResult(pos: Position, isValid: Boolean, error: Option[String] = None)