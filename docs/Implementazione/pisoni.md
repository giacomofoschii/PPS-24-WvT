---
title: Giovanni Pisoni
nav_order: 4
parent: Implementazione
---

# Implementazione - Giovanni Pisoni

---

## Panoramica dei Contributi

Il mio contributo al progetto si è focalizzato sulle seguenti aree:

* **Architettura del `GameEngine`**: Definizione del `GameEngine` e del `GameState` per la gestione dello stato di gioco.

* **Implementazione del `GameLoop`**: Creazione di un game loop a timestep fisso per garantire aggiornamenti consistenti.

* **Implementazione del `GameController` e gestione degli eventi**: Sviluppo di un sistema di gestione degli eventi per 
disaccoppiare i componenti del sistema e implementazione del `GameController` per coordinare le interazioni 
tra i vari sistemi di gioco.
* **Logica dei Troll**: Implementazione del `MovementSystem` e dello `SpawnSystem` per il comportamento dei nemici.

* **Interfaccia utente**: Sviluppo dei menu di pausa e delle schermate di vittoria/sconfitta.

* **Testing**: Scrittura di test per i sistemi implementati, come `MovementSystemTest` e `SpawnSystemTest`.


## Implementazione - Game Engine e Game Loop

Il cuore del gioco è rappresentato dal `GameEngine` e dal `GameLoop`, componenti che ho sviluppato per orchestrare 
l'intero flusso di gioco.

---

### Game Engine

Il `GameEngine` è stato progettato come una macchina a stati finiti che gestisce le fasi principali del 
gioco (`MainMenu`, `Playing`, `Paused`, `GameOver`). L'engine è responsabile di:

* **Inizializzare e arrestare il gioco**: Avvia e ferma il `GameLoop` e gestisce il ciclo di vita del `GameController`.

* **Gestire lo stato del gioco**: Mantiene il `GameState` corrente, che include la fase di gioco, il tempo trascorso 
e lo stato di pausa.

* **Coordinare gli aggiornamenti**: Durante la fase `Playing`, invoca il metodo `update` del `GameController` 
per far avanzare la logica di gioco.

L'immutabilità è un principio chiave: ogni modifica dello stato non altera l'oggetto corrente, 
ma ne crea una nuova istanza. Questo approccio funzionale previene effetti collaterali e 
semplifica la gestione dello stato.

```scala
case class GameState(
    phase: GamePhase = GamePhase.MainMenu,
    isPaused: Boolean = false,
    elapsedTime: Long = 0L,
    fps: Int = 0
):
  def transitionTo(newPhase: GamePhase): GameState = newPhase match
    case Paused  => copy(phase = newPhase, isPaused = true)
    case Playing => copy(phase = newPhase, isPaused = false)
    case other   => copy(phase = other)
```

### Game Loop

Per garantire un'esperienza di gioco fluida e un comportamento deterministico, ho implementato un 
**game loop a timestep fisso**.Questo approccio disaccoppia la logica di gioco dalla velocità di rendering, 
assicurando che il gioco si comporti allo stesso modo su hardware diversi.

Il game loop a timestep fisso garantisce:
- **Determinismo**: il gioco si comporta identicamente su hardware diverso
- **Stabilità fisica**: la simulazione rimane coerente indipendentemente dal frame rate
- **Prevedibilità**: facilita il testing e il debugging del comportamento di gioco.

Il `GameLoop` utilizza un `accumulator` per gestire il tempo trascorso tra i fotogrammi. La logica di gioco 
viene aggiornata in passi discreti di tempo fisso (`FRAME_TIME_MILLIS`), garantendo che, anche in 
caso di cali di frame rate, la simulazione di gioco progredisca correttamente.

La gestione del ciclo di aggiornamenti avviene attraverso il metodo `processAccumulatedFrames`. 
Questo metodo verifica se il tempo accumulato è sufficiente per eseguire un passo di aggiornamento 
della logica di gioco. In caso affermativo, invoca il metodo `update` del `GameEngine` e sottrae 
il tempo fisso dall'accumulatore.

```scala
@tailrec
private def processAccumulatedFrames(): Unit =
  val state = readState
  if state.hasAccumulatedTime(fixedTimeStep) && !engine.isPaused then
    engine.update(fixedTimeStep)
    updateState(_.consumeTimeStep(fixedTimeStep))
    processAccumulatedFrames()
```

## Implementazione - Game Controller e gestione degli eventi

---

### Game Controller e Gestione degli Stati

