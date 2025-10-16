package it.unibo.pps.wvt.ecs.config

import it.unibo.pps.wvt.ecs.components.TrollType
import it.unibo.pps.wvt.utilities.GamePlayConstants.*

/** This object contains methods to calculate various parameters for each wave level in the game. */
object WaveLevel:
  /** It calculates the spawn interval for trolls based on the current wave number.
    * The spawn interval decreases as the wave number increases, but it will not go below a defined minimum.
    * @param wave The current wave number.
    * @return The calculated spawn interval in milliseconds.
    */
  def calculateSpawnInterval(wave: Int): Long =
    Math.max(
      MIN_SPAWN_INTERVAL,
      SPAWN_INTERVAL - (wave - 1) * INTERVAL_DECREASE_PER_WAVE
    )

  private def calculateHealthMultiplier(wave: Int): Double =
    1.0 + (wave - 1) * HEALTH_INCREASE_PER_WAVE

  private def calculateSpeedMultiplier(wave: Int): Double =
    1.0 + (wave - 1) * SPEED_INCREASE_PER_WAVE

  private def calculateDamageMultiplier(wave: Int): Double =
    1.0 + (wave - 1) * DAMAGE_INCREASE_PER_WAVE

  /** It calculates the distribution of different troll types based on the current wave number.
    * The distribution changes as the wave number increases, introducing more challenging troll types.
    * @param wave The current wave number.
    * @return A map containing the troll types and their corresponding probabilities.
    */
  def calculateTrollDistribution(wave: Int): Map[TrollType, Double] =
    wave match
      case w if w <= 1 =>
        Map(
          TrollType.Base     -> 1.0,
          TrollType.Warrior  -> 0.0,
          TrollType.Assassin -> 0.0,
          TrollType.Thrower  -> 0.0
        )

      case w if w <= 2 =>
        Map(
          TrollType.Base     -> 0.7,
          TrollType.Warrior  -> 0.3,
          TrollType.Assassin -> 0.0,
          TrollType.Thrower  -> 0.0
        )

      case w if w <= 3 =>
        Map(
          TrollType.Base     -> 0.5,
          TrollType.Warrior  -> 0.3,
          TrollType.Assassin -> 0.2,
          TrollType.Thrower  -> 0.0
        )

      case w if w <= 4 =>
        Map(
          TrollType.Base     -> 0.4,
          TrollType.Warrior  -> 0.3,
          TrollType.Assassin -> 0.2,
          TrollType.Thrower  -> 0.1
        )

      case _ =>
        Map(
          TrollType.Base     -> 0.3,
          TrollType.Warrior  -> 0.3,
          TrollType.Assassin -> 0.25,
          TrollType.Thrower  -> 0.15
        )

  /** It selects a random troll type based on the provided distribution.
    * The selection is done using a weighted random choice, where each troll type has a probability defined in the distribution map.
    * @param distribution A map containing the troll types and their corresponding probabilities.
    * @return The selected troll type.
    */
  def selectRandomTrollType(distribution: Map[TrollType, Double]): TrollType =
    val random = scala.util.Random.nextDouble()
    distribution
      .toSeq
      .sortBy(_._2)
      .foldLeft((0.0, Option.empty[TrollType])) {
        case ((cumulative, Some(selected)), _) => (cumulative, Some(selected))
        case ((cumulative, None), (trollType, probability)) =>
          val newCumulative = cumulative + probability
          if random <= newCumulative then (newCumulative, Some(trollType))
          else (newCumulative, None)
      }
      ._2
      .getOrElse(TrollType.Base)

  /** It applies multipliers to the base health, speed, and damage of a troll based on the current wave number.
    * The multipliers increase the base values as the wave number increases, making the trolls more
    * @param baseHealth the initial health
    * @param baseSpeed the initial speed
    * @param baseDamage the initial damage
    * @param currentWave the wave that is playing
    * @return the updated stats
    */
  def applyMultipliers(
      baseHealth: Int,
      baseSpeed: Double,
      baseDamage: Int,
      currentWave: Int
  ): (Int, Double, Int) =
    val scaledHealth = (baseHealth * calculateHealthMultiplier(currentWave)).toInt
    val scaledSpeed  = baseSpeed * calculateSpeedMultiplier(currentWave)
    val scaledDamage = (baseDamage * calculateDamageMultiplier(currentWave)).toInt
    (scaledHealth, scaledSpeed, scaledDamage)

  /** It defines how many trolls can spawn per wave
    * @param wave, the wave that we are playing
    */
  def maxTrollsPerWave(wave: Int): Int =
    MAX_TROLLS_PER_WAVE_1 + (wave - 1) * 5
