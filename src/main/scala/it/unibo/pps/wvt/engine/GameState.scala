package it.unibo.pps.wvt.engine

import it.unibo.pps.wvt.engine.GamePhase.*

/** Represents the different phases of the game.
  * The game can be in one of the following phases:
  * - MainMenu: The main menu of the game.
  * - InfoMenu: An informational menu.
  * - Playing: The active gameplay phase.
  * - Paused: The game is paused.
  * - GameOver: The game has ended.
  *
  * Each phase has specific behaviors and allowed transitions to other phases.
  *
  * @see GameState for managing the current state of the game including phase transitions.
  */
sealed trait GamePhase:

  /** Checks if the current phase is a playable phase (i.e., Playing).
    *
    * @return true if the phase is Playing, false otherwise.
    */
  def isPlayable: Boolean = this == GamePhase.Playing

  /** Checks if the current phase is a menu phase (i.e., MainMenu or InfoMenu).
    *
    * @return true if the phase is MainMenu or InfoMenu, false otherwise.
    */
  def isMenu: Boolean = this match
    case GamePhase.MainMenu | GamePhase.InfoMenu => true
    case _                                       => false

/** Companion object for GamePhase containing all possible game phases. */
object GamePhase:
  case object MainMenu extends GamePhase
  case object InfoMenu extends GamePhase
  case object Playing  extends GamePhase
  case object Paused   extends GamePhase
  case object GameOver extends GamePhase

/** Represents the current state of the game, including the current phase, pause status, elapsed time, and frames per second (FPS).
  *
  * @param phase The current phase of the game.
  * @param isPaused Indicates if the game is currently paused.
  * @param elapsedTime The total elapsed time in milliseconds since the game started.
  * @param fps The current frames per second (FPS) of the game.
  */
case class GameState(
    phase: GamePhase = GamePhase.MainMenu,
    isPaused: Boolean = false,
    elapsedTime: Long = 0L,
    fps: Int = 0
):

  /** Checks if the game is currently in a playable phase.
    *
    * @return true if the game is in the Playing phase, false otherwise.
    */
  def isInGame: Boolean = phase.isPlayable

  /** Checks if the game is currently in a menu phase.
    *
    * @return true if the game is in MainMenu or InfoMenu phase, false otherwise.
    */
  def isInMenu: Boolean = phase.isMenu

  /** Transitions the game to a new phase if the transition is valid.
    *
    * @param newPhase The new phase to transition to.
    * @return A new GameState with the updated phase and pause status.
    */
  def transitionTo(newPhase: GamePhase): GameState = newPhase match
    case Paused  => copy(phase = newPhase, isPaused = true)
    case Playing => copy(phase = newPhase, isPaused = false)
    case other   => copy(phase = other)

  /** Updates the elapsed time if the game is not paused.
    *
    * @param deltaTime The time to add to the elapsed time in milliseconds.
    * @return A new GameState with the updated elapsed time.
    */
  def updateTime(deltaTime: Long): GameState =
    Option.when(!isPaused)(copy(elapsedTime = elapsedTime + deltaTime))
      .getOrElse(this)

/** Companion object for GameState providing utility methods.
  *
  * It includes methods to create an initial game state.
  *
  * @see GameState for the representation of the game's current state.
  */
object GameState:

  /** Creates an initial GameState with default values.
    *
    * @return A new GameState representing the initial state of the game.
    */
  def initial(): GameState = GameState()
