
package it.unibo.pps.wvt.input

import it.unibo.pps.wvt.utilities.Position

case class MouseClick(x: Int, y: Int)
case class ClickResult(position: Position, isValid: Boolean, error: Option[String] = None)