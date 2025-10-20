---
title: Requisiti
nav_order: 2
parent: Report
---

# Requisiti

L'analisi del problema svolta nella prima fase del progetto ha permesso di evidenziare i requisiti elencati di seguito.

## Requisiti di business

* **Creare un'esperienza di gioco coinvolgente e sfidante**: Il genere tower defense è stato scelto per la sua natura strategica che richiede pianificazione e decisioni tattiche continue. La scelta di un sistema a ondate infinite con difficoltà crescente, invece di livelli predefiniti, è motivata dalla volontà di massimizzare la rigiocabilità e creare una sfida sempre nuova per il giocatore.
* **Utilizzo di Scala e di paradigmi funzionali**: Il progetto deve essere sviluppato in Scala 3 per permettere al team di apprendere e applicare costrutti funzionali di alto livello.
* **Rispetto della deadline**: Realizzare il progetto entro il 24 ottobre 2025, pianificando gli sprint in modo da completare prima le funzionalità core e lasciare le feature opzionali per le iterazioni finali.

## Modello di dominio

Il dominio del progetto ruota attorno ai seguenti concetti principali:

### Grid

La griglia di gioco rappresenta il campo di battaglia.
* Definisce le posizioni valide dove possono essere posizionati i maghi.
* È composta da celle organizzate in righe (5) e colonne (9).
* Mantiene traccia delle celle occupate e di quelle disponibili.
* Valida i tentativi di posizionamento impedendo sovrapposizioni.

### Wizard

Unità difensiva posizionabile dal giocatore sulla griglia.
* Ha un costo in elisir necessario per il posizionamento.
* Possiede statistiche: salute, danno, raggio d'attacco, tempo di ricarica tra attacchi.
* Attacca automaticamente i troll che entrano nel proprio raggio.
* Rimane fermo nella posizione in cui è stato piazzato.
* Viene rimosso quando la salute raggiunge zero.
* Esistono cinque tipi di maghi:
    * **Mago Generatore**: Genera 25 elisir ogni 10 secondi invece di attaccare. Costo: 100, Vita: 150.
    * **Mago del Vento**: Attacco base a distanza. Costo: 150, Vita: 100, Danno: 25, Gittata: 3.0, Cooldown: 3s.
    * **Mago del Fuoco**: Infligge alto danno a corto raggio. Costo: 250, Vita: 100, Danno: 50, Gittata: 2.0, Cooldown: 2.5s.
    * **Mago del Ghiaccio**: Rallenta temporaneamente i nemici colpiti. Costo: 200, Vita: 150, Danno: 25, Gittata: 2.5, Cooldown: 4s.
    * **Mago Barriera**: Alta salute ma nessun attacco. Funziona come muro difensivo. Costo: 200, Vita: 300.

### Troll