Il `GameController` agisce come il principale orchestratore del gioco, facendo da ponte tra l'input dell'utente, 
la logica di gioco (i sistemi ECS) e il `GameEngine`. La sua responsabilità è quella di tradurre le azioni 
del giocatore e gli eventi di sistema in aggiornamenti dello stato del mondo di gioco.

Per gestire la complessità dei numerosi sistemi che compongono la logica del gioco 
(movimento, combattimento, generazione di elisir, ecc.), ho introdotto la case class `GameSystemsState`. 
Questa classe incapsula lo stato di tutti i sistemi, garantendo che vengano aggiornati in un ordine predicibile e coerente.

```scala
// in GameSystemsState.scala
case class GameSystemsState(
    movement: MovementSystem,
    collision: CollisionSystem,
    combat: CombatSystem,
    elixir: ElixirSystem,
    health: HealthSystem,
    spawn: SpawnSystem,
    render: RenderSystem,
    selectedWizardType: Option[WizardType] = None,
    currentWave: Int = 1
)
```
Il metodo `updateAll` all'interno di `GameSystemsState` è cruciale: definisce la pipeline di esecuzione dei 
sistemi ad ogni ciclo di gioco. L'ordine di esecuzione è fondamentale:ad esempio, il `MovementSystem` viene eseguito 
prima del `CollisionSystem` per garantire che le collisioni vengano rilevate sulle nuove posizioni, 
e l'`HealthSystem` viene eseguito dopo, per applicare i danni risultanti. Questa struttura garantisce 
che le interdipendenze tra i sistemi siano gestite correttamente.

```scala
// in GameSystemsState.scala
def updateAll(world: World): (World, GameSystemsState) =
    val (world1, updatedElixir)    = elixir.update(world)
    val (world2, updatedMovement)  = movement.update(world1)
    val (world3, updatedCombat)    = combat.update(world2)
    val (world4, updatedCollision) = collision.update(world3)
    // ... e così via per gli altri sistemi
```

Il `GameController` mantiene un'istanza di `GameSystemsState` e la utilizza per evolvere lo stato del gioco. 
Inoltre, gestisce le azioni del giocatore, come il posizionamento dei maghi, verificando 
le condizioni necessarie (es. elisir sufficiente) e aggiornando lo stato di conseguenza in modo asincrono tramite 
una coda di azioni (`pendingActions`), per evitare problemi di concorrenza con il game loop.

### Gestione degli Eventi

Per disaccoppiare i vari componenti del gioco e gestire le transizioni di stato in modo pulito e centralizzato, 
ho implementato un sistema di eventi. Questo sistema si basa su due componenti principali: `EventSystem` e `EventHandler`.

`EventSystem` definisce la gerarchia degli eventi di gioco (`GameEvent`) e una `EventQueue` immutabile. 
Gli eventi sono stati suddivisi per priorità, per garantire che le operazioni critiche (come `ExitGame`) vengano 
processate prima di altre.

```scala
// in EventSystem.scala
trait GameEvent:
  def priority: Int

object GameEvent:
  // System events
  case object ExitGame extends GameEvent:
    override def priority: Int = 0
  // Game State events
  case object GameWon extends GameEvent:
    override def priority: Int = 1
  // Menu events
  case object ShowMainMenu extends GameEvent:
    override def priority: Int = 2
  // Input events
  case class GridClicked(logicalPos: LogicalCoords, screenX: Int, screenY: Int) extends GameEvent:
    override def priority: Int = 3
```

L'`EventHandler` è il cuore del sistema di eventi. È responsabile della gestione della coda di eventi e del dispatching 
degli stessi ai gestori appropriati. Utilizza un `AtomicReference` per gestire il suo stato (`EventHandlerState`) in 
modo thread-safe, un aspetto cruciale dato che gli eventi possono essere generati da thread diversi 
(es. il thread del game loop e il thread della UI).

Il metodo processEvent dell'`EventHandler` funge da macchina a stati finiti, gestendo le transizioni tra le varie 
fasi del gioco (`GamePhase`) in risposta a eventi specifici. Ad esempio, quando riceve un evento `ShowGameView`, 
non solo cambia la vista, ma avvia anche il GameEngine se non è già in esecuzione.

```scala
// in EventHandler.scala
  private def handleEvent(event: GameEvent): Unit =
    val state = stateRef.get()

    event match
      case ShowMainMenu =>
        handleMenuTransition(MainMenu, Some(ViewState.MainMenu))
        Option.when(isGameActive)(stopEngine())
      case ShowGameView =>
        handleMenuTransition(Playing, Some(ViewState.GameView))
        Option.when(!engine.isRunning)(startEngine())
      case Pause if state.currentPhase == Playing =>
        pauseEngine()
        handleMenuTransition(Paused, Some(ViewState.PauseMenu))
      // ... altri casi di eventi
```

