package it.unibo.pps.wvt.ecs.factories

import it.unibo.pps.wvt.ecs.core.{EntityId, World}
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.components.TrollType.*
import it.unibo.pps.wvt.utilities.GamePlayConstants.*
import it.unibo.pps.wvt.utilities.Position
import it.unibo.pps.wvt.utilities.ViewConstants.*
import scalafx.scene.paint.Color

object EntityFactory:
  def createProjectile(world: World, position:Position, projectileType: ProjectileType): EntityId =
    val entity = world.createEntity()
    world.addComponent(entity, PositionComponent(position))
    world.addComponent(entity, MovementComponent(PROJECTILE_SPEED))
    world.addComponent(entity, ProjectileTypeComponent(projectileType))

    val (damage, imagePath) = projectileType match
      case ProjectileType.Fire => (FIRE_WIZARD_ATTACK_DAMAGE, "/projectile/fire.png")
      case ProjectileType.Ice => (ICE_WIZARD_ATTACK_DAMAGE, "/projectile/ice.png")
      case ProjectileType.Troll => (THROWER_TROLL_DAMAGE, "/projectile/troll.png")
      case ProjectileType.Base => (WIND_WIZARD_ATTACK_DAMAGE, "/projectile/base.png")
      
    world.addComponent(entity, DamageComponent(damage, projectileType))
    world.addComponent(entity, ImageComponent(imagePath))
    entity

  // WIZARD IMPLEMENTATIONS

  def createGeneratorWizard(world: World, position: Position): EntityId =
    val entity = createBaseWizard(world, position, WizardType.Generator, GENERATOR_WIZARD_HEALTH,
      GENERATOR_WIZARD_COST, "/wizard/generator.png")
    world.addComponent(entity, ElixirGeneratorComponent(GENERATOR_WIZARD_ELIXIR_PER_SECOND, GENERATOR_WIZARD_COOLDOWN))
    entity

  def createWindWizard(world: World, position: Position): EntityId =
    val entity = createBaseWizard(world, position, WizardType.Wind, WIND_WIZARD_HEALTH,
      WIND_WIZARD_COST, "/wizard/wind.png")
    world.addComponent(entity, AttackComponent(WIND_WIZARD_ATTACK_DAMAGE, WIND_WIZARD_RANGE, WIND_WIZARD_COOLDOWN))
    entity

  def createBarrierWizard(world: World, position: Position): EntityId =
    val entity = createBaseWizard(world, position, WizardType.Barrier, BARRIER_WIZARD_HEALTH,
      BARRIER_WIZARD_COST, "/wizard/barrier.png")
    entity

  def createFireWizard(world: World, position: Position): EntityId =
    val entity = createBaseWizard(world, position, WizardType.Fire, FIRE_WIZARD_HEALTH,
      FIRE_WIZARD_COST, "/wizard/fire.png")
    world.addComponent(entity, AttackComponent(FIRE_WIZARD_ATTACK_DAMAGE, FIRE_WIZARD_RANGE, FIRE_WIZARD_COOLDOWN))
    entity

  def createIceWizard(world: World, position: Position): EntityId =
    val entity = createBaseWizard(world, position, WizardType.Ice, ICE_WIZARD_HEALTH,
      ICE_WIZARD_COST, "/wizard/ice.png")
    world.addComponent(entity, AttackComponent(ICE_WIZARD_ATTACK_DAMAGE, ICE_WIZARD_RANGE, ICE_WIZARD_COOLDOWN))
    entity

  private def createBaseWizard(world: World, position: Position, wizardType: WizardType,
                               health: Int, cost: Int, spritePath: String): EntityId =
    val entity = world.createEntity()
    world.addComponent(entity, PositionComponent(position))
    world.addComponent(entity, HealthComponent(health, health))
    world.addComponent(entity, CostComponent(cost))
    world.addComponent(entity, ImageComponent(spritePath))
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
      BASE_TROLL_COOLDOWN, "/troll/BaseTroll.png"
    )
    entity

  def createWarriorTroll(world: World, pos: Position): EntityId =
    val entity = createStandardTroll(world, pos, TrollType.Warrior,
      WARRIOR_TROLL_HEALTH, WARRIOR_TROLL_SPEED,
      WARRIOR_TROLL_DAMAGE, WARRIOR_TROLL_RANGE,
      WARRIOR_TROLL_COOLDOWN, "/troll/WarriorTroll.png"
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
      THROWER_TROLL_COOLDOWN, "/troll/ThrowerTroll.png"
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
    world.addComponent(entity, ImageComponent(spritePath))
    world.addComponent(entity, HealthBarComponent(
      barColor = Color.Red,
      barWidth = HEALTH_BAR_WIDTH,
      offsetY = HEALTH_BAR_OFFSET_Y,
    ))
    entity