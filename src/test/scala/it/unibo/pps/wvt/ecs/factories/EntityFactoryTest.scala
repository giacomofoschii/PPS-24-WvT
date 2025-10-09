package it.unibo.pps.wvt.ecs.factories

import it.unibo.pps.wvt.ecs.core.{EntityId, World}
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.utilities.Position
import it.unibo.pps.wvt.utilities.GamePlayConstants.*
import it.unibo.pps.wvt.utilities.TestConstants.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EntityFactoryTest extends AnyFlatSpec with Matchers:

  private object EntityFactoryTestDSL:
    extension (world: World)
      def verifyEntityHasComponents(entity: EntityId, componentTypes: Class[_]*): Unit =
        val accessors: Map[Class[_], EntityId => Option[_]] = Map(
          classOf[PositionComponent] -> world.getComponent[PositionComponent],
          classOf[HealthComponent] -> world.getComponent[HealthComponent],
          classOf[CostComponent] -> world.getComponent[CostComponent],
          classOf[ImageComponent] -> world.getComponent[ImageComponent],
          classOf[WizardTypeComponent] -> world.getComponent[WizardTypeComponent],
          classOf[HealthBarComponent] -> world.getComponent[HealthBarComponent],
          classOf[TrollTypeComponent] -> world.getComponent[TrollTypeComponent],
          classOf[MovementComponent] -> world.getComponent[MovementComponent],
          classOf[AttackComponent] -> world.getComponent[AttackComponent],
          classOf[DamageComponent] -> world.getComponent[DamageComponent],
          classOf[ProjectileTypeComponent] -> world.getComponent[ProjectileTypeComponent],
          classOf[ElixirGeneratorComponent] -> world.getComponent[ElixirGeneratorComponent]
        )
        componentTypes.foreach: compType =>
          accessors.get(compType).flatMap(_(entity)) shouldBe defined

      def getEntityImagePath(entity: EntityId): Option[String] =
        world.getComponent[ImageComponent](entity).map(_.imagePath)

      def getEntityHealth(entity: EntityId): Option[(Int, Int)] =
        world.getComponent[HealthComponent](entity).map: h =>
          (h.currentHealth, h.maxHealth)

      def getEntityDamage(entity: EntityId): Option[Int] =
        world.getComponent[DamageComponent](entity).map(_.amount)
          .orElse(world.getComponent[AttackComponent](entity).map(_.damage))

      def getEntityCost(entity: EntityId): Option[Int] =
        world.getComponent[CostComponent](entity).map(_.cost)

      def getEntityMovementSpeed(entity: EntityId): Option[Double] =
        world.getComponent[MovementComponent](entity).map(_.speed)

  import EntityFactoryTestDSL.*

  behavior of "EntityFactory - Projectile Creation"

  it should "create fire projectile with correct components and stats" in :
    val world = World()
    val position = Position(TEST_PROJECTILE_X, TEST_PROJECTILE_Y)

    val projectile = EntityFactory.createProjectile(world, position, ProjectileType.Fire)

    world.getComponent[ProjectileTypeComponent](projectile) shouldBe defined
    world.getComponent[ProjectileTypeComponent](projectile).get.projectileType shouldBe ProjectileType.Fire
    world.getEntityImagePath(projectile) shouldBe Some(TEST_FIRE_PROJECTILE_PATH)
    world.getEntityDamage(projectile) shouldBe Some(FIRE_WIZARD_ATTACK_DAMAGE)
    world.getEntityMovementSpeed(projectile) shouldBe Some(PROJECTILE_SPEED)

  it should "create ice projectile with correct damage" in :
    val world = World()
    val projectile = EntityFactory.createProjectile(world, Position(TEST_PROJECTILE_X, TEST_PROJECTILE_Y), ProjectileType.Ice)

    world.getEntityDamage(projectile) shouldBe Some(ICE_WIZARD_ATTACK_DAMAGE)
    world.getEntityImagePath(projectile) shouldBe Some(TEST_ICE_PROJECTILE_PATH)

  it should "create troll projectile with correct damage" in :
    val world = World()
    val projectile = EntityFactory.createProjectile(world, Position(TEST_PROJECTILE_X, TEST_PROJECTILE_Y), ProjectileType.Troll)

    world.getEntityDamage(projectile) shouldBe Some(THROWER_TROLL_DAMAGE)
    world.getEntityImagePath(projectile) shouldBe Some(TEST_TROLL_PROJECTILE_PATH)

  it should "create wind projectile with correct stats" in :
    val world = World()
    val projectile = EntityFactory.createProjectile(world, Position(TEST_PROJECTILE_X, TEST_PROJECTILE_Y), ProjectileType.Wind)

    world.getEntityDamage(projectile) shouldBe Some(WIND_WIZARD_ATTACK_DAMAGE)
    world.getEntityImagePath(projectile) shouldBe Some(TEST_WIND_PROJECTILE_PATH)

  behavior of "EntityFactory - Wizard Creation"

  it should "create generator wizard with elixir generation component" in :
    val world = World()
    val position = Position(TEST_WIZARD_X, TEST_WIZARD_Y)

    val wizard = EntityFactory.createGeneratorWizard(world, position)

    world.getComponent[WizardTypeComponent](wizard).get.wizardType shouldBe WizardType.Generator
    world.getComponent[ElixirGeneratorComponent](wizard) shouldBe defined
    world.getComponent[ElixirGeneratorComponent](wizard).get.elixirPerSecond shouldBe GENERATOR_WIZARD_ELIXIR_PER_SECOND
    world.getEntityHealth(wizard) shouldBe Some((GENERATOR_WIZARD_HEALTH, GENERATOR_WIZARD_HEALTH))
    world.getEntityCost(wizard) shouldBe Some(GENERATOR_WIZARD_COST)

  it should "create wind wizard with attack component" in :
    val world = World()
    val wizard = EntityFactory.createWindWizard(world, Position(TEST_WIZARD_X, TEST_WIZARD_Y))

    world.getComponent[WizardTypeComponent](wizard).get.wizardType shouldBe WizardType.Wind
    world.getComponent[AttackComponent](wizard) shouldBe defined
    world.getEntityDamage(wizard) shouldBe Some(WIND_WIZARD_ATTACK_DAMAGE)
    world.getEntityHealth(wizard) shouldBe Some((WIND_WIZARD_HEALTH, WIND_WIZARD_HEALTH))

  it should "create barrier wizard without attack component" in :
    val world = World()
    val wizard = EntityFactory.createBarrierWizard(world, Position(TEST_WIZARD_X, TEST_WIZARD_Y))

    world.getComponent[WizardTypeComponent](wizard).get.wizardType shouldBe WizardType.Barrier
    world.getComponent[AttackComponent](wizard) shouldBe None
    world.getEntityHealth(wizard) shouldBe Some((BARRIER_WIZARD_HEALTH, BARRIER_WIZARD_HEALTH))
    world.getEntityCost(wizard) shouldBe Some(BARRIER_WIZARD_COST)

  it should "create fire wizard with correct range" in :
    val world = World()
    val wizard = EntityFactory.createFireWizard(world, Position(TEST_WIZARD_X, TEST_WIZARD_Y))

    val attack = world.getComponent[AttackComponent](wizard).get
    attack.range shouldBe FIRE_WIZARD_RANGE
    attack.damage shouldBe FIRE_WIZARD_ATTACK_DAMAGE
    world.getEntityHealth(wizard) shouldBe Some((FIRE_WIZARD_HEALTH, FIRE_WIZARD_HEALTH))

  it should "create ice wizard with correct cooldown" in :
    val world = World()
    val wizard = EntityFactory.createIceWizard(world, Position(TEST_WIZARD_X, TEST_WIZARD_Y))

    val attack = world.getComponent[AttackComponent](wizard).get
    attack.cooldown shouldBe ICE_WIZARD_COOLDOWN
    world.getEntityCost(wizard) shouldBe Some(ICE_WIZARD_COST)

  behavior of "EntityFactory - Troll Creation"

  it should "create base troll with standard stats" in :
    val world = World()
    val position = Position(TEST_TROLL_X, TEST_TROLL_Y)

    val troll = EntityFactory.createBaseTroll(world, position)

    world.getComponent[TrollTypeComponent](troll).get.trollType shouldBe TrollType.Base
    world.getEntityHealth(troll) shouldBe Some((BASE_TROLL_HEALTH, BASE_TROLL_HEALTH))
    world.getEntityMovementSpeed(troll) shouldBe Some(BASE_TROLL_SPEED)
    world.getEntityDamage(troll) shouldBe Some(BASE_TROLL_DAMAGE)

  it should "create warrior troll with high health" in :
    val world = World()
    val troll = EntityFactory.createWarriorTroll(world, Position(TEST_TROLL_X, TEST_TROLL_Y))

    world.getComponent[TrollTypeComponent](troll).get.trollType shouldBe TrollType.Warrior
    world.getEntityHealth(troll) shouldBe Some((WARRIOR_TROLL_HEALTH, WARRIOR_TROLL_HEALTH))
    world.getEntityMovementSpeed(troll) shouldBe Some(WARRIOR_TROLL_SPEED)

  it should "create assassin troll with high speed" in :
    val world = World()
    val troll = EntityFactory.createAssassinTroll(world, Position(TEST_TROLL_X, TEST_TROLL_Y))

    world.getComponent[TrollTypeComponent](troll).get.trollType shouldBe TrollType.Assassin
    world.getEntityMovementSpeed(troll) shouldBe Some(ASSASSIN_TROLL_SPEED)
    world.getEntityDamage(troll) shouldBe Some(ASSASSIN_TROLL_DAMAGE)

  it should "create thrower troll with range attack" in :
    val world = World()
    val troll = EntityFactory.createThrowerTroll(world, Position(TEST_TROLL_X, TEST_TROLL_Y))

    world.getComponent[TrollTypeComponent](troll).get.trollType shouldBe TrollType.Thrower
    val attack = world.getComponent[AttackComponent](troll).get
    attack.range shouldBe THROWER_TROLL_RANGE
    attack.damage shouldBe THROWER_TROLL_DAMAGE

  behavior of "EntityFactory - Component Presence"

  it should "create all trolls with required components" in :
    val world = World()
    val position = Position(TEST_TROLL_X, TEST_TROLL_Y)

    val trollTypes = Seq(
      EntityFactory.createBaseTroll(world, position),
      EntityFactory.createWarriorTroll(world, position),
      EntityFactory.createAssassinTroll(world, position),
      EntityFactory.createThrowerTroll(world, position)
    )

    trollTypes.foreach: troll =>
      world.verifyEntityHasComponents(
        troll,
        classOf[PositionComponent],
        classOf[TrollTypeComponent],
        classOf[HealthComponent],
        classOf[MovementComponent],
        classOf[AttackComponent],
        classOf[ImageComponent]
      )

  it should "create all wizards with health bar component" in :
    val world = World()
    val position = Position(TEST_WIZARD_X, TEST_WIZARD_Y)

    val wizards = Seq(
      EntityFactory.createGeneratorWizard(world, position),
      EntityFactory.createWindWizard(world, position),
      EntityFactory.createBarrierWizard(world, position),
      EntityFactory.createFireWizard(world, position),
      EntityFactory.createIceWizard(world, position)
    )

    wizards.foreach: wizard =>
      world.getComponent[HealthBarComponent](wizard) shouldBe defined
      world.getComponent[PositionComponent](wizard) shouldBe defined
      world.getComponent[WizardTypeComponent](wizard) shouldBe defined

  behavior of "EntityFactory - Position Assignment"

  it should "create entities at specified positions" in :
    val world = World()
    val testPosition = Position(TEST_CUSTOM_X, TEST_CUSTOM_Y)

    val wizard = EntityFactory.createFireWizard(world, testPosition)
    val troll = EntityFactory.createBaseTroll(world, testPosition)
    val projectile = EntityFactory.createProjectile(world, testPosition, ProjectileType.Fire)

    world.getComponent[PositionComponent](wizard).get.position shouldBe testPosition
    world.getComponent[PositionComponent](troll).get.position shouldBe testPosition
    world.getComponent[PositionComponent](projectile).get.position shouldBe testPosition