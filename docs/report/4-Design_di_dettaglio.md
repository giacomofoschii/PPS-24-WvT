---
title: Design di dettaglio
nav_order: 4
parent: Report
---

# Design di Dettaglio

---

## Panoramica

In questa sezione verrà approfondito il design delle componenti chiave del progetto *Wizards vs Trolls*, illustrando le principali responsabilità funzionali, le scelte implementative e le interazioni tra i moduli. L'analisi segue il pattern **Model-View-Controller (MVC)**, con un focus sull'implementazione del **Model** tramite l'architettura **Entity-Component-System (ECS)**, ispirandosi alla struttura descrittiva vista nel documento di esempio.

---

## Pattern Architetturale MVC (Model-View-Controller)

Questo pattern è stato adottato per separare la logica di gioco dalla sua rappresentazione grafica e dall'interazione con l'utente, garantendo modularità e manutenibilità. I tre componenti principali svolgono ruoli specifici:

* **Model**: Rappresenta il cuore dell'applicazione, contenendo i dati e la logica di business del gioco (stato delle entità, regole di combattimento, gestione risorse). È implementato utilizzando il pattern **Entity Component System (ECS)**.
* **View**: È l'interfaccia utente, responsabile della presentazione dei dati del Model all'utente e della raccolta degli input. Non contiene logica di gioco.
* **Controller**: Agisce come intermediario tra Model e View. Riceve input dalla View, li elabora, aggiorna il Model e istruisce la View a riflettere i cambiamenti.

Questa separazione permette lo sviluppo e il test indipendente dei componenti e facilita future estensioni.

---

## Model (ECS)

Il Model racchiude e gestisce l'intera logica di business del gioco, implementata tramite il pattern **Entity Component System (ECS)**. Di seguito sono riportate le principali scelte di design e responsabilità.

### Gestione dello Stato del Mondo (`World`)

Il nucleo del Model è rappresentato dal `World`, una `case class` **immutabile** che funge da contenitore centrale per tutte le entità e i loro componenti. L'immutabilità garantisce che ogni operazione (eseguita dai `System`) restituisca un nuovo stato del mondo senza modificare quello precedente, aderendo ai principi funzionali e semplificando la gestione dello stato in un ambiente potenzialmente concorrente.

* **Responsabilità**:
    * Mantenere l'insieme di tutte le entità attive (`EntityId`).
    * Mappare i tipi di componenti alle entità che li possiedono (`Map[Class[_], Map[EntityId, Component]]`).
    * Fornire API funzionali per creare/distruggere entità e aggiungere/rimuovere/aggiornare/recuperare componenti (es. `createEntity()`, `addComponent()`, `getComponent()`, `getEntitiesWithComponent()`).
    * Permettere query specifiche sullo stato (es. `getEntitiesByType()`, `getEntityAt()`, `hasWizardAt()`).

```mermaid
classDiagram
    class World {
        <<Immutable>>
        -entities: Set~EntityId~
        -components: Map~Class[_], Map~EntityId, Component~~
        -entitiesByType: Map~String, Set~EntityId~~
        +createEntity(): World
        +destroyEntity(EntityId): World
        +addComponent(EntityId, Component): World
        +removeComponent(EntityId): World
        +updateComponent(EntityId, Function): World
        +getComponent(EntityId): Option~Component~
        +getEntitiesWithComponent(Class): Set~EntityId~
        +getEntitiesByType(String): Set~EntityId~
        +getEntityAt(Position): Option~EntityId~
        +hasWizardAt(Position): Boolean
    }
    class EntityId {
        <<Opaque Type>>
        +value: Long
    }
    class Component {
        <<Sealed Trait>>
    }
    World *-- EntityId
    World *-- Component
```
### Creazione delle Entità (EntityFactory)

La creazione delle diverse entità del gioco (Maghi, Troll, Proiettili) è centralizzata nell'object **`EntityFactory`**. Questo approccio utilizza il pattern **Factory Method** combinato con **Type Classes** (`EntityBuilder`) per assemblare le entità in modo componibile e type-safe.

