package it.unibo.pps.wvt.ecs.core

/**
 * Trait for systems that operate on the world and update its state
 */
trait System:
  def update(world: World): (World, System)
