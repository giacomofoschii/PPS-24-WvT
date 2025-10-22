---
title: Testing
nav_order: 6
parent: Report
---

## Testing

### Approccio

Considerata la natura del gioco — basato su un'architettura ECS (Entity-Component-System) e su logiche di aggiornamento continue — è stato fondamentale garantire l'affidabilità del codice attraverso test unitari e di integrazione.

L'obiettivo è stato quello di mantenere un'elevata qualità del codice sin dalle prime fasi di sviluppo, scrivendo i test **in parallelo all'implementazione** delle funzionalità e assicurando che ogni parte del sistema fosse verificabile in modo indipendente.

### Tecnologie utilizzate

Per la scrittura e l'esecuzione dei test è stato utilizzato **ScalaTest**.  
Le principali caratteristiche sfruttate includono:

- **Suite modulari**: una suite di test per ogni componente logico (Engine, ECS, Game Logic, Rendering)
- **Matchers espressivi**: per una sintassi più leggibile e semantica rispetto ai semplici `assert`
- **Test isolati**: grazie alla progettazione funzionale e immutabile, ogni test può essere eseguito senza dipendenze da stato globale

### Grado di copertura

Tutte le principali funzionalità del gioco sono coperte da test automatici.  
In particolare, sono stati testati:

- **Game engine core**: gestione dello stato, aggiornamenti e game loop
- **Entity & Component System**: creazione, rimozione e interazioni tra entità
- **Logica di gioco**: movimento e comportamento dei troll, attacchi e abilità dei maghi, generazione dell'elisir, condizioni di vittoria/sconfitta
- **Sistema dell'elisir e progressione**: bilanciamento risorse, costi e progressione delle ondate
- **Gestione eventi e collisioni**: eventi interni e interazioni tra entità

I test sono stati eseguiti regolarmente durante tutto il ciclo di sviluppo, assicurando:

- **Correttezza logica**
- **Robustezza rispetto a input non validi**
- **Stabilità e coerenza tra moduli** dopo ogni refactoring