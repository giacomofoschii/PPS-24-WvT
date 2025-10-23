---
title: Giacomo Foschi
nav_order: 1
parent: Implementazione
---

# Implementazione - Giacomo Foschi

## Panoramica dei Contributi

Il mio contributo al progetto si è concentrato sulla progettazione e implementazione di diversi aspetti chiave del sistema, con un focus particolare sull'architettura ECS (Entity-Component-System) e sul rendering grafico. Le aree principali di cui mi sono occupato sono:

* **Architettura del `World` ECS**: Progettazione del `World` come contenitore **immutabile** per la gestione di entità e componenti, sfruttando le `case class` e le collezioni immutabili di Scala.
* **Struttura della View**: Implementazione del `ViewController` per la gestione degli stati della UI e del `GameView` per il rendering della scena di gioco.
* **Factory per la UI**: Creazione di `ButtonFactory` e `ImageFactory` per standardizzare e ottimizzare la creazione di elementi grafici, con gestione funzionale degli errori tramite `Option` ed `Either` e caching.
* **Logica di Combattimento e Collisioni**: Sviluppo del `CombatSystem` e del `CollisionSystem` come **funzioni pure** (`World => World`) sullo stato del mondo, comunicando tramite l'aggiunta/rimozione di componenti immutabili.
* **Utility per la Griglia**: Implementazione del `GridMapper` per la conversione tra coordinate logiche e fisiche.
* **Movimento dei Proiettili**: Definizione della logica di movimento specifica per i proiettili all'interno del `MovementSystem`.
* **Sistema di Rendering**: Creazione del `RenderSystem` per la visualizzazione delle entità a schermo, con ottimizzazioni basate su hashing dello stato per evitare rendering ridondanti.

## Architettura del `World` in ECS

Il `World` è il cuore del nostro pattern **Entity-Component-System (ECS)**. È stato progettato come una `case class` **immutabile** che agisce da contenitore per tutte le entità di gioco e i loro componenti (`case class` immutabili per design). La sua immutabilità è fondamentale per aderire ai principi della programmazione funzionale: ogni aggiornamento (eseguito dai `System`) produce un *nuovo* stato del mondo senza modificare quello precedente (nessun side effect), semplificando il debugging, la gestione dello stato e garantendo la thread-safety intrinseca.

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

Questa architettura favorisce la **composizione** sull'ereditarietà (le entità sono definite dinamicamente dalla combinazione dei loro componenti) e garantisce una netta **separazione tra dati** (Componenti, `case class` immutabili) e **logica** (Sistemi, funzioni pure `World => World`), rendendo il codice modulare, testabile e manutenibile.

## Struttura della View: `ViewController` e `GameView`

L'interfaccia utente (UI), basata su ScalaFX, è gestita principalmente da `ViewController` e `GameView`, seguendo i principi del pattern MVC, che collaborano per presentare lo stato del gioco all'utente e per gestire i suoi input.

### ViewController

Il `ViewController`, implementato come `object` che estende `JFXApp3`, orchestra l'intera UI. È l'entry point dell'applicazione ScalaFX e gestisce le transizioni tra le diverse schermate del gioco (`ViewState`, un Algebraic Data Type definito con `sealed trait`) aggiornando la `Scene` principale della `PrimaryStage`.

Le sue responsabilità includono:

* **Gestione degli Stati della UI**: Il metodo `updateView` riceve un nuovo `ViewState` e, tramite pattern matching, seleziona e carica il layout corretto (`MainMenu()`, `GameView()`, etc.). Mantiene lo stato corrente in una `case class` `ViewControllerState`.
* **Inizializzazione e Pulizia**: Si occupa di creare l'istanza del `GameController` all'avvio e gestisce la pulizia (`cleanup`) delle risorse (es. cache immagini, stato `GameView`) quando si esce da una schermata complessa come quella di gioco verso il menù principale.
* **Inoltro degli Input**: Fornisce metodi `requestXYZ` (es. `requestGameView`, `requestPlaceWizard`) che traducono le azioni dell'utente in `GameEvent` specifici, inviandoli poi al `GameController` tramite `postEvent`. Questo disaccoppia la View dalla logica del Controller.

### GameView

Il `GameView`, anch'esso un `object`, è il componente responsabile del rendering della schermata di gioco principale. Utilizza uno `StackPane` per sovrapporre diversi livelli (`Pane`): Sfondo, Griglia overlay, Entità (maghi/troll), Proiettili, Barre della Vita, e UI Overlay (Shop, Wave, Pausa).

* **Sfondo**: L'immagine di background della mappa di gioco
* **Griglia**: Un `Pane` per disegnare overlay sulla griglia, come le celle valide per il posizionamento
* **Entità**: Un `Pane` dove vengono renderizzati i maghi e i troll
* **Proiettili**: Un `Pane` separato per i proiettili, per poterli gestire indipendentemente
* **Barre della Vita**: Un `Pane` per le barre della salute
* **UI Overlay**: Il livello più alto che contiene elementi come lo `ShopPanel`, il `WavePanel` e il pulsante di pausa

Il `GameView` espone metodi come `renderEntities` e `renderHealthBars` che vengono invocati dal `RenderSystem` (tramite il `ViewController`) per aggiornare la visualizzazione a schermo in modo efficiente, assicurando che le operazioni di rendering avvengano sul thread della UI di JavaFX tramite `Platform.runLater`.

## Factory per la UI: `ButtonFactory` e `ImageFactory`

Per promuovere il riutilizzo del codice e garantire uno stile grafico coerente, ho implementato due factory object.

### ImageFactory

