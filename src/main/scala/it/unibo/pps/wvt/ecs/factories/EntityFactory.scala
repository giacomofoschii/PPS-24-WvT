package it.unibo.pps.wvt.ecs.factories

import it.unibo.pps.wvt.ecs.core.{EntityId, World}
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.components.TrollType.*
import it.unibo.pps.wvt.utilities.GamePlayConstants.*
import it.unibo.pps.wvt.utilities.Position
import it.unibo.pps.wvt.utilities.ViewConstants.*
import scalafx.scene.paint.Color

object EntityFactory:
  def createProjectile(world: World, position:Position): EntityId =
    val entity = world.createEntity()
    world.addComponent(entity, PositionComponent(position))
    world.addComponent(entity, MovementComponent(PROJECTILE_SPEED))
    world.addComponent(entity, SpriteComponent("/projectile.png"))
    entity

  // WIZARD IMPLEMENTATIONS

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

  private def createBaseWizard(world: World, position: Position, wizardType: WizardType,
                               health: Int, cost: Int, spritePath: String): EntityId =
    val entity = world.createEntity()
    world.addComponent(entity, PositionComponent(position))
    world.addComponent(entity, HealthComponent(health, health))
    world.addComponent(entity, CostComponent(cost))
    world.addComponent(entity, SpriteComponent(spritePath))
    world.addComponent(entity, WizardTypeComponent(wizardType))
    world.addComponent(entity, HealthBarComponent(
      barColor = Color.Blue,
      barWidth = HEALTH_BAR_WIDTH,
      offsetY = HEALTH_BAR_OFFSET_Y,
    ))
    entity

  // TROLL IMPLEMENTATIONS

  def createBaseTroll(world: World, pos: Position): EntityId =
    val entity = createStandardTroll(world, pos, TrollType.Base,
      BASE_TROLL_HEALTH, BASE_TROLL_SPEED,
      BASE_TROLL_DAMAGE, BASE_TROLL_RANGE,
      BASE_TROLL_COOLDOWN, "/troll/BASE_TROLL/WALK_005.png"
    )
    entity

  def createWarriorTroll(world: World, pos: Position): EntityId =
    val entity = createStandardTroll(world, pos, TrollType.Warrior,
      WARRIOR_TROLL_HEALTH, WARRIOR_TROLL_SPEED,
      WARRIOR_TROLL_DAMAGE, WARRIOR_TROLL_RANGE,
      WARRIOR_TROLL_COOLDOWN, "/troll/WAR_TROLL/WALK_005.png"
    )
    entity

  def createAssassinTroll(world: World, pos: Position): EntityId =
    val entity = createStandardTroll(world, pos, TrollType.Assassin,
      ASSASSIN_TROLL_HEALTH, ASSASSIN_TROLL_SPEED,
      ASSASSIN_TROLL_DAMAGE, ASSASSIN_TROLL_RANGE,
      ASSASSIN_TROLL_COOLDOWN, "/troll/Assassin.png"
    )
    entity

  def createThrowerTroll(world: World, pos: Position): EntityId =
    val entity = createStandardTroll(world, pos, TrollType.Thrower,
      THROWER_TROLL_HEALTH, THROWER_TROLL_SPEED,
      THROWER_TROLL_DAMAGE, THROWER_TROLL_RANGE,
      THROWER_TROLL_COOLDOWN, "/troll/THROW_TROLL/WALK_005.png"
    )
    entity

  private def createStandardTroll(world: World, pos: Position, tType: TrollType,
                                  health: Int, speed: Double, damage: Int,
                                  range: Double, cooldown: Long, spritePath: String): EntityId =
    val entity = world.createEntity()
    world.addComponent(entity, PositionComponent(pos))
    world.addComponent(entity, TrollTypeComponent(tType))
    world.addComponent(entity, HealthComponent(health, health))
    world.addComponent(entity, MovementComponent(speed))
    world.addComponent(entity, AttackComponent(
      damage = damage,
      range = range,
      cooldown = cooldown
    ))
    world.addComponent(entity, SpriteComponent(spritePath))
    world.addComponent(entity, HealthBarComponent(
      barColor = Color.Red,
      barWidth = HEALTH_BAR_WIDTH,
      offsetY = HEALTH_BAR_OFFSET_Y,
    ))
    entity