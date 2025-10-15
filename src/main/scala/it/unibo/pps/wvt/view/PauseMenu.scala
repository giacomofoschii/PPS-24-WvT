package it.unibo.pps.wvt.view

import scalafx.scene._
import scalafx.scene.layout.*
import scalafx.geometry.*
import scalafx.scene.image.*
import it.unibo.pps.wvt.utilities.ViewConstants._
import it.unibo.pps.wvt.view.ButtonFactory._
import it.unibo.pps.wvt.view.ButtonFactory.Presets._
import it.unibo.pps.wvt.view.ImageFactory._

/** Factory object for creating the pause menu UI component.
  *
  * The pause menu includes a background image, a title image, and buttons for resuming the game,
  * returning to the main menu, and exiting the game. The layout is managed using a StackPane
  * containing a BorderPane for organizing the title and buttons.
  */
object PauseMenu:

  /** Creates and returns the pause menu as a Parent node.
    *
    * @return A Parent node representing the pause menu UI.
    */
  def apply(): Parent =
    lazy val backgroundImage =
      createBackgroundView("/in_game_menu.jpg", IN_GAME_MENU_SCALE_FACTOR).getOrElse(new ImageView())
    lazy val titleImage =
      createImageView("/paused.png", (backgroundImage.fitWidth * IN_GAME_TITLE_SCALE_FACTOR).toInt) match
        case Right(imageView) => imageView
        case Left(error) =>
          println(error)
          new ImageView()

    val menuLayout = createMenuLayout(titleImage)
    new StackPane:
      children = Seq(backgroundImage, menuLayout)

  /** Creates the menu layout with title and buttons.
    *
    * @param title The ImageView representing the title of the pause menu.
    * @return A BorderPane containing the title and buttons.
    */
  private def createMenuLayout(title: ImageView): BorderPane =
    val resumeButton = createStyledButton(inGameButtonPreset("Resume"))(handleAction(ResumeGame))
    val mainButton   = createStyledButton(inGameButtonPreset("Main Menu"))(handleAction(BackToMenu))
    val exitButton   = createStyledButton(inGameButtonPreset("Exit"))(handleAction(ExitGame))

    new BorderPane:
      top = new VBox:
        alignment = Pos.Center
        children = title

      center = new HBox:
        alignment = Pos.Center
        spacing = PAUSE_BUTTON_SPICING
        padding = Insets(PADDING_MENU)
        children = Seq(mainButton, resumeButton, exitButton)
