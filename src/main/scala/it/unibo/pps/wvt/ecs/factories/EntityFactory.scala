package it.unibo.pps.wvt.ecs.factories

import it.unibo.pps.wvt.ecs.core.{World, EntityId}
import it.unibo.pps.wvt.ecs.components._
import it.unibo.pps.wvt.utilities.Position

object EntityFactory {

  def createBaseTroll(world: World, position: Position, trollType: TrollType,
                      health: Int, speed: Double, spritePath: String): EntityId =
    val entity = world.createEntity()
    world.addComponent(entity, PositionComponent(position))
    world.addComponent(entity, HealthComponent(health, health))
    world.addComponent(entity, MovementComponent(speed))
    world.addComponent(entity, SpriteComponent(spritePath))
    world.addComponent(entity, TrollTypeComponent(trollType))
    entity

  def createWarriorTroll(): EntityId = ???
  def createAssassinTroll(): EntityId = ???
  def createThrowerTroll(): EntityId = ???
  
}