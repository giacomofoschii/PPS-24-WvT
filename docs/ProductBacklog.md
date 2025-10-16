# Product Backlog

---

## Sprint 1 - Foundation & Core Systems

### Giovanni Pisoni
**Logica**
- Game Engine Core: `GameState`, `GameEngine` trait, architettura base
- Game Loop: main loop, delta time, cicli di aggiornamento
- Event System: definizione eventi, coda eventi, processamento eventi

**UI / Rendering**
- Game Loop Rendering: integrazione del rendering nel loop principale

---

### Giovanni Rinchiuso
**Logica**
- Menu State Management: gestione della navigazione tra menu, transizioni di stato
- Input Handling System: gestione input utente, validazione, key mapping
- ECS Base Models: definizione dei trait `Entity`, `World`, `System`, e degli enum base

**UI / Rendering**
- Info Menu UI: schermata delle regole e descrizione di maghi/troll
- UI Framework Setup: sistema base di rendering e gestione finestra

---

### Giacomo Foschi
**Logica**
- Project Setup: configurazione repository, build SBT, architettura MVC, CI/CD
- Grid Logic: struttura dati per la griglia (5x9), logica di posizionamento e validazioni
- Configuration System: costanti di gioco, valori di bilanciamento, gestione impostazioni, creazione componenti

**UI / Rendering**
- Main Menu UI: schermata principale con bottoni Start/Info/Exit
- Basic Grid Visualization: rendering base della griglia vuota

**Milestone Sprint 1:**  
Menu funzionanti, griglia visualizzabile, engine base attivo.

---

## Sprint 2 - Entities & Basic Interactions

### Giovanni Pisoni
**Logica**
- Troll Logic: implementazione dei 4 tipi di troll
- Troll Movement AI: pathfinding e comportamento di movimento
- Spawn System: logica di generazione ondate e gestione del timing

**UI / Rendering**
- Pause Menu UI
- Visual Feedback: barre della vita per troll e maghi

---

### Giovanni Rinchiuso
**Logica**
- Wizard Logic: implementazione dei 5 tipi di maghi
- Wizard Abilities: generazione elisir, attacco, barriera, ghiaccio
- Wizard Unlocking: sistema di progressione e sblocco maghi

**UI / Rendering**
- Wizard Shop UI: interfaccia di selezione e acquisto maghi
- Wizard Visual Effects: effetti visivi per gli attacchi dei maghi

---

### Giacomo Foschi
**Logica**
- Entity Management: creazione, rimozione e tracking delle entità
- Collision System: rilevamento collisioni e interazioni base
- Game Rules Engine: validazione delle mosse e gestione condizioni di vittoria/sconfitta

**UI / Rendering**
- Entity Rendering: visualizzazione di maghi e troll sulla griglia

**Milestone Sprint 2:**  
Maghi e troll funzionanti, interazioni base, shop UI attivo.

---

## Sprint 3 - Economy & Combat Systems

### Giovanni Pisoni
**Logica**
- Movement System: passaggio dal movimento a celle a quello in pixel per troll e proiettili
- Win and Lose Condition: gestione delle condizioni di vittoria e sconfitta
- (Se possibile) Ottimizzazione del game loop

**UI / Rendering**
- Win/Lose Panel: pannello di vittoria o sconfitta

---

### Giovanni Rinchiuso
**Logica**
- Balancing: bilanciamento dei parametri di gioco
- Wave Management: progressione delle ondate e difficoltà crescente

**UI / Rendering**
- Game HUD: contatore elisir (opzionale) e informazioni sull’ondata
- Game Statistics (Pause Menu): pannello con troll uccisi, livello corrente e danno totale

---

### Giacomo Foschi
**Logica**
- Collision System: miglioramento e rifinitura del sistema di collisioni
- Effetto Gelo: implementazione dell’effetto rallentante sui troll

**UI / Rendering**
- Effetto Gelo: visualizzazione grafica del congelamento

**Milestone Sprint 3:**  
Sistema economico completo, combattimento funzionante.

---

## Sprint 4 - Polish & Game Flow

### Giovanni Pisoni
**Logica**
- Code Refactoring: ottimizzazione e pulizia dell’architettura
- Testing
- Relazione finale

**UI / Rendering**
- Miglioramenti estetici e ottimizzazione finale dell’interfaccia

---

### Giovanni Rinchiuso
**Logica**
- Code Refactoring
- Testing
- Relazione finale

---

### Giacomo Foschi
**Logica**
- Code Refactoring
- Testing
- Relazione finale

**Milestone Sprint 4:**  
Gioco completo, rifinito e pronto per la consegna.

---

## Distribuzione complessiva e metriche

### Success Metrics per Sprint

**Sprint 1 – Foundation**
- Menu navigabili
- Griglia renderizzata
- Game loop funzionante
- Test coverage ≥ 70%

**Sprint 2 – Core Gameplay**
- Maghi piazzabili
- Troll in movimento
- Shop funzionante
- Interazioni base operative

**Sprint 3 – Complete Systems**
- Combat system completo
- Economia bilanciata
- Power-up utilizzabili
- HUD informativo

**Sprint 4 – Production Ready**
- Gioco completamente giocabile
- UI rifinita
- Bug critici risolti
- Documentazione completa
