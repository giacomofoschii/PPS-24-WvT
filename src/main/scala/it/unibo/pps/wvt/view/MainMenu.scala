package it.unibo.pps.wvt.view

import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.geometry._
import scalafx.scene.text._
import scalafx.scene.image._

object MainMenu extends JFXApp3 {

  override def start(): Unit = {
    stage = new JFXApp3.PrimaryStage {
      title = "Wizards vs Trolls"
      val background = new Image(getClass.getResourceAsStream("/main_menu.jpg"))
      scene = new Scene (background.width.value*0.6,background.height.value*0.6) {
        val backgroundImage = new ImageView(background) {
          fitWidth = background.width.value * 0.6
          fitHeight = background.height.value * 0.6
          preserveRatio = false
        }

        val startButton = new Button("Start Game") {
          font = Font.font("Times New Roman", FontWeight.Bold, 24)
          prefWidth = 250
          prefHeight = 150
          style = "-fx-background-image: url('/pause_menu.png'); " +
            "-fx-background-size: 250px 150px; " +
            "-fx-background-repeat: no-repeat; " +
            "-fx-background-position: center; " +
            "-fx-background-color: transparent; " +
            "-fx-border-color: transparent; " +
            "-fx-padding: 0; " +
            "-fx-text-fill: #DAA520;"
          onAction = _ => startGame()
        }

        val logoText = new ImageView(new Image(getClass.getResourceAsStream("/logo_title.png"))) {
          fitWidth = background.width.value * 0.3
          preserveRatio = true
        }

        val infoButton = new Button("Game Info") {
          font = Font.font("Arial", FontWeight.Bold, 15)
          prefWidth = 150
          prefHeight = 80
          style = "-fx-background-image: url('/pause_menu.png'); " +
            "-fx-background-size: 150px 80px; " +
            "-fx-background-repeat: no-repeat; " +
            "-fx-background-position: center; " +
            "-fx-background-color: transparent; " +
            "-fx-border-color: transparent; " +
            "-fx-padding: 0; " +
            "-fx-text-fill: #DAA520;"
          onAction = _ => showInfo()
        }

        val exitButton = new Button("Exit") {
          font = Font.font("Arial", FontWeight.Bold, 15)
          prefWidth = 150
          prefHeight = 80
          style = "-fx-background-image: url('/pause_menu.png'); " +
            "-fx-background-size: 150px 80px; " +
            "-fx-background-repeat: no-repeat; " +
            "-fx-background-position: center; " +
            "-fx-background-color: transparent; " +
            "-fx-border-color: transparent; " +
            "-fx-padding: 0; " +
            "-fx-text-fill: #DAA520;"
          onAction = _ => sys.exit(0)
        }

        val menuLayout = new BorderPane {
          top = new VBox {
            alignment = Pos.Center
            padding = Insets(5)
            children = logoText
          }

          center = new VBox {
            alignment = Pos.Center
            padding = Insets(5)
            children = startButton
          }

          bottom = new BorderPane {
            padding = Insets(15)
            left = infoButton
            right = exitButton
          }
        }

        root = new StackPane {
          children = Seq(backgroundImage, menuLayout)
        }
      }

      resizable = false
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
}
