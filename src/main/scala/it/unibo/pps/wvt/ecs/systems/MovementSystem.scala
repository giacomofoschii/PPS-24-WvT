package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.components.TrollType.*
import it.unibo.pps.wvt.ecs.core.*
import it.unibo.pps.wvt.utilities.Position

class MovementSystem extends System {

  private type MovementStrategy = (Position, Double, EntityId, World) => Option[Position]

  override def update(world: World): Unit =
    val movableEntities = world.getEntitiesWithTwoComponents[PositionComponent, MovementComponent]

    movableEntities
      .map(entity => (entity, calculateNewPosition(entity, world)))
      .collect { case (entity, Some(newPos)) => (entity, newPos) }
      .foreach { case (entity, newPos) =>
        world.addComponent(entity, PositionComponent(newPos))
      }

  private def calculateNewPosition(entity: EntityId, world: World): Option[Position] =
    for
      position <- world.getComponent[PositionComponent](entity)
      movement <- world.getComponent[MovementComponent](entity)
      strategy = selectMovementStrategy(entity, world)
      newPos <- strategy(position.position, movement.speed, entity, world)
      if isValidPosition(newPos, entity, world)
    yield newPos

  private def selectMovementStrategy(entity: EntityId, world: World): MovementStrategy =
    world.getComponent[TrollTypeComponent](entity)
      .map(trollMovementStrategy)
      .getOrElse(defaultMovementStrategy)

  private val trollMovementStrategy: TrollTypeComponent => MovementStrategy = trollType =>
    trollType.trollType match
      case Base | Warrior => straightMovement
      case Assassin => zigzagMovement
      case Thrower => conditionalMovement(6)

  private val straightMovement: MovementStrategy = (pos, speed, _, _) =>
    Option.when(pos.col > 0 && speed > 0)(
      Position(pos.row, (pos.col - math.ceil(speed).toInt).max(0))
    )

  private val zigzagMovement: MovementStrategy = (pos, speed, _, _) =>
    val zigzag = if (pos.col % 2 == 0) -1 else 1
    val newRow = (pos.row + zigzag).max(0).min(4)
    Option.when(pos.col > 0 && speed > 0)(
      Position(newRow, (pos.col - math.ceil(speed).toInt).max(0))
    )
    
  private def conditionalMovement(stopColumn: Int): MovementStrategy = (pos, speed, _, _) =>
    Option.when(pos.col > stopColumn && speed > 0)(
      Position(pos.row, (pos.col - math.ceil(speed).toInt).max(stopColumn))
    )
    
  private val defaultMovementStrategy: MovementStrategy = (_, _, _, _) => None
  
  private def isValidPosition(pos: Position, entity: EntityId, world: World): Boolean =
    pos.isValid && world.getEntityAt(pos).forall(_ == entity)

}