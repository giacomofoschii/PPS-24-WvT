package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.utilities.*
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.ecs.core.*
import it.unibo.pps.wvt.view.GameView

class RenderSystem extends System{
  override def update(world: World): Unit =
    val trollPositions = world.getEntitiesByType("troll").flatMap(entity =>
      world.getComponent[PositionComponent](entity).map(_.position)
    ).map(GridMapper.logicalToPhysical).toSeq

    val wizardPositions = world.getEntitiesByType("wizard").flatMap(entity =>
      world.getComponent[PositionComponent](entity).map(_.position)
    ).map(GridMapper.logicalToPhysical).toSeq

    GameView.drawGrid(wizardPositions, trollPositions)

}