---
title: Giovanni Rinchiuso
nav_order: 3
parent: Implementazione
---

# Implementazione - Giovanni Rinchiuso

## Panoramica dei Contributi

Il mio contributo al progetto si è focalizzato sulle seguenti aree:

* **Sistemi di gioco**: `ElixirSystem`, `HealthSystem`, gestione economia e salute delle entità.
* **Configurazione e bilanciamento**: `WaveLevel`, calcolo parametri ondate e distribuzione troll.
* **Sistema di input**: `InputProcessor`, `InputSystem`, `InputTypes` con validazione.
* **Interfaccia utente**: `InfoMenu`, `ShopPanel`, `WavePanel` con gestione stato reattiva.
* **Testing**: DSL per `ElixirSystemTest`, `HealthSystemTest`, `InputProcessorTest`, `InputSystemTest`.

## Gestione dell'Economia: ElixirSystem

L'elisir è la risorsa centrale del gioco, necessaria per acquistare maghi e difendersi dai troll. Ho implementato `ElixirSystem` come sistema immutabile che gestisce la generazione periodica di elisir, la produzione dai maghi generatori, e le transazioni di spesa.

Il sistema è implementato come case class immutabile che estende il trait `System`, integrandosi così nell'architettura ECS del gioco. Ogni operazione restituisce una nuova istanza del sistema, garantendo che lo stato sia sempre consistente.
```scala
case class ElixirSystem(
    totalElixir: Int = INITIAL_ELIXIR,
    lastPeriodicGeneration: Long = 0L,
    firstWizardPlaced: Boolean = false,
    activationTime: Long = 0L
) extends System
```

L'aggiunta di elisir è una funzione pura che restituisce un nuovo sistema senza modificare quello esistente:
```scala
def addElixir(amount: Int): ElixirSystem =
  copy(totalElixir = Math.min(totalElixir + amount, MAX_ELIXIR))
```

La spesa di elisir utilizza `Option.when` per validare la transazione, restituendo sia il nuovo stato che un booleano di successo:
```scala
def spendElixir(amount: Int): (ElixirSystem, Boolean) =
  Option.when(totalElixir >= amount):
    copy(totalElixir = totalElixir - amount)
  .map((_, true))
    .getOrElse((this, false))
```

Questo approccio rende lo stato del gioco consistente.

L'aggiornamento del system utilizza `Option.when` per gestire la logica condizionale:
```scala
override def update(world: World): (World, System) =
  Option.when(firstWizardPlaced):
    val periodicSystem = updatePeriodicElixirGeneration()
    periodicSystem.updateGeneratorWizardElixir(world)
  .getOrElse((world, this))
```

Se il primo mago non è stato ancora piazzato, il sistema semplicemente restituisce lo stato attuale senza eseguire alcuna elaborazione. Questo pattern elimina la necessità di statement `if-else` espliciti, rendendo il codice più dichiarativo.

La generazione periodica utilizza `Option` per gestire l'inizializzazione e i controlli temporali:
```scala
private def updatePeriodicElixirGeneration(): ElixirSystem =
  val currentTime = System.currentTimeMillis()
  Option.when(lastPeriodicGeneration == 0L):
    copy(
      lastPeriodicGeneration = currentTime,
      activationTime = Option.when(activationTime == 0L)(currentTime).getOrElse(activationTime)
    )
  .orElse:
    checkAndGenerateElixir(currentTime)
  .getOrElse(this)
```

Il pattern `orElse` permette di concatenare logiche alternative: se è la prima generazione, inizializza i timestamp; altrimenti, controlla se è il momento di generare elisir.


Per l'elaborazione dei maghi generatori, ho utilizzato for-comprehension per validare le condizioni in sequenza, fermandosi alla prima che fallisce:
```scala
private def processGeneratorEntity(
    world: World,
    entityId: EntityId,
    currentTime: Long,
    system: ElixirSystem
): (World, ElixirSystem) =
  (for
    wizardType      <- world.getComponent[WizardTypeComponent](entityId)
    _               <- Option.when(wizardType.wizardType == WizardType.Generator)(())
    elixirGenerator <- world.getComponent[ElixirGeneratorComponent](entityId)
  yield processGeneratorCooldown(world, entityId, currentTime, elixirGenerator, system))
    .getOrElse((world, system))
```

