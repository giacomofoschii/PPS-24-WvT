package it.unibo.pps.wvt.view

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.layout.VBox
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight, Text}
import scalafx.application.Platform

object WavePanel:
  private var waveText: Option[Text] = None
  private var lastWaveNumber: Int = -1
  private var wavePanel: Option[VBox] = None

  def createWavePanel(): VBox =
    waveText = None
    lastWaveNumber = -1

    val waveDisplay = createWaveDisplay()
    waveText = Some(waveDisplay)

    val panel = new VBox:
      spacing = 5
      padding = Insets(10)
      alignment = Pos.Center
      prefWidth = 140
      maxWidth = 140
      minWidth = 140
      prefHeight = 60
      maxHeight = 60
      minHeight = 60
      children = Seq(
        new Text("Wave"):
          font = Font.font("Times New Roman", FontWeight.Bold, 20)
          fill = Color.web("#DAA520")
          style = """
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 6, 0.7, 1, 1);
          """
          margin = Insets(20, 0, 0, 0)
        ,
        waveDisplay
      )

    wavePanel = Some(panel)

    val clipRect = new scalafx.scene.shape.Rectangle:
      width <== panel.width
      height <== panel.height
      arcWidth = 20
      arcHeight = 20

    panel.clip = clipRect
    updatePanelBackground(panel)
    panel

  private def createWaveDisplay(): Text =
    val currentWave = ViewController.getController
      .map(_.getCurrentWaveInfo._1)
      .getOrElse(1)

    new Text(s"$currentWave"):
      font = Font.font("Times New Roman", FontWeight.Bold, 36)
      fill = Color.web("#DAA520")
      style = """
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 8, 0.7, 1, 1);
      """
      margin = Insets(0, 0, 20, 0)

  def updateWave(): Unit = Platform.runLater:
    val (currentWave, spawned, maxTrolls) = ViewController.getController
      .map(_.getCurrentWaveInfo)
      .getOrElse((1, 0, 10))

    if currentWave != lastWaveNumber then
      waveText.foreach(_.text = s"$currentWave")
      lastWaveNumber = currentWave

  private def updatePanelBackground(panel: VBox): Unit =
    panel.style = s"""-fx-background-image: url('/shop_background.jpg');
                     -fx-background-size: cover;
                     -fx-background-repeat: no-repeat;
                     -fx-background-position: center;
                     -fx-background-radius: 20;
                     -fx-border-radius: 20;
                     -fx-border-color: transparent;
                     -fx-border-width: 1;
                     -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 8,0,2,2);"""