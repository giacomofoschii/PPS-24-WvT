package it.unibo.pps.wvt.view

import it.unibo.pps.wvt.utilities.ViewConstants._
import scalafx.scene.Parent
import scalafx.scene.image.ImageView
import it.unibo.pps.wvt.view.ButtonFactory.*
import it.unibo.pps.wvt.view.ImageFactory.*
import scalafx.scene.layout.StackPane

object GameView {
  def apply(): Parent = {
    lazy val backgroundImage = 
      createBackgroundView("/background_grid.png", GAME_MAP_SCALE_FACTOR).getOrElse(new ImageView())

    new StackPane {
      children = Seq(backgroundImage)
    }
  }
}