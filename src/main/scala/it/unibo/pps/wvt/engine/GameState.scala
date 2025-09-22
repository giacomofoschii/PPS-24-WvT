package it.unibo.pps.wvt.engine

enum GamePhase {
  case MainMenu, InfoMenu, Playing, Paused, GameOver

  def isPlayable: Boolean = this == Playing
  def isMenu: Boolean = this == MainMenu || this == InfoMenu
}

case class GameState(
                      phase: GamePhase = GamePhase.MainMenu,
                      isPaused: Boolean = false,
                      elapsedTime: Long = 0L,
                      fps: Int = 0
                    ) {
  def isInGame: Boolean = phase.isPlayable
  def isInMenu: Boolean = phase.isMenu

  // functional method for the state transition
  def transitionTo(newPhase: GamePhase): GameState = newPhase match {
    case GamePhase.Paused => copy(phase = newPhase, isPaused = true)
    case GamePhase.Playing => copy(phase = newPhase, isPaused = false)
    case other => copy(phase = other)
  }

  def updateTime(deltaTime: Long): GameState =
    if (!isPaused) copy(elapsedTime = elapsedTime + deltaTime)
    else this
}

object GameState {
  def initial(): GameState = GameState()

  // Helper for transition validation
  def isValidTransition(from: GamePhase, to: GamePhase): Boolean = (from, to) match {
    case (GamePhase.Playing, GamePhase.Paused) => true
    case (GamePhase.Paused, GamePhase.Playing) => true
    case (_, GamePhase.MainMenu) => true
    case (GamePhase.MainMenu, _) => true
    case _ => false
  }
}