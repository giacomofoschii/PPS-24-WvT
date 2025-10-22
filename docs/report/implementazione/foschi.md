---
title: Giacomo Foschi
nav_order: 1
parent: Implementazione
---

# Implementazione - Giacomo Foschi

## Panoramica dei Contributi

Il mio contributo al progetto si è concentrato sulla progettazione e implementazione di diversi aspetti chiave del sistema, con un focus particolare sull'architettura ECS (Entity-Component-System) e sul rendering grafico. Le aree principali di cui mi sono occupato sono:

* **Architettura del `World` ECS**: Progettazione del `World` come contenitore immutabile per la gestione di entità e componenti
* **Struttura della View**: Implementazione del `ViewController` per la gestione degli stati della UI e del `GameView` per il rendering della scena di gioco
* **Factory per la UI**: Creazione di `ButtonFactory` e `ImageFactory` per standardizzare e ottimizzare la creazione di elementi grafici
* **Logica di Combattimento e Collisioni**: Sviluppo del `CombatSystem` per la gestione degli attacchi e del `CollisionSystem` per il rilevamento delle collisioni
* **Utility per la Griglia**: Implementazione del `GridMapper` per la conversione tra coordinate logiche e fisiche
* **Movimento dei Proiettili**: Definizione della logica di movimento per i proiettili all'interno del `MovementSystem`
* **Sistema di Rendering**: Creazione del `RenderSystem` per la visualizzazione delle entità a schermo

## Architettura del `World` in ECS

Il `World` è il cuore del nostro pattern **Entity-Component-System (ECS)**. È stato progettato come una struttura dati **immutabile** che agisce da contenitore per tutte le entità di gioco e i loro componenti. La sua immutabilità è fondamentale per aderire ai principi della programmazione funzionale, garantendo che ogni aggiornamento produca un nuovo stato del mondo senza effetti collaterali, semplificando il debugging e la gestione dello stato.

Le sue responsabilità principali sono:

* **Gestione delle Entità**: Fornisce metodi per creare (`createEntity`) e distruggere (`destroyEntity`) entità in modo sicuro. Ogni entità è rappresentata da un `EntityId` univoco
* **Gestione dei Componenti**: Permette di aggiungere (`addComponent`), rimuovere (`removeComponent`) e aggiornare (`updateComponent`) componenti associati a un'entità. I componenti sono semplici `case class` che contengono solo dati (es. `PositionComponent`, `HealthComponent`)
* **Querying**: Offre API per interrogare lo stato del gioco, come ottenere tutte le entità con un certo componente (`getEntitiesWithComponent`) o di un certo tipo (`getEntitiesByType`)

Un esempio di come un sistema interagisce con il `World` per aggiungere un componente a un'entità:
```scala
// Esempio di utilizzo del World
case class World(
    private val entities: Set[EntityId] = Set.empty,
    private val components: Map[Class[_], Map[EntityId, Component]] = Map.empty,
    // ...
):

  def addComponent[T <: Component](entity: EntityId, component: T): World =
    if !entities.contains(entity) then
      this
    else
      // ... logica per aggiungere il componente in modo immutabile
      val componentClass = component.getClass
      val updatedComponents = components.updatedWith(componentClass): opt =>
        Some(opt.getOrElse(Map.empty) + (entity -> component))

      val updatedEntitiesByType = updateEntityTypeMapping(entity, component)

      copy(components = updatedComponents, entitiesByType = updatedEntitiesByType)
```

Questa architettura favorisce la composizione sull'ereditarietà, permettendo di definire entità complesse semplicemente combinando diversi componenti, e garantisce una netta separazione tra dati (Componenti) e logica (Sistemi).

## Struttura della View: `ViewController` e `GameView`

L'interfaccia utente del nostro gioco è gestita principalmente da due classi: `ViewController` e `GameView`, che collaborano per presentare lo stato del gioco all'utente e per gestire i suoi input.

### ViewController

Il `ViewController` funge da orchestratore centrale per tutta l'interfaccia grafica. È l'entry point dell'applicazione ScalaFX e gestisce le transizioni tra le diverse schermate del gioco (MainMenu, GameView, InfoMenu, etc.). Per fare ciò, utilizza una macchina a stati basata sull'ADT `ViewState`.

Le sue responsabilità includono:

