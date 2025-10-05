package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.components.TrollType.*
import it.unibo.pps.wvt.ecs.core.*
import it.unibo.pps.wvt.utilities.PixelPosition
import it.unibo.pps.wvt.utilities.ViewConstants.*
import scala.annotation.tailrec

case class MovementSystem(
                           private val deltaTime: Double = 16.0
                         ) extends System:

  private type MovementStrategy = (PixelPosition, MovementComponent, EntityId, World, Double) => PixelPosition

  override def update(world: World): System =
    val movableEntities = world.getEntitiesWithTwoComponents[PositionComponent, MovementComponent]

    @tailrec
    def processMovements(entities: List[EntityId]): Unit =
      entities match
        case Nil => ()
        case head :: tail =>
          calculateNewPixelPosition(head, world).foreach: newPixelPos =>
            world.addComponent(head, PositionComponent(newPixelPos))
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
            val pixelPos = posComp.position.toPixel
            val isTrollProjectile = world.getComponent[ProjectileTypeComponent](head)
              .exists(_.projectileType == ProjectileType.Troll)

            val shouldRemove = if isTrollProjectile then
              val minX = GRID_OFFSET_X
              pixelPos.x < minX
            else
              val maxX = GRID_OFFSET_X + GRID_COLS * CELL_WIDTH
              pixelPos.x > maxX

            if shouldRemove then
              world.destroyEntity(head)

          checkAndRemove(tail)

    val projectiles = world.getEntitiesByType("projectile").toList
    checkAndRemove(projectiles)

  private def calculateNewPixelPosition(entity: EntityId, world: World): Option[PixelPosition] =
    for
      posComp <- world.getComponent[PositionComponent](entity)
      moveComp <- world.getComponent[MovementComponent](entity)
      strategy = selectMovementStrategy(entity, world)
      currentPixelPos = posComp.position.toPixel
      newPixelPos = strategy(currentPixelPos, moveComp, entity, world, deltaTime / 1000.0)
      validPos = validateAndConstrainPosition(newPixelPos, entity, world)
    yield validPos

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
    PixelPosition(
      pos.x - pixelsPerSecond * dt,
      pos.y
    )

  private val projectileRightMovement: MovementStrategy = (pos, movement, _, _, dt) =>
    val pixelsPerSecond = movement.speed * CELL_WIDTH * 2
    PixelPosition(
      pos.x + pixelsPerSecond * dt,
      pos.y
    )

  private val zigzagMovement: MovementStrategy = (pos, movement, entity, world, dt) =>
    val pixelsPerSecond = movement.speed * CELL_WIDTH
    val time = System.currentTimeMillis() / 1000.0
    val zigzagAmplitude = CELL_HEIGHT * 0.3
    val zigzagFrequency = 2.0

    PixelPosition(
      pos.x - pixelsPerSecond * dt,
      pos.y + math.sin(time * zigzagFrequency) * zigzagAmplitude * dt
    )

  private def conditionalMovement(stopX: Double): MovementStrategy = (pos, movement, _, _, dt) =>
    if pos.x > stopX then
      val pixelsPerSecond = movement.speed * CELL_WIDTH
      PixelPosition(
        (pos.x - pixelsPerSecond * dt).max(stopX),
        pos.y
      )
    else pos

  private val defaultMovementStrategy: MovementStrategy = (pos, _, _, _, _) => pos

  private def validateAndConstrainPosition(pos: PixelPosition, entity: EntityId, world: World): PixelPosition =
    val isProjectile = world.getEntitiesByType("projectile").contains(entity)

    if isProjectile then
      val minY = GRID_OFFSET_Y
      val maxY = GRID_OFFSET_Y + GRID_ROWS * CELL_HEIGHT - CELL_HEIGHT / 2
      PixelPosition(pos.x, pos.y.max(minY).min(maxY))
    else
      val constrained = constrainToGrid(pos)
      if canMoveToPosition(constrained, entity, world) then
        constrained
      else
        world.getComponent[PositionComponent](entity)
          .map(_.position.toPixel)
          .getOrElse(constrained)

  private def constrainToGrid(pos: PixelPosition): PixelPosition =
    val minX = GRID_OFFSET_X
    val maxX = GRID_OFFSET_X + GRID_COLS * CELL_WIDTH - CELL_WIDTH / 2
    val minY = GRID_OFFSET_Y
    val maxY = GRID_OFFSET_Y + GRID_ROWS * CELL_HEIGHT - CELL_HEIGHT / 2

    pos.clamp(minX, maxX, minY, maxY)

  private def canMoveToPosition(pos: PixelPosition, entity: EntityId, world: World): Boolean =
    val gridPos = pos.toGrid
    if !gridPos.isValid then false
    else
      world.getEntityAt(gridPos) match
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