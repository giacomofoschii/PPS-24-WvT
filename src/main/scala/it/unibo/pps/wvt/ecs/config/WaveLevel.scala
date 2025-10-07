package it.unibo.pps.wvt.ecs.config

import it.unibo.pps.wvt.ecs.components.TrollType
import it.unibo.pps.wvt.utilities.GamePlayConstants.*

object WaveLevel:

  def calculateSpawnInterval(wave: Int): Long =
    Math.max(
      MIN_SPAWN_INTERVAL,
      INITIAL_SPAWN_INTERVAL - (wave - 1) * INTERVAL_DECREASE_PER_WAVE
    )

  private def calculateHealthMultiplier(wave: Int): Double =
    1.0 + (wave - 1) * HEALTH_INCREASE_PER_WAVE

  private def calculateSpeedMultiplier(wave: Int): Double =
    1.0 + (wave - 1) * SPEED_INCREASE_PER_WAVE

  private def calculateDamageMultiplier(wave: Int): Double =
    1.0 + (wave - 1) * DAMAGE_INCREASE_PER_WAVE

  def calculateTrollDistribution(wave: Int): Map[TrollType, Double] =
    wave match
      case w if w <= 2 =>
        Map(
          TrollType.Base -> 1.0,
          TrollType.Warrior -> 0.0,
          TrollType.Assassin -> 0.0,
          TrollType.Thrower -> 0.0
        )

      case w if w <= 4 =>
        Map(
          TrollType.Base -> 0.7,
          TrollType.Warrior -> 0.3,
          TrollType.Assassin -> 0.0,
          TrollType.Thrower -> 0.0
        )

      case w if w <= 6 =>
        Map(
          TrollType.Base -> 0.5,
          TrollType.Warrior -> 0.3,
          TrollType.Assassin -> 0.2,
          TrollType.Thrower -> 0.0
        )

      case w if w <= 9 =>
        Map(
          TrollType.Base -> 0.4,
          TrollType.Warrior -> 0.3,
          TrollType.Assassin -> 0.2,
          TrollType.Thrower -> 0.1
        )

      case _ =>
        Map(
          TrollType.Base -> 0.3,
          TrollType.Warrior -> 0.3,
          TrollType.Assassin -> 0.25,
          TrollType.Thrower -> 0.15
        )

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

    TrollType.Base

  def applyMultipliers(
                        baseHealth: Int,
                        baseSpeed: Double,
                        baseDamage: Int,
                        currentWave: Int
                      ): (Int, Double, Int) =
    val scaledHealth = (baseHealth * calculateHealthMultiplier(currentWave)).toInt
    val scaledSpeed = baseSpeed * calculateSpeedMultiplier(currentWave)
    val scaledDamage = (baseDamage * calculateDamageMultiplier(currentWave)).toInt
    (scaledHealth, scaledSpeed, scaledDamage)

  def maxTrollsPerWave(wave: Int): Int =
    MAX_TROLLS_PER_WAVE_1 +(wave - 1) * 5