Questa implementazione verifica che:
1. L'entità abbia un componente `WizardTypeComponent`
2. Il tipo di mago sia effettivamente `Generator`
3. L'entità abbia un componente `ElixirGeneratorComponent`

Se una qualsiasi di queste verifiche fallisce, il for-comprehension termina e restituisce lo stato originale tramite `getOrElse`. Questo approccio è molto più sicuro e leggibile rispetto a una serie di statement `if` annidati.

Per processare tutti i maghi generatori nel mondo, utilizzo `foldLeft` per accumulare i cambiamenti attraverso tutte le entità:
```scala
private def updateGeneratorWizardElixir(world: World): (World, ElixirSystem) =
  val currentTime       = System.currentTimeMillis()
  val generatorEntities = world.getEntitiesWithTwoComponents[WizardTypeComponent, ElixirGeneratorComponent].toList

  generatorEntities.foldLeft((world, this)): (acc, entityId) =>
    val (currentWorld, currentSystem) = acc
    processGeneratorEntity(currentWorld, entityId, currentTime, currentSystem)
```

Il `foldLeft` accumula sia il `World` aggiornato che l'`ElixirSystem` aggiornato, propagando lo stato attraverso l'elaborazione di ogni entità. Questo pattern è utile nella programmazione funzionale per gestire sequenze di trasformazioni mantenendo l'immutabilità.

### Gestione della Salute: HealthSystem

`HealthSystem` è responsabile della gestione delle collisioni, dei danni, della morte delle entità e delle ricompense. 

L'update del sistema segue un pattern di pipeline funzionale, dove ogni fase trasforma lo stato e lo passa alla successiva:
```scala
override def update(world: World): (World, System) =
  val (world1, system1) = processCollisionComponents(world)
  val (world2, system2) = system1.processDeaths(world1)
  val (world3, system3) = system2.removeDeadEntities(world2)
  (world3, system3)
```

Ogni funzione nella pipeline:
1. Riceve il mondo e il sistema correnti
2. Esegue una trasformazione specifica
3. Restituisce il nuovo mondo e sistema

Questo approccio garantisce che ogni fase sia isolata e testabile indipendentemente, seguendo il principio di Single Responsibility (SRP).

Per processare tutte le entità con componenti di collisione, utilizzo `foldLeft` per accumulare i cambiamenti:
```scala
private def processCollisionComponents(world: World): (World, HealthSystem) =
  world.getEntitiesWithComponent[CollisionComponent]
    .foldLeft((world, this)): (acc, entityId) =>
      val (currentWorld, currentSystem) = acc
      currentWorld.getComponent[CollisionComponent](entityId)
        .map: collision =>
          val worldWithoutCollision = currentWorld.removeComponent[CollisionComponent](entityId)
          currentSystem.applyCollisionToEntity(worldWithoutCollision, entityId, collision)
        .getOrElse(acc)
```

Il pattern utilizzato qui combina `foldLeft` con `map` su `Option`: per ogni entità, tentiamo di ottenere il componente di collisione. Se presente, applichiamo il danno e rimuoviamo il componente; altrimenti, manteniamo lo stato corrente. Questo evita la necessità di controlli null o eccezioni.

L'applicazione del danno utilizza `Option` e `filter` per validare lo stato dell'entità prima di applicare modifiche:
```scala
private def applyCollisionToEntity(
    world: World,
    entityId: EntityId,
    collisionComp: CollisionComponent
): (World, HealthSystem) =
  world.getComponent[HealthComponent](entityId)
    .filter(_.isAlive)
    .map: healthComp =>
      val newHealth     = math.max(0, healthComp.currentHealth - collisionComp.amount)
      val newHealthComp = healthComp.copy(currentHealth = newHealth)
      val updatedWorld  = updateHealth(world, entityId, newHealthComp)
      handlePossibleDeath(updatedWorld, entityId, newHealthComp)
    .getOrElse((world, this))
```

Il `filter(_.isAlive)` garantisce che il danno venga applicato solo alle entità vive, mentre il pattern `map`-`getOrElse` gestisce l'assenza del componente senza eccezioni.

