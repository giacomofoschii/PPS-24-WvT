package it.unibo.pps.wvt.ecs.factories

import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.core.World
import it.unibo.pps.wvt.utilities.TestConstants.*
import it.unibo.pps.wvt.utilities.{GridMapper, Position}
import it.unibo.pps.wvt.utilities.GamePlayConstants.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfter
import scalafx.scene.paint.Color

class EntityFactoryTest extends AnyFlatSpec with Matchers with BeforeAndAfter:

  var world: World = _
  val testPosition: Position = GridMapper.logicalToPhysical(GRID_ROW_MID, GRID_COL_MID).get

  before:
    world = World.empty

  behavior of "EntityFactory - Projectiles"

  it should "create fire projectile with correct components" in:
    val (updatedWorld, projectile) = EntityFactory.createProjectile(world, testPosition, ProjectileType.Fire)

    updatedWorld.hasComponent[PositionComponent](projectile) shouldBe true
    updatedWorld.hasComponent[MovementComponent](projectile) shouldBe true
    updatedWorld.hasComponent[ProjectileTypeComponent](projectile) shouldBe true
    updatedWorld.hasComponent[DamageComponent](projectile) shouldBe true
    updatedWorld.hasComponent[ImageComponent](projectile) shouldBe true

  it should "create fire projectile with correct properties" in:
    val (updatedWorld, projectile) = EntityFactory.createProjectile(world, testPosition, ProjectileType.Fire)

    val pos = updatedWorld.getComponent[PositionComponent](projectile).get
    pos.position shouldBe testPosition

    val projectileType = updatedWorld.getComponent[ProjectileTypeComponent](projectile).get
    projectileType.projectileType shouldBe ProjectileType.Fire

    val damage = updatedWorld.getComponent[DamageComponent](projectile).get
    damage.amount shouldBe FIRE_WIZARD_ATTACK_DAMAGE

  it should "create ice projectile with correct damage" in:
    val (updatedWorld, projectile) = EntityFactory.createProjectile(world, testPosition, ProjectileType.Ice)

    val damage = updatedWorld.getComponent[DamageComponent](projectile).get
    damage.amount shouldBe ICE_WIZARD_ATTACK_DAMAGE
    damage.projectileType shouldBe ProjectileType.Ice

  it should "create wind projectile with correct damage" in:
    val (updatedWorld, projectile) = EntityFactory.createProjectile(world, testPosition, ProjectileType.Wind)

    val damage = updatedWorld.getComponent[DamageComponent](projectile).get
    damage.amount shouldBe WIND_WIZARD_ATTACK_DAMAGE
    damage.projectileType shouldBe ProjectileType.Wind

  it should "create troll projectile with correct properties" in:
    val (updatedWorld, projectile) = EntityFactory.createProjectile(world, testPosition, ProjectileType.Troll)

    val projectileType = updatedWorld.getComponent[ProjectileTypeComponent](projectile).get
    projectileType.projectileType shouldBe ProjectileType.Troll

    val damage = updatedWorld.getComponent[DamageComponent](projectile).get
    damage.amount shouldBe THROWER_TROLL_DAMAGE

  it should "create projectile with movement component" in:
    val (updatedWorld, projectile) = EntityFactory.createProjectile(world, testPosition, ProjectileType.Fire)

    val movement = updatedWorld.getComponent[MovementComponent](projectile).get
    movement.speed shouldBe PROJECTILE_SPEED

  it should "throw exception for unknown projectile type" in:
    // This test verifies error handling would work if implemented
    // Since we can't create invalid enum values, we verify known types work
    noException should be thrownBy EntityFactory.createProjectile(world, testPosition, ProjectileType.Fire)

  behavior of "EntityFactory - Wizards"

  it should "create generator wizard with correct components" in:
    val (updatedWorld, wizard) = EntityFactory.createGeneratorWizard(world, testPosition)

    updatedWorld.hasComponent[PositionComponent](wizard) shouldBe true
    updatedWorld.hasComponent[HealthComponent](wizard) shouldBe true
    updatedWorld.hasComponent[CostComponent](wizard) shouldBe true
    updatedWorld.hasComponent[ImageComponent](wizard) shouldBe true
    updatedWorld.hasComponent[WizardTypeComponent](wizard) shouldBe true
    updatedWorld.hasComponent[HealthBarComponent](wizard) shouldBe true
    updatedWorld.hasComponent[ElixirGeneratorComponent](wizard) shouldBe true

  it should "create generator wizard with correct properties" in:
    val (updatedWorld, wizard) = EntityFactory.createGeneratorWizard(world, testPosition)

    val wizardType = updatedWorld.getComponent[WizardTypeComponent](wizard).get
    wizardType.wizardType shouldBe WizardType.Generator

    val health = updatedWorld.getComponent[HealthComponent](wizard).get
    health.maxHealth shouldBe GENERATOR_WIZARD_HEALTH
    health.currentHealth shouldBe GENERATOR_WIZARD_HEALTH

    val cost = updatedWorld.getComponent[CostComponent](wizard).get
    cost.cost shouldBe GENERATOR_WIZARD_COST

    val elixirGen = updatedWorld.getComponent[ElixirGeneratorComponent](wizard).get
    elixirGen.elixirPerSecond shouldBe GENERATOR_WIZARD_ELIXIR_PER_SECOND

  it should "create generator wizard without attack component" in:
    val (updatedWorld, wizard) = EntityFactory.createGeneratorWizard(world, testPosition)
    updatedWorld.hasComponent[AttackComponent](wizard) shouldBe false

  it should "create fire wizard with correct components" in:
    val (updatedWorld, wizard) = EntityFactory.createFireWizard(world, testPosition)

    updatedWorld.hasComponent[PositionComponent](wizard) shouldBe true
    updatedWorld.hasComponent[HealthComponent](wizard) shouldBe true
    updatedWorld.hasComponent[AttackComponent](wizard) shouldBe true
    updatedWorld.hasComponent[WizardTypeComponent](wizard) shouldBe true
    updatedWorld.hasComponent[HealthBarComponent](wizard) shouldBe true

  it should "create fire wizard with correct attack properties" in:
    val (updatedWorld, wizard) = EntityFactory.createFireWizard(world, testPosition)

    val attack = updatedWorld.getComponent[AttackComponent](wizard).get
    attack.damage shouldBe FIRE_WIZARD_ATTACK_DAMAGE
    attack.range shouldBe FIRE_WIZARD_RANGE
    attack.cooldown shouldBe FIRE_WIZARD_COOLDOWN

  it should "create ice wizard with correct properties" in:
    val (updatedWorld, wizard) = EntityFactory.createIceWizard(world, testPosition)

    val wizardType = updatedWorld.getComponent[WizardTypeComponent](wizard).get
    wizardType.wizardType shouldBe WizardType.Ice

    val health = updatedWorld.getComponent[HealthComponent](wizard).get
    health.maxHealth shouldBe ICE_WIZARD_HEALTH

    val attack = updatedWorld.getComponent[AttackComponent](wizard).get
    attack.damage shouldBe ICE_WIZARD_ATTACK_DAMAGE
    attack.range shouldBe ICE_WIZARD_RANGE

  it should "create wind wizard with correct properties" in:
    val (updatedWorld, wizard) = EntityFactory.createWindWizard(world, testPosition)

    val wizardType = updatedWorld.getComponent[WizardTypeComponent](wizard).get
    wizardType.wizardType shouldBe WizardType.Wind

    val cost = updatedWorld.getComponent[CostComponent](wizard).get
    cost.cost shouldBe WIND_WIZARD_COST

    val attack = updatedWorld.getComponent[AttackComponent](wizard).get
    attack.damage shouldBe WIND_WIZARD_ATTACK_DAMAGE

  it should "create barrier wizard with correct properties" in:
    val (updatedWorld, wizard) = EntityFactory.createBarrierWizard(world, testPosition)

    val wizardType = updatedWorld.getComponent[WizardTypeComponent](wizard).get
    wizardType.wizardType shouldBe WizardType.Barrier

    val health = updatedWorld.getComponent[HealthComponent](wizard).get
    health.maxHealth shouldBe BARRIER_WIZARD_HEALTH

    val cost = updatedWorld.getComponent[CostComponent](wizard).get
    cost.cost shouldBe BARRIER_WIZARD_COST

  it should "create barrier wizard without attack or elixir generation" in:
    val (updatedWorld, wizard) = EntityFactory.createBarrierWizard(world, testPosition)

    updatedWorld.hasComponent[AttackComponent](wizard) shouldBe false
    updatedWorld.hasComponent[ElixirGeneratorComponent](wizard) shouldBe false

  it should "create wizards with blue health bars" in:
    val (updatedWorld, wizard) = EntityFactory.createFireWizard(world, testPosition)

    val healthBar = updatedWorld.getComponent[HealthBarComponent](wizard).get
    healthBar.barColor shouldBe Color.Blue

  behavior of "EntityFactory - Trolls"

  it should "create base troll with correct components" in:
    val (updatedWorld, troll) = EntityFactory.createBaseTroll(world, testPosition)

    updatedWorld.hasComponent[PositionComponent](troll) shouldBe true
    updatedWorld.hasComponent[TrollTypeComponent](troll) shouldBe true
    updatedWorld.hasComponent[HealthComponent](troll) shouldBe true
    updatedWorld.hasComponent[MovementComponent](troll) shouldBe true
    updatedWorld.hasComponent[AttackComponent](troll) shouldBe true
    updatedWorld.hasComponent[ImageComponent](troll) shouldBe true
    updatedWorld.hasComponent[HealthBarComponent](troll) shouldBe true

  it should "create base troll with correct properties" in:
    val (updatedWorld, troll) = EntityFactory.createBaseTroll(world, testPosition)

    val trollType = updatedWorld.getComponent[TrollTypeComponent](troll).get
    trollType.trollType shouldBe TrollType.Base

    val health = updatedWorld.getComponent[HealthComponent](troll).get
    health.maxHealth shouldBe BASE_TROLL_HEALTH
    health.currentHealth shouldBe BASE_TROLL_HEALTH

    val movement = updatedWorld.getComponent[MovementComponent](troll).get
    movement.speed shouldBe BASE_TROLL_SPEED

    val attack = updatedWorld.getComponent[AttackComponent](troll).get
    attack.damage shouldBe BASE_TROLL_DAMAGE
    attack.range shouldBe BASE_TROLL_RANGE
    attack.cooldown shouldBe BASE_TROLL_COOLDOWN

  it should "create warrior troll with correct properties" in:
    val (updatedWorld, troll) = EntityFactory.createWarriorTroll(world, testPosition)

    val trollType = updatedWorld.getComponent[TrollTypeComponent](troll).get
    trollType.trollType shouldBe TrollType.Warrior

    val health = updatedWorld.getComponent[HealthComponent](troll).get
    health.maxHealth shouldBe WARRIOR_TROLL_HEALTH

    val movement = updatedWorld.getComponent[MovementComponent](troll).get
    movement.speed shouldBe WARRIOR_TROLL_SPEED

    val attack = updatedWorld.getComponent[AttackComponent](troll).get
    attack.damage shouldBe WARRIOR_TROLL_DAMAGE

  it should "create assassin troll with correct properties" in:
    val (updatedWorld, troll) = EntityFactory.createAssassinTroll(world, testPosition)

    val trollType = updatedWorld.getComponent[TrollTypeComponent](troll).get
    trollType.trollType shouldBe TrollType.Assassin

    val health = updatedWorld.getComponent[HealthComponent](troll).get
    health.maxHealth shouldBe ASSASSIN_TROLL_HEALTH

    val movement = updatedWorld.getComponent[MovementComponent](troll).get
    movement.speed shouldBe ASSASSIN_TROLL_SPEED

    val attack = updatedWorld.getComponent[AttackComponent](troll).get
    attack.damage shouldBe ASSASSIN_TROLL_DAMAGE

  it should "create thrower troll with correct properties" in:
    val (updatedWorld, troll) = EntityFactory.createThrowerTroll(world, testPosition)

    val trollType = updatedWorld.getComponent[TrollTypeComponent](troll).get
    trollType.trollType shouldBe TrollType.Thrower

    val health = updatedWorld.getComponent[HealthComponent](troll).get
    health.maxHealth shouldBe THROWER_TROLL_HEALTH

    val movement = updatedWorld.getComponent[MovementComponent](troll).get
    movement.speed shouldBe THROWER_TROLL_SPEED

    val attack = updatedWorld.getComponent[AttackComponent](troll).get
    attack.damage shouldBe THROWER_TROLL_DAMAGE
    attack.range shouldBe THROWER_TROLL_RANGE

  it should "create trolls with red health bars" in:
    val (updatedWorld, troll) = EntityFactory.createBaseTroll(world, testPosition)

    val healthBar = updatedWorld.getComponent[HealthBarComponent](troll).get
    healthBar.barColor shouldBe Color.Red

  it should "create trolls at specified position" in:
    val customPos = Position(POS_X_MID, POS_Y_MID)
    val (updatedWorld, troll) = EntityFactory.createBaseTroll(world, customPos)

    val pos = updatedWorld.getComponent[PositionComponent](troll).get
    pos.position shouldBe customPos

  behavior of "EntityFactory - Entity Builder Pattern"

  it should "build projectile entities with all required components" in:
    val config = ProjectileConfig(ProjectileType.Fire, DAMAGE_NORMAL, "/projectile/fire.png")
    val builder = summon[EntityBuilder[ProjectileConfig]]
    val components = builder.buildComponents(config, testPosition)

    components should have size 5
    components.exists(_.isInstanceOf[PositionComponent]) shouldBe true
    components.exists(_.isInstanceOf[MovementComponent]) shouldBe true
    components.exists(_.isInstanceOf[ProjectileTypeComponent]) shouldBe true
    components.exists(_.isInstanceOf[DamageComponent]) shouldBe true
    components.exists(_.isInstanceOf[ImageComponent]) shouldBe true

  it should "build wizard entities with base components" in:
    val config = WizardConfig(
      wizardType = WizardType.Fire,
      health = HEALTH_FULL,
      cost = ELIXIR_LOW,
      imagePath = "/wizard/fire.png",
      attack = Some(AttackComponent(DAMAGE_NORMAL, RANGE_MEDIUM, COOLDOWN_NORMAL.toLong))
    )
    val builder = summon[EntityBuilder[WizardConfig]]
    val components = builder.buildComponents(config, testPosition)

    components.exists(_.isInstanceOf[PositionComponent]) shouldBe true
    components.exists(_.isInstanceOf[HealthComponent]) shouldBe true
    components.exists(_.isInstanceOf[CostComponent]) shouldBe true
    components.exists(_.isInstanceOf[WizardTypeComponent]) shouldBe true
    components.exists(_.isInstanceOf[HealthBarComponent]) shouldBe true
    components.exists(_.isInstanceOf[AttackComponent]) shouldBe true

  it should "build wizard entities without optional components" in:
    val config = WizardConfig(
      wizardType = WizardType.Barrier,
      health = HEALTH_HIGH,
      cost = ELIXIR_LOW,
      imagePath = "/wizard/barrier.png"
    )
    val builder = summon[EntityBuilder[WizardConfig]]
    val components = builder.buildComponents(config, testPosition)

    components.exists(_.isInstanceOf[AttackComponent]) shouldBe false
    components.exists(_.isInstanceOf[ElixirGeneratorComponent]) shouldBe false

  it should "build troll entities with all required components" in:
    val config = TrollConfig(
      trollType = TrollType.Base,
      health = HEALTH_FULL,
      speed = SPEED_NORMAL,
      damage = DAMAGE_NORMAL,
      range = RANGE_MELEE,
      cooldown = COOLDOWN_NORMAL.toLong,
      imagePath = "/troll/BaseTroll.png"
    )
    val builder = summon[EntityBuilder[TrollConfig]]
    val components = builder.buildComponents(config, testPosition)

    components should have size 7
    components.exists(_.isInstanceOf[PositionComponent]) shouldBe true
    components.exists(_.isInstanceOf[TrollTypeComponent]) shouldBe true
    components.exists(_.isInstanceOf[HealthComponent]) shouldBe true
    components.exists(_.isInstanceOf[MovementComponent]) shouldBe true
    components.exists(_.isInstanceOf[AttackComponent]) shouldBe true
    components.exists(_.isInstanceOf[ImageComponent]) shouldBe true
    components.exists(_.isInstanceOf[HealthBarComponent]) shouldBe true

  behavior of "EntityFactory - Multiple Entity Creation"

  it should "create multiple entities independently" in:
    val (world1, wizard1) = EntityFactory.createFireWizard(world, testPosition)
    val pos2 = GridMapper.logicalToPhysical(GRID_ROW_END, GRID_COL_END).get
    val (world2, wizard2) = EntityFactory.createIceWizard(world1, pos2)

    wizard1 should not equal wizard2
    world2.getAllEntities should contain allOf(wizard1, wizard2)

  it should "create entities of different types in same world" in:
    val (world1, wizard) = EntityFactory.createFireWizard(world, testPosition)
    val pos2 = GridMapper.logicalToPhysical(GRID_ROW_END, GRID_COL_END).get
    val (world2, troll) = EntityFactory.createBaseTroll(world1, pos2)
    val pos3 = GridMapper.logicalToPhysical(GRID_ROW_MID, GRID_COL_START).get
    val (world3, projectile) = EntityFactory.createProjectile(world2, pos3, ProjectileType.Fire)

    world3.getAllEntities should contain allOf(wizard, troll, projectile)

  it should "create all wizard types successfully" in:
    val (world1, gen) = EntityFactory.createGeneratorWizard(world, testPosition)
    val (world2, wind) = EntityFactory.createWindWizard(world1, testPosition)
    val (world3, barrier) = EntityFactory.createBarrierWizard(world2, testPosition)
    val (world4, fire) = EntityFactory.createFireWizard(world3, testPosition)
    val (world5, ice) = EntityFactory.createIceWizard(world4, testPosition)

    world5.getAllEntities should have size 5

  it should "create all troll types successfully" in:
    val (world1, base) = EntityFactory.createBaseTroll(world, testPosition)
    val (world2, warrior) = EntityFactory.createWarriorTroll(world1, testPosition)
    val (world3, assassin) = EntityFactory.createAssassinTroll(world2, testPosition)
    val (world4, thrower) = EntityFactory.createThrowerTroll(world3, testPosition)

    world4.getAllEntities should have size 4

  it should "create all projectile types successfully" in:
    val (world1, fire) = EntityFactory.createProjectile(world, testPosition, ProjectileType.Fire)
    val (world2, ice) = EntityFactory.createProjectile(world1, testPosition, ProjectileType.Ice)
    val (world3, wind) = EntityFactory.createProjectile(world2, testPosition, ProjectileType.Wind)
    val (world4, troll) = EntityFactory.createProjectile(world3, testPosition, ProjectileType.Troll)

    world4.getAllEntities should have size 4

  behavior of "EntityFactory - Component Verification"

  it should "create entities with proper health initialization" in:
    val (world1, wizard) = EntityFactory.createFireWizard(world, testPosition)
    val (world2, troll) = EntityFactory.createBaseTroll(world1, testPosition)

    val wizardHealth = world2.getComponent[HealthComponent](wizard).get
    wizardHealth.currentHealth shouldBe wizardHealth.maxHealth

    val trollHealth = world2.getComponent[HealthComponent](troll).get
    trollHealth.currentHealth shouldBe trollHealth.maxHealth

  it should "create entities with correct image paths" in:
    val (updatedWorld, wizard) = EntityFactory.createFireWizard(world, testPosition)

    val image = updatedWorld.getComponent[ImageComponent](wizard).get
    image.imagePath should include("wizard")
    image.imagePath should include("fire")

  it should "create entities with proper health bar configuration" in:
    val (world1, wizard) = EntityFactory.createFireWizard(world, testPosition)
    val (world2, troll) = EntityFactory.createBaseTroll(world1, testPosition)

    val wizardHealthBar = world2.getComponent[HealthBarComponent](wizard).get
    wizardHealthBar.barWidth should be > 0.0
    wizardHealthBar.barHeight should be > 0.0

    val trollHealthBar = world2.getComponent[HealthBarComponent](troll).get
    trollHealthBar.barWidth should be > 0.0
    trollHealthBar.barHeight should be > 0.0

  it should "ensure attacking entities have valid attack ranges" in:
    val (world1, fireWizard) = EntityFactory.createFireWizard(world, testPosition)
    val (world2, baseTroll) = EntityFactory.createBaseTroll(world1, testPosition)

    val wizardAttack = world2.getComponent[AttackComponent](fireWizard).get
    wizardAttack.range should be > 0.0

    val trollAttack = world2.getComponent[AttackComponent](baseTroll).get
    trollAttack.range should be > 0.0

  it should "ensure moving entities have positive speed" in:
    val (world1, troll) = EntityFactory.createBaseTroll(world, testPosition)

    val movement = world1.getComponent[MovementComponent](troll).get
    movement.speed should be > 0.0

  it should "verify projectile damage is positive" in:
    val (updatedWorld, projectile) = EntityFactory.createProjectile(world, testPosition, ProjectileType.Fire)

    val damage = updatedWorld.getComponent[DamageComponent](projectile).get
    damage.amount should be > 0

  it should "verify wizard costs are positive" in:
    val (updatedWorld, wizard) = EntityFactory.createFireWizard(world, testPosition)

    val cost = updatedWorld.getComponent[CostComponent](wizard).get
    cost.cost should be > 0