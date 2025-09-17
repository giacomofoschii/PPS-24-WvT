package it.unibo.pps.wvt.view

import scalafx.scene.Cursor
import scalafx.scene.control.Button
import scalafx.scene.effect.DropShadow
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}

object ButtonFactory {
  case class ButtonConfig(text: String, width: Int, height: Int, fontSize: Int, fontFamily: String)

  def createStyledButton(config: ButtonConfig)(action: => Unit): Button =
    new Button(config.text) {
      cursor = Cursor.Hand
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
      onMouseEntered = _ => 
        effect = new DropShadow {
          color = Color.Gray
          radius = 10
        }
      onMouseExited = _ => effect = null
    }
  
  
  sealed trait ButtonAction
  case object StartGame extends ButtonAction
  case object BackToMenu extends ButtonAction
  case object ShowInfo extends ButtonAction
  case object ExitGame extends ButtonAction
  case object PauseGame extends ButtonAction
  case object ResumeGame extends ButtonAction

  def handleAction(action: ButtonAction): Unit = action match
    case StartGame => ViewController.showGameView()
    case ShowInfo => ViewController.showGameInfo()
    case BackToMenu => ViewController.showMainMenu()
    case PauseGame => println("Game Paused")
    case ResumeGame => println("Game Resumed")
    case ExitGame => sys.exit(0)
}