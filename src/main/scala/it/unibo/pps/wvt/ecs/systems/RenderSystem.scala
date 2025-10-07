package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.utilities.*
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.core.*
import it.unibo.pps.wvt.view.GameView
import scala.annotation.tailrec

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
      GameView.renderEntities(entities)
      GameView.renderHealthBars(healthBars)
      lastRenderedState = Some(currentState)

    copy(healthBarSystem = updatedHealthBars)

  private def shouldRender(currentState: String): Boolean =
    !lastRenderedState.contains(currentState)

  private def generateStateHash(
                                 entities: Seq[(Position, String)],
                                 healthBars: Seq[(Position, Double, scalafx.scene.paint.Color, Double, Double, Double)]
                               ): String =
    val entitiesHash = entities.map { case (Position(x, y), path) => s"$x,$y,$path" }.mkString(";")
    val healthHash = healthBars.map { case (Position(x, y), p, _, _, _, _) => s"$x,$y,$p" }.mkString(";")
    s"$entitiesHash|$healthHash"

  private def collectEntitiesWithImages(world: World): Seq[(Position, String)] =
    @tailrec
    def collectEntities(entities: List[EntityId], acc: List[(Position, String)]): List[(Position, String)] =
      entities match
        case Nil => acc.reverse
        case head :: tail =>
          val entityData = for
            pos <- world.getComponent[PositionComponent](head)
            img <- world.getComponent[ImageComponent](head)
          yield
            (pos.position, img.imagePath)

          collectEntities(tail, entityData.fold(acc)(acc :+ _))

    collectEntities(world.getEntitiesWithComponent[ImageComponent].toList, List.empty)

  def forceRender(): Unit =
    lastRenderedState = None

  def clearCache(): Unit =
    lastRenderedState = None