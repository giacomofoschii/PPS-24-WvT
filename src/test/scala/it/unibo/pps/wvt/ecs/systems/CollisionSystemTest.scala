package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.core.{EntityId, World}
import it.unibo.pps.wvt.utilities.{Position, GridMapper}

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfter

class CollisionSystemTest extends AnyFunSuite with Matchers with BeforeAndAfter:
  var world: World = _
  var collisionSystem: CollisionSystem = _

  before:
    world = new World()
    collisionSystem = CollisionSystem()

  test("should detect collision between projectile and troll"):
    val world = World()
    val system = CollisionSystem()

    val projectilePos = GridMapper.logicalToPhysical((2, 2)).get
    val projectile = world.createEntity()
    world.addComponent(projectile, ProjectileTypeComponent(ProjectileType.Wind))
    world.addComponent(projectile, PositionComponent(projectilePos))
    world.addComponent(projectile, DamageComponent(25, ProjectileType.Wind))

    val troll = world.createEntity()
    world.addComponent(troll, TrollTypeComponent(TrollType.Base))
    world.addComponent(troll, PositionComponent(projectilePos))
    world.addComponent(troll, HealthComponent(100, 100))

    system.update(world)

    world.getAllEntities should not contain projectile
    val trollHealth = world.getComponent[HealthComponent](troll)
    trollHealth.get.currentHealth should be < 100

  test("should not detect collision in different cells"):
    val world = World()
    val system = CollisionSystem()

    val projectile = world.createEntity()
    world.addComponent(projectile, ProjectileTypeComponent(ProjectileType.Wind))
    world.addComponent(projectile, PositionComponent(GridMapper.logicalToPhysical((0, 0)).get))
    world.addComponent(projectile, DamageComponent(25, ProjectileType.Wind))

    val troll = world.createEntity()
    world.addComponent(troll, TrollTypeComponent(TrollType.Base))
    world.addComponent(troll, PositionComponent(GridMapper.logicalToPhysical((5, 5)).get))
    world.addComponent(troll, HealthComponent(100, 100))

    system.update(world)

    world.getAllEntities should contain(projectile)
    world.getAllEntities should contain(troll)

  test("should process melee collision between troll and wizard"):
    val world = World()
    val system = CollisionSystem()

    val pos = GridMapper.logicalToPhysical((2, 2)).get
    val troll = world.createEntity()
    world.addComponent(troll, TrollTypeComponent(TrollType.Base))
    world.addComponent(troll, PositionComponent(pos))
    world.addComponent(troll, AttackComponent(20, 1.0, 1000))

    val wizard = world.createEntity()
    world.addComponent(wizard, WizardTypeComponent(WizardType.Fire))
    world.addComponent(wizard, PositionComponent(pos))
    world.addComponent(wizard, HealthComponent(100, 100))

    system.update(world)

    world.getComponent[CollisionComponent](wizard) shouldBe defined
    world.getComponent[BlockedComponent](troll) shouldBe defined

  test("should apply ice freeze effect"):
    val world = World()
    val system = CollisionSystem()

    val pos = GridMapper.logicalToPhysical((3, 3)).get
    val projectile = world.createEntity()
    world.addComponent(projectile, ProjectileTypeComponent(ProjectileType.Ice))
    world.addComponent(projectile, PositionComponent(pos))
    world.addComponent(projectile, DamageComponent(25, ProjectileType.Ice))

    val troll = world.createEntity()
    world.addComponent(troll, TrollTypeComponent(TrollType.Base))
    world.addComponent(troll, PositionComponent(pos))
    world.addComponent(troll, HealthComponent(100, 100))

    system.update(world)

    world.getComponent[FreezedComponent](troll) shouldBe defined

  test("should remove BlockedComponent when blocker is gone"):
    val world = World()
    val system = CollisionSystem()

    val troll = world.createEntity()
    world.addComponent(troll, TrollTypeComponent(TrollType.Base))
    world.addComponent(troll, PositionComponent(Position(100, 100)))
    world.addComponent(troll, BlockedComponent(EntityId(999)))

    system.update(world)

    world.getComponent[BlockedComponent](troll) shouldBe None