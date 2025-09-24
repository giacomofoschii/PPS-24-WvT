package it.unibo.pps.wvt.ecs.systems

import it.unibo.pps.wvt.ecs.core.{System, World}
import it.unibo.pps.wvt.ecs.components.*
import it.unibo.pps.wvt.utilities.GamePlayConstants.*

case class ElixirSystem(
                         totalElixir: Int = INITIAL_ELIXIR,
                         lastPeriodicGeneration: Long = System.currentTimeMillis()
                       ) extends System:

  override def update(world: World): System =
    updatePeriodicElixirGeneration()
      .updateGeneratorWizardElixir(world)

  // Manage periodic elixir generation
  private def updatePeriodicElixirGeneration(): ElixirSystem =
    val currentTime = System.currentTimeMillis()
    if currentTime - lastPeriodicGeneration >= ELIXIR_GENERATION_INTERVAL then
      copy(
        totalElixir = totalElixir + PERIODIC_ELIXIR,
        lastPeriodicGeneration = currentTime
      )
    else this

  // Manage elixir generation from Generator Wizards
  private def updateGeneratorWizardElixir(world: World): ElixirSystem =
    val generatorEntities = world.getEntitiesWithTwoComponents[WizardTypeComponent, ElixirGeneratorComponent]
    val currentTime = System.currentTimeMillis()
    var updatedSystem = this
    generatorEntities.foreach: entityId =>
      for
        wizardType <- world.getComponent[WizardTypeComponent](entityId)
        elixirGenerator <- world.getComponent[ElixirGeneratorComponent](entityId)
        cooldownComponent <- world.getComponent[CooldownComponent](entityId).orElse(Some(CooldownComponent(0L)))
      yield
        if wizardType.wizardType == WizardType.Generator then
          if cooldownComponent.remainingTime <= currentTime then
            updatedSystem = updatedSystem.copy(totalElixir = updatedSystem.totalElixir + elixirGenerator.elixirPerSecond)
            val newCooldown = CooldownComponent(currentTime + elixirGenerator.cooldown * 1000)
            if world.hasComponent[CooldownComponent](entityId) then
              world.removeComponent[CooldownComponent](entityId)
            world.addComponent(entityId, newCooldown)
    updatedSystem

  // Remove elixir if enough is available
  def spendElixir(amount: Int): (ElixirSystem, Boolean) =
    if totalElixir >= amount then
      (copy(totalElixir = totalElixir - amount), true)
    else (this, false)

  // Add elixir to total
  def addElixir(amount: Int): ElixirSystem =
    copy(totalElixir = totalElixir + amount)

  // Get current amount of elixir
  def getCurrentElixir: Int = totalElixir

  // Check if can afford amount
  def canAfford(amount: Int): Boolean = totalElixir >= amount

  // System reset
  def reset(): ElixirSystem =
    copy(totalElixir = INITIAL_ELIXIR, lastPeriodicGeneration = System.currentTimeMillis())