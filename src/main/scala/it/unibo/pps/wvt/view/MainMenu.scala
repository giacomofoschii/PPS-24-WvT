package it.unibo.pps.wvt.view

import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.geometry._
import scalafx.scene.text._
import scalafx.scene.image._

import it.unibo.pps.wvt.utilities.ViewConstants._

case class ButtonConfig(text: String, width: Int, height: Int,
                        fontSize: Int, fontFamily: String)

case class MenuConfig(windowScale: Double = BG_SCALE_FACTOR, buttons: Map[String, ButtonConfig])

sealed trait MenuAction
case object StartGame extends MenuAction
case object ShowInfo extends MenuAction
case object ExitGame extends MenuAction

object MainMenu extends JFXApp3 {

  override def start(): Unit = {
    lazy val backgroundImage = createBackgroundView("/main_menu.jpg")
    lazy val logoImage = 
      createImageView("/logo_title.png", (backgroundImage.get.fitWidth*TITLE_SCALE_FACTOR).toInt) match
      case Right(imageView) => imageView
      case Left(error) =>
        println(error)
        new ImageView() // Placeholder if image fails to load

    val menuLayout = createMenuLayout(logoImage)
    stage = new JFXApp3.PrimaryStage {
      title = "Wizards vs Trolls"
      scene = new Scene {
        root = new StackPane {
          children = Seq(
            backgroundImage.getOrElse(new ImageView()), // Placeholder if image fails to load
            menuLayout
          )
        }
      }
    }
  }

  private def startGame(): Unit = {
    println("Starting game...")
    // Logic to start the game goes here
  }

  private def showInfo(): Unit = {
    println("Showing game info...")
    // Logic to show game information goes here
  }

  private def loadImage(path: String): Option[Image] =
    Option(getClass.getResourceAsStream(path)).map(new Image(_))

  private def createImageView(imagePath: String, width: Int): Either[String, ImageView] =
    for
      image <- loadImage(imagePath).toRight(s"Error to load image at path: $imagePath")
    yield new ImageView(image) {
      fitWidth = width
      preserveRatio = true
    }

  private def createStyledButton(config: ButtonConfig)(action: => Unit): Button =
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

  private def createBackgroundView(path: String): Option[ImageView] =
    loadImage(path).map(myImage => new ImageView(myImage) {
      fitWidth = myImage.width.value * BG_SCALE_FACTOR
      fitHeight = myImage.height.value * BG_SCALE_FACTOR
      preserveRatio = true
    })

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
        padding = Insets(5)
        children = logo
      }
      center = new VBox {
        alignment = Pos.Center
        padding = Insets(15) // Aumentato da 10 a 15 per miglior allineamento
        children = startButton
      }
      bottom = new BorderPane {
        padding = Insets(15)
        left = infoButton
        right = exitButton
      }
    }

  private def handleAction(action: MenuAction): Unit = action match {
    case StartGame => println("Starting game...")
    case ShowInfo => println("Showing game info...")
    case ExitGame => sys.exit(0)
  }
}