Il calcolo delle ricompense utilizza pattern matching per mappare i tipi di troll alle ricompense appropriate. Questo approccio è più sicuro e leggibile rispetto a una serie di if-else:
```scala
private def calculateElixirReward(world: World, entityId: EntityId): Int =
  world.getComponent[TrollTypeComponent](entityId)
    .map(_.trollType)
    .map:
      case TrollType.Base     => BASE_TROLL_REWARD
      case TrollType.Warrior  => WARRIOR_TROLL_REWARD
      case TrollType.Assassin => ASSASSIN_TROLL_REWARD
      case TrollType.Thrower  => THROWER_TROLL_REWARD
    .getOrElse(0)
```

Se l'entità non è un troll (non ha `TrollTypeComponent`), restituisce 0. Il compilatore Scala verifica che tutti i casi siano gestiti, prevenendo bug a runtime.

Per identificare le entità morte, utilizzo for-comprehension con filtri multipli:
```scala
private def getNewlyDeadEntities(world: World): List[EntityId] =
  for
    entityId <- world.getEntitiesWithComponent[HealthComponent].toList
    if !entitiesToRemove.contains(entityId)
    health <- world.getComponent[HealthComponent](entityId).toList
    if !health.isAlive
  yield entityId
```

In questo modo:
1. Itera su tutte le entità con `HealthComponent`
2. Filtra quelle non già marcate per rimozione
3. Estrae il componente salute
4. Filtra quelle non vive

Il risultato è una lista di entità che sono morte ma non ancora rimosse. La sintassi for-comprehension rende la logica molto più chiara rispetto a una catena di `filter` e `flatMap`.

## Configurazione e Bilanciamento: WaveLevel

`WaveLevel` è l'oggetto che gestisce la progressione della difficoltà attraverso le ondate di troll. Determina quindi, come il gioco diventa progressivamente più sfidante mantenendo un equilibrio tra sfida e giocabilità.

`WaveLevel` deve risolvere diverse problematiche:
- **Varietà progressiva**: nelle prime ondate appaiono solo troll base, mentre ondate successive introducono gradualmente nemici più specializzati e pericolosi
- **Distribuzione probabilistica**: ogni ondata ha una specifica composizione di tipi di troll, definita tramite probabilità che determinano la frequenza di apparizione di ciascun tipo
- **Scalabilità**: i parametri dei troll (salute, velocità, danno) aumentano con le ondate per rendere il gioco sempre più sfidante
- **Bilanciamento**: gli intervalli di spawn diminuiscono progressivamente, aumentando la pressione sul giocatore




### Pattern Matching per Distribuzione Troll

La distribuzione dei tipi di troll cambia progressivamente con le ondate. Ho utilizzato pattern matching con guards per definire le distribuzioni di probabilità:
```scala
def calculateTrollDistribution(wave: Int): Map[TrollType, Double] =
  wave match
    case w if w <= 1 =>
      Map(
        TrollType.Base     -> 1.0,
        TrollType.Warrior  -> 0.0,
        TrollType.Assassin -> 0.0,
        TrollType.Thrower  -> 0.0
      )
    case w if w <= 2 =>
      Map(
        TrollType.Base     -> 0.7,
        TrollType.Warrior  -> 0.3,
        TrollType.Assassin -> 0.0,
        TrollType.Thrower  -> 0.0
      )
    case w if w <= 3 =>
      Map(
        TrollType.Base     -> 0.5,
        TrollType.Warrior  -> 0.3,
        TrollType.Assassin -> 0.2,
        TrollType.Thrower  -> 0.0
      )
    case w if w <= 4 =>
      Map(
        TrollType.Base     -> 0.4,
        TrollType.Warrior  -> 0.3,
        TrollType.Assassin -> 0.2,
        TrollType.Thrower  -> 0.1
      )
    case _ =>
      Map(
        TrollType.Base     -> 0.3,
        TrollType.Warrior  -> 0.3,
        TrollType.Assassin -> 0.25,
        TrollType.Thrower  -> 0.15
      )
```

Ogni pattern definisce una distribuzione di probabilità che determina quali tipi di troll appaiono in quella fase del gioco. Questo approccio offre numerosi vantaggi in termini di leggibilità: osservando i pattern, la progressione della difficoltà emerge naturalmente, mostrando come nelle prime ondate dominino i troll base per poi introdurre gradualmente le varianti più pericolose. L'estensibilità è altrettanto semplice: se volessimo aggiungere nuovi livelli di difficoltà, basterebbe inserire ulteriori case senza toccare la logica esistente.

