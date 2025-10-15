package it.unibo.pps.wvt.view

import scalafx.scene.Parent
import scalafx.scene.layout.*
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.image.ImageView
import it.unibo.pps.wvt.view.ButtonFactory._
import it.unibo.pps.wvt.view.ButtonFactory.Presets._
import it.unibo.pps.wvt.view.ImageFactory._
import it.unibo.pps.wvt.utilities.ViewConstants._

object GameResultPanel:

  sealed trait ResultType:
    def titleImagePath: String
    def continueButtonText: String
    def continueAction: ButtonAction

  case object Victory extends ResultType:
    val titleImagePath               = "/victory.png"
    val continueButtonText           = "Next wave"
    val continueAction: ButtonAction = ContinueBattle

  case object Defeat extends ResultType:
    val titleImagePath               = "/defeat.png"
    val continueButtonText           = "New game"
    val continueAction: ButtonAction = NewGame

  def apply(resultType: ResultType): Parent =
    createPanel(resultType)

  private val createPanel: ResultType => Parent = resultType =>
    lazy val backgroundImage = createBackgroundView("/in_game_menu.jpg", IN_GAME_MENU_SCALE_FACTOR)
      .getOrElse(new ImageView())

    lazy val titleImage = createImageView(
      resultType.titleImagePath,
      (backgroundImage.fitWidth * IN_GAME_TITLE_SCALE_FACTOR).toInt
    ).fold(
      _ => new ImageView(),
      identity
    )

    val menuLayout = createMenuLayout(titleImage, resultType)

    new StackPane:
      children = Seq(backgroundImage, menuLayout)

  private def createMenuLayout(titleImg: ImageView, resultType: ResultType): BorderPane =
    val continueButton = createStyledButton(inGameButtonPreset(resultType.continueButtonText))(
      handleAction(resultType.continueAction)
    )
    val exitButton = createStyledButton(inGameButtonPreset("Exit"))(handleAction(ExitGame))

    new BorderPane:
      top = new VBox:
        alignment = Pos.Center
        children = titleImg
      center = new HBox:
        alignment = Pos.Center
        spacing = PAUSE_BUTTON_SPICING
        padding = Insets(PADDING_MENU)
        children = Seq(continueButton, exitButton)
