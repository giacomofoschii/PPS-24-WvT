package it.unibo.pps.wvt.ecs.factories

import it.unibo.pps.wvt.ecs.core.{World, EntityId}
import it.unibo.pps.wvt.ecs.components._
import it.unibo.pps.wvt.utilities.Position

object EntityFactory {

  def createGeneratorWizard(world: World, position: Position): EntityId =
    val entity = createBaseWizard(world, position, WizardType.Generator, 100, 50, "/generator_wizard.png")
    world.addComponent(entity, ElixirGeneratorComponent(5, 10))
    entity

  def createWindWizard(world: World, position: Position): EntityId =
    val entity = createBaseWizard(world, position, WizardType.Wind, 80, 40, "/wind_wizard.png")
    world.addComponent(entity, AttackComponent(15, 3.0, 2000))
    entity

  def createBarrierWizard(world: World, position: Position): EntityId =
    val entity = createBaseWizard(world, position, WizardType.Barrier, 120, 60, "/barrier_wizard.png")
    entity

  def createFireWizard(world: World, position: Position): EntityId =
    val entity = createBaseWizard(world, position, WizardType.Fire, 90, 45, "/fire_wizard.png")
    world.addComponent(entity, AttackComponent(20, 2.5, 1500))
    entity

  def createIceWizard(world: World, position: Position): EntityId =
    val entity = createBaseWizard(world, position, WizardType.Ice, 85, 42, "/ice_wizard.png")
    world.addComponent(entity, AttackComponent(10, 2.0, 1000))
    entity
  
  

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