#### Responsabilità:

* Definire **configurazioni** (`WizardConfig`, `TrollConfig`, `ProjectileConfig`) che descrivono le proprietà base di ciascun tipo di entità.
* Utilizzare `EntityBuilder` (implementati tramite `given instances`) per costruire la lista di `Component` necessari per ogni configurazione.
* Fornire **metodi specifici** (es. `createFireWizard`, `createBaseTroll`, `createProjectile`) che prendono il `World` corrente, la posizione e il tipo di entità, restituendo il `World` aggiornato con la nuova entità e il suo `EntityId`.
* Astrarre i dettagli dell'aggiunta dei singoli componenti al `World`.

```mermaid
classDiagram
    class EntityFactory {
        <<object>>
        +createProjectile(World, Position, ProjectileType): Tuple2~World, EntityId~
        +createGeneratorWizard(World, Position): Tuple2~World, EntityId~
        +createBaseTroll(World, Position): Tuple2~World, EntityId~
        #createEntity~T: EntityBuilder~(World, Position, T): Tuple2~World, EntityId~
    }
    class EntityBuilder~T~ {
        <<trait>>
        +buildComponents(T, Position): List~Component~
    }
    class ProjectileConfig
    class WizardConfig
    class TrollConfig

    EntityFactory ..> World : modifies
    EntityFactory ..> EntityBuilder : uses
    EntityFactory ..> ProjectileConfig : uses
    EntityFactory ..> WizardConfig : uses
    EntityFactory ..> TrollConfig : uses
    EntityBuilder <|.. ImplicitProjectileBuilder
    EntityBuilder <|.. ImplicitWizardBuilder
    EntityBuilder <|.. ImplicitTrollBuilder
```
## Logica di Gioco (Systems)

Tutta la logica comportamentale è incapsulata nei **`System`**. Ogni `System` è una `case class` (solitamente stateless) che implementa il trait `System`, definendo un metodo `update(world: World): (World, System)`. Questo metodo prende lo stato attuale del mondo e restituisce il nuovo stato modificato e, potenzialmente, una nuova istanza del sistema (anche se spesso restituisce `this` essendo stateless).

---

### Strategie di Movimento (MovementSystem)

Questo sistema gestisce lo spostamento di tutte le entità mobili.

#### Responsabilità:

