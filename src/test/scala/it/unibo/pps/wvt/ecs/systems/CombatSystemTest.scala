package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.core.World
import it.unibo.pps.wvt.utilities.{GridMapper, Position}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CombatSystemTest extends AnyFlatSpec with Matchers:

  behavior of "CombatSystem"

  it should "make a wizard spawn a projectile when a troll is in range" in:
    var world = World()
    val wizardPos = GridMapper.logicalToPhysical(2, 2).get
    val trollPos = GridMapper.logicalToPhysical(2, 5).get

    val (w1, wizard) = world.createEntity()
    world = w1.addComponent(wizard, WizardTypeComponent(WizardType.Fire))
      .addComponent(wizard, PositionComponent(wizardPos))
      .addComponent(wizard, AttackComponent(damage = 50, range = 4.0, cooldown = 2500L))

    val (w2, troll) = world.createEntity()
    world = w2.addComponent(troll, TrollTypeComponent(TrollType.Base))
      .addComponent(troll, PositionComponent(trollPos))

    val (finalWorld, _) = CombatSystem().update(world)

    finalWorld.getEntitiesByType("projectile").size shouldBe 1
    finalWorld.getComponent[CooldownComponent](wizard) shouldBe defined

  it should "not make a wizard attack if no target is in range" in:
    var world = World()
    val wizardPos = GridMapper.logicalToPhysical(2, 2).get
    val trollPos = GridMapper.logicalToPhysical(2, 8).get // Fuori portata

    val (w1, wizard) = world.createEntity()
    world = w1.addComponent(wizard, WizardTypeComponent(WizardType.Fire))
      .addComponent(wizard, PositionComponent(wizardPos))
      .addComponent(wizard, AttackComponent(damage = 50, range = 4.0, cooldown = 2500L))

    val (w2, troll) = world.createEntity()
    world = w2.addComponent(troll, TrollTypeComponent(TrollType.Base))
      .addComponent(troll, PositionComponent(trollPos))

    val (finalWorld, _) = CombatSystem().update(world)

    finalWorld.getEntitiesByType("projectile") shouldBe empty
    finalWorld.getComponent[CooldownComponent](wizard) shouldBe None

  it should "not make a wizard attack if it is on cooldown" in:
    var world = World()
    val wizardPos = GridMapper.logicalToPhysical(2, 2).get
    val trollPos = GridMapper.logicalToPhysical(2, 5).get

    val (w1, wizard) = world.createEntity()
    world = w1.addComponent(wizard, WizardTypeComponent(WizardType.Fire))
      .addComponent(wizard, PositionComponent(wizardPos))
      .addComponent(wizard, AttackComponent(damage = 50, range = 4.0, cooldown = 2500L))
      .addComponent(wizard, CooldownComponent(1000L)) // Cooldown attivo

    val (w2, troll) = world.createEntity()
    world = w2.addComponent(troll, TrollTypeComponent(TrollType.Base))
      .addComponent(troll, PositionComponent(trollPos))

    val (finalWorld, _) = CombatSystem().update(world)

    finalWorld.getEntitiesByType("projectile") shouldBe empty

  it should "make a Thrower troll attack a wizard in range" in:
    var world = World()
    val trollPos = GridMapper.logicalToPhysical(3, 7).get
    val wizardPos = GridMapper.logicalToPhysical(3, 4).get

    val (w1, troll) = world.createEntity()
    world = w1.addComponent(troll, TrollTypeComponent(TrollType.Thrower))
      .addComponent(troll, PositionComponent(trollPos))
      .addComponent(troll, AttackComponent(damage = 10, range = 5.0, cooldown = 3000L))

    val (w2, wizard) = world.createEntity()
    world = w2.addComponent(wizard, WizardTypeComponent(WizardType.Ice))
      .addComponent(wizard, PositionComponent(wizardPos))

    val (finalWorld, _) = CombatSystem().update(world)

    finalWorld.getEntitiesByType("projectile").size shouldBe 1
    val projectileType = finalWorld.getComponent[ProjectileTypeComponent](finalWorld.getEntitiesByType("projectile").head)
    projectileType.get.projectileType shouldBe ProjectileType.Troll
    finalWorld.getComponent[CooldownComponent](troll) shouldBe defined

  it should "decrease timers for CooldownComponent and FreezedComponent over time" in:
    var world = World()
    val (w1, entity) = world.createEntity()
    world = w1.addComponent(entity, CooldownComponent(100L))
      .addComponent(entity, FreezedComponent(100L, 0.5))

    val (finalWorld, _) = CombatSystem().update(world)

    val cooldown = finalWorld.getComponent[CooldownComponent](entity)
    val freezed = finalWorld.getComponent[FreezedComponent](entity)

    cooldown.get.remainingTime should be < 100L
    freezed.get.remainingTime should be < 100L

  it should "remove components when their timers reach zero" in:
    var world = World()
    val (w1, entity) = world.createEntity()
    world = w1.addComponent(entity, CooldownComponent(16L)) // Scade in un tick
      .addComponent(entity, FreezedComponent(10L, 0.5))

    val (finalWorld, _) = CombatSystem().update(world)

    finalWorld.getComponent[CooldownComponent](entity) shouldBe None
    finalWorld.getComponent[FreezedComponent](entity) shouldBe None