* **Gestione degli Stati della UI**: Il metodo `updateView` riceve un nuovo `ViewState` e si occupa di sostituire la scena corrente con quella appropriata, caricando il layout corretto
* **Inizializzazione e Pulizia**: Si occupa di creare l'istanza del `GameController` e di gestire la pulizia delle risorse (`cleanup`) quando si passa da una schermata all'altra (es. tornando al menu principale dal gioco)
* **Inoltro degli Input**: Traduce le azioni dell'utente (es. click sui bottoni) in eventi di gioco (`GameEvent`) che vengono inviati al `GameController` per essere processati

### GameView

Il `GameView` è il componente responsabile del rendering della schermata di gioco principale. È un `StackPane` che sovrappone diversi livelli (layers) per organizzare gli elementi grafici:

* **Sfondo**: L'immagine di background della mappa di gioco
* **Griglia**: Un `Pane` per disegnare overlay sulla griglia, come le celle valide per il posizionamento
* **Entità**: Un `Pane` dove vengono renderizzati i maghi e i troll
* **Proiettili**: Un `Pane` separato per i proiettili, per poterli gestire indipendentemente
* **Barre della Vita**: Un `Pane` per le barre della salute
* **UI Overlay**: Il livello più alto che contiene elementi come lo `ShopPanel`, il `WavePanel` e il pulsante di pausa

Il `GameView` espone metodi come `renderEntities` e `renderHealthBars` che vengono invocati dal `RenderSystem` (tramite il `ViewController`) per aggiornare la visualizzazione a schermo in modo efficiente, assicurando che le operazioni di rendering avvengano sul thread della UI di JavaFX tramite `Platform.runLater`.

## Factory per la UI: `ButtonFactory` e `ImageFactory`

Per promuovere il riutilizzo del codice e garantire uno stile grafico coerente in tutta l'applicazione, ho implementato due factory object: `ImageFactory` e `ButtonFactory`.

### ImageFactory

`ImageFactory` centralizza la creazione di `ImageView`. La sua caratteristica principale è l'implementazione di un **sistema di caching** per le immagini.
```scala
object ImageFactory:
  private val imageCache: mutable.Map[String, Image] = mutable.Map.empty

  private def loadImage(path: String): Option[Image] =
    imageCache.get(path).orElse(loadAndCacheImage(path))
```

Quando viene richiesta un'immagine, la factory controlla prima se è già presente nella cache. Se lo è, restituisce l'istanza esistente; altrimenti, la carica dal disco, la memorizza nella cache e poi la restituisce. Questo approccio ottimizza le performance e riduce il consumo di memoria, evitando di ricaricare più volte la stessa immagine.

### ButtonFactory

`ButtonFactory` standardizza la creazione dei bottoni. Utilizza una `case class` `ButtonConfig` per definire l'aspetto di un bottone (testo, dimensioni, font) e una serie di `Presets` per configurazioni comuni (es. `mainMenuButtonPreset`, `shopButtonPreset`).

Questo permette di creare bottoni con uno stile omogeneo in tutta l'applicazione con una sola riga di codice, associando direttamente un'`azione` che viene eseguita `onAction`.
```scala
def createStyledButton(config: ButtonConfig)(action: => Unit): Button =
    createButton(config).withAction(action).withOverEffect().build()
```

Inoltre, il ButtonFactory gestisce anche l'associazione tra ButtonAction (un ADT che rappresenta le azioni possibili) e le chiamate al ViewController, mantenendo la logica di navigazione disaccoppiata dalla definizione dei bottoni.

## Logica di Combattimento e Collisioni

Il combattimento in *Wizards vs Trolls* è gestito da due sistemi distinti ma collaborativi: `CombatSystem` e `CollisionSystem`.

### CombatSystem

Il `CombatSystem` è responsabile di **avviare gli attacchi**. Ad ogni ciclo di gioco, esegue le seguenti operazioni:

