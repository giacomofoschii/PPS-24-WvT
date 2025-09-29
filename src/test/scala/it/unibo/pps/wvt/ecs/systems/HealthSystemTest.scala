package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.World
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.utilities.GamePlayConstants.*
import it.unibo.pps.wvt.utilities.TestConstants.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class HealthSystemTest extends AnyFlatSpec with Matchers:

  "HealthSystem" should "process collision components correctly" in:
    val world = World()
    val elixirSystem = ElixirSystem(totalElixir = INITIAL_ELIXIR)
    val healthSystem = HealthSystem(elixirSystem)
    val entity = world.createEntity()
    world.addComponent(entity, HealthComponent(currentHealth = TEST_ENTITY_HALF_HEALTH, maxHealth = TEST_ENTITY_MAX_HEALTH))
    world.addComponent(entity, CollisionComponent(amount = TEST_DAMAGE_HEAVY))
    val updatedSystem = healthSystem.update(world).asInstanceOf[HealthSystem]
    val health = world.getComponent[HealthComponent](entity)
    health should be(defined)
    health.get.currentHealth shouldBe TEST_EXPECTED_HEALTH_AFTER_MEDIUM_DAMAGE
    world.getComponent[CollisionComponent](entity) should be(empty)

  it should "kill entities when health reaches zero" in:
    val world = World()
    val elixirSystem = ElixirSystem(totalElixir = INITIAL_ELIXIR)
    val healthSystem = HealthSystem(elixirSystem)
    val troll = world.createEntity()
    world.addComponent(troll, HealthComponent(currentHealth = TEST_ENTITY_LOW_HEALTH, maxHealth = TEST_ENTITY_MAX_HEALTH))
    world.addComponent(troll, TrollTypeComponent(TrollType.Base))
    world.addComponent(troll, CollisionComponent(amount = TEST_DAMAGE_FATAL))
    val updatedSystem = healthSystem.update(world).asInstanceOf[HealthSystem]
    world.getAllEntities should not contain troll

  it should "give elixir reward when troll dies" in:
    val world = World()
    val elixirSystem = ElixirSystem(totalElixir = INITIAL_ELIXIR)
    val healthSystem = HealthSystem(elixirSystem)
    val troll = world.createEntity()
    world.addComponent(troll, HealthComponent(currentHealth = TEST_ENTITY_VERY_LOW_HEALTH, maxHealth = TEST_ENTITY_HALF_HEALTH))
    world.addComponent(troll, TrollTypeComponent(TrollType.Base))
    world.addComponent(troll, CollisionComponent(amount = TEST_DAMAGE_MEDIUM))
    val updatedSystem = healthSystem.update(world).asInstanceOf[HealthSystem]
    updatedSystem.elixirSystem.getCurrentElixir shouldBe INITIAL_ELIXIR + BASE_TROLL_REWARD

  it should "not give reward when wizard dies" in:
    val world = World()
    val elixirSystem = ElixirSystem(totalElixir = INITIAL_ELIXIR)
    val healthSystem = HealthSystem(elixirSystem)
    val wizard = world.createEntity()
    world.addComponent(wizard, HealthComponent(currentHealth = TEST_ENTITY_VERY_LOW_HEALTH, maxHealth = TEST_ENTITY_HALF_HEALTH))
    world.addComponent(wizard, WizardTypeComponent(WizardType.Fire))
    healthSystem.createCollision(world, wizard, TEST_DAMAGE_MEDIUM)
    val updatedSystem = healthSystem.update(world).asInstanceOf[HealthSystem]
    world.getAllEntities should not contain wizard
    updatedSystem.elixirSystem.getCurrentElixir shouldBe INITIAL_ELIXIR

  it should "handle multiple collision components" in:
    val world = World()
    val elixirSystem = ElixirSystem(totalElixir = INITIAL_ELIXIR)
    val healthSystem = HealthSystem(elixirSystem)
    val entity = world.createEntity()
    world.addComponent(entity, HealthComponent(currentHealth = TEST_ENTITY_MAX_HEALTH, maxHealth = TEST_ENTITY_MAX_HEALTH))
    world.addComponent(entity, CollisionComponent(amount = TEST_DAMAGE_HEAVY))
    val updatedSystem = healthSystem.update(world).asInstanceOf[HealthSystem]
    val health = world.getComponent[HealthComponent](entity)
    health should be(defined)
    health.get.currentHealth shouldBe TEST_EXPECTED_HEALTH_AFTER_HEAVY_DAMAGE

  it should "ignore collision to dead entities" in:
    val world = World()
    val elixirSystem = ElixirSystem(totalElixir = INITIAL_ELIXIR)
    val healthSystem = HealthSystem(elixirSystem)
    val entity = world.createEntity()
    world.addComponent(entity, HealthComponent(currentHealth = TEST_ENTITY_DEAD_HEALTH, maxHealth = TEST_ENTITY_MAX_HEALTH))
    healthSystem.createCollision(world, entity, TEST_DAMAGE_LIGHT)
    val updatedSystem = healthSystem.update(world).asInstanceOf[HealthSystem]
    world.getAllEntities should not contain entity

  it should "ignore collision to entities without health component" in:
    val world = World()
    val elixirSystem = ElixirSystem(totalElixir = INITIAL_ELIXIR)
    val healthSystem = HealthSystem(elixirSystem)
    val entity = world.createEntity()
    healthSystem.createCollision(world, entity, TEST_DAMAGE_LIGHT)
    val updatedSystem = healthSystem.update(world).asInstanceOf[HealthSystem]
    world.getComponent[CollisionComponent](entity) should be(empty)
    world.getAllEntities should contain(entity)

  it should "check if entity is alive correctly" in:
    val world = World()
    val elixirSystem = ElixirSystem(totalElixir = INITIAL_ELIXIR)
    val healthSystem = HealthSystem(elixirSystem)
    val aliveEntity = world.createEntity()
    world.addComponent(aliveEntity, HealthComponent(currentHealth = TEST_ENTITY_HALF_HEALTH, maxHealth = TEST_ENTITY_MAX_HEALTH))
    val deadEntity = world.createEntity()
    world.addComponent(deadEntity, HealthComponent(currentHealth = TEST_ENTITY_DEAD_HEALTH, maxHealth = TEST_ENTITY_MAX_HEALTH))
    val noHealthEntity = world.createEntity()
    healthSystem.isAlive(world, aliveEntity) shouldBe true
    healthSystem.isAlive(world, deadEntity) shouldBe false
    healthSystem.isAlive(world, noHealthEntity) shouldBe false

  it should "get current health correctly" in:
    val world = World()
    val elixirSystem = ElixirSystem(totalElixir = INITIAL_ELIXIR)
    val healthSystem = HealthSystem(elixirSystem)
    val entity = world.createEntity()
    world.addComponent(entity, HealthComponent(currentHealth = TEST_HEALTH_THREE_QUARTER, maxHealth = TEST_ENTITY_MAX_HEALTH))
    val noHealthEntity = world.createEntity()
    healthSystem.getCurrentHealth(world, entity) shouldBe Some(TEST_HEALTH_THREE_QUARTER)
    healthSystem.getCurrentHealth(world, noHealthEntity) shouldBe None

  it should "calculate health percentage correctly" in:
    val world = World()
    val elixirSystem = ElixirSystem(totalElixir = INITIAL_ELIXIR)
    val healthSystem = HealthSystem(elixirSystem)
    val entity = world.createEntity()
    world.addComponent(entity, HealthComponent(currentHealth = TEST_HEALTH_QUARTER, maxHealth = TEST_ENTITY_MAX_HEALTH))
    val fullHealthEntity = world.createEntity()
    world.addComponent(fullHealthEntity, HealthComponent(currentHealth = TEST_ENTITY_MAX_HEALTH, maxHealth = TEST_ENTITY_MAX_HEALTH))
    val zeroMaxHealthEntity = world.createEntity()
    world.addComponent(zeroMaxHealthEntity, HealthComponent(currentHealth = TEST_ENTITY_HALF_HEALTH, maxHealth = TEST_ENTITY_DEAD_HEALTH))
    healthSystem.getHealthPercentage(world, entity) shouldBe Some(TEST_HEALTH_PERCENTAGE_QUARTER)
    healthSystem.getHealthPercentage(world, fullHealthEntity) shouldBe Some(TEST_HEALTH_PERCENTAGE_FULL)
    healthSystem.getHealthPercentage(world, zeroMaxHealthEntity) shouldBe Some(TEST_HEALTH_PERCENTAGE_ZERO)

  it should "not remove entities marked for removal twice" in:
    val world = World()
    val elixirSystem = ElixirSystem(totalElixir = INITIAL_ELIXIR)
    val healthSystem = HealthSystem(elixirSystem)
    val entity = world.createEntity()
    world.addComponent(entity, HealthComponent(currentHealth = TEST_ENTITY_VERY_LOW_HEALTH, maxHealth = TEST_ENTITY_MAX_HEALTH))
    world.addComponent(entity, TrollTypeComponent(TrollType.Base))
    healthSystem.createCollision(world, entity, TEST_DAMAGE_MEDIUM)
    val updatedSystem1 = healthSystem.update(world).asInstanceOf[HealthSystem]
    world.getAllEntities should not contain entity
    val updatedSystem2 = updatedSystem1.update(world).asInstanceOf[HealthSystem]

  it should "handle entities dying from existing low health" in:
    val world = World()
    val elixirSystem = ElixirSystem(totalElixir = INITIAL_ELIXIR)
    val healthSystem = HealthSystem(elixirSystem)
    val deadEntity = world.createEntity()
    world.addComponent(deadEntity, HealthComponent(currentHealth = TEST_ENTITY_DEAD_HEALTH, maxHealth = TEST_ENTITY_MAX_HEALTH))
    world.addComponent(deadEntity, TrollTypeComponent(TrollType.Base))
    val updatedSystem = healthSystem.update(world).asInstanceOf[HealthSystem]
    world.getAllEntities should not contain deadEntity

  it should "calculate correct rewards for different troll types" in:
    val world = World()
    val elixirSystem = ElixirSystem(totalElixir = INITIAL_ELIXIR)
    val healthSystem = HealthSystem(elixirSystem)
    val baseTroll = world.createEntity()
    world.addComponent(baseTroll, HealthComponent(currentHealth = TEST_ENTITY_MINIMAL_HEALTH, maxHealth = TEST_ENTITY_MAX_HEALTH))
    world.addComponent(baseTroll, TrollTypeComponent(TrollType.Base))
    val warriorTroll = world.createEntity()
    world.addComponent(warriorTroll, HealthComponent(currentHealth = TEST_ENTITY_MINIMAL_HEALTH, maxHealth = TEST_ENTITY_MAX_HEALTH))
    world.addComponent(warriorTroll, TrollTypeComponent(TrollType.Warrior))
    healthSystem.createCollision(world, baseTroll, TEST_ENTITY_MINIMAL_HEALTH + 1)
    healthSystem.createCollision(world, warriorTroll, TEST_ENTITY_MINIMAL_HEALTH + 1)
    val updatedSystem = healthSystem.update(world).asInstanceOf[HealthSystem]
    world.getAllEntities should not contain baseTroll
    world.getAllEntities should not contain warriorTroll
    updatedSystem.elixirSystem.getCurrentElixir shouldBe INITIAL_ELIXIR + BASE_TROLL_REWARD + WARRIOR_TROLL_REWARD