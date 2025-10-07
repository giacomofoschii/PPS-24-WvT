package it.unibo.pps.wvt.view

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.layout.{VBox, StackPane}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight, Text}
import scalafx.scene.image.ImageView
import scalafx.application.Platform
import it.unibo.pps.wvt.view.ImageFactory.*

object WavePanel:
  private var waveText: Option[Text] = None
  private var lastWaveNumber: Int = -1

  def createWavePanel(): VBox =
    waveText = None
    lastWaveNumber = -1

    val waveDisplay = new Text(getCurrentWave.toString):
      font = Font.font("Times New Roman", FontWeight.Bold, 20)
      fill = Color.web("#DAA520")

    waveText = Some(waveDisplay)

    val content = new VBox:
      spacing = -2
      padding = Insets(8)
      alignment = Pos.Center
      children = Seq(
        new Text("Wave"):
          font = Font.font("Times New Roman", FontWeight.Bold, 20)
          fill = Color.web("#DAA520")
        ,
        waveDisplay
      )

    val backgroundImg = createImageView("/button_background.png", 200) match
      case Right(img) =>
        img.fitWidth = 200
        img.fitHeight = 100
        img.preserveRatio = false
        img
      case Left(_) => new ImageView()

    val stackPane = new StackPane:
      children = Seq(backgroundImg, content)

    new VBox:
      prefWidth = 200
      prefHeight = 100
      alignment = Pos.Center
      children = stackPane

  private def getCurrentWave: Int =
    ViewController.getController.map(_.getCurrentWaveInfo._1).getOrElse(1)

  def updateWave(): Unit = Platform.runLater:
    val currentWave = getCurrentWave
    if currentWave != lastWaveNumber then
      waveText.foreach(_.text = s"$currentWave")
      lastWaveNumber = currentWave