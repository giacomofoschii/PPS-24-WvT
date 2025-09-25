package it.unibo.pps.wvt.view

import scalafx.scene._
import scalafx.scene.layout.*
import scalafx.geometry.*
import scalafx.scene.image.*
import it.unibo.pps.wvt.utilities.ViewConstants._
import it.unibo.pps.wvt.view.ButtonFactory._
import it.unibo.pps.wvt.view.ImageFactory._

object PauseMenu:
  def apply(): Parent =
    lazy val backgroundImage = createBackgroundView("/main_menu.jpg", MENU_SCALE_FACTOR).getOrElse(new ImageView())
    lazy val logoImage =
      createImageView("/paused.png", (backgroundImage.fitWidth*TITLE_SCALE_FACTOR).toInt) match
        case Right(imageView) => imageView
        case Left(error) =>
          println(error)
          new ImageView()

    val menuLayout = createMenuLayout(logoImage)
    new StackPane:
      children = Seq(backgroundImage, menuLayout)

  private def createMenuLayout(logo: ImageView): BorderPane =
    val buttonConfigs = Map(
      "resume" -> ButtonConfig("Resume", 250, 150, 24, "Times New Roman"),
      "main" -> ButtonConfig("Main Menu", 150, 80, 15, "Times New Roman"),
      "exit" -> ButtonConfig("Exit", 150, 80, 15, "Times New Roman")
    )
    val resumeButton = createStyledButton(buttonConfigs("resume"))(handleAction(ResumeGame))
    val mainButton = createStyledButton(buttonConfigs("main"))(handleAction(BackToMenu))
    val exitButton = createStyledButton(buttonConfigs("exit"))(handleAction(ExitGame))

    new BorderPane:
      top = new VBox:
        alignment = Pos.Center
        padding = Insets(PADDING_MENU)
        children = logo

      center = new VBox:
        alignment = Pos.Center
        padding = Insets(PADDING_MENU)
        children = resumeButton

      bottom = new BorderPane:
        padding = Insets(PADDING_MENU)
        left = mainButton
        right = exitButton