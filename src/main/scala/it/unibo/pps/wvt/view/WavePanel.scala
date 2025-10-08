package it.unibo.pps.wvt.view

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.layout.VBox
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight, Text}


object WavePanel:
  private var waveText: Option[Text] = None
  private var lastWaveNumber: Int = -1
  private var wavePanel: Option[VBox] = None

  def createWavePanel(): VBox =
    waveText = None
    lastWaveNumber = -1

    val waveDisplay = createWaveDisplay()
    waveText = Some(waveDisplay)

    val waveLabel = createStyledText(
      "Wave: ",
      27,
      Color.web("#DAA520")
    )

    waveDisplay.font = Font.font("Times New Roman", FontWeight.Bold, 27)
    waveDisplay.margin = Insets(0, 0, 0, 5)

    val hbox = new scalafx.scene.layout.HBox:
      spacing = 5
      alignment = Pos.Center
      children = Seq(waveLabel, waveDisplay)

    val panel = createStyledPanel(hbox)
    wavePanel = Some(panel)

    panel

  private def createStyledText(
                                content: String,
                                fontSize: Int,
                                textColor: Color
                              ): Text =
    new Text(content):
      font = Font.font("Times New Roman", FontWeight.Bold, fontSize)
      fill = textColor
      style = """
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 6, 0.7, 1, 1);
      """

  private def createWaveDisplay(): Text =
    val currentWave = getCurrentWaveNumber
    createStyledText(
      s"$currentWave",
      36,
      Color.web("#DAA520")
    )

  private def getCurrentWaveNumber: Int =
    ViewController.getController
      .map(_.getCurrentWaveInfo._1)
      .getOrElse(1)

  def updateWaveNumber(waveNumber: Int): Unit =
    if shouldUpdateDisplay(waveNumber) then
      updateWaveDisplay(waveNumber)
      lastWaveNumber = waveNumber

  def updateWave(): Unit =
    val (currentWave, _, _) = getCurrentWaveInfo
    updateWaveNumber(currentWave)

  private def performWaveUpdate(): Unit =
    val (currentWave, spawned, maxTrolls) = getCurrentWaveInfo

    if shouldUpdateDisplay(currentWave) then
      updateWaveDisplay(currentWave)
      lastWaveNumber = currentWave

  private def getCurrentWaveInfo: (Int, Int, Int) =
    ViewController.getController
      .map(_.getCurrentWaveInfo)
      .getOrElse((1, 0, 10))

  private def shouldUpdateDisplay(currentWave: Int): Boolean =
    currentWave != lastWaveNumber

  private def updateWaveDisplay(waveNumber: Int): Unit =
    waveText.foreach(_.text = s"$waveNumber")

  private def createStyledPanel(content: scalafx.scene.layout.HBox): VBox =
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
      children = Seq(content)

    applyClipAndStyle(panel)
    panel

  private def applyClipAndStyle(panel: VBox): Unit =
    val clipRect = new scalafx.scene.shape.Rectangle:
      width <== panel.width
      height <== panel.height
      arcWidth = 20
      arcHeight = 20

    panel.clip = clipRect
    updatePanelBackground(panel)

  private def getPanelStyle: String =
    """-fx-background-image: url('/shop_background.jpg');
       -fx-background-size: cover;
       -fx-background-repeat: no-repeat;
       -fx-background-position: center;
       -fx-background-radius: 20;
       -fx-border-radius: 20;
       -fx-border-color: #4B2E06;
       -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 8,0,2,2);"""

  private def updatePanelBackground(panel: VBox): Unit =
    panel.style = getPanelStyle

  def reset(): Unit =
      lastWaveNumber = -1