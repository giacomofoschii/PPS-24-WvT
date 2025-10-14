package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.*
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.utilities.*

import scalafx.scene.paint.Color

import scala.annotation.tailrec

type HealthBarData = (EntityId, Position, Double, Color, HealthBarComponent)
type RenderableHealthBar = (Position, Double, Color, Double, Double, Double)

case class HealthBarRenderSystem(
                                  private val healthBarCache: Map[EntityId, RenderableHealthBar] = Map.empty
                                ) extends System:

  override def update(world: World): (World, System) =
    collectHealthBarData(world)
      .map(calculateHealthBarRendering)
      .map(filterVisibleBars)
      .fold((world, this)): renderBars =>
        (world, copy(healthBarCache = renderBars))

  private def collectHealthBarData(world: World): Option[List[HealthBarData]] =
    @tailrec
    def collectBars(entities: List[EntityId], acc: List[HealthBarData]): List[HealthBarData] =
      entities match
        case Nil => acc.reverse
        case head :: tail =>
          val barData = for
            health <- world.getComponent[HealthComponent](head)
            pos <- world.getComponent[PositionComponent](head)
            healthBar <- world.getComponent[HealthBarComponent](head).orElse(Some(createDefaultHealthBar(head, world)))
            percentage = health.currentHealth.toDouble / health.maxHealth.toDouble
            updatedBar = healthBar.updateColorByHealthPercentage(percentage)
          yield (head, pos.position, percentage, updatedBar.barColor, updatedBar)

          collectBars(tail, barData.fold(acc)(acc :+ _))

    val entities = world.getEntitiesWithComponent[HealthComponent].toList
    if entities.nonEmpty then Some(collectBars(entities, List.empty)) else None

  private def createDefaultHealthBar(entity: EntityId, world: World): HealthBarComponent =
    world.getComponent[WizardTypeComponent](entity)
      .map(_ => HealthBarComponent(barColor = Color.Blue))
      .orElse(world.getComponent[TrollTypeComponent](entity)
        .map(_ => HealthBarComponent(barColor = Color.Red)))
      .getOrElse(HealthBarComponent())

  private def calculateHealthBarRendering(data: List[HealthBarData]): Map[EntityId, RenderableHealthBar] =
    @tailrec
    def buildMap(remaining: List[HealthBarData],
                 acc: Map[EntityId, RenderableHealthBar]): Map[EntityId, RenderableHealthBar] =
      remaining match
        case Nil => acc
        case (entityId, position, percentage, color, barComponent) :: tail =>
          val (centerX, centerY) = (position.x, position.y)

          val renderData = (
            Position(centerX, centerY),
            percentage,
            color,
            barComponent.barWidth,
            barComponent.barHeight,
            barComponent.offsetY
          )
          buildMap(tail, acc + (entityId -> renderData))

    buildMap(data, Map.empty)

  private def filterVisibleBars(bars: Map[EntityId, RenderableHealthBar]): Map[EntityId, RenderableHealthBar] =
    bars.filter { case (_, (_, percentage, _, _, _, _)) => percentage < 1.0 && percentage > 0.0 }

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
    @tailrec
    def collectPercentages(entities: List[EntityId], acc: List[Double]): List[Double] =
      entities match
        case Nil => acc.reverse
        case head :: tail =>
          val percentage = world.getComponent[HealthComponent](head).map: health =>
            health.currentHealth.toDouble / health.maxHealth.toDouble
          collectPercentages(tail, percentage.fold(acc)(acc :+ _))

    collectPercentages(world.getEntitiesWithComponent[T].toList, List.empty)
