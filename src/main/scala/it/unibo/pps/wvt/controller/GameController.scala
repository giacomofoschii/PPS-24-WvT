package it.unibo.pps.wvt.controller

import it.unibo.pps.wvt.engine.GameState

class GameController(initialState: GameState) {
  def update(state: GameState, deltaTime: Long): GameState = {
    state.copy(elapsedTime = state.elapsedTime + deltaTime)
  }
}