package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.{World, EntityId}
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.utilities.GamePlayConstants.*
import it.unibo.pps.wvt.utilities.TestConstants.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class HealthSystemTest extends AnyFlatSpec with Matchers:

  "HealthSystem" should "process collision components correctly" in:
    aHealthSystem
      .withEntity
      .havingHealth(TEST_ENTITY_HALF_HEALTH, TEST_ENTITY_MAX_HEALTH)
      .takingDamage(TEST_DAMAGE_HEAVY)
      .whenUpdated
      .entityShouldHaveHealth(TEST_EXPECTED_HEALTH_AFTER_MEDIUM_DAMAGE)
      .entityShouldNotHaveCollisionComponent

  it should "kill entities when health reaches zero" in:
    aHealthSystem
      .withTroll(TrollType.Base)
      .havingHealth(TEST_ENTITY_LOW_HEALTH, TEST_ENTITY_MAX_HEALTH)
      .takingDamage(TEST_DAMAGE_FATAL)
      .whenUpdated
      .entityShouldBeDead

  it should "give elixir reward when troll dies" in:
    aHealthSystem
      .withTroll(TrollType.Base)
      .havingHealth(TEST_ENTITY_VERY_LOW_HEALTH, TEST_ENTITY_HALF_HEALTH)
      .takingDamage(TEST_DAMAGE_MEDIUM)
      .whenUpdated
      .systemShouldHaveElixir(INITIAL_ELIXIR + BASE_TROLL_REWARD)

  it should "not give reward when wizard dies" in:
    aHealthSystem
      .withWizard(WizardType.Fire)
      .havingHealth(TEST_ENTITY_VERY_LOW_HEALTH, TEST_ENTITY_HALF_HEALTH)
      .takingDamage(TEST_DAMAGE_MEDIUM)
      .whenUpdated
      .entityShouldBeDead
      .andSystemShouldHaveElixir(INITIAL_ELIXIR)

  it should "handle multiple collision components" in:
    aHealthSystem
      .withEntity
      .havingHealth(TEST_ENTITY_MAX_HEALTH, TEST_ENTITY_MAX_HEALTH)
      .takingDamage(TEST_DAMAGE_HEAVY)
      .whenUpdated
      .entityShouldHaveHealth(TEST_EXPECTED_HEALTH_AFTER_HEAVY_DAMAGE)

  it should "ignore collision to dead entities" in:
    aHealthSystem
      .withEntity
      .havingHealth(TEST_ENTITY_DEAD_HEALTH, TEST_ENTITY_MAX_HEALTH)
      .takingDamage(TEST_DAMAGE_LIGHT)
      .whenUpdated
      .entityShouldBeDead

  it should "ignore collision to entities without health component" in:
    aHealthSystem
      .withEntity
      .takingDamage(TEST_DAMAGE_LIGHT)
      .whenUpdated
      .entityShouldBeAlive
      .entityShouldNotHaveCollisionComponent

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
      .whenUpdated
      .entityShouldBeDead
      .whenUpdatedAgain
  // Should not throw exception

  it should "handle entities dying from existing low health" in:
    aHealthSystem
      .withTroll(TrollType.Base)
      .havingHealth(TEST_ENTITY_DEAD_HEALTH, TEST_ENTITY_MAX_HEALTH)
      .whenUpdated
      .entityShouldBeDead

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

  // DSL Implementation
  private def aHealthSystem: HealthSystemDSL =
    val elixirSystem = ElixirSystem(totalElixir = INITIAL_ELIXIR)
    HealthSystemDSL(
      world = World(),
      elixirSystem = elixirSystem,
      healthSystem = HealthSystem(elixirSystem),
      entities = List.empty,
      currentEntity = None
    )

  case class HealthSystemDSL(
                              world: World,
                              elixirSystem: ElixirSystem,
                              healthSystem: HealthSystem,
                              entities: List[EntityId],
                              currentEntity: Option[EntityId] = None,
                              updatedSystem: Option[HealthSystem] = None
                            ):

    // Entity creation
    def withEntity: EntityBuilder =
      val entity = world.createEntity()
      EntityBuilder(this.copy(entities = entities :+ entity, currentEntity = Some(entity)))

    def withTroll(trollType: TrollType): EntityBuilder =
      val entity = world.createEntity()
      world.addComponent(entity, TrollTypeComponent(trollType))
      EntityBuilder(this.copy(entities = entities :+ entity, currentEntity = Some(entity)))

    def withWizard(wizardType: WizardType): EntityBuilder =
      val entity = world.createEntity()
      world.addComponent(entity, WizardTypeComponent(wizardType))
      EntityBuilder(this.copy(entities = entities :+ entity, currentEntity = Some(entity)))

    // Actions
    def whenUpdated: HealthSystemDSL =
      val updated = healthSystem.update(world).asInstanceOf[HealthSystem]
      copy(updatedSystem = Some(updated), healthSystem = updated)

    def whenUpdatedAgain: HealthSystemDSL =
      val system = updatedSystem.getOrElse(healthSystem)
      val updated = system.update(world).asInstanceOf[HealthSystem]
      copy(updatedSystem = Some(updated), healthSystem = updated)

    // Entity selection for assertions
    def entity(index: Int): EntityAssertions =
      EntityAssertions(this, entities(index))

    // Assertions on current entity
    def entityShouldHaveHealth(expected: Int): HealthSystemDSL =
      currentEntity.foreach: entity =>
        val health = world.getComponent[HealthComponent](entity)
        health should be(defined)
        health.get.currentHealth shouldBe expected
      this

    def entityShouldBeAlive: HealthSystemDSL =
      currentEntity.foreach: entity =>
        world.getAllEntities should contain(entity)
      this

    def entityShouldBeDead: HealthSystemDSL =
      currentEntity.foreach: entity =>
        world.getAllEntities should not contain entity
      this

    def entityShouldNotHaveCollisionComponent: HealthSystemDSL =
      currentEntity.foreach: entity =>
        world.getComponent[CollisionComponent](entity) should be(empty)
      this

    // System-level assertions
    def systemShouldHaveElixir(expected: Int): HealthSystemDSL =
      val system = updatedSystem.getOrElse(healthSystem)
      system.elixirSystem.getCurrentElixir shouldBe expected
      this

    def andSystemShouldHaveElixir(expected: Int): HealthSystemDSL =
      systemShouldHaveElixir(expected)

  case class EntityBuilder(dsl: HealthSystemDSL):
    private def entity: EntityId = dsl.currentEntity.get

    def havingHealth(current: Int, max: Int): EntityBuilder =
      dsl.world.addComponent(entity, HealthComponent(currentHealth = current, maxHealth = max))
      this

    def takingDamage(amount: Int): EntityBuilder =
      dsl.world.addComponent(entity, CollisionComponent(amount = amount))
      this

    def ofType(trollType: TrollType): EntityBuilder =
      dsl.world.addComponent(entity, TrollTypeComponent(trollType))
      this

    def ofType(wizardType: WizardType): EntityBuilder =
      dsl.world.addComponent(entity, WizardTypeComponent(wizardType))
      this

    // Return to DSL for immediate update
    def whenUpdated: HealthSystemDSL =
      dsl.whenUpdated

    // Complete entity building and return to DSL
    def done: HealthSystemDSL =
      dsl.copy(currentEntity = None)

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