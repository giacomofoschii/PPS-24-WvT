package it.unibo.pps.wvt.view

import scalafx.scene.*
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.image.Image

object ViewController extends JFXApp3 {
  override def start(): Unit =
    showMainMenu()
  
  def showMainMenu(): Unit =
    stage = createStandardStage(MainMenu())

  def showGameView(): Unit =
    stage = createStandardStage(GameView())
    
  def showGameInfo(): Unit = ???

  private def createStandardStage(pRoot: Parent): PrimaryStage =
    new PrimaryStage {
      title = "Wizards vs Trolls"
      scene = new Scene {
        root = pRoot
      }

      Option(getClass.getResourceAsStream("/window_logo.png"))
        .map(new Image(_))
        .foreach(icon => icons += icon)

      resizable = false
      centerOnScreen()
    }
}
