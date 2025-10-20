---
title: Design architetturale
nav_order: 3
parent: Report
---

# Design architetturale

Il design architetturale del sistema è stato elaborato a partire dai requisiti funzionali e non funzionali identificati. L'obiettivo principale è stato creare una struttura modulare, performante ed estensibile che potesse gestire la complessità di un gioco tower defense come _Wizards vs Trolls_, garantendo alte performance con numerose entità simultanee e una chiara separazione delle responsabilità tra i vari componenti.

## Pattern Architetturale MVC (Model-View-Controller)

Questo pattern è ampiamente utilizzato nello sviluppo di applicazioni software per separare la logica dalla sua rappresentazione grafica e dall'interazione con l'utente. I suoi tre componenti principali svolgono ruoli specifici:

* **Model**: Il Model rappresenta il cuore dell'applicazione. Contiene i dati e la logica di business del sistema. La sua responsabilità è gestire lo stato dell'applicazione, manipolare i dati e implementare tutte le regole del gioco. È completamente disaccoppiato dalla rappresentazione grafica e non ha conoscenza dell'interfaccia utente. Nel nostro caso, il Model contiene tutte le entità di gioco (maghi, troll, proiettili), le loro caratteristiche, la logica di combattimento, movimento, gestione risorse e progressione delle ondate. Per implementare il Model abbiamo scelto di utilizzare il pattern **Entity Component System (ECS)**, che verrà descritto in dettaglio nella sezione successiva.

* **View**: La View è l'interfaccia utente. Il suo scopo è presentare i dati del Model all'utente e raccogliere gli input dell'utente. La View non contiene alcuna logica di gioco e si limita a visualizzare lo stato corrente del Model. Nel nostro progetto, la View include la griglia di gioco, la visualizzazione delle entità (maghi, troll, proiettili), l'HUD con informazioni su elisir e ondata corrente, il menu principale e la schermata di Game Over. Quando l'utente interagisce con la View (ad esempio, cliccando per posizionare un mago), questa inoltra l'input al Controller per la sua elaborazione.

* **Controller**: Il Controller agisce come intermediario tra il Model e la View. Riceve l'input dall'utente tramite la View, lo elabora, aggiorna il Model di conseguenza e istruisce la View a riflettere i cambiamenti di stato. Nel nostro caso, il Controller coordina l'esecuzione del game loop, gestisce il posizionamento dei maghi, valida gli input dell'utente e determina le condizioni di game over. In sostanza, il Controller garantisce che le diverse parti dell'applicazione rimangano indipendenti e comunichino attraverso interfacce ben definite.

Questa separazione è la motivazione per la quale abbiamo scelto di utilizzare il pattern MVC per il nostro progetto. Permette di sviluppare e testare ciascun componente in modo indipendente, facilita la manutenzione e l'estensione del sistema. Inoltre, consente di sostituire facilmente la View (ad esempio, passare da ScalaFX a un'altra libreria grafica) senza dover modificare il Model o il Controller.

## Pattern Entity Component System (ECS) per il Model

All'interno del Model, abbiamo scelto di implementare la logica di gioco utilizzando il pattern **Entity Component System (ECS)**, un pattern data-oriented ampiamente utilizzato nello sviluppo di videogiochi.

L'ECS si basa su tre concetti fondamentali:

* **Entity**: Un identificatore unico che rappresenta un'entità di gioco (mago, troll, proiettile). L'Entity non contiene dati né comportamenti, è semplicemente un ID che collega diversi Component.

* **Component**: Strutture dati pure che rappresentano singoli aspetti di un'entità (posizione, salute, attacco, movimento). I Component non contengono logica, solo dati. Ad esempio, `PositionComponent` contiene le coordinate (x, y), `HealthComponent` la salute corrente e massima, `AttackComponent` il danno, raggio e cooldown.

* **System**: Contengono tutta la logica di gioco e operano su gruppi di entità che possiedono specifici Component. Ogni System ha una responsabilità unica e ben definita: `MovementSystem` aggiorna le posizioni, `CombatSystem` gestisce gli attacchi, `ElixirSystem` gestisce le risorse, `HealthSystem` applica i danni.

Questa scelta architetturale è stata motivata da diversi fattori:

1. **Performance**: La separazione tra dati (Component) e logica (System) favorisce la località dei dati in memoria, migliorando l'efficienza della cache CPU. Questo è cruciale in un tower defense dove possono esistere decine di entità simultanee che devono essere aggiornate 60 volte al secondo.

2. **Composizione flessibile**: Invece di gerarchie di ereditarietà rigide, le entità sono definite dalla combinazione di Component che possiedono. Un mago attaccante ha Component di posizione, salute e attacco, mentre un Mago Generatore ha un Component generatore di elisir al posto dell'attacco. Questo permette di creare nuovi tipi di entità senza modificare gerarchie esistenti.

3. **Modularità**: Ogni System gestisce un aspetto specifico del gioco in modo indipendente, rendendo il codice più comprensibile e manutenibile.

