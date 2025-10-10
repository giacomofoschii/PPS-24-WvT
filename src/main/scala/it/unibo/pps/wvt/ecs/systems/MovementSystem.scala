package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.components.TrollType.*
import it.unibo.pps.wvt.ecs.components.ZigZagPhase.{OnAlternateRow, OnSpawnRow}
import it.unibo.pps.wvt.ecs.core.*
import it.unibo.pps.wvt.utilities.{GridMapper, Position}
import it.unibo.pps.wvt.utilities.ViewConstants.*

import scala.annotation.tailrec
import scala.util.Random

case class MovementSystem(
                           private val deltaTime: Double = 16.0,
                           private val zigZagPhaseDuration: Long = 5000L
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
      speedModifier = world.getComponent[FreezedComponent](entity).map(_.speedModifier).getOrElse(1.0)
      adjustedMoveComp = moveComp.copy(speed = moveComp.speed * speedModifier)
      strategy = selectMovementStrategy(entity, world)
      currentPos = posComp.position
      newPos = strategy(currentPos, adjustedMoveComp, entity, world, deltaTime / 1000.0)
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
      case Assassin => zigzagMovement
      case _ => linearLeftMovement

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
    world.getComponent[ZigZagStateComponent](entity) match
      case Some(state) =>
        val currentTime = System.currentTimeMillis()
        val updatedPos = calculateZigZagPosition(pos, movement.speed, dt, state, currentTime)

        if shouldChangePhase(currentTime, state.phaseStartTime) then
          updatedZigZagPhase(entity, state, world)

        updatedPos
      case None =>
        initializeZigzagComponent(entity, pos, world)
        pos

  private val defaultMovementStrategy: MovementStrategy = (pos, _, _, _, _) => pos

  private def calculateZigZagPosition(pos: Position,
                                      speed: Double,
                                      dt: Double,
                                      state: ZigZagStateComponent,
                                      currentTime: Long
                                     ): Position =
    val basePos = moveLeft(pos, speed, dt)

    state.currentPhase match
      case OnSpawnRow =>
        val targetRow = state.alternateRow
        transitionToRow(basePos, targetRow, currentTime, state.phaseStartTime)

      case OnAlternateRow =>
        val targetRow = state.spawnRow
        transitionToRow(basePos, targetRow, currentTime, state.phaseStartTime)

  private def moveLeft(pos: Position, speed: Double, dt: Double): Position =
    val pixelsPerSecond = speed * CELL_WIDTH
    Position(pos.x - pixelsPerSecond * dt, pos.y)

  private def transitionToRow(pos: Position,
                              targetRow: Int,
                              currentTime: Long,
                              phaseStartTime: Long
                             ): Position =
    val elapsed = currentTime - phaseStartTime
    val progress = (elapsed.toDouble / zigZagPhaseDuration).min(1.0)

    val currentY = pos.y
    val targetY = GRID_OFFSET_Y + targetRow * CELL_HEIGHT + CELL_HEIGHT / 2
    val newY = currentY + (targetY - currentY) * progress

    val minY = GRID_OFFSET_Y
    val maxY = GRID_OFFSET_Y + GRID_ROWS * CELL_HEIGHT - CELL_HEIGHT / 2

    Position(pos.x, newY.max(minY).min(maxY))

  private def shouldChangePhase(currentTime: Long, phaseStartTime: Long): Boolean =
    currentTime - phaseStartTime >= zigZagPhaseDuration

  private def updatedZigZagPhase(entity: EntityId,
                                 currentState: ZigZagStateComponent,
                                 world: World
                                ): Unit =
    val newPhase = currentState.currentPhase match
      case OnSpawnRow => OnAlternateRow
      case OnAlternateRow => OnSpawnRow

    val newState = currentState.copy(
      currentPhase = newPhase,
      phaseStartTime = System.currentTimeMillis()
    )

    world.updateComponent[ZigZagStateComponent](entity, _ => newState)

  private def initializeZigzagComponent(entity: EntityId,
                                        pos: Position,
                                        world: World
                                       ): Unit =
    for
      (row, _) <- GridMapper.physicalToLogical(pos)
      alternateRow = calculateAlternateRow(row)
    do
      val zigzagState = ZigZagStateComponent(
        spawnRow = row,
        currentPhase = OnSpawnRow,
        phaseStartTime = System.currentTimeMillis(),
        alternateRow = alternateRow
      )
      world.addComponent(entity, zigzagState)

  private def calculateAlternateRow(spawnRow: Int): Int =
    spawnRow match
      case 0 => 1
      case r if r == GRID_ROWS - 1 => r -1
      case r => if Random.nextBoolean() then r + 1 else r - 1

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