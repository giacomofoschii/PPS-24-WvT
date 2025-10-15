package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.core.World
import it.unibo.pps.wvt.utilities.Position
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RenderSystemTest extends AnyFlatSpec with Matchers:

  // Helper per accedere ai metodi privati per i test
  private implicit class RenderSystemTestHelper(system: RenderSystem):
    def collectEntitiesWithImages(world: World): Seq[(Position, String)] =
      val method = classOf[RenderSystem].getDeclaredMethod("collectEntitiesWithImages", classOf[World])
      method.setAccessible(true)
      method.invoke(system, world).asInstanceOf[Seq[(Position, String)]]

    def shouldRender(state: String): Boolean =
      val method = classOf[RenderSystem].getDeclaredMethod("shouldRender", classOf[String])
      method.setAccessible(true)
      method.invoke(system, state).asInstanceOf[Boolean]

  behavior of "RenderSystem"

  it should "collect all entities that have both PositionComponent and ImageComponent" in:
    var world         = World()
    val (w1, entity1) = world.createEntity()
    world = w1.addComponent(entity1, PositionComponent(Position(10, 10)))
      .addComponent(entity1, ImageComponent("/test.png"))

    val (w2, entity2) = world.createEntity()
    world = w2.addComponent(entity2, PositionComponent(Position(20, 20)))
    // entity2 non ha ImageComponent

    val renderSystem      = RenderSystem()
    val collectedEntities = renderSystem.collectEntitiesWithImages(world)

    collectedEntities should have size 1
    collectedEntities.head shouldBe (Position(10, 10), "/test.png")

  it should "use a different image path for freezed entities" in:
    var world        = World()
    val (w1, entity) = world.createEntity()
    world = w1.addComponent(entity, PositionComponent(Position(10, 10)))
      .addComponent(entity, ImageComponent("/troll/BaseTroll.png"))
      .addComponent(entity, FreezedComponent(1000, 0.5))

    val renderSystem      = RenderSystem()
    val collectedEntities = renderSystem.collectEntitiesWithImages(world)

    collectedEntities.head._2 shouldBe "/freezed/troll/BaseTroll.png"

  it should "render on the first update" in:
    val renderSystem = RenderSystem()
    renderSystem.shouldRender("any_state_hash") shouldBe true

  it should "not render if the state hash is unchanged" in:
    val stateHash    = "identical_hash"
    val renderSystem = RenderSystem(lastRenderedState = Some(stateHash))

    renderSystem.shouldRender(stateHash) shouldBe false

  it should "render if the state hash has changed" in:
    val renderSystem = RenderSystem(lastRenderedState = Some("old_hash"))
    renderSystem.shouldRender("new_hash") shouldBe true

  it should "force a render by clearing the last rendered state" in:
    val renderSystem = RenderSystem(lastRenderedState = Some("initial_hash"))
    val forcedSystem = renderSystem.forceRender()

    forcedSystem.shouldRender("initial_hash") shouldBe true
