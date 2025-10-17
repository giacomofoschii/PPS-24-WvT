---
title: Sprint 2
layout: default
nav_order: 2
parent: Sprint
---

# Sprint 2 - Entities & Basic Interactions

## Obiettivo

L'obiettivo di questo secondo Sprint è quello di implementare le entità principali del gioco (maghi e troll) con le loro logiche di base, il sistema di shop per l'acquisto dei maghi, e le interazioni fondamentali tra le entità. Al termine dello sprint, l'utente dovrà poter posizionare maghi sulla griglia attraverso uno shop funzionante, vedere i troll muoversi lungo il percorso. Il sistema di pausa dovrà essere operativo per permettere una migliore gestione del gioco.

## Deadline

La scadenza dello sprint è il 28 settembre.

## Backlog

| Nome | Descrizione | Sprint Task                                   | Volontario |
|------|-------------|-----------------------------------------------|------------|
| **Troll Logic** | Implementazione dei 4 tipi di troll | Implementazione troll base                    | Giovanni Pisoni |
| | | Implementazione troll warrior                 | Giovanni Pisoni |
| | | Implementazione troll assassino               | Giovanni Pisoni |
| | | Implementazione troll thrower                 | Giovanni Pisoni |
| **Troll Movement** | Sistema di movimento dei troll | Implementazione comportamento di movimento    | Giovanni Pisoni |
| **Spawn System** | Sistema di generazione delle ondate di troll | Implementazione logica generazione ondate     | Giovanni Pisoni |
| | | Implementazione gestione timing spawn         | Giovanni Pisoni |
| **Pause Menu UI** | Interfaccia di pausa del gioco | Implementazione schermata pausa               | Giovanni Pisoni |
| | | Implementazione bottone Resume                | Giovanni Pisoni |
| | | Implementazione bottone Main Menu             | Giovanni Pisoni |
| **Visual Feedback** | Sistema di feedback visivo per la salute delle entità | Implementazione barre della vita troll        | Giovanni Pisoni |
| | | Implementazione barre della vita maghi        | Giovanni Pisoni |
| **Wizard Logic** | Implementazione dei 5 tipi di maghi | Implementazione mago wind                     | Giovanni Rinchiuso |
| | | Implementazione mago ice                      | Giovanni Rinchiuso |
| | | Implementazione mago fire                     | Giovanni Rinchiuso |
| | | Implementazione mago barrier                  | Giovanni Rinchiuso |
| | | Implementazione mago generator                | Giovanni Rinchiuso |
| **Wizard Abilities** | Implementazione delle abilità specifiche dei maghi | Implementazione generazione elisir            | Giovanni Rinchiuso |
| | | Implementazione sistema attacco               | Giovanni Rinchiuso |
| | | Implementazione abilità barriera              | Giovanni Rinchiuso |
| | | Implementazione abilità ghiaccio              | Giovanni Rinchiuso |
| **Wizard Shop UI** | Interfaccia per l'acquisto dei maghi | Implementazione interfaccia shop              | Giovanni Rinchiuso |
| | | Implementazione selezione maghi               | Giovanni Rinchiuso |
| | | Implementazione sistema acquisto              | Giovanni Rinchiuso |
| **Entity Management** | Sistema di gestione del ciclo di vita delle entità | Implementazione creazione entità              | Giacomo Foschi |
| | | Implementazione rimozione entità              | Giacomo Foschi |
| | | Implementazione tracking entità               | Giacomo Foschi |
| **Entity Rendering** | Visualizzazione delle entità sulla griglia | Implementazione rendering maghi               | Giacomo Foschi |
| | | Implementazione rendering troll               | Giacomo Foschi |

## Sprint Review

Lo stakeholder esprime soddisfazione per i progressi realizzati durante il secondo sprint. Tutti gli obiettivi chiave sono stati raggiunti con successo: i cinque tipi di maghi sono stati implementati con le loro abilità distintive e possono essere acquistati attraverso uno shop intuitivo e funzionale. Tutti i maghi sono disponibili fin dall'inizio, semplificando l'esperienza di gioco. I quattro tipi di troll si muovono correttamente lungo il percorso, mostrando comportamenti differenziati in base al tipo.  Il sistema di pause permette all'utente di fermare il gioco in qualsiasi momento. Lo stakeholder apprezza la qualità del sistema di shop e la varietà delle abilità dei maghi, che rendono il gameplay già interessante.

## Sprint Retrospective

Lo sprint ha avuto una durata di una settimana e ha visto un'intensa attività di sviluppo su tutti e tre i fronti: entità, UI e sistemi di gioco. La complessità è aumentata rispetto allo Sprint 1, richiedendo una maggiore coordinazione tra i membri del team. La suddivisione dei task ha mantenuto una chiara separazione delle responsabilità: Giovanni Pisoni sui troll e spawn system, Giovanni Rinchiuso sui maghi e shop, Giacomo Foschi sui sistemi di gestione delle entità.

### Cosa è andato bene

- L'implementazione dei diversi tipi di maghi e troll è proceduta senza intoppi grazie alla solida architettura ECS definita nello Sprint 1


### Cosa può essere migliorato

- Il balancing delle abilità dei maghi non era stato previsto nel backlog e ha dovuto essere fatto in modo affrettato alla fine dello sprint
- La mancanza di un sistema di collisioni completo ha limitato la possibilità di testare a fondo le interazioni di combattimento

### Action items per il prossimo sprint

- Prioritizzare l'implementazione del sistema di collisioni per permettere interazioni complete tra entità
- Includere esplicitamente nel backlog task di balancing e tuning, non solo implementazione

