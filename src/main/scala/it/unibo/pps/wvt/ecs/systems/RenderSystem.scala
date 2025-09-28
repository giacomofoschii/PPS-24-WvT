package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.utilities.*
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.core.*
import it.unibo.pps.wvt.view.GameView

case class RenderSystem(
                         private val healthBarSystem: HealthBarRenderSystem = HealthBarRenderSystem()
                       ) extends System:

  private var lastRenderedState: Option[String] = None

  override def update(world: World): System =
    val updatedHealthBars = healthBarSystem.update(world).asInstanceOf[HealthBarRenderSystem]
    val entities = collectEntitiesWithImages(world)
    val healthBars = updatedHealthBars.getHealthBarsToRender
    val currentState = generateStateHash(entities, healthBars)

    if shouldRender(currentState) then
      GameView.clearGrid()
      GameView.renderEntities(entities)
      GameView.renderHealthBars(healthBars)
      lastRenderedState = Some(currentState)
      copy(healthBarSystem = updatedHealthBars)
    else
      copy(healthBarSystem = updatedHealthBars)

  private def shouldRender(currentState: String): Boolean =
    !lastRenderedState.contains(currentState)

  private def generateStateHash(
                                 entities: Seq[(GridMapper.PhysicalCoords, String)],
                                 healthBars: Seq[(GridMapper.PhysicalCoords, Double, scalafx.scene.paint.Color, Double, Double, Double)]
                               ): String =
    val entitiesHash = entities.map { case ((x, y), path) => s"$x,$y,$path" }.mkString(";")
    val healthHash = healthBars.map { case ((x, y), p, _, _, _, _) => s"$x,$y,$p" }.mkString(";")
    s"$entitiesHash|$healthHash"

  private def collectEntitiesWithImages(world: World): Seq[(GridMapper.PhysicalCoords, String)] =
    val entitiesImages = world.getEntitiesWithComponent[ImageComponent].flatMap: entity =>
      for
        pos <- world.getComponent[PositionComponent](entity)
        img <- world.getComponent[ImageComponent](entity)
      yield (GridMapper.logicalToPhysical(pos.position), img.imagePath)
    entitiesImages.toSeq

  def forceRender(): Unit =
    lastRenderedState = None

  def clearCache(): Unit =
    lastRenderedState = None