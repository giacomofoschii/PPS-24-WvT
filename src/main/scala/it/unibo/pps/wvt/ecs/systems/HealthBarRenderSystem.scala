// src/main/scala/it/unibo/pps/wvt/ecs/systems/HealthBarRenderSystem.scala
package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.*
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.utilities.*
import scalafx.scene.paint.Color

type HealthBarData = (EntityId, Position, Double, Color, HealthBarComponent)
type RenderableHealthBar = (GridMapper.PhysicalCoords, Double, Color, Double, Double, Double)

case class HealthBarRenderSystem(
                                  private val healthBarCache: Map[EntityId, RenderableHealthBar] = Map.empty
                                ) extends System:

  override def update(world: World): System =
    collectHealthBarData(world)
      .map(calculateHealthBarRendering)
      .map(filterVisibleBars)
      .fold(this)(renderBars => copy(healthBarCache = renderBars))

  private def collectHealthBarData(world: World): Option[List[HealthBarData]] =
    val entities = for
      entity <- world.getEntitiesWithComponent[HealthComponent]
      health <- world.getComponent[HealthComponent](entity)
      pos <- world.getComponent[PositionComponent](entity)
      healthBar <- world.getComponent[HealthBarComponent](entity).orElse(Some(createDefaultHealthBar(entity, world)))
      percentage = health.currentHealth.toDouble / health.maxHealth.toDouble
      updatedBar = healthBar.updateColorByHealthPercentage(percentage)
    yield (entity, pos.position, percentage, updatedBar.barColor, updatedBar)

    if entities.nonEmpty then Some(entities.toList) else None

  private def createDefaultHealthBar(entity: EntityId, world: World): HealthBarComponent =
    world.getComponent[WizardTypeComponent](entity)
      .map(_ => HealthBarComponent(barColor = Color.Blue))
      .orElse(world.getComponent[TrollTypeComponent](entity)
        .map(_ => HealthBarComponent(barColor = Color.Red)))
      .getOrElse(HealthBarComponent())

  private def calculateHealthBarRendering(data: List[HealthBarData]): Map[EntityId, RenderableHealthBar] =
    data.map { case (entityId, position, percentage, color, barComponent) =>
      val (x, y) = GridMapper.logicalToPhysical(position)
      entityId -> (
        (x, y),
        percentage,
        color,
        barComponent.barWidth,
        barComponent.barHeight,
        barComponent.offsetY
      )
    }.toMap

  private def filterVisibleBars(bars: Map[EntityId, RenderableHealthBar]): Map[EntityId, RenderableHealthBar] =
    bars.filter { case (_, (_, percentage, _, _, _, _)) => percentage < 1.0 && percentage > 0 }

  def getHealthBarsToRender: Seq[RenderableHealthBar] = healthBarCache.values.toSeq

  def getAverageHealth(world: World, entityType: String): Option[Double] =
    val healthPercentages = entityType match
      case "wizard" => getHealthPercentagesForType[WizardTypeComponent](world)
      case "troll" => getHealthPercentagesForType[TrollTypeComponent](world)
      case _ => Seq.empty

    if healthPercentages.nonEmpty then
      Some(healthPercentages.foldLeft(0.0)(_ + _) / healthPercentages.length)
    else None

  private def getHealthPercentagesForType[T <: EntityTypeComponent : scala.reflect.ClassTag](world: World): Seq[Double] =
    world.getEntitiesWithComponent[T].flatMap { entity =>
      world.getComponent[HealthComponent](entity).map { health =>
        health.currentHealth.toDouble / health.maxHealth.toDouble
      }
    }.toSeq