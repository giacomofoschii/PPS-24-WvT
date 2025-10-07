package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.components.TrollType.*
import it.unibo.pps.wvt.ecs.core.*
import it.unibo.pps.wvt.utilities.Position
import it.unibo.pps.wvt.utilities.ViewConstants.*
import scala.annotation.tailrec

case class MovementSystem(
                           private val deltaTime: Double = 16.0
                         ) extends System:

  private type MovementStrategy = (Position, MovementComponent, EntityId, World, Double) => Position

  override def update(world: World): System =
    val movableEntities = world.getEntitiesWithTwoComponents[PositionComponent, MovementComponent]

    @tailrec
    def processMovements(entities: List[EntityId]): Unit =
      entities match
        case Nil => ()
        case head :: tail =>
          calculateNewPosition(head, world).foreach: newPos =>
            world.addComponent(head, PositionComponent(newPos))
          processMovements(tail)

    processMovements(movableEntities.toList)
    removeOutOfBoundsProjectiles(world)
    this

  private def removeOutOfBoundsProjectiles(world: World): Unit =
    @tailrec
    def checkAndRemove(projectiles: List[EntityId]): Unit =
      projectiles match
        case Nil => ()
        case head :: tail =>
          world.getComponent[PositionComponent](head).foreach: posComp =>
            val pos = posComp.position
            val isTrollProjectile = world.getComponent[ProjectileTypeComponent](head)
              .exists(_.projectileType == ProjectileType.Troll)

            val shouldRemove = if isTrollProjectile then
              val minX = GRID_OFFSET_X
              pos.x < minX
            else
              val maxX = GRID_OFFSET_X + GRID_COLS * CELL_WIDTH
              pos.x > maxX

            if shouldRemove then
              world.destroyEntity(head)

          checkAndRemove(tail)

    val projectiles = world.getEntitiesByType("projectile").toList
    checkAndRemove(projectiles)

  private def calculateNewPosition(entity: EntityId, world: World): Option[Position] =
    for
      posComp <- world.getComponent[PositionComponent](entity)
      moveComp <- world.getComponent[MovementComponent](entity)
      if !world.hasComponent[BlockedComponent](entity)
      strategy = selectMovementStrategy(entity, world)
      currentPos = posComp.position
      newPos = strategy(currentPos, moveComp, entity, world, deltaTime / 1000.0)
    yield newPos

  private def selectMovementStrategy(entity: EntityId, world: World): MovementStrategy =
    if world.getEntitiesByType("troll").contains(entity) then
      world.getComponent[TrollTypeComponent](entity)
        .map(trollMovementStrategy)
        .getOrElse(defaultMovementStrategy)
    else if world.getEntitiesByType("projectile").contains(entity) then
      world.getComponent[ProjectileTypeComponent](entity)
        .map(proj => if proj.projectileType == ProjectileType.Troll
        then linearLeftMovement
        else projectileRightMovement)
        .getOrElse(defaultMovementStrategy)
    else
      defaultMovementStrategy

  private val trollMovementStrategy: TrollTypeComponent => MovementStrategy = trollType =>
    trollType.trollType match
      case Base | Warrior => linearLeftMovement
      case Assassin => zigzagMovement
      case Thrower => conditionalMovement(GRID_OFFSET_X + CELL_WIDTH * 6)

  private val linearLeftMovement: MovementStrategy = (pos, movement, _, _, dt) =>
    val pixelsPerSecond = movement.speed * CELL_WIDTH
    val minY = GRID_OFFSET_Y
    val maxY = GRID_OFFSET_Y + GRID_ROWS * CELL_HEIGHT - CELL_HEIGHT / 2
    Position(
      pos.x - pixelsPerSecond * dt,
      pos.y
    )

  private val projectileRightMovement: MovementStrategy = (pos, movement, _, _, dt) =>
    val pixelsPerSecond = movement.speed * CELL_WIDTH * 2
    val minY = GRID_OFFSET_Y
    val maxY = GRID_OFFSET_Y + GRID_ROWS * CELL_HEIGHT - CELL_HEIGHT / 2
    Position(
      pos.x + pixelsPerSecond * dt,
      pos.y.max(minY).min(maxY)
    )

  private val zigzagMovement: MovementStrategy = (pos, movement, entity, world, dt) =>
    val pixelsPerSecond = movement.speed * CELL_WIDTH
    val time = System.currentTimeMillis() / 1000.0
    val zigzagAmplitude = CELL_HEIGHT * 0.3
    val zigzagFrequency = 2.0

    val newY = pos.y + math.sin(time * zigzagFrequency) * zigzagAmplitude * dt
    val minY = GRID_OFFSET_Y
    val maxY = GRID_OFFSET_Y + GRID_ROWS * CELL_HEIGHT - CELL_HEIGHT / 2

    Position(
      pos.x - pixelsPerSecond * dt,
      newY.max(minY).min(maxY)
    )

  private def conditionalMovement(stopX: Double): MovementStrategy = (pos, movement, _, _, dt) =>
    if pos.x > stopX then
      val pixelsPerSecond = movement.speed * CELL_WIDTH
      Position(
        (pos.x - pixelsPerSecond * dt).max(stopX),
        pos.y
      )
    else pos

  private val defaultMovementStrategy: MovementStrategy = (pos, _, _, _, _) => pos

  private def canMoveToPosition(pos: Position, entity: EntityId, world: World): Boolean =
    if !pos.isValid then false
    else
      world.getEntityAt(pos) match
        case None => true
        case Some(other) if other == entity => true
        case Some(other) =>
          canEntitiesOverlap(
            world.getEntityType(entity),
            world.getEntityType(other)
          )

  private def canEntitiesOverlap(type1: Option[EntityTypeComponent], type2: Option[EntityTypeComponent]): Boolean =
    (type1, type2) match
      case (Some(_: ProjectileTypeComponent), _) => true
      case (_, Some(_: ProjectileTypeComponent)) => true
      case (Some(_: TrollTypeComponent), Some(_: TrollTypeComponent)) => true
      case _ => false