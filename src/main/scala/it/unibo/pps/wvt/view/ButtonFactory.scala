package it.unibo.pps.wvt.view

import it.unibo.pps.wvt.ecs.components.WizardType

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
      style = "-fx-background-image: url('/button_background.png'); " +
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
  case class PlacingWizard(wizardType: WizardType) extends ButtonAction

  def handleAction(action: ButtonAction): Unit = action match
    case StartGame => ViewController.requestGameView()
    case ShowInfo => ViewController.requestInfoMenu()
    case BackToMenu => ViewController.requestMainMenu()
    case PlacingWizard(wizardType) => ViewController.requestPlaceWizard(wizardType)
    case PauseGame => ViewController.requestPauseGame()
    case ResumeGame => ViewController.requestResumeGame()
    case ExitGame => ViewController.requestExitGame()
}