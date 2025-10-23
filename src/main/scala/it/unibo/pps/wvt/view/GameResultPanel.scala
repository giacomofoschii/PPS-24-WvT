package it.unibo.pps.wvt.view

import scalafx.scene.Parent
import scalafx.scene.layout.*
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.image.ImageView
import scalafx.scene.control.Button
import it.unibo.pps.wvt.view.ButtonFactory.*
import it.unibo.pps.wvt.view.ButtonFactory.Presets.*
import it.unibo.pps.wvt.view.ImageFactory.*
import it.unibo.pps.wvt.utilities.ViewConstants.*
import scalafx.application.Platform

import java.util.concurrent.atomic.AtomicBoolean
import scala.collection.mutable.ArrayBuffer

/** Factory object for creating the game result panel UI component.
  *
  * The game result panel displays either a victory or defeat message along with options to continue
  * to the next wave or start a new game, as well as an option to exit the game. The layout is managed
  * using a StackPane containing a BorderPane for organizing the title and buttons.
  */
object GameResultPanel:

  private val isProcessing = new AtomicBoolean(false)
  private val buttonRefs   = ArrayBuffer[Button]()

  /** Represents the type of game result, either Victory or Defeat.
    *
    * Each result type defines its own title image path, continue button text, and action to be taken
    * when the continue button is pressed.
    */
  sealed trait ResultType:
    def titleImagePath: String
    def continueButtonText: String
    def continueAction: ButtonAction

  /** Represents a victory result type.
    *
    * Displays a victory image and provides an option to continue to the next wave.
    */
  case object Victory extends ResultType:
    val titleImagePath               = "/victory.png"
    val continueButtonText           = "Next wave"
    val continueAction: ButtonAction = ContinueBattle

  /** Represents a defeat result type.
    *
    * Displays a defeat image and provides an option to start a new game.
    */
  case object Defeat extends ResultType:
    val titleImagePath               = "/defeat.png"
    val continueButtonText           = "New game"
    val continueAction: ButtonAction = NewGame

  /** Creates and returns the game result panel as a Parent node based on the provided result type.
    *
    * @param resultType The type of game result (Victory or Defeat).
    * @return A Parent node representing the game result panel UI.
    */
  def apply(resultType: ResultType): Parent =
    isProcessing.set(false)
    createPanel(resultType)

  /** Creates the game result panel layout with title and buttons based on the result type.
    *
    * @return A StackPane containing the background image and menu layout.
    */
  private val createPanel: ResultType => Parent = resultType =>
    lazy val backgroundImage = createBackgroundView("/in_game_menu.jpg", IN_GAME_MENU_SCALE_FACTOR)
      .getOrElse(new ImageView())

    lazy val titleImage = createImageView(
      resultType.titleImagePath,
      (backgroundImage.fitWidth * IN_GAME_TITLE_SCALE_FACTOR).toInt
    ).fold(
      _ => new ImageView(),
      identity
    )

    val menuLayout = createMenuLayout(titleImage, resultType)

    new StackPane:
      children = Seq(backgroundImage, menuLayout)

  /** Creates a debounced button that prevents rapid clicks from causing race conditions.
    *
    * @param config The button configuration
    * @param action The action to perform when clicked
    * @return A button with debouncing logic
    */
  private def createDebouncedButton(config: ButtonConfig, action: ButtonAction): Button =
    val button = createStyledButton(config):
      if isProcessing.compareAndSet(false, true) then
        try
          disableButtons()
          handleAction(action)

          Platform.runLater:
            Thread.sleep(DEBOUNCE_MS)
            isProcessing.set(false)
            enableButtons()
        catch
          case e: Exception =>
            isProcessing.set(false)
            enableButtons()
            throw e

    buttonRefs.addOne(button)
    button

  private def disableButtons(): Unit =
    buttonRefs.foreach(_.disable = true)

  private def enableButtons(): Unit =
    buttonRefs.foreach(_.disable = false)

  /** Creates the menu layout with title and buttons based on the result type.
    *
    * @param titleImg   The ImageView representing the title of the game result panel.
    * @param resultType The type of game result (Victory or Defeat).
    * @return A BorderPane containing the title and buttons.
    */
  private def createMenuLayout(titleImg: ImageView, resultType: ResultType): BorderPane =
    buttonRefs.clear()

    val continueButton = createDebouncedButton(
      inGameButtonPreset(resultType.continueButtonText),
      resultType.continueAction
    )

    val exitButton = createDebouncedButton(
      inGameButtonPreset("Exit"),
      ExitGame
    )

    new BorderPane:
      top = new VBox:
        alignment = Pos.Center
        children = titleImg
      center = new HBox:
        alignment = Pos.Center
        spacing = PAUSE_BUTTON_SPICING
        padding = Insets(PADDING_MENU)
        children = Seq(continueButton, exitButton)
