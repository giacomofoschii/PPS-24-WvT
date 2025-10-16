package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.{World, EntityId}
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.utilities.GamePlayConstants.*
import it.unibo.pps.wvt.utilities.TestConstants.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class HealthSystemTest extends AnyFlatSpec with Matchers:

  behavior of "HealthSystem"

  it should "process collision components correctly" in:
    aHealthSystem
      .withEntity
      .havingHealth(100, 100)
      .takingDamage(20)
      .done
      .whenUpdated
      .entity(0).shouldHaveHealth(80)

  it should "kill entities when health reaches zero" in:
    aHealthSystem
      .withTroll(TrollType.Base)
      .havingHealth(TEST_ENTITY_LOW_HEALTH, TEST_ENTITY_MAX_HEALTH)
      .takingDamage(TEST_DAMAGE_FATAL)
      .done
      .whenUpdated
      .entity(0).shouldBeDead

  it should "give elixir reward when troll dies" in:
    aHealthSystem
      .withTroll(TrollType.Base)
      .havingHealth(TEST_ENTITY_VERY_LOW_HEALTH, TEST_ENTITY_HALF_HEALTH)
      .takingDamage(TEST_DAMAGE_MEDIUM)
      .done
      .whenUpdated
      .systemShouldHaveElixir(INITIAL_ELIXIR + BASE_TROLL_REWARD)

  it should "not give reward when wizard dies" in:
    aHealthSystem
      .withWizard(WizardType.Fire)
      .havingHealth(TEST_ENTITY_VERY_LOW_HEALTH, TEST_ENTITY_HALF_HEALTH)
      .takingDamage(TEST_DAMAGE_MEDIUM)
      .done
      .whenUpdated
      .entity(0).shouldBeDead
      .andSystemShouldHaveElixir(INITIAL_ELIXIR)

  it should "handle multiple collision components" in:
    aHealthSystem
      .withEntity
      .havingHealth(TEST_ENTITY_MAX_HEALTH, TEST_ENTITY_MAX_HEALTH)
      .takingDamage(TEST_DAMAGE_HEAVY)
      .done
      .whenUpdated
      .entity(0).shouldHaveHealth(TEST_EXPECTED_HEALTH_AFTER_HEAVY_DAMAGE)

  it should "ignore collision to dead entities" in:
    aHealthSystem
      .withEntity
      .havingHealth(TEST_ENTITY_DEAD_HEALTH, TEST_ENTITY_MAX_HEALTH)
      .takingDamage(TEST_DAMAGE_LIGHT)
      .done
      .whenUpdated
      .entity(0).shouldBeDead

  it should "ignore collision to entities without health component" in:
    aHealthSystem
      .withEntity
      .takingDamage(TEST_DAMAGE_LIGHT)
      .done
      .whenUpdated
      .entityShouldExistInWorld(0)
      .entityShouldNotHaveCollisionComponent(0)

  it should "setup entity with health correctly without update" in:
    aHealthSystem
      .withEntity.havingHealth(100, 100).done
      .entity(0).shouldHaveHealth(100)

  it should "check if entity is alive correctly" in:
    aHealthSystem
      .withEntity.havingHealth(TEST_ENTITY_HALF_HEALTH, TEST_ENTITY_MAX_HEALTH).done
      .withEntity.havingHealth(TEST_ENTITY_DEAD_HEALTH, TEST_ENTITY_MAX_HEALTH).done
      .withEntity.done
      .entity(0).shouldBeAlive
      .entity(1).shouldBeDead
      .entity(2).shouldBeDead

  it should "get current health correctly" in:
    aHealthSystem
      .withEntity.havingHealth(TEST_HEALTH_THREE_QUARTER, TEST_ENTITY_MAX_HEALTH).done
      .withEntity.done
      .entity(0).shouldHaveHealth(TEST_HEALTH_THREE_QUARTER)
      .entity(1).shouldHaveNoHealth

  it should "not remove entities marked for removal twice" in:
    aHealthSystem
      .withTroll(TrollType.Base)
      .havingHealth(TEST_ENTITY_VERY_LOW_HEALTH, TEST_ENTITY_MAX_HEALTH)
      .takingDamage(TEST_DAMAGE_MEDIUM)
      .done
      .whenUpdated
      .entity(0).shouldBeDead
      .whenUpdatedAgain

  it should "handle entities dying from existing low health" in:
    aHealthSystem
      .withTroll(TrollType.Base)
      .havingHealth(TEST_ENTITY_DEAD_HEALTH, TEST_ENTITY_MAX_HEALTH)
      .done
      .whenUpdated
      .entity(0).shouldBeDead

  it should "calculate correct rewards for different troll types" in:
    aHealthSystem
      .withTroll(TrollType.Base)
      .havingHealth(TEST_ENTITY_MINIMAL_HEALTH, TEST_ENTITY_MAX_HEALTH)
      .takingDamage(TEST_ENTITY_MINIMAL_HEALTH + 1)
      .done
      .withTroll(TrollType.Warrior)
      .havingHealth(TEST_ENTITY_MINIMAL_HEALTH, TEST_ENTITY_MAX_HEALTH)
      .takingDamage(TEST_ENTITY_MINIMAL_HEALTH + 1)
      .done
      .whenUpdated
      .entity(0).shouldBeDead
      .entity(1).shouldBeDead
      .systemShouldHaveElixir(INITIAL_ELIXIR + BASE_TROLL_REWARD + WARRIOR_TROLL_REWARD)

  it should "process multiple entities with damage in sequence" in:
    aHealthSystem
      .withEntity.havingHealth(100, 100).takingDamage(30).done
      .withEntity.havingHealth(100, 100).takingDamage(50).done
      .whenUpdated
      .entity(0).shouldHaveHealth(70)
      .entity(1).shouldHaveHealth(50)

  it should "remove collision component after processing" in:
    aHealthSystem
      .withEntity
      .havingHealth(TEST_ENTITY_MAX_HEALTH, TEST_ENTITY_MAX_HEALTH)
      .takingDamage(TEST_DAMAGE_LIGHT)
      .done
      .whenUpdated
      .entityShouldNotHaveCollisionComponent(0)

  it should "kill entity when health equals damage amount" in:
    aHealthSystem
      .withTroll(TrollType.Base)
      .havingHealth(50, 100)
      .takingDamage(50)
      .done
      .whenUpdated
      .entity(0).shouldBeDead
      .systemShouldHaveElixir(INITIAL_ELIXIR + BASE_TROLL_REWARD)

  it should "handle entity with zero health from setup" in:
    aHealthSystem
      .withEntity.havingHealth(0, 100).done
      .entity(0).shouldBeDead

  it should "calculate correct rewards for all troll types" in:
    aHealthSystem
      .withTroll(TrollType.Base)
      .havingHealth(1, 100)
      .takingDamage(10)
      .done
      .withTroll(TrollType.Warrior)
      .havingHealth(1, 100)
      .takingDamage(10)
      .done
      .withTroll(TrollType.Assassin)
      .havingHealth(1, 100)
      .takingDamage(10)
      .done
      .withTroll(TrollType.Thrower)
      .havingHealth(1, 100)
      .takingDamage(10)
      .done
      .whenUpdated
      .entity(0).shouldBeDead
      .entity(1).shouldBeDead
      .entity(2).shouldBeDead
      .entity(3).shouldBeDead
      .systemShouldHaveElixir(INITIAL_ELIXIR + BASE_TROLL_REWARD + WARRIOR_TROLL_REWARD + ASSASSIN_TROLL_REWARD + THROWER_TROLL_REWARD)

  it should "handle cascading removal of blocked components" in:
    aHealthSystem
      .withEntity.havingHealth(100, 100).done
      .withEntity
      .havingHealth(50, 100)
      .takingDamage(100)
      .done
      .blockingSecondEntityWithFirst
      .whenUpdated
      .entity(0).shouldBeAlive
      .entity(1).shouldBeDead
      .entityShouldNotHaveBlockedComponent(0)

  it should "remove blocked components when blocking entity dies" in:
    aHealthSystem
      .withEntity.havingHealth(100, 100).done
      .withEntity
      .havingHealth(1, 100)
      .takingDamage(10)
      .done
      .blockingFirstWithSecond
      .whenUpdated
      .entity(1).shouldBeDead
      .entity(0).shouldBeAlive
      .entityShouldNotHaveBlockedComponent(0)

  it should "handle zero damage application" in:
    aHealthSystem
      .withEntity
      .havingHealth(100, 100)
      .takingDamage(0)
      .done
      .whenUpdated
      .entity(0).shouldHaveHealth(100)

  it should "prevent double removal of blocked components" in:
    aHealthSystem
      .withEntity.havingHealth(100, 100).done
      .withEntity
      .havingHealth(1, 100)
      .takingDamage(10)
      .done
      .blockingFirstWithSecond
      .whenUpdated
      .entity(1).shouldBeDead
      .entityShouldNotHaveBlockedComponent(0)
      .whenUpdatedAgain

  // === DSL Setup ===
  private def aHealthSystem: HealthSystemDSL =
    val elixirSystem = ElixirSystem(totalElixir = INITIAL_ELIXIR)
    HealthSystemDSL(
      world = World(),
      elixirSystem = elixirSystem,
      healthSystem = HealthSystem(elixirSystem),
      entities = List.empty
    )

  // === DSL Definitions ===
  case class HealthSystemDSL(
                              world: World,
                              elixirSystem: ElixirSystem,
                              healthSystem: HealthSystem,
                              entities: List[EntityId]
                            ):

    // === Entity Creation ===
    def withEntity: EntityBuilder =
      val (updatedWorld, entity) = world.createEntity()
      EntityBuilder(this.copy(world = updatedWorld, entities = entities :+ entity), entity)

    def withTroll(trollType: TrollType): EntityBuilder =
      val (updatedWorld, entity) = world.createEntity()
      val worldWithComponent = updatedWorld.addComponent(entity, TrollTypeComponent(trollType))
      EntityBuilder(this.copy(world = worldWithComponent, entities = entities :+ entity), entity)

    def withWizard(wizardType: WizardType): EntityBuilder =
      val (updatedWorld, entity) = world.createEntity()
      val worldWithComponent = updatedWorld.addComponent(entity, WizardTypeComponent(wizardType))
      EntityBuilder(this.copy(world = worldWithComponent, entities = entities :+ entity), entity)

    // === Actions ===
    def whenUpdated: HealthSystemDSL =
      val (updatedWorld, updatedSystem) = healthSystem.update(world)
      copy(
        world = updatedWorld,
        healthSystem = updatedSystem.asInstanceOf[HealthSystem],
        elixirSystem = updatedSystem.asInstanceOf[HealthSystem].elixirSystem
      )

    def whenUpdatedAgain: HealthSystemDSL =
      whenUpdated

    // === Entity Selection for Assertions ===
    def entity(index: Int): EntityAssertions =
      EntityAssertions(this, entities(index))

    // === Assertions on System ===
    def systemShouldHaveElixir(expected: Int): HealthSystemDSL =
      healthSystem.elixirSystem.getCurrentElixir shouldBe expected
      this

    def andSystemShouldHaveElixir(expected: Int): HealthSystemDSL =
      systemShouldHaveElixir(expected)

    def entityShouldNotHaveCollisionComponent(index: Int): HealthSystemDSL =
      world.getComponent[CollisionComponent](entities(index)) should be(empty)
      this

    def entityShouldHaveHealth(index: Int, expected: Int): HealthSystemDSL =
      val health = world.getComponent[HealthComponent](entities(index))
      health should be(defined)
      health.get.currentHealth shouldBe expected
      this

    def entityShouldExistInWorld(index: Int): HealthSystemDSL =
      world.getAllEntities should contain(entities(index))
      this

    def entityShouldNotHaveBlockedComponent(index: Int): HealthSystemDSL =
      world.getComponent[BlockedComponent](entities(index)) should be(empty)
      this

    def entityShouldHaveBlockedComponent(index: Int): HealthSystemDSL =
      world.getComponent[BlockedComponent](entities(index)) should be(defined)
      this

    def blockingSecondEntityWithFirst: HealthSystemDSL =
      val blockedWorld = world.addComponent(entities(1), BlockedComponent(blockedBy = entities(0)))
      copy(world = blockedWorld)

    def blockingSecondWithThird: HealthSystemDSL =
      val blockedWorld = world.addComponent(entities(1), BlockedComponent(blockedBy = entities(2)))
      copy(world = blockedWorld)

    def blockingSecondWithFirst: HealthSystemDSL =
      val blockedWorld = world.addComponent(entities(1), BlockedComponent(blockedBy = entities(0)))
      copy(world = blockedWorld)

    def blockingFirstWithSecond: HealthSystemDSL =
      val blockedWorld = world.addComponent(entities(0), BlockedComponent(blockedBy = entities(1)))
      copy(world = blockedWorld)

    def blockingThirdWithThird: HealthSystemDSL =
      val blockedWorld = world.addComponent(entities(0), BlockedComponent(blockedBy = entities(2)))
      copy(world = blockedWorld)

  case class EntityBuilder(dsl: HealthSystemDSL, entity: EntityId):

    def havingHealth(current: Int, max: Int): EntityBuilder =
      val updatedWorld = dsl.world.addComponent(entity, HealthComponent(currentHealth = current, maxHealth = max))
      copy(dsl = dsl.copy(world = updatedWorld))

    def takingDamage(amount: Int): EntityBuilder =
      val updatedWorld = dsl.world.addComponent(entity, CollisionComponent(amount = amount))
      copy(dsl = dsl.copy(world = updatedWorld))

    def done: HealthSystemDSL =
      dsl

  case class EntityAssertions(dsl: HealthSystemDSL, entity: EntityId):

    def shouldBeAlive: HealthSystemDSL =
      dsl.healthSystem.isAlive(dsl.world, entity) shouldBe true
      dsl

    def shouldBeDead: HealthSystemDSL =
      dsl.healthSystem.isAlive(dsl.world, entity) shouldBe false
      dsl

    def shouldHaveHealth(expected: Int): HealthSystemDSL =
      dsl.healthSystem.getCurrentHealth(dsl.world, entity) shouldBe Some(expected)
      dsl

    def shouldHaveNoHealth: HealthSystemDSL =
      dsl.healthSystem.getCurrentHealth(dsl.world, entity) shouldBe None
      dsl