* Aggiornare la `PositionComponent` delle entità in base alla loro `MovementComponent` e al `deltaTime`.
* Applicare diverse **strategie di movimento** in base al tipo di entità (tramite pattern matching sull' `EntityTypeComponent`):
    * Movimento lineare a sinistra per i Troll (`linearLeftMovement`).
    * Movimento lineare a destra per i Proiettili dei Maghi (`projectileRightMovement`).
    * Movimento lineare a sinistra per i Proiettili dei Troll.
    * Movimento a zigzag per i Troll Assassini (`zigzagMovement`), gestito tramite lo `ZigZagStateComponent` per mantenere lo stato specifico dell'entità.
* Considerare gli effetti di stato come il rallentamento (`FreezedComponent`).
* Rimuovere i proiettili che escono dai limiti dello schermo.

```mermaid
classDiagram
    class MovementSystem {
        +update(World): Tuple2~World, System~
        #selectMovementStrategy(EntityId, World): MovementStrategy
        -linearLeftMovement(...) : Position
        -projectileRightMovement(...) : Position
        -zigzagMovement(...) : Position
    }
    class PositionComponent { +position: Position }
    class MovementComponent { +speed: Double }
    class ZigZagStateComponent { +currentPhase: ZigZagPhase }
    class FreezedComponent { +speedModifier: Double }
    MovementSystem ..> World : reads/updates
    MovementSystem ..> PositionComponent : updates
    MovementSystem ..> MovementComponent : reads
    MovementSystem ..> ZigZagStateComponent : reads/updates
    MovementSystem ..> FreezedComponent : reads
```

### Gestione del Combattimento e Collisioni (CombatSystem, CollisionSystem)

Il combattimento è diviso in due fasi gestite da sistemi distinti:

#### `CombatSystem`:
* **Responsabilità**: Iniziare gli attacchi a distanza.
* **Logica**: Identifica le entità attaccanti (Maghi, Troll Lanciatori), cerca bersagli nel raggio d'azione (`findClosestTarget`), verifica il cooldown (`CooldownComponent`) e, se possibile, crea un'entità `Projectile` usando `EntityFactory` e imposta il cooldown sull'attaccante. Gestisce anche la logica di blocco (`BlockedComponent`) per i Troll Lanciatori. Aggiorna i timer dei `CooldownComponent` e `FreezedComponent`.

#### `CollisionSystem`:
* **Responsabilità**: Rilevare e risolvere le collisioni fisiche.
* **Logica**:
    * **Proiettili**: Verifica se la cella di un proiettile coincide con quella di un bersaglio valido. Se sì, distrugge il proiettile e aggiunge un `CollisionComponent` (con il danno) al bersaglio. Applica l'effetto `FreezedComponent` se il proiettile era di ghiaccio.
    * **Mischia**: Verifica se un Troll (non Lanciatore) è nella stessa cella di un Mago. Se sì, aggiunge un `BlockedComponent` al Troll, e se non è in cooldown, aggiunge un `CollisionComponent` al Mago e imposta il cooldown sul Troll.

```mermaid
classDiagram
    direction LR
    class CombatSystem {
        +update(World): Tuple2~World, System~
        -processWizardProjectiles(World): World
        -processThrowerProjectiles(World): World
        -findClosestTarget(...): Option~EntityId~
        -spawnProjectileAndSetCooldown(...): World
        -updateComponentTimer~C~(...): World
    }
    class CollisionSystem {
        +update(World): Tuple2~World, System~
        -processProjectileCollisions(World): World
        -processMeleeCollisions(World): World
        -findCollidingEntity(...): Option~EntityId~
    }
    class AttackComponent { +damage: Int; +range: Double; +cooldown: Long }
    class CooldownComponent { +remainingTime: Long }
    class BlockedComponent { +blockedBy: EntityId }
    class DamageComponent { +amount: Int }
    class CollisionComponent { +amount: Int }
    class FreezedComponent { +remainingTime: Long }
    class ProjectileTypeComponent

    CombatSystem ..> World : reads/updates
    CombatSystem ..> AttackComponent : reads
    CombatSystem ..> CooldownComponent : reads/updates
    CombatSystem ..> BlockedComponent : adds
    CombatSystem ..> EntityFactory : uses
    CombatSystem ..> FreezedComponent : updates timer

    CollisionSystem ..> World : reads/updates
    CollisionSystem ..> AttackComponent : reads (for melee)
    CollisionSystem ..> DamageComponent : reads (for projectile)
    CollisionSystem ..> CollisionComponent : adds
    CollisionSystem ..> FreezedComponent : adds
    CollisionSystem ..> BlockedComponent : adds
    CollisionSystem ..> CooldownComponent : adds (for melee)
    CollisionSystem ..> ProjectileTypeComponent : reads
```
### Generazione Nemici (SpawnSystem)

Questo sistema gestisce l'apparizione dei Troll sulla mappa.

#### Responsabilità:

* Schedulare e generare ondate di Troll (`SpawnEvent`).
* Attivarsi solo dopo il posizionamento del primo Mago.
* Incrementare la difficoltà (`WaveLevel`) aumentando numero, tipo e statistiche dei Troll generati.
* Generare Troll in "batch" a intervalli variabili per un flusso meno prevedibile.
* Applicare lo scaling delle statistiche ai Troll creati in base all'ondata corrente.
* Gestire la pausa del gioco sospendendo e riprendendo correttamente la generazione.

---

### Gestione Risorse ed Effetti (ElixirSystem, HealthSystem)

#### `ElixirSystem`:
* **Responsabilità**: Gestire la risorsa Elixir del giocatore.
* **Logica**: Traccia l'ammontare corrente (`totalElixir`), gestisce la generazione periodica automatica e quella dei Maghi Generatori (interagendo con `CooldownComponent`), permette di spendere (`spendElixir`) e aggiungere (`addElixir`) elisir, rispettando il cap massimo (`MAX_ELIXIR`).

#### `HealthSystem`:
* **Responsabilità**: Gestire la salute delle entità e le conseguenze del danno.
* **Logica**: Processa i `CollisionComponent` aggiunti dal `CollisionSystem`, sottrae la salute dalla `HealthComponent`, rimuove il `CollisionComponent`. Se la salute scende a zero:
    * Marca l'entità per la rimozione.
    * Se è un Troll, comunica all'`ElixirSystem` di aggiungere la ricompensa.
    * Rimuove fisicamente le entità marcate dal `World` (`destroyEntity`).
    * Gestisce la rimozione a cascata dei `BlockedComponent` quando l'entità bloccante muore.

```mermaid
classDiagram
    class ElixirSystem {
        +update(World): Tuple2~World, System~
        +spendElixir(Int): Tuple2~ElixirSystem, Boolean~
        +addElixir(Int): ElixirSystem
        +getCurrentElixir(): Int
        +canAfford(Int): Boolean
        +activateGeneration(): ElixirSystem
    }
    class HealthSystem {
        +update(World): Tuple2~World, System~
        -processCollisionComponents(World): Tuple2~World, HealthSystem~
        -applyCollisionToEntity(...): Tuple2~World, HealthSystem~
        -handlePossibleDeath(...): Tuple2~World, HealthSystem~
        -giveElixirReward(...): Tuple2~World, HealthSystem~
        -removeDeadEntities(World): Tuple2~World, HealthSystem~
    }
    class HealthComponent { +currentHealth: Int; +maxHealth: Int }
    class CollisionComponent { +amount: Int }
    class ElixirGeneratorComponent { +elixirPerSecond: Int }
    class CooldownComponent

    ElixirSystem ..> World : reads (Generators)
    ElixirSystem ..> ElixirGeneratorComponent : reads
    ElixirSystem ..> CooldownComponent : reads/updates (Generators)

    HealthSystem ..> World : reads/updates
    HealthSystem ..> HealthComponent : reads/updates
    HealthSystem ..> CollisionComponent : reads/removes
    HealthSystem ..> ElixirSystem : updates (reward)
    HealthSystem ..> TrollTypeComponent : reads (reward)
    HealthSystem ..> BlockedComponent : removes (cascade)

    HealthSystem *-- ElixirSystem : uses
```

## View

La **View** si occupa della presentazione grafica dello stato del gioco e dell'interazione diretta con l'utente, utilizzando **ScalaFX**.

---

### Gestione delle Schermate

* **Responsabilità**: Mostrare la schermata appropriata (Menu Principale, Gioco, Info, Pausa, Vittoria, Sconfitta) in base allo stato dell'applicazione.
* **Componenti**:
    * `ViewController`: Gestisce le transizioni tra stati (`ViewState`) e aggiorna la `Scene` della `PrimaryStage`.
    * `MainMenu`, `InfoMenu`, `PauseMenu`, `GameResultPanel`: `object` che definiscono la struttura e i controlli di ciascuna schermata statica.

---

### Rendering della Scena di Gioco

* **Responsabilità**: Disegnare lo stato corrente del `World` a schermo.
* **Componenti**:
    * `GameView`: Organizza i diversi livelli grafici (`Pane` sovrapposti) e fornisce metodi (`renderEntities`, `renderHealthBars`, `drawGrid`) per aggiornare specifici livelli. Utilizza `Platform.runLater` per garantire che gli aggiornamenti avvengano sul thread UI. Gestisce i click sulla griglia.
    * `RenderSystem` (nel Model, guida la View): Determina cosa deve essere disegnato.
    * `HealthBarRenderSystem` (nel Model, guida la View): Sottosistema specializzato per calcolare quali barre della vita mostrare.
    * `GridMapper`: Utility `object` utilizzato da `GameView` per convertire le coordinate fisiche (click del mouse) in logiche (cella della griglia) e viceversa, per disegnare la griglia (`drawGrid`) e posizionare le entità (`renderEntities`).
    * `Position`: `case class` che rappresenta le coordinate fisiche (pixel), utilizzata da `GameView` per posizionare gli elementi grafici.

---

### Creazione Componenti UI

* **Responsabilità**: Standardizzare la creazione e l'aspetto degli elementi UI riutilizzabili, simile ai factory pattern visti nell'esempio.
* **Componenti**:
    * `ButtonFactory`: Crea bottoni (`Button`) con stili predefiniti (Presets basati su `ButtonConfig`) e associa direttamente `ButtonAction` che vengono tradotte in `GameEvent`.
    * `ImageFactory`: Carica e gestisce `ImageView`, implementando un sistema di caching per ottimizzare l'uso della memoria e i tempi di caricamento.
    * `ShopPanel`, `WavePanel`: Creano e gestiscono i pannelli specifici dell'HUD (negozio e informazioni sull'ondata).

