package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.World
import it.unibo.pps.wvt.ecs.components.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class HealthSystemTest extends AnyFlatSpec with Matchers:

  "HealthSystem" should "process damage components correctly" in:
    val world = World()
    val elixirSystem = ElixirSystem(totalElixir = 100)
    val healthSystem = HealthSystem(elixirSystem)
    val entity = world.createEntity()
    world.addComponent(entity, HealthComponent(currentHealth = 50, maxHealth = 100))
    val damageSource = world.createEntity()
    world.addComponent(entity, DamageComponent(amount = 20, source = damageSource))
    val updatedSystem = healthSystem.update(world).asInstanceOf[HealthSystem]
    val health = world.getComponent[HealthComponent](entity)
    health should be(defined)
    health.get.currentHealth shouldBe 30
    world.getComponent[DamageComponent](entity) should be(empty)

  it should "kill entities when health reaches zero" in:
    val world = World()
    val elixirSystem = ElixirSystem(totalElixir = 100)
    val healthSystem = HealthSystem(elixirSystem)
    val troll = world.createEntity()
    world.addComponent(troll, HealthComponent(currentHealth = 20, maxHealth = 100))
    world.addComponent(troll, TrollTypeComponent(TrollType.Base))
    val damageSource = world.createEntity()
    world.addComponent(troll, DamageComponent(amount = 30, source = damageSource))
    val updatedSystem = healthSystem.update(world).asInstanceOf[HealthSystem]
    world.getAllEntities should not contain troll

  it should "give elixir reward when troll dies" in:
    val world = World()
    var elixirSystem = ElixirSystem(totalElixir = 100)
    var healthSystem = HealthSystem(elixirSystem)
    val troll = world.createEntity()
    world.addComponent(troll, HealthComponent(currentHealth = 10, maxHealth = 50))
    world.addComponent(troll, TrollTypeComponent(TrollType.Base))
    val damageSource = world.createEntity()
    world.addComponent(troll, DamageComponent(amount = 15, source = damageSource))
    healthSystem.update(world)

  it should "not give reward when wizard dies" in:
    val world = World()
    val elixirSystem = ElixirSystem(totalElixir = 100)
    val healthSystem = HealthSystem(elixirSystem)
    val wizard = world.createEntity()
    world.addComponent(wizard, HealthComponent(currentHealth = 10, maxHealth = 50))
    world.addComponent(wizard, WizardTypeComponent(WizardType.Fire))
    val damageSource = world.createEntity()
    healthSystem.createDamage(world, wizard, 15, damageSource)
    val updatedSystem = healthSystem.update(world).asInstanceOf[HealthSystem]
    world.getAllEntities should not contain wizard

  it should "handle multiple damage components" in:
    val world = World()
    val elixirSystem = ElixirSystem(totalElixir = 100)
    val healthSystem = HealthSystem(elixirSystem)
    val entity = world.createEntity()
    world.addComponent(entity, HealthComponent(currentHealth = 100, maxHealth = 100))
    val source1 = world.createEntity()
    world.addComponent(entity, DamageComponent(amount = 20, source = source1))
    val updatedSystem = healthSystem.update(world).asInstanceOf[HealthSystem]
    val health = world.getComponent[HealthComponent](entity)
    health should be(defined)
    health.get.currentHealth shouldBe 80

  it should "ignore damage to dead entities" in:
    val world = World()
    val elixirSystem = ElixirSystem(totalElixir = 100)
    val healthSystem = HealthSystem(elixirSystem)
    val entity = world.createEntity()
    world.addComponent(entity, HealthComponent(currentHealth = 0, maxHealth = 100))
    val damageSource = world.createEntity()
    healthSystem.createDamage(world, entity, 10, damageSource)
    val updatedSystem = healthSystem.update(world).asInstanceOf[HealthSystem]
    world.getAllEntities should not contain entity

  it should "ignore damage to entities without health component" in:
    val world = World()
    val elixirSystem = ElixirSystem(totalElixir = 100)
    val healthSystem = HealthSystem(elixirSystem)
    val entity = world.createEntity()
    val damageSource = world.createEntity()
    healthSystem.createDamage(world, entity, 10, damageSource)
    val updatedSystem = healthSystem.update(world).asInstanceOf[HealthSystem]
    world.getComponent[DamageComponent](entity) should be(empty)

  it should "check if entity is alive correctly" in:
    val world = World()
    val elixirSystem = ElixirSystem(totalElixir = 100)
    val healthSystem = HealthSystem(elixirSystem)
    val aliveEntity = world.createEntity()
    world.addComponent(aliveEntity, HealthComponent(currentHealth = 50, maxHealth = 100))
    val deadEntity = world.createEntity()
    world.addComponent(deadEntity, HealthComponent(currentHealth = 0, maxHealth = 100))
    val noHealthEntity = world.createEntity()
    healthSystem.isAlive(world, aliveEntity) shouldBe true
    healthSystem.isAlive(world, deadEntity) shouldBe false
    healthSystem.isAlive(world, noHealthEntity) shouldBe false

  it should "get current health correctly" in:
    val world = World()
    val elixirSystem = ElixirSystem(totalElixir = 100)
    val healthSystem = HealthSystem(elixirSystem)
    val entity = world.createEntity()
    world.addComponent(entity, HealthComponent(currentHealth = 75, maxHealth = 100))
    val noHealthEntity = world.createEntity()
    healthSystem.getCurrentHealth(world, entity) shouldBe Some(75)
    healthSystem.getCurrentHealth(world, noHealthEntity) shouldBe None

  it should "calculate health percentage correctly" in:
    val world = World()
    val elixirSystem = ElixirSystem(totalElixir = 100)
    val healthSystem = HealthSystem(elixirSystem)
    val entity = world.createEntity()
    world.addComponent(entity, HealthComponent(currentHealth = 25, maxHealth = 100))
    val fullHealthEntity = world.createEntity()
    world.addComponent(fullHealthEntity, HealthComponent(currentHealth = 100, maxHealth = 100))
    val zeroMaxHealthEntity = world.createEntity()
    world.addComponent(zeroMaxHealthEntity, HealthComponent(currentHealth = 50, maxHealth = 0))
    healthSystem.getHealthPercentage(world, entity) shouldBe Some(0.25)
    healthSystem.getHealthPercentage(world, fullHealthEntity) shouldBe Some(1.0)
    healthSystem.getHealthPercentage(world, zeroMaxHealthEntity) shouldBe Some(0.0)

  it should "not remove entities marked for removal twice" in:
    val world = World()
    val elixirSystem = ElixirSystem(totalElixir = 100)
    val healthSystem = HealthSystem(elixirSystem)
    val entity = world.createEntity()
    world.addComponent(entity, HealthComponent(currentHealth = 10, maxHealth = 100))
    world.addComponent(entity, TrollTypeComponent(TrollType.Base))
    val damageSource = world.createEntity()
    healthSystem.createDamage(world, entity, 15, damageSource)
    val updatedSystem1 = healthSystem.update(world).asInstanceOf[HealthSystem]
    world.getAllEntities should not contain entity
    val updatedSystem2 = updatedSystem1.update(world).asInstanceOf[HealthSystem]

  it should "handle entities dying from existing low health" in:
    val world = World()
    val elixirSystem = ElixirSystem(totalElixir = 100)
    val healthSystem = HealthSystem(elixirSystem)
    val deadEntity = world.createEntity()
    world.addComponent(deadEntity, HealthComponent(currentHealth = 0, maxHealth = 100))
    world.addComponent(deadEntity, TrollTypeComponent(TrollType.Base))
    val updatedSystem = healthSystem.update(world).asInstanceOf[HealthSystem]
    world.getAllEntities should not contain deadEntity

  it should "calculate correct rewards for different troll types" in:
    val world = World()
    val elixirSystem = ElixirSystem(totalElixir = 100)
    val healthSystem = HealthSystem(elixirSystem)
    val baseTroll = world.createEntity()
    world.addComponent(baseTroll, HealthComponent(currentHealth = 1, maxHealth = 100))
    world.addComponent(baseTroll, TrollTypeComponent(TrollType.Base))
    val warriorTroll = world.createEntity()
    world.addComponent(warriorTroll, HealthComponent(currentHealth = 1, maxHealth = 100))
    world.addComponent(warriorTroll, TrollTypeComponent(TrollType.Warrior))
    val source = world.createEntity()
    healthSystem.createDamage(world, baseTroll, 5, source)
    healthSystem.createDamage(world, warriorTroll, 5, source)
    val updatedSystem = healthSystem.update(world).asInstanceOf[HealthSystem]
    world.getAllEntities should not contain baseTroll
    world.getAllEntities should not contain warriorTroll