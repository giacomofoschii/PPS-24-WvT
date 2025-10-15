package it.unibo.pps.wvt.engine

import it.unibo.pps.wvt.engine.GamePhase.*

sealed trait GamePhase:
  def isPlayable: Boolean = this == GamePhase.Playing

  def isMenu: Boolean = this match
    case GamePhase.MainMenu | GamePhase.InfoMenu => true
    case _                                       => false

object GamePhase:
  case object MainMenu extends GamePhase
  case object InfoMenu extends GamePhase
  case object Playing  extends GamePhase
  case object Paused   extends GamePhase
  case object GameOver extends GamePhase

case class GameState(
    phase: GamePhase = GamePhase.MainMenu,
    isPaused: Boolean = false,
    elapsedTime: Long = 0L,
    fps: Int = 0
):
  def isInGame: Boolean = phase.isPlayable
  def isInMenu: Boolean = phase.isMenu

  def transitionTo(newPhase: GamePhase): GameState = newPhase match
    case Paused  => copy(phase = newPhase, isPaused = true)
    case Playing => copy(phase = newPhase, isPaused = false)
    case other   => copy(phase = other)

  def updateTime(deltaTime: Long): GameState =
    Option.when(!isPaused)(copy(elapsedTime = elapsedTime + deltaTime))
      .getOrElse(this)

object GameState:
  def initial(): GameState = GameState()

  def isValidTransition(from: GamePhase, to: GamePhase): Boolean = (from, to) match
    case (Playing, Paused) => true
    case (Paused, Playing) => true
    case (_, MainMenu)     => true
    case (MainMenu, _)     => true
    case _                 => false