Questa architettura a eventi permette di avere un controllo centralizzato e prevedibile sul flusso del gioco, 
rendendo il sistema più robusto e facile da estendere con nuove funzionalità e interazioni.

## Implementazione - Logica dei Troll

La logica comportamentale delle entità nemiche, i troll, è stata implementata attraverso due sistemi dedicati 
all'interno dell'architettura Entity-Component-System (ECS): lo `SpawnSystem` e il `MovementSystem`. 
Questi moduli sono responsabili, rispettivamente, della generazione procedurale delle ondate di nemici e 
della gestione del loro comportamento di movimento sulla plancia di gioco.

---

### Spawn System: Generazione Procedurale delle Ordate

Lo `SpawnSystem` orchestra la comparsa dei troll, introducendo una curva di difficoltà progressiva e un 
elemento di imprevedibilità. Una scelta progettuale chiave è stata quella di attivare il sistema solo dopo il 
posizionamento del primo mago da parte del giocatore. Questa decisione conferisce al giocatore il 
controllo sull'inizio effettivo della partita, permettendogli di stabilire una difesa iniziale 
prima di affrontare la prima ondata.

La generazione dei nemici è un processo dinamico e parametrico, governato da diverse logiche:

* **Difficoltà Progressiva**: La sfida si intensifica con l'avanzare delle ondate. 
Lo `SpawnSystem` si interfaccia con il modulo di configurazione `WaveLevel` per applicare moltiplicatori 
alle statistiche base dei troll (salute, velocità, danno). 
Questo scaling assicura che la difficoltà aumenti in modo controllato e predicibile.

* **Distribuzione Dinamica dei Nemici**: Per evitare la monotonia, la composizione delle ondate varia nel tempo. 
Le ondate iniziali sono dominate da troll di base, ma con il progredire della partita, il sistema introduce 
gradualmente tipologie di nemici più specializzate e complesse,
come i `Warrior` o gli `Assassin`, seguendo una distribuzione di probabilità che si evolve a ogni nuova ondata.

* **Generazione a "Batch"**: Anziché generare i troll a intervalli perfettamente regolari, è stata implementata
una logica di "batch". I nemici vengono generati in piccoli gruppi con intervalli temporali leggermente
randomizzati. Questo approccio crea un flusso di avversari più organico e meno prevedibile, costringendo 
il giocatore ad adattare costantemente le proprie strategie difensive.

```scala
  private def generateSpawnBatch(currentTime: Long, firstRow: Option[Int], numOfSpawns: Int): List[SpawnEvent] =
    val isFirstBatch = pendingSpawns.isEmpty && firstRow.isDefined
    List.tabulate(numOfSpawns): index =>
      val useFirstRow = isFirstBatch && index == 0
      generateSingleSpawn(currentTime + index * BATCH_INTERVAL, useFirstRow, firstRow)
```

Come si evince dal codice, il primo troll di un'ondata viene sempre generato sulla stessa riga del primo mago 
posizionato, una scelta implementativa per focalizzare l'azione iniziale nel punto in cui il giocatore ha 
deciso di stabilire la sua prima linea di difesa.

### Movement System: Strategie di movimento dei Troll

Una volta che un'entità è stata generata, il suo comportamento spaziale è governato dal `MovementSystem`. 
Questo sistema è responsabile dell'aggiornamento della posizione di tutte le entità mobili, applicando strategie di 
movimento differenziate in base alla tipologia dell'entità, secondo un'implementazione del Strategy Pattern.

* **Movimento Lineare**: La maggior parte dei troll implementa una strategia di movimento lineare, 
avanzando da destra verso sinistra con una velocità definita nel loro `MovementComponent`. 
Questo comportamento costituisce il fondamento della sfida tattica del gioco, richiedendo 
un posizionamento strategico delle unità difensive per intercettare l'avanzata nemica.

* **Movimento a Zigzag**: Per introdurre una maggiore complessità tattica, è stata implementata una 
strategia di movimento non lineare per il `Troll Assassino`. Questa unità alterna il proprio percorso 
tra la corsia di generazione e una corsia adiacente, scelta in modo pseudocasuale. 
Questo comportamento a zigzag lo rende un bersaglio più elusivo, obbligando il giocatore a 
considerare un posizionamento difensivo più flessibile.