Unità nemica che avanza verso la torre.
* Si muove automaticamente da destra verso sinistra.
* Ha statistiche: salute, velocità, danno, raggio d'attacco, cooldown.
* Attacca i maghi che incontra sul percorso (se nel raggio d'azione).
* Se raggiunge il lato sinistro della griglia, causa la sconfitta immediata del giocatore.
* Viene rimosso quando la salute raggiunge zero.
* Rilascia elisir come ricompensa quando eliminato.
* Esistono quattro tipi di troll:
    * **Troll Base**: Statistiche equilibrate. Vita: 100, Vel: 0.10, Danno: 20, Gittata: 1.0, Cooldown: 1s.
    * **Troll Guerriero**: Alta salute e danno, movimento più veloce, corto raggio. Vita: 130, Vel: 0.15, Danno: 30, Gittata: 0.5, Cooldown: 1.5s.
    * **Troll Assassino**: Altissima velocità e danno ma bassa salute, si muove a zigzag. Vita: 70, Vel: 0.2, Danno: 60, Gittata: 1.5, Cooldown: 0.8s.
    * **Troll Lanciatore**: Attacca i maghi a distanza, rimanendo fermo. Vita: 40, Vel: 0.10, Danno: 10, Gittata: 5.0, Cooldown: 3s.

### Elixir

Risorsa economica gestita dal giocatore.
* Ha un valore corrente (inizia a 200) e un limite massimo (1000).
* Si rigenera automaticamente (+100 elisir ogni 10 secondi).
* Viene consumato per posizionare i maghi.
* Viene guadagnato sconfiggendo i troll (quantità varia per tipo di troll).
* Può essere generato dai Maghi Generatori (+25 elisir ogni 10 secondi).
* Determina quali maghi possono essere posizionati in un dato momento.

### Castle

Obiettivo da difendere (non una vera entità, ma la condizione di sconfitta).
* Rappresenta la meta finale del percorso dei troll (lato sinistro della griglia).
* Se un troll la raggiunge, la partita termina immediatamente con la sconfitta.

### Wave

Ondata di troll che appare periodicamente.
* Ha un numero identificativo progressivo.
* Contiene una composizione di diversi tipi di troll generata proceduralmente.
* La difficoltà aumenta ad ogni ondata successiva:
    * Più troll vengono generati (max troll per ondata aumenta).
    * Le statistiche dei troll (salute, velocità, danno) aumentano progressivamente.
    * Il tempo tra le generazioni diminuisce.
    * La distribuzione dei tipi di troll cambia, introducendo tipi più forti.
* Il sistema di spawn si attiva dopo il posizionamento del primo mago.
* Non ha limite massimo: il gioco continua all'infinito fino alla sconfitta.

### Projectile

Proiettile lanciato dai maghi (Vento, Fuoco, Ghiaccio) o dai Troll Lanciatori.
* Ha una posizione e si muove verso il lato opposto (destra per maghi, sinistra per troll).
* Ha un tipo che determina il danno e gli effetti (Fuoco, Ghiaccio, Vento, Troll).
* Colpisce il primo bersaglio valido sulla stessa riga nella cella in cui entra.
* Viene rimosso dopo aver colpito.
* I proiettili del Mago del Ghiaccio applicano un effetto di rallentamento temporaneo.

## Requisiti funzionali

### Requisiti di utente

Dal punto di vista dell'utente, il sistema deve consentire:

* **Il setup della partita**:
    * Visualizzazione del menu principale.
    * Avvio di una nuova partita.
    * Accesso alle informazioni di gioco.

* **L'interazione di gioco**:
    * Selezionare un tipo di mago dal pannello laterale (shop) visualizzando costo, icona e nome.
    * Posizionare i maghi cliccando su celle valide della griglia.
    * Visualizzare in tempo reale:
        * Quantità di elisir disponibile.
        * Numero dell'ondata corrente.
        * Barre della vita di maghi e troll (quando non a vita piena o uguale a 0).
        * Proiettili in movimento.
    * Ricevere feedback immediato per:
        * Tentativi di posizionamento non validi (cella occupata, elisir insufficiente, mago non selezionato).
        * Inizio di nuove ondate.
    * Mettere in pausa il gioco e riprendere.

* **La fine della partita**:
    * Ricevere notifica chiara di game over quando un troll raggiunge la fine.
    * Ricevere notifica di vittoria alla fine di un'ondata.
    * Possibilità di continuare alla prossima ondata (dopo vittoria) o iniziare una nuova partita (dopo sconfitta o da menu pausa).
    * Possibilità di tornare al menu principale.

### Requisiti di sistema

Il sistema dovrà occuparsi di:

* **Gestione delle entità di gioco**:
    * Creare e mantenere tutte le entità (maghi, troll, proiettili) con identificatori unici.
    * Associare a ogni entità le sue caratteristiche (componenti: posizione, salute, statistiche, etc.).
    * Permettere ricerche efficienti di entità con specifiche caratteristiche.
    * Rimuovere automaticamente entità quando vengono eliminate.

* **Gestione dell'elisir**:
    * Inizializzare l'elisir al valore predefinito (200) all'inizio della partita.
    * Rigenerare elisir automaticamente a intervalli regolari (+100 ogni 10s).
    * Rispettare il limite massimo di elisir accumulabile (1000).
    * Verificare che il giocatore abbia elisir sufficiente prima di permettere il posizionamento di maghi.
    * Sottrarre il costo corretto quando un mago viene posizionato.
    * Aggiungere elisir quando un troll viene eliminato (quantità variabile).
    * Gestire la generazione periodica di elisir dai Maghi Generatori (+25 ogni 10s).

* **Validazione del posizionamento**:
    * Verificare che il click sia all'interno della griglia.
    * Verificare che la cella non sia già occupata da un altro mago.
    * Verificare che il giocatore abbia elisir sufficiente per il mago selezionato.
    * Fornire feedback (messaggio di errore) in caso di tentativo non valido.

* **Gestione del movimento**:
    * Aggiornare continuamente le posizioni dei troll (da destra a sinistra) e dei proiettili (direzione opposta) in base alla loro velocità e al delta time.
    * Implementare il movimento a zigzag per i Troll Assassini.
    * Applicare gli effetti di rallentamento quando un troll viene colpito dal ghiaccio, riducendone la velocità.
    * Rilevare quando un troll raggiunge il lato sinistro della griglia.
    * Rimuovere i proiettili che escono dai bordi dello schermo.

* **Gestione del combattimento**:
    * Implementare il targeting automatico: maghi attaccano il troll più vicino sulla stessa riga nel loro range; Troll Lanciatori attaccano il mago più vicino sulla stessa riga nel loro range.
    * Gestire i tempi di ricarica per ogni entità attaccante.
    * Creare proiettili quando un'entità effettua un attacco a distanza.
    * Rilevare le collisioni tra entità e bersagli nella stessa cella e gestirne l'effetto (meleeAttack o projectile-entity collision).
    * Applicare effetti speciali alla collisione.
    * Gestire il blocco del movimento per i Troll che attaccano in mischia.

* **Gestione della salute**:
    * Processare le collisioni per ridurre la vita delle entità.
    * Rilevare quando un'entità raggiunge salute zero o inferiore.
    * Rimuovere le entità morte dal gioco.
    * Assegnare ricompense in elisir quando un troll viene eliminato.

* **Gestione delle ondate**:
    * Iniziare a generare ondate di troll solo dopo che il primo mago è stato piazzato.
    * Determinare la composizione di ogni ondata (numero e tipi di troll) proceduralmente in base al numero dell'ondata.
    * Aumentare progressivamente la difficoltà:
        * Incrementando il numero massimo di troll per ondata.
        * Aumententando le statistiche base (salute, velocità, danno) dei troll generati.
        * Diminuendo l'intervallo tra le generazioni.
        * Modificando la probabilità di apparizione dei tipi di troll.
    * Generare troll in "batch" a intervalli randomizzati.
    * Rilevare il completamento di un'ondata (spawn terminato e nessun troll rimasto).

* **Ciclo di gioco principale**:
    * Mantenere aggiornato e consistente lo stato del gioco.
    * Processare tutti gli aggiornamenti dei sistemi ECS in un ordine definito.
    * Gestire correttamente la pausa e la ripresa del gioco, sospendendo gli aggiornamenti e la generazione di spawn.
    * Rilevare le condizioni di vittoria e sconfitta e terminare/procedere la partita.

* **Rendering e interfaccia**:
    * Disegnare la mappa di gioco e la griglia.
    * Visualizzare tutte le entità (maghi, troll, proiettili) nelle loro posizioni con le loro icone.
    * Mostrare l'effetto visivo per le entità congelate.
    * Disegnare le barre della vita delle entità con salute non piena.
    * Mostrare l'HUD con informazioni su elisir, ondata corrente, pannello shop.
    * Gestire la visualizzazione e l'interazione con i menu.

## Requisiti non funzionali

### Requisiti esterni

* **Performance**:
    * Mantenere un frame rate stabile (idealmente vicino a 60 FPS) anche con un numero elevato di entità.

* **Affidabilità**:
    * Garantire stabilità durante sessioni di gioco prolungate senza crash.
    * Gestire robustamente input utente non validi.
    * Mantenere la consistenza dello stato durante pausa/ripresa.

* **Usabilità**:
    * Interfaccia intuitiva.
    * Icone e testi chiari.
    * Feedback visivo immediato per le azioni.
    * Informazioni essenziali sempre visibili.

### Requisiti interni

* **Scalabilità**:
    * Facilità di aggiungere nuovi tipi di maghi e troll modificando principalmente le configurazioni.
    * Aggiungere nuove caratterisitche e funzionalità per le entità di gioco.
    * Capacità di introdurre nuove meccaniche.

* **Manutenibilità**:
    * Separazione netta tra logica di gioco e interfaccia grafica.
    * Codice modulare.
    * Utilizzo di immutabilità per ridurre effetti collaterali.
    * Codice ben documentato e con nomi descrittivi.

* **Testabilità**:
    * Logica di gioco testabile in isolamento dal rendering.
    * Comportamento deterministico facilitato dall'immutabilità e dal game loop fisso.
    * Utilizzo di DSL per creare scenari di test specifici.
    * Buona copertura dei test sulle logiche critiche.

## Requisiti di implementazione

* **Metodologia di sviluppo**: Agile SCRUM-inspired.
* **Architettura**: MVC con Model implementato tramite ECS.
* **Tecnologie e linguaggio**: Scala 3.x, ScalaFX per la UI, SBT per il build.
* **Testing**: ScalaTest.
* **Versioning e collaborazione**: Git, GitHub, GitHub Actions per CI.