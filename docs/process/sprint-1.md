---
title: Sprint 1
layout: default
nav_order: 0
parent: Processo di Sviluppo
---

# Sprint 1 - Foundation & Core Systems

## Obiettivo

L'obiettivo di questo primo Sprint è quello di creare le fondamenta del gioco, implementando il core dell'engine, i sistemi di input ed eventi, e i menu di base. Al termine dello sprint, l'utente dovrà poter navigare tra i menu principali (Main Menu e Info Menu) e visualizzare la griglia di gioco vuota. Il game loop principale dovrà essere funzionante e pronto per integrare le meccaniche di gioco negli sprint successivi.

## Deadline

La scadenza dello sprint è il 16 settembre.

## Backlog

| Nome | Descrizione | Sprint Task | Volontario         |
|------|-------------|-------------|--------------------|
| **Project Setup** | Configurazione iniziale del progetto per adattarsi alla metodologia agile SCRUM | Configurazione repository | Giacomo Foschi     |
| | | Setup build SBT | Giacomo Foschi     |
| | | Definizione architettura MVC | Giacomo Foschi     |
| | | Setup CI/CD | Giacomo Foschi     |
| **Game Engine Core** | Implementazione del cuore del game engine | Implementazione `GameState` | Giovanni Pisoni    |
| | | Definizione trait `GameEngine` | Giovanni Pisoni    |
| | | Implementazione architettura base engine | Giovanni Pisoni    |
| **Game Loop** | Creazione del main loop di gioco | Implementazione main loop | Giovanni Pisoni    |
| | | Implementazione gestione delta time | Giovanni Pisoni    |
| | | Implementazione cicli di aggiornamento | Giovanni Pisoni    |
| **Event System** | Sistema di gestione eventi | Definizione eventi | Giovanni Pisoni    |
| | | Implementazione coda eventi | Giovanni Pisoni    |
| | | Implementazione processamento eventi | Giovanni Pisoni    |
| **Game Loop Rendering** | Integrazione del sistema di rendering nel game loop principale | Integrazione rendering nel loop principale | Giovanni Pisoni    |
| **UI Framework Setup** | Implementazione del sistema base di rendering | Implementazione sistema base di rendering | Giacomo Foschi     |
| | | Implementazione gestione finestra | Giacomo Foschi     |
| **Main Menu UI** | Realizzazione della schermata principale del gioco | Creazione schermata principale | Giacomo Foschi     |
| | | Implementazione bottone Start | Giacomo Foschi     |
| | | Implementazione bottone Info | Giacomo Foschi     |
| | | Implementazione bottone Exit | Giacomo Foschi     |
| **Info Menu UI** | Creazione della schermata informativa | Implementazione schermata regole | Giovanni Rinchiuso |
| | | Implementazione descrizione maghi | Giovanni Rinchiuso |
| | | Implementazione descrizione troll | Giovanni Rinchiuso |
| **Menu State Management** | Gestione della navigazione tra i vari menu | Implementazione gestione navigazione tra menu | Giovanni Rinchiuso |
| | | Implementazione transizioni di stato | Giovanni Pisoni    |
| **Input Handling System** | Sistema di gestione degli input dell'utente | Implementazione gestione input utente | Giovanni Rinchiuso |
| | | Implementazione validazione input | Giovanni Rinchiuso |
| | | Implementazione key mapping | Giovanni Rinchiuso |
| **ECS Base Models** | Definizione dei modelli base per l'Entity Component System | Definizione trait `Entity` | Giovanni Rinchiuso |
| | | Definizione trait `World` | Giovanni Rinchiuso |
| | | Definizione trait `System` | Giovanni Rinchiuso |
| | | Definizione enum base | Giovanni Rinchiuso |
| **Grid Logic** | Implementazione della struttura dati per la griglia di gioco (5x9) | Implementazione struttura dati griglia | Giacomo Foschi     |
| | | Implementazione logica di posizionamento | Giacomo Foschi     |
| | | Implementazione validazioni | Giacomo Foschi     |
| **Basic Grid Visualization** | Rendering base della griglia vuota | Implementazione visualizzazione griglia | Giacomo Foschi     |
| | | Implementazione rendering celle | Giacomo Foschi     |
| | | Implementazione sistema coordinate | Giacomo Foschi     |
| **Configuration System** | Sistema di gestione delle configurazioni | Definizione costanti di gioco | Giacomo Foschi     |
| | | Definizione valori di bilanciamento | Giacomo Foschi     |
| | | Implementazione gestione impostazioni | Giacomo Foschi     |
| | | Implementazione creazione componenti | Giacomo Foschi     |

## Sprint Review

Lo stakeholder si ritiene soddisfatto del lavoro svolto durante il primo sprint. Gli obiettivi prefissati sono stati raggiunti: il sistema di menu è completamente navigabile, permettendo all'utente di spostarsi fluentemente tra Main Menu e Info Menu. La griglia di gioco viene visualizzata correttamente nella sua forma base, mostrando tutte le 45 celle (5x9) con un sistema di coordinate funzionante.

## Sprint Retrospective

Lo sprint ha avuto una durata di una settimana, dedicando la prima fase principalmente alla configurazione iniziale del progetto e allo studio approfondito dell'architettura. La suddivisione dei task è risultata nel complesso equilibrata tra i tre membri del team, con una chiara separazione delle responsabilità: Giovanni Pisoni sul core engine, Giovanni Rinchiuso sui sistemi di input e UI, e Giacomo Foschi sulla configurazione e sulla griglia.

### Cosa è andato bene

- La configurazione iniziale del progetto (repository, SBT, CI/CD) è stata completata rapidamente, permettendo a tutti di iniziare lo sviluppo senza blocchi tecnici
- L'architettura MVC ed ECS è stata definita in modo chiaro fin dall'inizio, facilitando il lavoro parallelo
- La comunicazione tra i membri del team è stata efficace, con meeting giornalieri produttivi

### Cosa può essere migliorato

- Il carico di lavoro per questo sprint si è rivelato leggermente eccessivo, nonostante gli obiettivi siano stati raggiunti

### Action items per il prossimo sprint

- Monitorare il carico di lavoro per evitare sovraccarichi e garantire un ritmo sostenibile