```scala
private val trollMovementStrategy: TrollTypeComponent => MovementStrategy = trollType =>
    trollType.trollType match
      case Assassin => zigzagMovement
      case _        => linearLeftMovement
```

Il `MovementSystem` gestisce anche l'interazione con altri sistemi attraverso il sistema a componenti. 
Ad esempio, la presenza di un `FreezedComponent` su un troll, applicato dal `CollisionSystem` a seguito di un 
attacco di ghiaccio, viene rilevata dal MovementSystem per modificare dinamicamente la velocità dell'entità. 
Questo disaccoppiamento tra la logica del movimento e gli effetti di stato, facilitato dall'architettura ECS, 
ha permesso di implementare interazioni complesse tra entità in modo modulare e manutenibile.

## Interfaccia Utente

Oltre alla logica di gioco, il mio contributo si è esteso all'implementazione di componenti cruciali dell'interfaccia utente, in particolare i menu di overlay che gestiscono le interruzioni del flusso di gioco. Questi elementi sono stati sviluppati utilizzando **ScalaFX**, adottando un approccio funzionale e dichiarativo per la costruzione della UI.

---

### Menu di Pausa e Schermate di Vittoria/Sconfitta

Ho sviluppato i pannelli `PauseMenu` e `GameResultPanel` per gestire, rispettivamente, la messa in pausa del gioco 
da parte dell'utente e la conclusione di un'ondata o della partita.

La progettazione di questi componenti si è basata su alcuni principi chiave:

* **Componibilità e Riuso**: Entrambi i pannelli condividono una struttura simile, basata su uno `StackPane` che 
sovrappone un layout di controlli a un'immagine di sfondo. La creazione dei bottoni e la gestione delle 
loro azioni sono state delegate a un `ButtonFactory` centralizzato, che traduce le interazioni 
dell'utente in `GameEvent` specifici (es. `ResumeGame`, `ContinueBattle`, `NewGame`). 
Questo approccio ha permesso di ridurre la duplicazione del codice e di mantenere una 
netta separazione tra la vista e la logica di controllo.

* **Gestione Dichiarativa degli Stati**: Per il `GameResultPanel`, ho utilizzato un **Algebraic Data Type (ADT)**, 
definito tramite una `sealed trait`, per modellare i due possibili esiti della partita: `Victory` e `Defeat`. 
Questa scelta progettuale permette di rappresentare gli stati in modo type-safe ed estensibile. 
Ogni `case object` incapsula le informazioni specifiche per quel determinato stato, 
come l'immagine del titolo da mostrare e l'azione da associare al pulsante di continuazione.

    ```scala
      sealed trait ResultType:
        def titleImagePath: String
        def continueButtonText: String
        def continueAction: ButtonAction
    
      case object Victory extends ResultType:
        val titleImagePath               = "/victory.png"
        val continueButtonText           = "Next wave"
        val continueAction: ButtonAction = ContinueBattle
    
      case object Defeat extends ResultType:
        val titleImagePath               = "/defeat.png"
        val continueButtonText           = "New game"
        val continueAction: ButtonAction = NewGame
    ```
  Questo pattern non solo rende il codice più leggibile e manutenibile, ma garantisce anche che il pannello si adatti 
correttamente al contesto, offrendo azioni pertinenti all'utente (ad esempio, "Prossima ondata" dopo una vittoria 
e "Nuova partita" dopo una sconfitta).

* **Caricamento Efficiente delle Risorse**: Per ottimizzare le performance, il caricamento delle immagini 
di sfondo e dei titoli è stato implementato utilizzando `lazy val`. In questo modo, le risorse grafiche 
vengono caricate dal disco solo al momento del loro primo utilizzo effettivo, riducendo il tempo di 
avvio e il consumo di memoria dell'applicazione. La logica di caricamento e caching è stata incapsulata 
nell'`ImageFactory`, promuovendo ulteriormente il riuso del codice.

In sintesi, l'implementazione di questi componenti dell'interfaccia utente ha seguito i principi della 
programmazione funzionale e della separazione delle responsabilità, portando a un codice modulare, 
efficiente e facilmente estensibile.

## Testing e Validazione

La validazione della correttezza e della robustezza del software è stata una componente integrante del processo di 
sviluppo. Sebbene non sia stata adottata una metodologia strettamente **Test-Driven Development (TDD)**, 
i test sono stati scritti in modo sistematico parallelamente o immediatamente dopo l'implementazione 
di ogni funzionalità. Questo approccio ha permesso di garantire la stabilità del codice, 
facilitare le fasi di refactoring e prevenire l'introduzione di regressioni.

