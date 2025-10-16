package it.unibo.pps.wvt.view

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight, Text}

case class WaveState(
    lastWaveNumber: Int = -1,
    waveText: Option[Text] = None,
    wavePanel: Option[VBox] = None
)

object WavePanel:
  private val stateRef = new java.util.concurrent.atomic.AtomicReference[WaveState](WaveState())

  def createWavePanel(): VBox =
    val newState = WaveState()
    stateRef.set(newState)

    val waveDisplay = createWaveDisplay()
    val waveLabel   = createWaveLabel()
    val hbox        = createWaveContainer(waveLabel, waveDisplay)
    val panel       = createStyledPanel(hbox)

    stateRef.updateAndGet(_.copy(waveText = Some(waveDisplay), wavePanel = Some(panel)))
    panel

  private def createWaveLabel(): Text =
    createStyledText("Wave: ", 27, Color.web("#DAA520"))

  private def createWaveDisplay(): Text =
    val currentWave = getCurrentWaveNumber
    val waveText    = createStyledText(s"$currentWave", 27, Color.web("#DAA520"))
    waveText.margin = Insets(0, 0, 0, 5)
    waveText

  private def createWaveContainer(label: Text, display: Text): HBox =
    new HBox:
      spacing = 5
      alignment = Pos.Center
      children = Seq(label, display)

  private def createStyledText(content: String, fontSize: Int, textColor: Color): Text =
    new Text(content):
      font = Font.font("Times New Roman", FontWeight.Bold, fontSize)
      fill = textColor
      style = "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 6, 0.7, 1, 1);"

  private def getCurrentWaveNumber: Int =
    ViewController.getController
      .map(_.getCurrentWaveInfo._1)
      .getOrElse(1)

  def updateWaveNumber(waveNumber: Int): Unit =
    val currentState = stateRef.get()
    shouldUpdateDisplay(waveNumber, currentState.lastWaveNumber) match
      case true =>
        updateWaveDisplay(waveNumber)
        stateRef.updateAndGet(_.copy(lastWaveNumber = waveNumber))
      case false => ()

  def updateWave(): Unit =
    val (currentWave, _, _) = getCurrentWaveInfo
    updateWaveNumber(currentWave)

  private def getCurrentWaveInfo: (Int, Int, Int) =
    ViewController.getController
      .map(_.getCurrentWaveInfo)
      .getOrElse((1, 0, 10))

  private def shouldUpdateDisplay(currentWave: Int, lastWave: Int): Boolean =
    currentWave != lastWave

  private def updateWaveDisplay(waveNumber: Int): Unit =
    stateRef.get().waveText.foreach(_.text = s"$waveNumber")

  private def createStyledPanel(content: HBox): VBox =
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

  private lazy val panelStyle: String =
    """-fx-background-image: url('/shop_background.jpg');
       -fx-background-size: cover;
       -fx-background-repeat: no-repeat;
       -fx-background-position: center;
       -fx-background-radius: 20;
       -fx-border-radius: 20;
       -fx-border-color: #4B2E06;
       -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 8,0,2,2);"""

  private def updatePanelBackground(panel: VBox): Unit =
    panel.style = panelStyle

  def reset(): Unit =
    stateRef.updateAndGet(_.copy(lastWaveNumber = -1))
