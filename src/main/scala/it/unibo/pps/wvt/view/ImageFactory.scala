package it.unibo.pps.wvt.view

import scalafx.scene.image.{Image, ImageView}
import scala.collection.mutable

object ImageFactory {

  private val imageCache: mutable.Map[String, Image] = mutable.Map.empty

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
    imageCache.get(path) match
      case Some(cachedImage) => Some(cachedImage)
      case None =>
        Option(getClass.getResourceAsStream(path)).map { stream =>
          val image = new Image(stream)
          imageCache.update(path, image)
          image
        }

  def clearCache(): Unit =
    imageCache.clear()
}