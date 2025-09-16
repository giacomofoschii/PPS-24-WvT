package it.unibo.pps.wvt.view

import scalafx.scene.*
import scalafx.scene.layout.*
import scalafx.geometry.*
import scalafx.scene.image.*
import it.unibo.pps.wvt.utilities.ViewConstants.*
import it.unibo.pps.wvt.view.ButtonFactory.*
import it.unibo.pps.wvt.view.ImageFactory.*

object InfoMenu {
  def apply(): Parent = {
    lazy val backgroundImage = createBackgroundView("/main_menu.jpg", MENU_SCALE_FACTOR).getOrElse(new ImageView())

    val infoLayout = createInfoLayout
    new StackPane {
      children = Seq(backgroundImage, infoLayout)
    }
  }

  private def createInfoLayout: BorderPane =
    val buttonConfigs = Map(
      "back" -> ButtonConfig("Main Menu", 150, 80, 15, "Times New Roman"),
      "exit" -> ButtonConfig("Exit", 150, 80, 15, "Times New Roman")
    )
    val backButton = createStyledButton(buttonConfigs("back"))(handleAction(BackToMenu))
    val exitButton = createStyledButton(buttonConfigs("exit"))(handleAction(ExitGame))

    new BorderPane {
      bottom = new BorderPane {
        padding = Insets(15)
        left = backButton
        right = exitButton
      }
    }

  private def handleAction(action: ButtonAction): Unit = action match {
    case BackToMenu => ViewController.showMainMenu()
    case _ => sys.exit(0)
  }
}
