package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.World
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.components.TrollType.*
import it.unibo.pps.wvt.utilities.Position
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MovementSystemTest extends AnyFlatSpec with Matchers:

  "MovementSystem" should "move base trolls straight left" in:
    val world = World()
    val system = MovementSystem()

    val troll = world.createEntity()
    world.addComponent(troll, TrollTypeComponent(Base))
    world.addComponent(troll, PositionComponent(Position(2, 5)))
    world.addComponent(troll, MovementComponent(speed = 1.0))

    system.update(world)

    val newPosition = world.getComponent[PositionComponent](troll)
    newPosition shouldBe defined
    newPosition.get.position.row shouldBe 2
    newPosition.get.position.col shouldBe 4 // 5 - 1

  it should "move warrior trolls straight left" in:
    val world = World()
    val system = MovementSystem()

    val warrior = world.createEntity()
    world.addComponent(warrior, TrollTypeComponent(Warrior))
    world.addComponent(warrior, PositionComponent(Position(1, 8)))
    world.addComponent(warrior, MovementComponent(speed = 2.0))

    system.update(world)

    val newPosition = world.getComponent[PositionComponent](warrior)
    newPosition shouldBe defined
    newPosition.get.position.row shouldBe 1
    newPosition.get.position.col shouldBe 6 // 8 - 2

  it should "move assassin trolls in zigzag pattern" in:
    val world = World()
    val system = MovementSystem()

    // Test even column (should move up)
    val assassin1 = world.createEntity()
    world.addComponent(assassin1, TrollTypeComponent(Assassin))
    world.addComponent(assassin1, PositionComponent(Position(2, 4))) // Even column
    world.addComponent(assassin1, MovementComponent(speed = 1.0))

    system.update(world)

    val pos1 = world.getComponent[PositionComponent](assassin1)
    pos1.get.position.row shouldBe 1 // 2 - 1 (zigzag up)
    pos1.get.position.col shouldBe 3 // 4 - 1 (left)

  it should "move assassin trolls in zigzag pattern from odd column" in:
    val world = World()
    val system = MovementSystem()

    // Test odd column (should move down)
    val assassin2 = world.createEntity()
    world.addComponent(assassin2, TrollTypeComponent(Assassin))
    world.addComponent(assassin2, PositionComponent(Position(2, 5))) // Odd column
    world.addComponent(assassin2, MovementComponent(speed = 1.0))

    system.update(world)

    val pos2 = world.getComponent[PositionComponent](assassin2)
    pos2.get.position.row shouldBe 3 // 2 + 1 (zigzag down)
    pos2.get.position.col shouldBe 4 // 5 - 1 (left)

  it should "handle assassin zigzag at boundaries" in:
    val world = World()
    val system = MovementSystem()

    // Test top boundary
    val topAssassin = world.createEntity()
    world.addComponent(topAssassin, TrollTypeComponent(Assassin))
    world.addComponent(topAssassin, PositionComponent(Position(0, 4))) // Top row, even col
    world.addComponent(topAssassin, MovementComponent(speed = 1.0))

    system.update(world)

    val topPos = world.getComponent[PositionComponent](topAssassin)
    topPos.get.position.row shouldBe 0 // Should stay at 0 (boundary)
    topPos.get.position.col shouldBe 3

  it should "stop thrower trolls at specified column" in:
    val world = World()
    val system = MovementSystem()

    val thrower = world.createEntity()
    world.addComponent(thrower, TrollTypeComponent(Thrower))
    world.addComponent(thrower, PositionComponent(Position(3, 8)))
    world.addComponent(thrower, MovementComponent(speed = 1.0))

    system.update(world)

    val newPosition = world.getComponent[PositionComponent](thrower)
    newPosition.get.position.row shouldBe 3
    newPosition.get.position.col shouldBe 7 // 8 - 1

  it should "not move thrower trolls beyond stop column" in:
    val world = World()
    val system = MovementSystem()

    val thrower = world.createEntity()
    world.addComponent(thrower, TrollTypeComponent(Thrower))
    world.addComponent(thrower, PositionComponent(Position(1, 6))) // At stop column
    world.addComponent(thrower, MovementComponent(speed = 1.0))

    system.update(world)

    val newPosition = world.getComponent[PositionComponent](thrower)
    newPosition.get.position.row shouldBe 1
    newPosition.get.position.col shouldBe 6 // Should not move

  it should "move troll projectiles straight left" in:
      val world = World()
      val system = MovementSystem()
    
      val projectile = world.createEntity()
      world.addComponent(projectile, ProjectileTypeComponent(ProjectileType.Troll))
      world.addComponent(projectile, PositionComponent(Position(2, 5)))
      world.addComponent(projectile, MovementComponent(speed = 2.0))
    
      system.update(world)
    
      val newPosition = world.getComponent[PositionComponent](projectile)
      newPosition.get.position.row shouldBe 2
      newPosition.get.position.col shouldBe 3 // 5 - 2

  it should "move wizard projectiles straight right" in:
    val world = World()
    val system = MovementSystem()

    val projectile = world.createEntity()
    world.addComponent(projectile, ProjectileTypeComponent(ProjectileType.Fire))
    world.addComponent(projectile, PositionComponent(Position(1, 2)))
    world.addComponent(projectile, MovementComponent(speed = 1.5))

    system.update(world)

    val newPosition = world.getComponent[PositionComponent](projectile)
    newPosition.get.position.row shouldBe 1
    newPosition.get.position.col shouldBe 4 // 2 + ceil(1.5)

  it should "not move entities at left boundary" in:
    val world = World()
    val system = MovementSystem()

    val troll = world.createEntity()
    world.addComponent(troll, TrollTypeComponent(Base))
    world.addComponent(troll, PositionComponent(Position(2, 0))) // At left edge
    world.addComponent(troll, MovementComponent(speed = 1.0))

    system.update(world)

    val newPosition = world.getComponent[PositionComponent](troll)
    newPosition.get.position.col shouldBe 0 // Should not move left

  it should "not move projectiles at right boundary" in:
    val world = World()
    val system = MovementSystem()

    val projectile = world.createEntity()
    world.addComponent(projectile, ProjectileTypeComponent(ProjectileType.Fire))
    world.addComponent(projectile, PositionComponent(Position(1, 8))) // At right edge
    world.addComponent(projectile, MovementComponent(speed = 1.0))

    system.update(world)

    val newPosition = world.getComponent[PositionComponent](projectile)
    newPosition.get.position.col shouldBe 8 // Should not move right

  it should "not move entities with zero speed" in:
    val world = World()
    val system = MovementSystem()

    val troll = world.createEntity()
    world.addComponent(troll, TrollTypeComponent(Base))
    world.addComponent(troll, PositionComponent(Position(2, 5)))
    world.addComponent(troll, MovementComponent(speed = 0.0))

    system.update(world)

    val newPosition = world.getComponent[PositionComponent](troll)
    newPosition.get.position shouldBe Position(2, 5) // Should not move

  it should "not move entities without movement component" in:
    val world = World()
    val system = MovementSystem()

    val entity = world.createEntity()
    world.addComponent(entity, PositionComponent(Position(1, 3)))

    system.update(world)

    val position = world.getComponent[PositionComponent](entity)
    position.get.position shouldBe Position(1, 3) // Should not move