package it.unibo.pps.wvt.ecs.factories

import it.unibo.pps.wvt.ecs.core.{World, EntityId}
import it.unibo.pps.wvt.ecs.components._
import it.unibo.pps.wvt.utilities.Position

object EntityFactory {

  def createGeneratorWizard(world: World, position: Position): EntityId =
    val entity = createBaseWizard(world, position, WizardType.Generator, 100, 50, "/generator_wizard.png")
    world.addComponent(entity, ElixirGeneratorComponent(5, 10))
    entity

  def createWindWizard(): EntityId = ???

  def createBarrierWizard(): EntityId = ???

  def createFireWizard(): EntityId = ???

  def createIceWizard(): EntityId = ???
  
  

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

  private def createBaseWizard(world: World, position: Position, wizardType: WizardType,
                               health: Int, cost: Int, spritePath: String): EntityId =
    val entity = world.createEntity()
    world.addComponent(entity, PositionComponent(position))
    world.addComponent(entity, HealthComponent(health, health))
    world.addComponent(entity, CostComponent(cost))
    world.addComponent(entity, SpriteComponent(spritePath))
    world.addComponent(entity, WizardTypeComponent(wizardType))
    entity
  
}