4. **Estensibilità**: Aggiungere nuove funzionalità significa creare nuovi Component e/o System senza modificare il codice esistente, rispettando il principio Open/Closed.

5. **Testabilità**: Ogni System può essere testato in isolamento creando scenari specifici con solo le entità e i Component necessari.

## Struttura del Progetto

``` mermaid
classDiagram
    %% Package Model - ECS
    namespace Model_ECS {
        class World {
        }
        class EntityId {
        }
        class Component {
            <<trait>>
        }
        class System {
            <<trait>>
        }
        class EntityFactory {
        }
    }
    
    %% Package Controller
    namespace Controller {
        class GameController
        class GameSystemsState
        class EventHandler {
          <<trait>>
        }
        class EventQueue
    }
    
    %% Package Engine
    namespace Engine {
        class GameEngine {
          <<trait>>
        }
        class GameLoop {
          <<trait>>
        }
        class GameState
    }
    
    %% Package View
    namespace View {
        class ViewController {
          <<object>>
        }
        class GameView {
          <<object>>
        }
        class MainMenu {
          <<object>>
        }
        class ShopPanel {
          <<object>>
        }
        class WavePanel {
          <<object>>
        }
    }
    
    %% Relationships - Model Internal (ECS Pattern)
    World o-- EntityId
    World o-- Component
    System ..> World
    EntityFactory ..> World
    EntityFactory ..> Component
    
    %% Relationships - Controller Internal
    GameController --> GameSystemsState
    GameController --> EventHandler
    EventHandler --> EventQueue
    EventHandler ..> GameController
    GameSystemsState *-- System
    
    %% Relationships - Engine Internal
    GameEngine --> GameLoop
    GameEngine --> GameState
    GameEngine --> GameController
    
    %% Relationships - View Internal
    ViewController ..> GameView
    ViewController ..> MenuView
    GameView ..> ShopPanel
    GameView ..> WavePanel
    
    %% Relationships - Between Packages (MVC)
    GameController --> World
    GameController --> System
    GameController --> GameEngine
    ViewController --> GameController
    GameView ..> ViewController
```

La struttura del progetto è organizzata in quattro moduli principali, che riflettono una chiara separazione delle 
responsabilità ispirata al pattern Model-View-Controller (MVC), con una distinzione esplicita per il motore di 
gioco (Engine).

1. **Model (ECS)**: Rappresenta il nucleo logico del gioco. Implementato con il pattern Entity-Component-System, questo 
modulo definisce la struttura dei dati (`Component`), la logica di gioco (`System`) e il contenitore dello 
stato del mondo (`World`). È completamente disaccoppiato dagli altri moduli e si occupa esclusivamente 
delle regole e dello stato della simulazione. La EntityFactory astrae la creazione delle entità di gioco.

2. **Engine**: È il cuore pulsante dell'applicazione. Il `GameEngine` gestisce il ciclo di vita del gioco, 
orchestrando le transizioni di stato (es. `Playing`, `Paused`) definite in `GameState`. Si appoggia al `GameLoop` per 
garantire un'esecuzione a timestep fisso, assicurando che la logica di gioco progredisca in modo deterministico e 
indipendente dal frame rate.

3. **Controller**: Agisce come intermediario, collegando l'input dell'utente, la logica di gioco e il motore. 
Il `GameController` riceve eventi dall'`EventHandler` e comanda al `GameEngine` di aggiornare lo stato. 
Il `GameSystemsState` aggrega e gestisce l'esecuzione ordinata di tutti i sistemi logici del Model.

4. **View**: Costituisce l'interfaccia grafica. Il `ViewController` gestisce la navigazione tra le diverse 
schermate (`GameView`, `MenuView`, etc.). La `GameView` si occupa del rendering degli elementi di gioco, 
inclusi componenti specifici come `ShopPanel` e `WavePanel`. La `View` cattura le interazioni dell'utente e 
le inoltra al `Controller` sotto forma di eventi, senza contenere alcuna logica di gioco.

## Principi di Programmazione Funzionale

L'intero progetto è stato sviluppato seguendo principi di programmazione funzionale, come richiesto dai requisiti:

* **Immutabilità**: Tutte le strutture dati (Component, configurazioni) sono immutabili. I System restituiscono nuove versioni di sé stessi invece di modificare lo stato interno. Questo elimina bug legati a modifiche inattese e garantisce thread-safety.

* **Composizione su Ereditarietà**: L'ECS favorisce la composizione tramite Component invece di gerarchie di classi rigide.

* **Funzioni Pure**: I System sono implementati come funzioni il più possibile pure: dato un input (World), producono un output (nuovo World) in modo deterministico.

* **Type Classes**: Utilizzo di given instances e type classes per il polimorfismo ad-hoc nella creazione delle entità, garantendo type-safety ed estensibilità.

* **Opaque Types**: Utilizzo di opaque types (EntityId) per type-safety senza overhead runtime.


Questo approccio combina performance e correttezza, garantendo stabilità anche con thread multipli.