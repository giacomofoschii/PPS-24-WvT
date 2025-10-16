---
title: Design Architetturale
layout: default
nav_order: 5
has_children: false
---

# Design architetturale

Il design architetturale del sistema è stato elaborato a partire dai requisiti funzionali e non funzionali identificati. L'obiettivo principale è stato creare una struttura modulare, performante ed estensibile che potesse gestire la complessità di un gioco tower defense come _Wizards vs Trolls_, garantendo alte performance con numerose entità simultanee e una chiara separazione delle responsabilità tra i vari componenti.

## Pattern Architetturale MVC (Model-View-Controller)

Questo pattern è ampiamente utilizzato nello sviluppo di applicazioni software per separare la logica dalla sua rappresentazione grafica e dall'interazione con l'utente. I suoi tre componenti principali svolgono ruoli specifici:

* **Model**: Il Model rappresenta il cuore dell'applicazione. Contiene i dati e la logica di business del sistema. La sua responsabilità è gestire lo stato dell'applicazione, manipolare i dati e implementare tutte le regole del gioco. È completamente disaccoppiato dalla rappresentazione grafica e non ha conoscenza dell'interfaccia utente. Nel nostro caso, il Model contiene tutte le entità di gioco (maghi, troll, proiettili), le loro caratteristiche, la logica di combattimento, movimento, gestione risorse e progressione delle ondate. Per implementare il Model abbiamo scelto di utilizzare il pattern **Entity Component System (ECS)**, che verrà descritto in dettaglio nella sezione successiva.

* **View**: La View è l'interfaccia utente. Il suo scopo è presentare i dati del Model all'utente e raccogliere gli input dell'utente. La View non contiene alcuna logica di gioco e si limita a visualizzare lo stato corrente del Model. Nel nostro progetto, la View include la griglia di gioco, la visualizzazione delle entità (maghi, troll, proiettili), l'HUD con informazioni su elisir e ondata corrente, il menu principale e la schermata di game over. Quando l'utente interagisce con la View (ad esempio, cliccando per posizionare un mago), questa inoltra l'input al Controller per la sua elaborazione.

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

La struttura del progetto è organizzata in moduli principali, riflettendo la separazione MVC con il Model implementato tramite ECS:

* **Model (ECS Core)**: Contiene la logica del gioco e la gestione dello stato tramite Entity Component System.
    * `EntityId`: Identificatore unico per ogni entità di gioco.
    * `EntityIdGenerator`: Generatore funzionale e thread-safe di identificatori univoci.
    * `Component`: Trait base per tutti i componenti che definiscono aspetti delle entità (posizione, salute, attacco, movimento, tipo, ecc.).
    * `System`: Trait base per tutti i sistemi che implementano la logica di gioco (movimento, combattimento, elisir, ondate, salute, proiettili).
    * `World`: Contenitore immutabile che mantiene tutte le entità e i loro Component, fornendo API per creare, modificare e interrogare le entità.
    * `EntityFactory`: Factory funzionale basata su type classes per la creazione di entità (maghi, troll, proiettili) a partire da configurazioni.

* **Controller**: Contiene i componenti che gestiscono il flusso del gioco e coordinano la comunicazione tra Model e View.
    * `GameEngine`: Coordina l'esecuzione sequenziale di tutti i System, mantiene il World corrente, gestisce le condizioni di game over e fornisce l'interfaccia per il posizionamento dei maghi.
    * `GameLoop`: Implementa il fixed timestep loop a 60 FPS, gestisce l'accumulator per aggiornamenti deterministici, supporta pausa/ripresa e calcola gli FPS correnti.

* **View**: Contiene tutti i componenti responsabili dell'interfaccia grafica e dell'interazione con l'utente.
    * `GameView`: Implementa l'interfaccia grafica principale del gioco, visualizzando la griglia, le entità (maghi, troll, proiettili), le barre della vita, l'HUD con elisir e ondata corrente, e gestendo gli input dell'utente per il posizionamento dei maghi.
    * `MenuView`: Gestisce il menu principale con opzioni per avviare una nuova partita o visualizzare le informazioni di gioco.
    * `GameOverView`: Mostra la schermata di fine partita con le statistiche finali (ondate superate, nemici eliminati, elisir speso).
    * `HUDComponents`: Componenti riutilizzabili per l'interfaccia (barra elisir, pannello selezione maghi, contatore ondata).

* **Utilities**: Contiene classi di supporto e configurazioni condivise.
    * `Position`: Classe per rappresentare coordinate (x, y) con operazioni di distanza e direzione.
    * `GamePlayConstants`: Costanti relative al gameplay (statistiche maghi e troll, costi, velocità, danni, cooldown).
    * `ViewConstants`: Costanti relative alla visualizzazione (dimensioni griglia, celle, colori, dimensioni barre vita).
    * `GameConstants`: Costanti generali (FPS target, timestep, intervalli di spawn).

## Principi di Programmazione Funzionale

L'intero progetto è stato sviluppato seguendo principi di programmazione funzionale, come richiesto dai requisiti:

* **Immutabilità**: Tutte le strutture dati (Component, configurazioni) sono immutabili. I System restituiscono nuove versioni di sé stessi invece di modificare lo stato interno. Questo elimina bug legati a modifiche inattese e garantisce thread-safety.

* **Composizione su Ereditarietà**: L'ECS favorisce la composizione tramite Component invece di gerarchie di classi rigide.

* **Funzioni Pure**: I System sono implementati come funzioni il più possibile pure: dato un input (World), producono un output (nuovo World) in modo deterministico.

* **Type Classes**: Utilizzo di given instances e type classes per il polimorfismo ad-hoc nella creazione delle entità, garantendo type-safety ed estensibilità.

* **Opaque Types**: Utilizzo di opaque types (EntityId) per type-safety senza overhead runtime.


Questo approccio combina performance e correttezza, garantendo stabilità anche con thread multipli.