package it.unibo.pps.wvt.view

import scalafx.scene._
import scalafx.scene.layout.*
import scalafx.geometry.*
import scalafx.scene.image.*
import it.unibo.pps.wvt.utilities.ViewConstants._
import it.unibo.pps.wvt.view.ButtonFactory._
import it.unibo.pps.wvt.view.ImageFactory._

object MainMenu {
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
    val buttonConfigs = Map(
      "start" -> ButtonConfig("Start Game", 250, 150, 24, "Times New Roman"),
      "info" -> ButtonConfig("Game Info", 150, 80, 15, "Times New Roman"),
      "exit" -> ButtonConfig("Exit", 150, 80, 15, "Times New Roman")
    )
    val startButton = createStyledButton(buttonConfigs("start"))(handleAction(StartGame))
    val infoButton = createStyledButton(buttonConfigs("info"))(handleAction(ShowInfo))
    val exitButton = createStyledButton(buttonConfigs("exit"))(handleAction(ExitGame))

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
}