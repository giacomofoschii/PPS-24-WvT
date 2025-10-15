package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.*
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.utilities.*

import scalafx.scene.paint.Color

import scala.annotation.tailrec

type HealthBarData       = (EntityId, Position, Double, Color, HealthBarComponent)
type RenderableHealthBar = (Position, Double, Color, Double, Double, Double)

/** System responsible for managing and rendering health bars for entities with health.
  * It collects health data, calculates rendering parameters, and filters visible health bars.
  *
  * @param healthBarCache A cache mapping entity IDs to their corresponding renderable health bar data.
  */
case class HealthBarRenderSystem(
    private val healthBarCache: Map[EntityId, RenderableHealthBar] = Map.empty
) extends System:

  override def update(world: World): (World, System) =
    collectHealthBarData(world)
      .map(calculateHealthBarRendering)
      .map(filterVisibleBars)
      .fold((world, this)): renderBars =>
        (world, copy(healthBarCache = renderBars))

  /** Collects health bar data for all entities with a HealthComponent in the world.
    *
    * @param world The game world containing entities and their components.
    * @return An optional list of HealthBarData tuples, each containing:
    *         - EntityId: The ID of the entity.
    *         - Position: The position of the entity.
    *         - Double: The health percentage (currentHealth / maxHealth).
    *         - Color: The color of the health bar based on health percentage.
    *         - HealthBarComponent: The health bar component with its properties.
    */
  private def collectHealthBarData(world: World): Option[List[HealthBarData]] =
    @tailrec
    def collectBars(entities: List[EntityId], acc: List[HealthBarData]): List[HealthBarData] =
      entities match
        case Nil => acc.reverse
        case head :: tail =>
          val barData =
            for
              health <- world.getComponent[HealthComponent](head)
              pos    <- world.getComponent[PositionComponent](head)
              healthBar <-
                world.getComponent[HealthBarComponent](head).orElse(Some(createDefaultHealthBar(head, world)))
              percentage = health.currentHealth.toDouble / health.maxHealth.toDouble
              updatedBar = healthBar.updateColorByHealthPercentage(percentage)
            yield (head, pos.position, percentage, updatedBar.barColor, updatedBar)

          collectBars(tail, barData.fold(acc)(acc :+ _))

    val entities = world.getEntitiesWithComponent[HealthComponent].toList
    if entities.nonEmpty then Some(collectBars(entities, List.empty)) else None

  /** Creates a default HealthBarComponent based on the entity type.
    * Wizards get blue health bars, trolls get red health bars, and others get default settings.
    *
    * @param entity The ID of the entity.
    * @param world  The game world containing entities and their components.
    * @return A HealthBarComponent with default settings based on entity type.
    */
  private def createDefaultHealthBar(entity: EntityId, world: World): HealthBarComponent =
    world.getComponent[WizardTypeComponent](entity)
      .map(_ => HealthBarComponent(barColor = Color.Blue))
      .orElse(world.getComponent[TrollTypeComponent](entity)
        .map(_ => HealthBarComponent(barColor = Color.Red)))
      .getOrElse(HealthBarComponent())

  /** Calculates rendering parameters for health bars based on collected data.
    *
    * @param data A list of HealthBarData tuples.
    * @return A map of EntityId to RenderableHealthBar tuples.
    */
  private def calculateHealthBarRendering(data: List[HealthBarData]): Map[EntityId, RenderableHealthBar] =
    @tailrec
    def buildMap(
        remaining: List[HealthBarData],
        acc: Map[EntityId, RenderableHealthBar]
    ): Map[EntityId, RenderableHealthBar] =
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

  /** Filters out health bars that are either full (100%) or empty (0%).
    *
    * @param bars A map of EntityId to RenderableHealthBar tuples.
    * @return A filtered map containing only visible health bars.
    */
  private def filterVisibleBars(bars: Map[EntityId, RenderableHealthBar]): Map[EntityId, RenderableHealthBar] =
    bars.filter { case (_, (_, percentage, _, _, _, _)) => percentage < 1.0 && percentage > 0.0 }

  /** Retrieves the current health bars to be rendered.
    *
    * @return A sequence of RenderableHealthBar tuples.
    */
  def getHealthBarsToRender: Seq[RenderableHealthBar] = healthBarCache.values.toSeq

  /** Calculates the average health percentage for a specified entity type.
    *
    * @param world      The game world containing entities and their components.
    * @param entityType The type of entity ("wizard" or "troll").
    * @return An Option containing the average health percentage, or None if no entities of that type exist.
    */
  def getAverageHealth(world: World, entityType: String): Option[Double] =
    val healthPercentages = entityType match
      case "wizard" => getHealthPercentagesForType[WizardTypeComponent](world)
      case "troll"  => getHealthPercentagesForType[TrollTypeComponent](world)
      case _        => Seq.empty

    if healthPercentages.nonEmpty then
      Some(healthPercentages.foldLeft(0.0)(_ + _) / healthPercentages.length)
    else None

  /** Helper method to get health percentages for entities with a specific type component.
    *
    * @param world The game world containing entities and their components.
    * @tparam T The type of entity component (e.g., WizardTypeComponent or TrollTypeComponent).
    * @return A sequence of health percentages for the specified entity type.
    */
  private def getHealthPercentagesForType[T <: EntityTypeComponent: scala.reflect.ClassTag](world: World): Seq[Double] =
    @tailrec
    def collectPercentages(entities: List[EntityId], acc: List[Double]): List[Double] =
      entities match
        case Nil => acc.reverse
        case head :: tail =>
          val percentage = world.getComponent[HealthComponent](head).map: health =>
            health.currentHealth.toDouble / health.maxHealth.toDouble
          collectPercentages(tail, percentage.fold(acc)(acc :+ _))

    collectPercentages(world.getEntitiesWithComponent[T].toList, List.empty)
