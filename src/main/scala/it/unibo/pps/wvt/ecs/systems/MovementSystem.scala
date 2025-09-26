package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.components.TrollType.*
import it.unibo.pps.wvt.ecs.core.*
import it.unibo.pps.wvt.utilities.Position

case class MovementSystem() extends System:

  private type MovementStrategy = (Position, Double, EntityId, World) => Option[Position]

  override def update(world: World): System =
    val movableEntities = world.getEntitiesWithTwoComponents[PositionComponent, MovementComponent]

    movableEntities
      .map(entity => (entity, calculateNewPosition(entity, world)))
      .collect { case (entity, Some(newPos)) => (entity, newPos) }
      .foreach: (entity, newPos) =>
        world.addComponent(entity, PositionComponent(newPos))
    this

  private def calculateNewPosition(entity: EntityId, world: World): Option[Position] =
    for
      position <- world.getComponent[PositionComponent](entity)
      movement <- world.getComponent[MovementComponent](entity)
      strategy = selectMovementStrategy(entity, world)
      newPos <- strategy(position.position, movement.speed, entity, world)
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
        then straightRightMovement
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
    Option.when(pos.col > 0 && speed > 0)(
      Position(pos.row, (pos.col - math.ceil(speed).toInt).max(0))
    )

  private val straightRightMovement: MovementStrategy = (pos, speed, _, _) =>
    Option.when(pos.col < 8 && speed > 0)(
      Position(pos.row, (pos.col + math.ceil(speed).toInt).min(8))
    )

  private val zigzagMovement: MovementStrategy = (pos, speed, _, _) =>
    val zigzag = if pos.col % 2 == 0 then -1 else 1
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