---

```mermaid
classDiagram
    namespace View {
class ViewController {
<<object>>
+start()
+stopApp()
+updateView(ViewState)
+requestMainMenu()
+requestGameView()
+getController(): Option~GameController~
+render()
+drawPlacementGrid(Seq~Position~, Seq~Position~)
+hidePlacementGrid()
}
class GameView {
<<object>>
+apply(): Parent
+renderEntities(Seq~Tuple2~Position, String~~)
+renderHealthBars(Seq~Tuple6~...~~)
+drawGrid(Seq~Position~, Seq~Position~)
+clearGrid()
+showError(String)
#handleMouseClick(Double, Double)
}
class MainMenu { <<object>> ; +apply(): Parent }
class InfoMenu { <<object>> ; +apply(): Parent }
class PauseMenu { <<object>> ; +apply(): Parent }
class GameResultPanel { <<object>> ; +apply(ResultType): Parent }
class ShopPanel { <<object>> ; +createShopPanel(): VBox; +updateElixir() }
class WavePanel { <<object>> ; +createWavePanel(): VBox; +updateWave() }
class ButtonFactory { <<object>> ; +createStyledButton(...): Button; +handleAction(ButtonAction) }
class ImageFactory { <<object>> ; +createImageView(...): Either; +createBackgroundView(...): Option }
class ViewState { <<Sealed Trait>> }
}
namespace Utilities {
class GridMapper {
<<object>>
+logicalToPhysical(LogicalCoords): Option~Position~
+physicalToLogical(Position): Option~LogicalCoords~
+getCellBounds(Int, Int): Tuple4~...~
+isInCell(Position): Boolean
}
class Position {
+x: Double
+y: Double
+isInCell(): Boolean
+isValid(): Boolean
}
    }
namespace Controller {
class GameController
class GameEvent
}

ViewController --> GameController : uses
ViewController --> GameView : creates/updates
ViewController --> MainMenu : creates
ViewController --> InfoMenu : creates
ViewController --> PauseMenu : creates
ViewController --> GameResultPanel : creates
ViewController --> ViewState : manages

GameView ..> ViewController : calls requests
GameView *-- ShopPanel : creates/uses
GameView *-- WavePanel : creates/uses
GameView ..> ButtonFactory : uses
GameView ..> ImageFactory : uses
GameView ..> GridMapper : uses
GameView ..> Position : uses

MainMenu ..> ButtonFactory : uses
MainMenu ..> ImageFactory : uses
MainMenu ..> ViewController : calls requests

InfoMenu ..> ButtonFactory : uses
InfoMenu ..> ImageFactory : uses
InfoMenu ..> ViewController : calls requests

PauseMenu ..> ButtonFactory : uses
PauseMenu ..> ImageFactory : uses
PauseMenu ..> ViewController : calls requests

GameResultPanel ..> ButtonFactory : uses
GameResultPanel ..> ImageFactory : uses
GameResultPanel ..> ViewController : calls requests

ShopPanel ..> ButtonFactory : uses
ShopPanel ..> ImageFactory : uses
ShopPanel ..> ViewController : calls requests / gets state

WavePanel ..> ViewController : gets state

ButtonFactory ..> ViewController : calls requests via handleAction
```

