package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.{System, World}
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.utilities.GamePlayConstants.*
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

class ElixirSystem extends System {

  private val totalElixir: AtomicInteger = AtomicInteger(INITIAL_ELIXIR)
  private val lastPeriodicGeneration: AtomicLong = AtomicLong(System.currentTimeMillis())

  override def update(world: World): Unit = {
    updatePeriodicElixirGeneration()
    updateGeneratorWizardElixir(world)
  }

  /**
   * Manage periodic elixir generation
   */
  private def updatePeriodicElixirGeneration(): Unit =
    val currentTime = System.currentTimeMillis()
    val lastGen = lastPeriodicGeneration.get()
    if (currentTime - lastGen >= ELIXIR_GENERATION_INTERVAL)
      if (lastPeriodicGeneration.compareAndSet(lastGen, currentTime))
        totalElixir.addAndGet(PERIODIC_ELIXIR)

  /**
   * Manage elixir generation from Generator Wizards
   */
  private def updateGeneratorWizardElixir(world: World): Unit =
    val generatorEntities = world.getEntitiesWithTwoComponents[WizardTypeComponent, ElixirGeneratorComponent]
    val currentTime = System.currentTimeMillis()
    generatorEntities.foreach { entityId =>
      for
        wizardType <- world.getComponent[WizardTypeComponent](entityId)
        elixirGenerator <- world.getComponent[ElixirGeneratorComponent](entityId)
        cooldownComponent <- world.getComponent[CooldownComponent](entityId).orElse(Some(CooldownComponent(0L)))
      yield
        if (wizardType.wizardType == WizardType.Generator)
          if (cooldownComponent.remainingTime <= currentTime)
            totalElixir.addAndGet(elixirGenerator.elixirPerSecond)
            val newCooldown = CooldownComponent(currentTime + elixirGenerator.cooldown * 1000)
            if (world.hasComponent[CooldownComponent](entityId))
              world.removeComponent[CooldownComponent](entityId)
            world.addComponent(entityId, newCooldown)
    }

  /**
   * Remove elixir if enough is available
   */
  def spendElixir(amount: Int): Boolean = {
    val current = totalElixir.get()
    if (current >= amount) {
      if (totalElixir.compareAndSet(current, current - amount)) {
        true
      } else {
        spendElixir(amount)
      }
    } else {
      false
    }
  }



  /**
   * Gives the current amount of elixir
   */
  def getCurrentElixir: Int = totalElixir.get()


  def canAfford(amount: Int): Boolean = totalElixir.get() >= amount

  /**
   * System reset
   */
  def reset(): Unit = {
    totalElixir.set(INITIAL_ELIXIR)
    lastPeriodicGeneration.set(System.currentTimeMillis())
  }
}