package it.unibo.pps.wvt.view

import scalafx.scene.control.Button
import scalafx.scene.text.{Font, FontWeight}

object ButtonFactory {
  case class ButtonConfig(text: String, width: Int, height: Int, fontSize: Int, fontFamily: String)

  def createStyledButton(config: ButtonConfig)(action: => Unit): Button =
    new Button(config.text) {
      font = Font.font(config.fontFamily, FontWeight.Bold, config.fontSize)
      prefWidth = config.width
      prefHeight = config.height
      style = "-fx-background-image: url('/pause_menu.png'); " +
        s"-fx-background-size: ${config.width}px ${config.height}px; " +
        "-fx-background-repeat: no-repeat; " +
        "-fx-background-position: center; " +
        "-fx-background-color: transparent; " +
        "-fx-border-color: transparent; " +
        "-fx-padding: 0; " +
        "-fx-text-fill: #DAA520;"
      onAction = _ => action
    }
  
  sealed trait ButtonAction
  case object StartGame extends ButtonAction
  case object ShowInfo extends ButtonAction
  case object ExitGame extends ButtonAction
}