## Controller

Il **Controller** agisce come collante, orchestrando il flusso di dati e la logica applicativa tra il Model e la View.

---

### Orchestrazione del Flusso di Gioco

* **Responsabilità**: Far avanzare lo stato del gioco nel tempo e coordinare l'esecuzione della logica.
* **Componenti**:
    * `GameController`: Riceve l'impulso (`update()`) dal `GameEngine` (tramite il `GameLoop`). Mantiene lo stato corrente dei sistemi (`GameSystemsState`). Chiama il metodo `updateAll()` di `GameSystemsState` per eseguire la pipeline dei sistemi ECS nell'ordine corretto. Gestisce le azioni del giocatore ricevute come `GameEvent` dall'`EventHandler`.
    * `GameSystemsState`: Raggruppa tutti i sistemi ECS e definisce l'ordine di update. Contiene anche metodi per verificare le condizioni di fine partita (`checkWinCondition`, `checkLoseCondition`).
    * `GameEngine` / `GameLoop`: (Esterni al Controller ma lo invocano) Forniscono il "battito cardiaco" del gioco, garantendo che `GameController.update()` sia chiamato a intervalli regolari (timestep fisso).

---

### Gestione degli Eventi

* **Responsabilità**: Disaccoppiare i componenti e gestire la comunicazione e le transizioni di stato in modo centralizzato.
* **Componenti**:
    * `EventHandler`: Mantiene una coda thread-safe (`EventQueue`) di `GameEvent`. Riceve eventi da View (input utente), `GameEngine` (cambiamenti di stato globali), `GameController` (condizioni di gioco). Processa gli eventi in base alla loro priorità, invocando handler registrati o gestendo direttamente le transizioni di stato del `GameEngine` e della `ViewController` (es. passaggio da `Playing` a `Paused`).
    * `GameEvent`: ADT (`sealed trait`) che definisce tutti i tipi di eventi possibili, con una priorità associata.

