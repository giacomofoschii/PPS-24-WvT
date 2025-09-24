package it.unibo.pps.wvt.ecs.core

trait System {
  def update(world: World): System
}