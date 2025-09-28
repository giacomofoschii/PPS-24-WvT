package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.components.TrollType.*
import it.unibo.pps.wvt.ecs.core.*
import it.unibo.pps.wvt.utilities.Position

case class MovementSystem(
                           private val accumulatedMovement: Map[EntityId, Double] = Map.empty
                         ) extends System:

  private type MovementStrategy = (Position, Double, EntityId, World) => Option[Position]

  override def update(world: World): System =
    val movableEntities = world.getEntitiesWithTwoComponents[PositionComponent, MovementComponent]

    val updatedAccumulated = movableEntities.foldLeft(accumulatedMovement) { (acc, entity) =>
      world.getComponent[MovementComponent](entity) match
        case Some(movement) =>
          val accumulated = acc.getOrElse(entity, 0.0) + movement.speed
          if accumulated >= 1.0 then
            val steps = accumulated.toInt
            calculateNewPosition(entity, world, steps).foreach { newPos =>
              world.addComponent(entity, PositionComponent(newPos))
            }
            acc + (entity -> (accumulated - accumulated.toInt))
          else
            acc + (entity -> accumulated)
        case None => acc
    }

    val cleanedAccumulated = updatedAccumulated.filter { case (entityId, _) =>
      world.getAllEntities.contains(entityId)
    }

    copy(accumulatedMovement = cleanedAccumulated)

  private def calculateNewPosition(entity: EntityId, world: World, steps: Int = 1): Option[Position] =
    for
      position <- world.getComponent[PositionComponent](entity)
      strategy = selectMovementStrategy(entity, world)
      newPos <- strategy(position.position, steps.toDouble, entity, world)
      if isValidPosition(newPos, entity, world)
    yield newPos

  private def selectMovementStrategy(entity: EntityId, world: World): MovementStrategy =
    if world.getEntitiesByType("troll").contains(entity) then
      world.getComponent[TrollTypeComponent](entity)
        .map(trollMovementStrategy)
        .getOrElse(defaultMovementStrategy)
    else if world.getEntitiesByType("projectile").contains(entity) then
      world.getComponent[ProjectileTypeComponent](entity)
        .map(proj => if proj.projectileType == ProjectileType.Troll
        then straightLeftMovement
        else straightRightMovement)
        .getOrElse(defaultMovementStrategy)
    else
      defaultMovementStrategy

  private val trollMovementStrategy: TrollTypeComponent => MovementStrategy = trollType =>
    trollType.trollType match
      case Base | Warrior => straightLeftMovement
      case Assassin => zigzagMovement
      case Thrower => conditionalMovement(6)

  private val straightLeftMovement: MovementStrategy = (pos, speed, _, _) =>
    val steps = speed.toInt
    Option.when(pos.col > 0 && steps > 0)(
      Position(pos.row, (pos.col - steps).max(0))
    )

  private val straightRightMovement: MovementStrategy = (pos, speed, _, _) =>
    val steps = speed.toInt
    Option.when(pos.col < 8 && steps > 0)(
      Position(pos.row, (pos.col + steps).min(8))
    )

  private val zigzagMovement: MovementStrategy = (pos, speed, _, _) =>
    val steps = speed.toInt
    val zigzag = if pos.col % 2 == 0 then -1 else 1
    val newRow = (pos.row + zigzag).max(0).min(4)
    Option.when(pos.col > 0 && steps > 0)(
      Position(newRow, (pos.col - steps).max(0))
    )

  private def conditionalMovement(stopColumn: Int): MovementStrategy = (pos, speed, _, _) =>
    val steps = speed.toInt
    Option.when(pos.col > stopColumn && steps > 0)(
      Position(pos.row, (pos.col - steps).max(stopColumn))
    )

  private val defaultMovementStrategy: MovementStrategy = (_, _, _, _) => None

  private def isValidPosition(pos: Position, entity: EntityId, world: World): Boolean =
    pos.isValid && world.getEntityAt(pos).forall(_ == entity)