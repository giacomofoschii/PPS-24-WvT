package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.core.{EntityId, World}
import it.unibo.pps.wvt.utilities.{GridMapper, Position}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfter

class CollisionSystemTest extends AnyFlatSpec with Matchers with BeforeAndAfter:

  var world: World                     = _
  var collisionSystem: CollisionSystem = _

  before:
    world = World()
    collisionSystem = CollisionSystem()

  behavior of "CollisionSystem"

  it should "detect collision between a projectile and a troll in the same cell" in:
    val projectilePos        = GridMapper.logicalToPhysical((2, 2)).get
    val (world1, projectile) = world.createEntity()
    var w = world1.addComponent(projectile, ProjectileTypeComponent(ProjectileType.Wind))
      .addComponent(projectile, PositionComponent(projectilePos))
      .addComponent(projectile, DamageComponent(25, ProjectileType.Wind))

    val (world2, troll) = w.createEntity()
    w = world2.addComponent(troll, TrollTypeComponent(TrollType.Base))
      .addComponent(troll, PositionComponent(projectilePos))
      .addComponent(troll, HealthComponent(100, 100))

    val (finalWorld, _) = collisionSystem.update(w)

    finalWorld.getAllEntities should not contain projectile

    val collisionComp = finalWorld.getComponent[CollisionComponent](troll)
    collisionComp shouldBe defined
    collisionComp.get.amount shouldBe 25

  it should "not detect collision when entities are in different cells" in:
    val (world1, projectile) = world.createEntity()
    var w = world1.addComponent(projectile, ProjectileTypeComponent(ProjectileType.Wind))
      .addComponent(projectile, PositionComponent(GridMapper.logicalToPhysical((0, 0)).get))
      .addComponent(projectile, DamageComponent(25, ProjectileType.Wind))

    val (world2, troll) = w.createEntity()
    w = world2.addComponent(troll, TrollTypeComponent(TrollType.Base))
      .addComponent(troll, PositionComponent(GridMapper.logicalToPhysical((1, 1)).get))
      .addComponent(troll, HealthComponent(100, 100))

    val (finalWorld, _) = collisionSystem.update(w)

    finalWorld.getAllEntities should contain(projectile)
    finalWorld.getComponent[HealthComponent](troll).get.currentHealth shouldBe 100

  it should "process melee collision between a troll and a wizard" in:
    val pos             = GridMapper.logicalToPhysical((2, 2)).get
    val (world1, troll) = world.createEntity()
    var w = world1.addComponent(troll, TrollTypeComponent(TrollType.Base))
      .addComponent(troll, PositionComponent(pos))
      .addComponent(troll, AttackComponent(20, 1.0, 1000))

    val (world2, wizard) = w.createEntity()
    w = world2.addComponent(wizard, WizardTypeComponent(WizardType.Fire))
      .addComponent(wizard, PositionComponent(pos))
      .addComponent(wizard, HealthComponent(100, 100))

    val (finalWorld, _) = collisionSystem.update(w)

    finalWorld.getComponent[CollisionComponent](wizard) shouldBe defined
    finalWorld.getComponent[BlockedComponent](troll) shouldBe defined

  it should "apply ice freeze effect on collision" in:
    val pos                  = GridMapper.logicalToPhysical((3, 3)).get
    val (world1, projectile) = world.createEntity()
    var w = world1.addComponent(projectile, ProjectileTypeComponent(ProjectileType.Ice))
      .addComponent(projectile, PositionComponent(pos))
      .addComponent(projectile, DamageComponent(10, ProjectileType.Ice))

    val (world2, troll) = w.createEntity()
    w = world2.addComponent(troll, TrollTypeComponent(TrollType.Base))
      .addComponent(troll, PositionComponent(pos))
      .addComponent(troll, HealthComponent(100, 100))

    val (finalWorld, _) = collisionSystem.update(w)

    finalWorld.getComponent[FreezedComponent](troll) shouldBe defined
    finalWorld.getComponent[FreezedComponent](troll).get.speedModifier shouldBe 0.5
