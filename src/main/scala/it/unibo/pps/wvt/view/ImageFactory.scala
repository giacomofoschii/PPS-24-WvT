package it.unibo.pps.wvt.view

import scalafx.scene.image.{Image, ImageView}
import scala.collection.mutable
import java.io.InputStream

/** Factory object for creating and caching images and image views. */
object ImageFactory:

  private val imageCache: mutable.Map[String, Image] = mutable.Map.empty

  def createBackgroundView(path: String, factor: Double): Option[ImageView] =
    loadImage(path).map: image =>
      createScaledImageView(image, factor)

  def createImageView(imagePath: String, width: Int): Either[String, ImageView] =
    loadImage(imagePath)
      .toRight(s"Error loading image at path: $imagePath")
      .map: image =>
        createFixedWidthImageView(image, width)

  def clearCache(): Unit =
    imageCache.clear()

  private def loadImage(path: String): Option[Image] =
    imageCache.get(path).orElse(loadAndCacheImage(path))

  private def loadAndCacheImage(path: String): Option[Image] =
    Option(getClass.getResourceAsStream(path))
      .map(createImageFromStream)
      .map(cacheImage(path))

  private def createImageFromStream(stream: InputStream): Image =
    new Image(stream)

  private def cacheImage(path: String)(image: Image): Image =
    imageCache.update(path, image)
    image

  private def createScaledImageView(myImage: Image, factor: Double): ImageView =
    new ImageView(myImage):
      fitWidth = myImage.width.value * factor
      fitHeight = myImage.height.value * factor
      preserveRatio = true

  private def createFixedWidthImageView(image: Image, width: Int): ImageView =
    new ImageView(image):
      fitWidth = width
      preserveRatio = true

  /** Object to provide statistics about the image cache.
    * Includes methods to get the number of cached images, check if an image is cached,
    * list cached image paths, and estimate memory usage.
    */
  object CacheStats:
    def size: Int = imageCache.size

    def contains(path: String): Boolean = imageCache.contains(path)

    def cachedPaths: Seq[String] = imageCache.keys.toSeq

    def memoryEstimate: String =
      val totalPixels = imageCache.values.map: img =>
        (img.width.value * img.height.value).toLong
      .sum

      val mbEstimate = (totalPixels * 4) / (1024 * 1024) // (4 bytes/pixel)
      s"~${mbEstimate}MB"
