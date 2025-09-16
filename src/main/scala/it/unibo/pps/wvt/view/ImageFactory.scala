package it.unibo.pps.wvt.view

import scalafx.scene.image.{Image, ImageView}

object ImageFactory {
  def createBackgroundView(path: String, factor: Double): Option[ImageView] =
    loadImage(path).map(myImage => new ImageView(myImage) {
      fitWidth = myImage.width.value * factor
      fitHeight = myImage.height.value * factor
      preserveRatio = true
    })

  def createImageView(imagePath: String, width: Int): Either[String, ImageView] =
    for
      image <- loadImage(imagePath).toRight(s"Error to load image at path: $imagePath")
    yield new ImageView(image) {
      fitWidth = width
      preserveRatio = true
    }

  private def loadImage(path: String): Option[Image] =
    Option(getClass.getResourceAsStream(path)).map(new Image(_))

}
