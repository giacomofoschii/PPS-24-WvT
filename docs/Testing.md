---
title: Testing
layout: default
nav_order: 6
has_children: false
---

## 🧪 Testing

### Approccio
Il progetto è stato sviluppato seguendo un approccio **Test-Driven Development (TDD)**.  
Considerata la natura del gioco — basato su un’architettura ECS (Entity-Component-System) e su logiche di aggiornamento continue — è stato fondamentale garantire l’affidabilità del codice attraverso test unitari e di integrazione.

L’obiettivo è stato quello di mantenere un’elevata qualità del codice sin dalle prime fasi di sviluppo, scrivendo i test **prima dell’implementazione** delle funzionalità e assicurando che ogni parte del sistema fosse verificabile in modo indipendente.

---

### Tecnologie utilizzate
Per la scrittura e l’esecuzione dei test è stato utilizzato **ScalaTest**, uno dei framework più diffusi e maturi dell’ecosistema Scala.  
Le principali caratteristiche sfruttate includono:

- **Suite modulari**: una suite di test per ogni componente logico (Engine, ECS, Game Logic, Rendering)
- **Matchers espressivi**: per una sintassi più leggibile e semantica rispetto ai semplici `assert`
- **Test isolati**: grazie alla progettazione funzionale e immutabile, ogni test può essere eseguito senza dipendenze da stato globale

---

### Metodologia
Ogni funzionalità è stata implementata seguendo rigorosamente le fasi del ciclo **TDD**:

1. **Scrittura dei test unitari** relativi al comportamento atteso (ad esempio, movimento dei troll, cooldown dei maghi, gestione elisir)
2. **Esecuzione iniziale dei test** per verificarne il fallimento
3. **Implementazione della logica** fino al superamento di tutti i test
4. **Refactoring** del codice mantenendo i test verdi

Questo processo ha garantito uno sviluppo incrementale, stabile e facilmente manutenibile.

---

### Grado di copertura
Tutte le principali funzionalità del gioco sono coperte da test automatici.  
In particolare, sono stati testati:

- **Game Engine Core**: gestione dello stato, aggiornamenti e game loop
- **Entity & Component System**: creazione, rimozione e interazioni tra entità
- **Logica di Gioco**: movimento e comportamento dei troll, attacchi e abilità dei maghi, generazione dell’elisir, condizioni di vittoria/sconfitta
- **Sistema Economico e Progressione**: bilanciamento risorse, costi e progressione delle ondate
- **Gestione Eventi e Collisioni**: eventi interni e interazioni tra entità

I test sono stati eseguiti regolarmente durante tutto il ciclo di sviluppo, assicurando:
- **Correttezza logica**
- **Robustezza rispetto a input non validi**
- **Stabilità e coerenza tra moduli** dopo ogni refactoring