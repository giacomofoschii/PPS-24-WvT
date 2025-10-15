package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.utilities.*
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.core.*
import it.unibo.pps.wvt.view.GameView
import scalafx.scene.paint.Color

import scala.annotation.tailrec

/** System responsible for rendering entities with images and health bars.
  *
  * @param healthBarSystem the health bar rendering system
  * @param lastRenderedState the last rendered state hash to avoid redundant rendering
  */
case class RenderSystem(
    private val healthBarSystem: HealthBarRenderSystem = HealthBarRenderSystem(),
    private val lastRenderedState: Option[String] = None
) extends System:

  override def update(world: World): (World, System) =
    val (world1, updatedHealthBars) = healthBarSystem.update(world)
    val healthBarSystemTyped        = updatedHealthBars.asInstanceOf[HealthBarRenderSystem]

    val entities   = collectEntitiesWithImages(world1)
    val healthBars = healthBarSystemTyped.getHealthBarsToRender

    val currentState = generateStateHash(entities, healthBars)
    if shouldRender(currentState) then
      GameView.renderEntities(entities)
      GameView.renderHealthBars(healthBars)
      (
        world1,
        copy(
          healthBarSystem = healthBarSystemTyped,
          lastRenderedState = Some(currentState)
        )
      )
    else
      (world1, copy(healthBarSystem = healthBarSystemTyped))

  private def shouldRender(currentState: String): Boolean =
    !lastRenderedState.contains(currentState)

  private def generateStateHash(
      entities: Seq[(Position, String)],
      healthBars: Seq[(Position, Double, Color, Double, Double, Double)]
  ): String =
    val entitiesHash = entities.map { case (Position(x, y), path) => s"$x,$y,$path" }.mkString(";")
    val healthHash   = healthBars.map { case (Position(x, y), p, _, _, _, _) => s"$x,$y,$p" }.mkString(";")
    s"$entitiesHash|$healthHash"

  private def collectEntitiesWithImages(world: World): Seq[(Position, String)] =
    @tailrec
    def collectEntities(entities: List[EntityId], acc: List[(Position, String)]): List[(Position, String)] =
      entities match
        case Nil => acc.reverse
        case head :: tail =>
          val entityData =
            for
              pos <- world.getComponent[PositionComponent](head)
              img <- world.getComponent[ImageComponent](head)
              prefix = if world.hasComponent[FreezedComponent](head) then "/freezed" else ""
            yield (pos.position, prefix + img.imagePath)

          collectEntities(tail, entityData.fold(acc)(acc :+ _))

    collectEntities(world.getEntitiesWithComponent[ImageComponent].toList, List.empty)

  def forceRender(): RenderSystem =
    copy(lastRenderedState = None)

  def clearCache(): RenderSystem =
    copy(
      healthBarSystem = HealthBarRenderSystem(),
      lastRenderedState = None
    )