Per la stesura e l'esecuzione dei test è stato utilizzato **ScalaTest**, 
un framework ampiamente diffuso nell'ecosistema Scala, che ha permesso di scrivere test chiari e leggibili.

---

### Domain-Specific Language (DSL) per Scenari di Test

Una delle sfide principali nel testare un'applicazione complessa come un videogioco, 
specialmente uno basato sull'architettura ECS, è la configurazione dello stato iniziale per ogni scenario di test. 
La creazione manuale di entità, l'aggiunta di componenti e l'impostazione dei parametri di gioco possono risultare 
verbose, ripetitive e difficili da leggere, oscurando l'intento effettivo del test.

Per superare questa difficoltà, è stato progettato e implementato un **Domain-Specific Language (DSL)** interno, 
specifico per la creazione di scenari di gioco. 
La scelta di sviluppare un DSL è stata motivata da diversi obiettivi:

* **Leggibilità e Espressività**: Il DSL consente di descrivere uno stato del mondo di gioco in modo dichiarativo e 
vicino al linguaggio naturale. Questo rende i test immediatamente comprensibili, anche a distanza 
di tempo o per chi non ha familiarità con i dettagli implementativi dei componenti.

* **Riduzione del Boilerplate**: Astraendo la complessità della creazione di `World` e `GameSystemsState`, 
il DSL riduce drasticamente la quantità di codice ripetitivo necessario per la configurazione di ogni test.

* **Manutenibilità**: Centralizzando la logica di creazione degli scenari, 
eventuali modifiche all'architettura (ad esempio, l'aggiunta di un nuovo componente di base per ogni entità) 
richiedono modifiche in un unico punto (il DSL stesso), anziché in decine di test.

Il DSL si basa su un `ScenarioBuilder` che offre una serie di metodi concatenabili per 
definire lo stato del gioco in modo fluido:

```scala
// in GameScenarioDSL.scala
def scenario(setup: ScenarioBuilder => Unit): (World, GameSystemsState) =
  val builder = ScenarioBuilder()
  setup(builder)
  builder.build()

class ScenarioBuilder:
  // ...
  def withWizard(wizardType: WizardType): WizardPlacer = ...
  def withTroll(trollType: TrollType): TrollPlacer = ...
  def withElixir(amount: Int): this.type = ...
  def atWave(waveNumber: Int): this.type = ...
  // ...
```

Questo permette di scrivere test estremamente concisi e focalizzati sul comportamento da verificare, 
come dimostrato nell'esempio seguente:

### Senza DSL (verboso e poco leggibile):
```scala
val world = World.empty
val (world1, wizard) = world.createEntity()
val world2 = world1.addComponent(wizard, WizardTypeComponent(WizardType.Fire))
val world3 = world2.addComponent(wizard, PositionComponent(pos))
val world4 = world3.addComponent(wizard, HealthComponent(100, 100))
// ... 10+ linee simili
```

### Con DSL (dichiarativo e chiaro):
```scala
val (world, state) = scenario: builder =>
  builder
    .withWizard(WizardType.Fire).at(2, 3)
    .withTroll(TrollType.Base).at(2, 8)
    .withElixir(200)
```

### Copertura dei Test

Sono state create suite di test per tutti i principali moduli logici del gioco, 
garantendo una solida copertura delle funzionalità critiche. 
In particolare, sono stati testati:

* **`GameEngineTest` e `GameLoopTest`**: Verificano la corretta gestione del ciclo di vita del gioco 
(avvio, arresto, pausa, ripresa) e la stabilità del ciclo di aggiornamento a timestep fisso.

* **`MovementSystemTest`**: Assicura che le diverse strategie di movimento (lineare, zigzag) vengano applicate 
correttamente e che gli effetti di stato (come il rallentamento) modifichino il comportamento delle entità come previsto.

* **`SpawnSystemTest`**: Valida la logica di generazione dei nemici, controllando il rispetto dei tempi, 
il numero massimo di troll per ondata e l'applicazione corretta dello scaling di difficoltà.

* **`GameSystemsStateTest`**: Verifica le transizioni di stato del gioco e la corretta 
rilevazione delle condizioni di vittoria e sconfitta.

L'approccio al testing adottato, si è dimostrato efficace nel garantire la qualità e la robustezza del codice, 
costituendo una rete di sicurezza indispensabile durante l'intero ciclo di sviluppo del progetto.
