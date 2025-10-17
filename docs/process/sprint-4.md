---
title: Sprint 4
layout: default
nav_order: 3
parent: Processo di Sviluppo
---

# Sprint 4 - Polish & Game Flow

## Obiettivo

L'obiettivo di questo quarto e ultimo Sprint è quello di rifinire il gioco attraverso refactoring mirati dell'architettura core, completare il testing e migliorare l'interfaccia utente. Al termine dello sprint, il gioco dovrà essere completamente funzionante, privo di bug critici, con un'architettura pulita e ben strutturata (in particolare World e GameController), un'interfaccia rifinita e professionale, e una copertura di test adeguata.

## Deadline

La scadenza dello sprint è il 15 ottobre.

## Backlog

| Nome | Descrizione | Sprint Task | Volontario |
|------|-------------|-------------|------------|
| **GameController Refactoring** | Ristrutturazione e ottimizzazione del GameController | Analisi struttura GameController | Giovanni Pisoni |
| | | Refactoring logica core GameController | Giovanni Pisoni |
| | | Ottimizzazione gestione stati | Giovanni Pisoni |
| | | Pulizia dipendenze GameController | Giovanni Pisoni |
| **Testing Core Systems** | Testing dei sistemi refactorati | Test suite GameController | Giovanni Pisoni |
| | | Test suite Game Loop | Giovanni Pisoni |
| | | Test suite Event System | Giovanni Pisoni |
| | | Test integrazione sistemi core | Giovanni Pisoni |
| **UI Polish** | Miglioramenti estetici e funzionali dell'interfaccia | Raffinamento Pause Menu | Giovanni Pisoni |
| | | Raffinamento Win/Lose Panel | Giovanni Pisoni |
| **UI Polish Menu** | Miglioramenti estetici menu principali | Raffinamento Main Menu | Giovanni Rinchiuso |
| | | Raffinamento Info Menu | Giovanni Rinchiuso |
| **Input Handling Refactoring** | Adattamento del sistema di input ai nuovi World e GameController | Refactoring InputHandler | Giovanni Rinchiuso |
| **System Refactoring** | Adattamento dei system ECS ai nuovi World e GameController | Refactoring ElixirSystem | Giovanni Rinchiuso |
| | | Refactoring HealthSystem | Giovanni Rinchiuso |
| **World Refactoring** | Ristrutturazione completa del World | Analisi struttura World | Giacomo Foschi |
| | | Refactoring gestione entità | Giacomo Foschi |
| | | Refactoring query sistema | Giacomo Foschi |
| | | Ottimizzazione performance World | Giacomo Foschi |
| | | Pulizia interfacce World | Giacomo Foschi |
| **System Refactoring Advanced** | Refactoring dei system complessi | Refactoring CollisionSystem | Giacomo Foschi |
| | | Refactoring CombatSystem | Giacomo Foschi |
| | | Refactoring RenderSystem | Giacomo Foschi |
| | | Refactoring HealthBarRenderSystem | Giacomo Foschi |
| | | Refactoring MovementSystem | Giacomo Foschi |
| | | Refactoring SpawnSystem | Giacomo Foschi |
| **Testing Infrastructure** | Testing dei sistemi infrastrutturali | Test suite World | Giacomo Foschi |
| | | Test suite Grid Logic | Giacomo Foschi |
| | | Test suite Entity Management | Giacomo Foschi |
| | | Test suite Collision System | Giacomo Foschi |
| **Bug Fixing Finale** | Risoluzione di bug emersi durante il refactoring | Identificazione bug post-refactoring | Giacomo Foschi |
| | | Risoluzione bug critici | Giacomo Foschi |
| | | Risoluzione bug minori | Giacomo Foschi |

## Sprint Review

Lo stakeholder riconosce che il gioco è funzionante e pronto per la consegna. Dal punto di vista del prodotto finale, non ci sono stati cambiamenti significativi rispetto allo Sprint 3: le funzionalità sono le stesse, l'interfaccia è sostanzialmente identica, e l'esperienza di gioco non ha subito modifiche evidenti. Lo stakeholder nota alcuni miglioramenti minori nell'interfaccia e conferma che il gioco è stabile e privo di bug critici. Apprezza il fatto che il team abbia dedicato tempo al consolidamento della qualità interna del progetto, anche se questi sforzi non sono direttamente visibili nell'esperienza utente. Il prodotto finale è completo e soddisfa tutti i requisiti iniziali stabiliti all'inizio del progetto.

## Sprint Retrospective

Lo sprint finale ha avuto una durata di nove giorni ed è stato caratterizzato da un refactoring architetturale significativo concentrato su World e GameController, i due componenti più critici del sistema. Questo approccio, pur rischioso nell'ultimo sprint, si è rivelato necessario per migliorare la qualità interna del codice. Il team ha lavorato con grande coordinazione per gestire le dipendenze tra i componenti durante il refactoring.

### Cosa è andato bene

- Il refactoring del World di Giacomo ha migliorato la struttura, rendendola più pulita, efficiente e facile da comprendere rispetto alla versione precedente
- Il refactoring del GameController ha eliminato molte responsabilità eccessive, rendendo il codice più manutenibile e testabile
- Il refactoring massiccio dei system (CollisionSystem, CombatSystem, RenderSystem, HealthBarRenderSystem, MovementSystem, SpawnSystem) ha migliorato significativamente la qualità dell'architettura ECS
- Il testing parallelo ha permesso di identificare immediatamente eventuali regressioni causate dal refactoring
- Nonostante il refactoring importante, non sono stati introdotti bug critici nel sistema


### Cosa può essere migliorato

- Alcuni test hanno dovuto essere riscritti completamente a causa dei cambiamenti architetturali
- La scelta di prioritizzare la qualità del codice rispetto alle funzionalità visibili all'utente potrebbe non essere stata ottimale per l'ultimo sprint

