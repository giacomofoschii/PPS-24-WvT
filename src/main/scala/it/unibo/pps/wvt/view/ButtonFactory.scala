package it.unibo.pps.wvt.view

import it.unibo.pps.wvt.ecs.components.WizardType

import scalafx.scene.Cursor
import scalafx.scene.control.Button
import scalafx.scene.effect.DropShadow
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}

/** Configuration for creating a styled button.
  *
  * @param text the button text
  * @param width the button width
  * @param height the button height
  * @param fontSize the font size of the button text
  * @param fontFamily the font family of the button text
  */
case class ButtonConfig(text: String, width: Int, height: Int, fontSize: Int, fontFamily: String):
  def backgroundStyle: String =
    s"""-fx-background-image: url('/button_background.png');
       -fx-background-size: ${width}px ${height}px;
       -fx-background-repeat: no-repeat;
       -fx-background-position: center;
       -fx-background-color: transparent;
       -fx-border-color: transparent;
       -fx-padding: 0;
       -fx-text-fill: #DAA520;"""

/** Defines actions associated with buttons in the UI. */
sealed trait ButtonAction

/** Companion object for ButtonAction containing predefined actions. */
object ButtonAction:
  case object StartGame                            extends ButtonAction
  case object ShowInfo                             extends ButtonAction
  case object BackToMenu                           extends ButtonAction
  case object ContinueBattle                       extends ButtonAction
  case object NewGame                              extends ButtonAction
  case class PlacingWizard(wizardType: WizardType) extends ButtonAction
  case object PauseGame                            extends ButtonAction
  case object ResumeGame                           extends ButtonAction
  case object ExitGame                             extends ButtonAction

/** Factory for creating styled buttons with predefined configurations and actions. */
object ButtonFactory:
  export ButtonAction.*

  def createStyledButton(config: ButtonConfig)(action: => Unit): Button =
    createButton(config).withAction(action).withOverEffect().build()

  def createCustomButton(config: ButtonConfig, customStyle: Button => Unit)(action: => Unit): Button =
    val button = createButton(config).build()
    customStyle(button)
    button.onAction = _ => action
    button

  def handleAction(action: ButtonAction): Unit = action match
    case StartGame                 => ViewController.requestGameView()
    case ShowInfo                  => ViewController.requestInfoMenu()
    case BackToMenu                => ViewController.requestMainMenu()
    case PlacingWizard(wizardType) => ViewController.requestPlaceWizard(wizardType)
    case PauseGame                 => ViewController.requestPauseGame()
    case ResumeGame                => ViewController.requestResumeGame()
    case ExitGame                  => ViewController.requestExitGame()
    case ContinueBattle            => ViewController.requestContinueBattle()
    case NewGame                   => ViewController.requestNewGame()

  /** Builder for creating buttons with a fluent interface.
    *
    * @param button the button being built
    */
  private case class ButtonBuilder(button: Button):
    def withAction(action: => Unit): ButtonBuilder =
      button.onAction = _ => action
      this

    def withOverEffect(): ButtonBuilder =
      button.onMouseEntered = _ =>
        button.effect = new DropShadow:
          color = Color.Gray
          radius = 10
      button.onMouseExited = _ => button.effect = null
      this

    def withCustomEffect(onEnter: Button => Unit, onExit: Button => Unit): ButtonBuilder =
      button.onMouseEntered = _ => onEnter(button)
      button.onMouseExited = _ => onExit(button)
      this

    def build(): Button = button

  private def createButton(config: ButtonConfig): ButtonBuilder =
    val button = new Button(config.text):
      cursor = Cursor.Hand
      font = Font.font(config.fontFamily, FontWeight.Bold, config.fontSize)
      prefWidth = config.width
      prefHeight = config.height
      style = config.backgroundStyle

    ButtonBuilder(button)

  /** Predefined button configurations for various UI contexts. */
  object Presets:
    def mainMenuButtonPreset(text: String): ButtonConfig =
      ButtonConfig(text, 250, 150, 24, "Times New Roman")

    def inGameButtonPreset(text: String): ButtonConfig =
      ButtonConfig(text, 175, 100, 18, "Times New Roman")

    def smallButtonPreset(text: String): ButtonConfig =
      ButtonConfig(text, 150, 80, 15, "Times New Roman")

    def shopButtonPreset(text: String): ButtonConfig =
      ButtonConfig(text, 200, 100, 20, "Times New Roman")

    def navButtonPreset(text: String): ButtonConfig =
      ButtonConfig(text, 140, 60, 16, "Times New Roman")
