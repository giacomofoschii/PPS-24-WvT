package it.unibo.pps.wvt.view

import scalafx.scene._
import scalafx.scene.layout.*
import scalafx.geometry.*
import scalafx.scene.image.*

import it.unibo.pps.wvt.utilities.ViewConstants._
import it.unibo.pps.wvt.view.ButtonFactory._
import it.unibo.pps.wvt.view.ButtonFactory.Presets.{smallButtonPreset, mainMenuButtonPreset}
import it.unibo.pps.wvt.view.ImageFactory._

object MainMenu:
  def apply(): Parent =
    lazy val backgroundImage = createBackgroundView("/main_menu.jpg", MENU_SCALE_FACTOR).getOrElse(new ImageView())
    lazy val logoImage =
      createImageView("/logo_title.png", (backgroundImage.fitWidth*TITLE_SCALE_FACTOR).toInt) match
        case Right(imageView) => imageView
        case Left(error) =>
          println(error)
          new ImageView() // Placeholder if image fails to load

    val menuLayout = createMenuLayout(logoImage)
    new StackPane {
      children = Seq(backgroundImage, menuLayout)
    }

  private def createMenuLayout(logo: ImageView): BorderPane =
    val startButton = createStyledButton(mainMenuButtonPreset("Start Game"))(handleAction(StartGame))
    val infoButton = createStyledButton(smallButtonPreset("Game Info"))(handleAction(ShowInfo))
    val exitButton = createStyledButton(smallButtonPreset("Exit"))(handleAction(ExitGame))

    new BorderPane {
      top = new VBox {
        alignment = Pos.Center
        padding = Insets(PADDING_MENU)
        children = logo
      }

      center = new VBox {
        alignment = Pos.Center
        padding = Insets(PADDING_MENU)
        children = startButton
      }

      bottom = new BorderPane {
        padding = Insets(PADDING_MENU)
        left = infoButton
        right = exitButton
      }
    }