Inoltre il compilatore verifica automaticamente che tutti i casi siano gestiti, e il case `_` finale garantisce un fallback sicuro per tutte le ondate oltre la quarta. Inoltre, ogni Map restituita è immutabile e la funzione è completamente pura, senza side-effects.

L'uso di guards nel pattern matching (`if w <= 1`, `if w <= 2`, etc.) Permette di definire range di ondate piuttosto che valori singoli, rendendo la configurazione più flessibile rispetto a un approccio basato su uguaglianza esatta. Ad esempio, tutte le ondate dalla quinta in poi usano la stessa distribuzione finale, che rappresenta il massimo livello di difficoltà del gioco.

### Selezione Pesata con FoldLeft

Per selezionare un tipo di troll random basato sulla distribuzione di probabilità, ho implementato un algoritmo di selezione pesata utilizzando `foldLeft`:
```scala
def selectRandomTrollType(distribution: Map[TrollType, Double]): TrollType =
  val random = scala.util.Random.nextDouble()
  distribution
    .toSeq
    .sortBy(_._2)
    .foldLeft((0.0, Option.empty[TrollType])) {
      case ((cumulative, Some(selected)), _) => 
        (cumulative, Some(selected))
      case ((cumulative, None), (trollType, probability)) =>
        val newCumulative = cumulative + probability
        if random <= newCumulative then (newCumulative, Some(trollType))
        else (newCumulative, None)
    }
    ._2
    .getOrElse(TrollType.Base)
```

L'algoritmo implementa la tecnica della "roulette wheel selection":

1. Genera un numero random tra 0 e 1
2. Accumula le probabilità usando `foldLeft`, creando segmenti cumulativi
3. Quando la somma cumulativa supera il valore random, seleziona quel tipo
4. Restituisce il tipo selezionato, con fallback a `TrollType.Base`


