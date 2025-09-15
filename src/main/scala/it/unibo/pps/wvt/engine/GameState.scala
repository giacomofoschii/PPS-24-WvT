package it.unibo.pps.wvt.engine

case class GameState(
                      phase: GamePhase = GamePhase.MainMenu,
                      isPaused: Boolean = false,
                      elapsedTime: Long = 0L,
                      fps: Int = 0
                    ) {
  def isInGame: Boolean = phase == GamePhase.Playing
  def isInMenu: Boolean = phase == GamePhase.MainMenu || phase == GamePhase.InfoMenu
}

sealed trait GamePhase
object GamePhase {
  case object MainMenu extends GamePhase
  case object InfoMenu extends GamePhase
  case object Playing extends GamePhase
  case object Paused extends GamePhase
  case object GameOver extends GamePhase
}

object GameState {
  def initial(): GameState = GameState()
}