`ImageFactory` centralizza la creazione e gestione di `ImageView`. La sua caratteristica principale è l'implementazione di un **sistema di caching** (`mutable.Map`) per ottimizzare performance e memoria.
```scala
object ImageFactory:
  private val imageCache: mutable.Map[String, Image] = mutable.Map.empty

  // Restituisce Option[Image], gestendo il fallimento del caricamento
  private def loadImage(path: String): Option[Image] =
    imageCache.get(path).orElse(loadAndCacheImage(path))

  // Restituisce Either per una gestione errori più esplicita
  def createImageView(imagePath: String, width: Int): Either[String, ImageView] =
    loadImage(imagePath)
      .toRight(s"Error loading image at path: $imagePath")
      .map(image => createFixedWidthImageView(image, width))
````
Quando viene richiesta un'immagine (`loadImage`), la factory controlla la cache; se l'immagine non è presente, tenta di caricarla (`loadAndCacheImage` usa `Option(getClass.getResourceAsStream(path))` per gestire resource non trovate) e la memorizza. Il metodo `createImageView` propaga l'eventuale fallimento usando `Either[String, ImageView]`, permettendo al chiamante (`GameView`) di gestire l'errore in modo funzionale (es. con `fold` o pattern matching) invece di usare eccezioni.
Questo approccio ottimizza le performance e riduce il consumo di memoria, evitando di ricaricare più volte la stessa immagine.

### ButtonFactory

`ButtonFactory` standardizza la creazione dei bottoni. Utilizza una `case class` `ButtonConfig` per definire l'aspetto di un bottone (testo, dimensioni, font) e una serie di `Presets` per configurazioni comuni (es. `mainMenuButtonPreset`, `shopButtonPreset`).

Questo permette di creare bottoni con uno stile omogeneo in tutta l'applicazione con una sola riga di codice, associando direttamente un'`azione` che viene eseguita `onAction`.
```scala
def createStyledButton(config: ButtonConfig)(action: => Unit): Button =
    createButton(config).withAction(action).withOverEffect().build()
```

Utilizza un `ButtonBuilder` interno (pattern Builder) per una configurazione fluente.
Inoltre, il ButtonFactory gestisce anche l'associazione tra ButtonAction (un ADT che rappresenta le azioni possibili) e le chiamate al ViewController, tramite pattern matching, mantenendo la logica di navigazione disaccoppiata dalla definizione dei bottoni.

## Logica di Combattimento e Collisioni

Il combattimento è gestito da `CombatSystem` e `CollisionSystem`, entrambi case class stateless che implementano il trait `System`, operando come funzioni pure `World => (World, System)`. Queste classi collaborano tra loro per gestire i cicli di attacco e risoluzione delle collisioni.

### CombatSystem

Questo sistema inizia gli attacchi a distanza. Il metodo `update` prende il `World` corrente e restituisce un nuovo `World` modificato. La logica interna utilizza ampiamente costrutti funzionali:

- **Iterazione Funzionale**: Usa `foldLeft` sulle liste di entità (ottenute tramite `world.getEntitiesByType`) per processare attaccanti maghi e troll lanciatori in modo immutabile
- **Gestione dell'Assenza (Monadi)**: La ricerca del bersaglio (`findClosestTarget`) usa `flatMap`, `filter`, `minByOption` su `Option` e collezioni per trovare il bersaglio più vicino sulla stessa riga, restituendo `Option[EntityId]` per gestire il caso in cui non ci siano bersagli validi

In particolare, il system si occupa di:

* **Scansionare le entità**: Itera su tutte le entità che possono attaccare (maghi e troll lanciatori)
* **Ricercare dei bersagli**: Per ogni attaccante, cerca un bersaglio valido all'interno del suo raggio d'attacco (`findClosestTarget`). La ricerca è ottimizzata per controllare solo le entità sulla stessa riga
* **Gestire i Cooldown**: Verifica che l'attaccante non sia in fase di cooldown
* **Creare dei Proiettili**: Se tutte le condizioni sono soddisfatte, utilizza l'`EntityFactory` per creare un'entità proiettile nella posizione dell'attaccante e aggiunge un `CooldownComponent` all'attaccante per prevenire attacchi troppo ravvicinati

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

Questo sistema risolve le collisioni, prendendo il `World` modificato dal `CombatSystem` e restituendo un nuovo `World`.

- **Iterazione Funzionale**: Come `CombatSystem`, usa `foldLeft` per processare proiettili e troll in mischia (`processProjectileList`, `processMeleeList`).

- **Gestione dell'Assenza (Monadi)**: La logica per processare una singola collisione (`processProjectileCollision`, `processMeleeCollision`) è incapsulata in for-comprehension su `Option` per estrarre i componenti necessari (posizione, tipo, danno). Se un componente manca, il for-comprehension fallisce e restituisce il `World` invariato tramite `.getOrElse(world)`. La ricerca dell'entità collidente (`findCollidingEntity`) usa anch'essa `Option` e `flatMap`.
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
* **Creazione di un Hash di Stato**: `generateStateHash` è una funzione pura che prende le sequenze immutabili di entità e barre vita e produce una stringa hash deterministica, usando `map` e `mkString`.
* **Confronto**: Confronta l'hash corrente con quello dell'ultimo frame renderizzato (`lastRenderedState`).
```scala
// In RenderSystem.scala
private def shouldRender(currentState: String): Boolean =
    !lastRenderedState.contains(currentState)
```
La decisione di renderizzare (`shouldRender`) è una semplice comparazione tra l'hash corrente e quello precedente. Se gli hash non corrispondono, avviene il rendering tramite il metodo `update`, che restituisce una nuova istanza del `RenderSystem` con `lastRenderedState` aggiornato, mantenendo l'immutabilità del sistema stesso. Questo approccio evita side effect (il rendering sulla UI) se non strettamente necessari.