Il pattern matching nei case del `foldLeft` implementa un "early exit" funzionale:
```scala
case ((cumulative, Some(selected)), _) => (cumulative, Some(selected))
```
Una volta che un tipo è stato selezionato (l'Option diventa `Some`), questo pattern mantiene la selezione ignorando tutte le iterazioni successive. 

Il caso alternativo:
```scala
case ((cumulative, None), (trollType, probability)) =>
  val newCumulative = cumulative + probability
  if random <= newCumulative then (newCumulative, Some(trollType))
  else (newCumulative, None)
```
aggiorna la somma cumulativa e verifica se il valore random cade in questo "segmento" della roulette. Se sì, avviene la selezione; altrimenti, continua ad accumulare.
Infine, `._2.getOrElse(TrollType.Base)` estrae il tipo selezionato dall'Option, fornendo un fallback sicuro nel caso improbabile che nessun tipo venga selezionato (ad esempio, se tutte le probabilità fossero 0).


## Sistema di Input: InputProcessor, InputSystem e InputTypes

Ho implementato un'architettura a tre livelli che separa le responsabilità nella gestione degli input dell'utente. Questa struttura garantisce una chiara separazione delle responsabilità, facilita il testing e rende il sistema facilmente estendibile.

### 1. InputTypes

Il livello più basso dell'architettura definisce i tipi di dato fondamentali utilizzati nel sistema di input. `MouseClick` rappresenta un evento di click del mouse con coordinate `(x, y)`, mentre `ClickResult` incapsula il risultato della validazione di un click, contenendo la posizione, un flag di validità e un messaggio di errore opzionale.

### 2. InputProcessor

Il livello intermedio è responsabile della logica di validazione vera e propria. Implementa metodi per verificare se un click cade all'interno dei bounds della griglia, convertire coordinate dello schermo in posizioni di gioco e validare le posizioni risultanti. La separazione tra processamento e validazione permette di testare facilmente la logica di validazione in isolamento.

### 3. InputSystem

Il livello più alto fornisce un'interfaccia per l'utilizzo del sistema. Utilizza `InputProcessor` internamente nascondendo i dettagli implementativi e offre metodi come `handleMouseClick` che accettano coordinate dello schermo e restituiscono un `ClickResult`. Inoltre, fornisce metodi utility come `processClicks` per elaborare batch di click, `validPositions` per filtrare solo le posizioni valide, e `partitionClicks` per separare click validi e invalidi.

### ClickResult

Un elemento centrale di questa implementazione è `ClickResult`, che ho implementato seguendo il pattern delle monadi per comporre validazioni. Questo approccio permette di concatenare multiple validazioni. 

Come mostrato nella sezione precedente, `ClickResult` incapsula il risultato di un click del mouse, memorizzando la posizione, un flag di validità e un messaggio di errore opzionale. Ho implementato le tre operazioni monadiche fondamentali (`map`, `flatMap` e `filter`).

L'operazione `map` permette di trasformare la posizione contenuta se il risultato è valido, lasciando inalterati i risultati invalidi. `flatMap` consente di concatenare validazioni che a loro volta producono `ClickResult`, implementando così il pattern della "railway-oriented programming" dove un errore in qualsiasi punto della catena cortocircuita le operazioni successive. `filter` aggiunge la capacità di validare predicati sulla posizione, convertendo un risultato valido in invalido se il predicato fallisce.

```scala
result
  .map(pos => pos.normalize())
  .filter(_.isInBounds, "Out of bounds")
  .flatMap(pos => validateCell(pos))
```

Ogni operazione nella catena viene eseguita solo se quella precedente ha avuto successo, e il primo fallimento propaga automaticamente attraverso tutta la catena senza bisogno di controlli espliciti.

### Composizione di Predicati di Validazione

Per semplificare l'applicazione di multiple validazioni, ho implementato un metodo `validate` nel companion object di `ClickResult` che accetta un numero variabile di predicati con i loro messaggi di errore associati:
```scala
def validate(pos: Position)(validations: (Position => Boolean, String)*): ClickResult =
  validations.foldLeft(valid(pos)): (result, validation) =>
    result.filter(validation._1, validation._2)
```

Questo metodo utilizza `foldLeft` per applicare sequenzialmente tutte le validazioni fornite. Ogni validazione è una tupla contenente un predicato (una funzione `Position => Boolean`) e un messaggio di errore. Il risultato iniziale è un `ClickResult` valido contenente la posizione, che viene poi trasformato applicando ogni validazione in sequenza tramite `filter`.

Un esempio di utilizzo all'interno di `InputProcessor`:
```scala
def processClickWithValidation(click: MouseClick): ClickResult =
  val position = click.toPosition
  ClickResult.validate(position)(
    (_.isValid, "Position is not valid"),
    (_ => isInGridArea(click.x, click.y), "Click outside grid area")
  )
```

In questo esempio, la posizione viene validata contro due predicati: prima si verifica che la posizione sia valida in sé, poi si controlla che cada all'interno dell'area della griglia. Se una qualsiasi validazione fallisce, il `ClickResult` diventa invalido con il messaggio di errore appropriato, e le validazioni successive vengono comunque eseguite (anche se il loro risultato viene ignorato) per completare il fold.

### Extension Methods in MouseClick

Ho utilizzato le extension methods di Scala 3 per arricchire il tipo `MouseClick` con metodi di validazione, rendendo l'API più fluente e intuitiva:
```scala
extension (click: MouseClick)
  def validate(processor: InputProcessor): ClickResult =
    processor.processClick(click)

  def isInGrid(processor: InputProcessor): Boolean =
    processor.isInGridArea(click.x, click.y)

  def validateWith(processor: InputProcessor)(errorMsg: String): ClickResult =
    processor.processClick(click) match
      case result if result.isValid => result
      case _                        => ClickResult.invalid(errorMsg)
```

Il metodo `validate` delega al `InputProcessor` per eseguire la validazione standard. `isInGrid` fornisce un controllo booleano per verificare se il click cade nell'area della griglia. `validateWith` permette di personalizzare il messaggio di errore, usando pattern matching per sostituire eventuali errori di default con un messaggio custom.

L'utilizzo permette di concatenare operazioni in modo naturale:
```scala
val click = MouseClick(x, y)
click.validate(processor)
  .filter(_.isInCell, "Not in cell")
  .map(_.toGridCoordinates)
```

Questa catena di operazioni valida il click, filtra per verificare che sia in una cella, e trasforma le coordinate. Se qualsiasi passo fallisce, l'errore si propaga automaticamente e il risultato finale sarà un `ClickResult` invalido con il messaggio di errore appropriato.

## Interfaccia Utente: InfoMenu, ShopPanel e WavePanel

Ho sviluppato diversi componenti dell'interfaccia utente che costituiscono l'esperienza visiva e interattiva del gioco. 

### InfoMenu

L'`InfoMenu` fornisce al giocatore informazioni dettagliate sulle meccaniche di gioco, sui diversi tipi di maghi e sui vari tipi di troll. La sua implementazione si basa su una struttura a tab che permette di navigare tra diverse sezioni informative: regole del gioco, caratteristiche dei maghi e caratteristiche dei troll.

La gestione dello stato della navigazione è implementata attraverso una closure che mantiene riferimenti ai bottoni di navigazione e aggiorna dinamicamente la loro opacità per indicare quale sezione è attualmente attiva. Quando l'utente clicca su un tab, il contenuto dell'area centrale viene sostituito con la vista appropriata e l'opacità dei bottoni viene aggiornata per riflettere lo stato corrente.

Per maghi e troll vengono mostrate delle card informative contenenti le statistiche (salute, danno, costo/ricompensa) e l'immagine rappresentativa di ogni tipologia.

### ShopPanel

Lo `ShopPanel` è il componente dell'interfaccia che permette al giocatore di acquistare i maghi durante la partita. Questo pannello mostra tutte le tipologie di maghi disponibili, con le loro icone e i rispettivi costi in elisir.

La caratteristica principale dello `ShopPanel` è la sua capacità di aggiornarsi dinamicamente in base alla quantità di elisir posseduta dal giocatore. Quando l'elisir aumenta o diminuisce, il pannello ricalcola automaticamente quali maghi sono acquistabili e aggiorna il loro aspetto visivo di conseguenza: i maghi acquistabili vengono resi interattivi con effetti hover e cursor a mano, mentre quelli non acquistabili vengono disabilitati visivamente con opacità ridotta e bordi grigi.

Lo stato del pannello è modellato attraverso una struttura dati immutabile che mantiene l'ammontare corrente di elisir, lo stato di apertura/chiusura del pannello e una mappa che associa ogni tipo di mago al suo stato di disponibilità. Questa architettura garantisce che il pannello rimanga sempre sincronizzato con lo stato del gioco, fornendo al giocatore un feedback visivo immediato sulle opzioni di acquisto disponibili.

### WavePanel

Il `WavePanel` mostra informazioni sull'ondata corrente e si aggiorna automaticamente quando il gioco progredisce. 

Lo stato del pannello mantiene l'ultimo numero di ondata renderizzato (per evitare aggiornamenti ridondanti), un riferimento opzionale al componente `Text` che mostra il numero e un riferimento opzionale al pannello stesso. Il metodo `updateWaveNumber` implementa un pattern di aggiornamento ottimizzato che garantisce che l'interfaccia venga aggiornata solo quando il numero dell'ondata effettivamente cambia, evitando rendering inutili e migliorando le performance.

## Testing: DSL Personalizzati

La validazione della correttezza delle implementazioni è stata una componente fondamentale del mio lavoro. Ho sviluppato test per i principali sistemi di cui mi sono occupato: ElixirSystem, HealthSystem, InputProcessor e InputSystem. Anche se non ho seguito rigorosamente il Test-Driven Development, ho scritto i test in modo sistematico parallelamente o immediatamente dopo l’implementazione di ogni funzionalità. Per semplificare la scrittura dei test e renderli più leggibili, ho sviluppato quattro DSL specializzati per testare i sistemi implementati, utilizzando pattern funzionali per garantire immutabilità e type-safety.

### ElixirSystemDSL

L'`ElixirSystem` presenta una sfida particolare per il testing: richiede la gestione di timing, generazione periodica e interazioni con il mondo di gioco. Per affrontare questa complessità, ho progettato un DSL che mantiene lo stato attraverso `Option`, permettendo di memorizzare valori tra le diverse fasi del test senza ricorrere a variabili mutabili.

Questo esempio di test mostra come il DSL renda espressivo il testing temporale:
```scala
"ElixirSystem" should "generate elixir from generator wizards" in {
  givenAnElixirSystem
    .activated
    .withWorld
    .andGeneratorWizardAt(Position(2, 3))
    .rememberingInitialElixir
    .afterWaiting(GENERATOR_WIZARD_COOLDOWN + ELIXIR_WAIT_MARGIN)
    .whenUpdated
    .shouldHaveAtLeast(PERIODIC_ELIXIR).moreElixirThanInitial
}
```

Ho implementato una enum `ComparisonType` e una case class `ElixirAmountComparison` che permettono di esprimere asserzioni come `shouldHaveAtLeast(50).moreElixirThanInitial` o `shouldHaveExactly(100).moreElixirThanInitial`:
```scala
enum ComparisonType:
  case AtLeast, Exactly

case class ElixirAmountComparison(dsl: ElixirSystemDSL, amount: Int, comparisonType: ComparisonType):
  def moreElixirThanInitial: ElixirSystemDSL =
    dsl.initialElixir.foreach: initial =>
      val diff = dsl.system.getCurrentElixir - initial
      comparisonType match
        case ComparisonType.AtLeast => diff should be >= amount
        case ComparisonType.Exactly => diff shouldBe amount
    dsl
```

### HealthSystemDSL

In `HealthSystem` i test devono creare entità, applicare danni e verificare sia lo stato di salute che le ricompense. Ho modellato queste operazioni attraverso tre case class che rappresentano diverse fasi del test.

Il flusso tipico di un test si presenta così:
```scala
"HealthSystem" should "kill entity and reward elixir" in {
  aHealthSystem
    .withTroll(TrollType.Base)
    .havingHealth(50, 100)
    .takingDamage(60)
    .done
    .whenUpdated
    .entity(0).shouldBeDead
    .systemShouldHaveElixir(INITIAL_ELIXIR + BASE_TROLL_REWARD)
}
```

Le tre case class che compongono il DSL sono `HealthSystemDSL` per il contesto principale, `EntityBuilder` per la configurazione delle entità, e `EntityAssertions` per le verifiche. Le transizioni avvengono attraverso i tipi di ritorno: chiamare `withEntity` restituisce un `EntityBuilder` che permette di configurare l'entità, `done` riporta al contesto principale, e `entity(n)` fornisce un `EntityAssertions` per le verifiche.

Questa struttura sfrutta il sistema di tipi di Scala per prevenire errori a compile-time. Ad esempio, non è possibile verificare lo stato di un'entità prima di averla configurata, perché il compilatore non permetterebbe di chiamare `entity(0)` prima di aver chiamato `done`.

L'accumulo delle entità avviene in modo immutabile: ogni operazione restituisce una nuova istanza del DSL con il world aggiornato e l'entità aggiunta alla lista attraverso `entities :+ entity`:
```scala
def withTroll(trollType: TrollType): EntityBuilder =
  val (updatedWorld, entity) = world.createEntity()
  val worldWithComponent = updatedWorld.addComponent(entity, TrollTypeComponent(trollType))
  EntityBuilder(this.copy(world = worldWithComponent, entities = entities :+ entity), entity)
```

### InputProcessorDSL e InputSystemDSL

Per i sistemi di input, la sfida principale era rendere naturale il testing di coordinate e validazioni. Ho progettato DSL che separano chiaramente la fase di setup delle coordinate dalla fase di verifica dei risultati.

Un esempio di test:
```scala
"InputProcessor" should "validate grid coordinates" in {
  aClick
    .atOffset(10, 10)
    .whenProcessed
    .shouldBeValid
    .andShouldBeInCell
}
```

La struttura si basa su due case class: `ClickBuilder` accumula le coordinate, mentre `ClickResultAssertions` gestisce le verifiche. Il metodo `whenProcessed` fa da ponte tra le due fasi, eseguendo la validazione e restituendo le asserzioni.

Questa architettura trasforma i test in documentazione eseguibile: leggendo un test è immediatamente chiaro cosa viene configurato, quale operazione viene eseguita e quale risultato ci si aspetta, il tutto mantenendo le garanzie di type-safety e immutabilità tipiche della programmazione funzionale.