* **Scansiona le entità**: Itera su tutte le entità che possono attaccare (maghi e troll lanciatori)
* **Ricerca dei bersagli**: Per ogni attaccante, cerca un bersaglio valido all'interno del suo raggio d'attacco (`findClosestTarget`). La ricerca è ottimizzata per controllare solo le entità sulla stessa riga
* **Gestione del Cooldown**: Verifica che l'attaccante non sia in fase di cooldown
* **Creazione dei Proiettili**: Se tutte le condizioni sono soddisfatte, utilizza l'`EntityFactory` per creare un'entità proiettile nella posizione dell'attaccante e aggiunge un `CooldownComponent` all'attaccante per prevenire attacchi troppo ravvicinati
```scala
// In CombatSystem.scala
private def spawnProjectileAndSetCooldown(
      world: World,
      entity: EntityId,
      position: Position,
      projectileType: ProjectileType,
      cooldown: Long
  ): World =
    val (world1, _) = EntityFactory.createProjectile(world, position, projectileType)
    world1.addComponent(entity, CooldownComponent(cooldown))
```
### CollisionSystem

Il `CollisionSystem` si occupa di **risolvere gli attacchi**. La sua logica è suddivisa in due fasi:

* **Collisioni dei Proiettili**: Il sistema itera su tutti i proiettili attivi e controlla se la loro posizione (cella della griglia) coincide con quella di un'entità bersaglio. Se viene rilevata una collisione:
    * Il proiettile viene distrutto
    * Un `CollisionComponent`, contenente l'ammontare del danno, viene aggiunto all'entità bersaglio
    * Se il proiettile è di tipo "ghiaccio", viene aggiunto anche un `FreezedComponent` per rallentare il bersaglio
* **Collisioni in Mischia**: Successivamente, il sistema gestisce gli attacchi in mischia dei troll. Se un troll si trova nella stessa cella di un mago, il troll viene bloccato (aggiungendo un `BlockedComponent`) e, se non è in cooldown, infligge danno al mago aggiungendo un `CollisionComponent`

Questa separazione di responsabilità permette di gestire in modo pulito e modulare i diversi tipi di interazione offensiva nel gioco. Il danno vero e proprio viene poi applicato dall'`HealthSystem` in una fase successiva del ciclo di gioco.

## Utility e Sistemi di Supporto

### GridMapper

Il `GridMapper` è un utility object fondamentale che funge da "traduttore" tra il sistema di coordinate logiche della griglia (righe e colonne) e il sistema di coordinate fisiche dello schermo (pixel x, y).

Fornisce metodi essenziali come:

* `logicalToPhysical`: Converte una coppia `(riga, colonna)` nella posizione centrale in pixel di quella cella
* `physicalToLogical`: Converte coordinate `(x, y)` in pixel nella coppia `(riga, colonna)` corrispondente

Questo disaccoppia completamente la logica di gioco (che ragiona in termini di griglia) dalla rappresentazione grafica (che lavora con i pixel), rendendo il codice più pulito e manutenibile.

### Movimento dei Proiettili

Il movimento di tutte le entità, inclusi i proiettili, è gestito dal `MovementSystem`. Per i proiettili, la logica è semplice e lineare:

* I proiettili dei maghi si muovono da sinistra verso destra (`projectileRightMovement`)
* I proiettili dei troll si muovono da destra verso sinistra (`linearLeftMovement`)

Il sistema aggiorna la `PositionComponent` di ogni proiettile in base alla sua velocità e al tempo trascorso (`deltaTime`). Inoltre, il `MovementSystem` è anche responsabile di rimuovere i proiettili che escono dai confini dello schermo, evitando l'accumulo di entità inutili.

### RenderSystem

Il `RenderSystem` orchestra il processo di visualizzazione delle entità. Ad ogni ciclo, non ridisegna ciecamente tutto, ma implementa un'ottimizzazione per migliorare le performance:

* **Raccolta delle Entità**: Colleziona tutte le entità che possiedono sia un `PositionComponent` che un `ImageComponent`
* **Creazione di un Hash di Stato**: Genera una stringa hash che rappresenta lo stato visuale corrente (posizione e path dell'immagine di ogni entità)
* **Confronto e Rendering**: Confronta l'hash corrente con quello dell'ultimo frame renderizzato (`lastRenderedState`). Se gli hash sono diversi, significa che qualcosa è cambiato (un'entità si è mossa, è apparsa o scomparsa) e solo in questo caso invoca i metodi di rendering del `GameView`
```scala
// In RenderSystem.scala
private def shouldRender(currentState: String): Boolean =
    !lastRenderedState.contains(currentState)
```

Questo meccanismo di "dirty checking" evita di ridisegnare la scena quando non ci sono cambiamenti visivi, riducendo il carico sulla GPU e contribuendo a mantenere un frame rate stabile