---

### Gestione dell'Input

* **Responsabilità**: Validare e interpretare l'input grezzo dell'utente.
* **Componenti**:
    * `InputSystem`: Riceve le coordinate grezze del mouse click dalla `GameView` (inoltrate tramite `ViewController` e `GameController`).
    * `InputProcessor`: Contiene la logica per verificare se un click (`MouseClick`) ricade all'interno dell'area valida della griglia (`isInGridArea`).
    * `ClickResult`: `case class` che rappresenta l'esito della validazione dell'input (posizione valida/invalida, eventuale messaggio di errore).
    * `GridMapper`: Utilizzato per convertire le coordinate fisiche (pixel) in coordinate logiche (riga/colonna) se il click è valido. L'`EventHandler` riceverà poi un `GridClicked` event con le coordinate logiche.

---

```mermaid
sequenceDiagram
    participant GameLoop
    participant GameEngine
    participant GameController
    participant GameSystemsState
    participant World
    participant EventHandler
    participant View

    GameLoop->>GameEngine: update(deltaTime)
    GameEngine->>GameController: update()
    GameController->>GameSystemsState: updateAll(currentWorld)
    GameSystemsState->>MovementSystem: update(world)
    Note right of GameSystemsState: Esegue tutti i sistemi in ordine...
    GameSystemsState-->>GameController: (newWorld, newSystemsState)
    GameController->>GameController: Aggiorna World e State
    GameController->>EventHandler: checkGameConditions(newWorld)
    alt Condizione Vittoria/Sconfitta
        EventHandler->>GameController: postEvent(GameWon/GameLost)
    end
    GameController->>View: render()

    View->>EventHandler: postEvent(GridClicked)
    GameEngine->>EventHandler: processEvents()
    EventHandler->>GameController: handleGridClick(position)
    GameController->>World: hasWizardAt(position)?
    GameController->>GameSystemsState: canAfford(cost)?
    opt Può piazzare
        GameController->>EntityFactory: createWizard(...)
        GameController->>World: addComponent(...)
        GameController->>GameSystemsState: spendElixir(cost)
    end
```