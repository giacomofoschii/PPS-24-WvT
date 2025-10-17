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
* Definisce le posizioni valide dove possono essere posizionati i maghi
* È composta da celle organizzate in righe e colonne
* Mantiene traccia delle celle occupate e di quelle disponibili
* Valida i tentativi di posizionamento impedendo sovrapposizioni

### Wizard
Unità difensiva posizionabile dal giocatore sulla griglia.
* Ha un costo in elisir necessario per il posizionamento
* Possiede statistiche: salute, danno, raggio d'attacco, tempo di ricarica tra attacchi
* Attacca automaticamente i troll che entrano nel proprio raggio
* Rimane fermo nella posizione in cui è stato piazzato
* Viene rimosso quando la salute raggiunge zero
* Esistono cinque tipi di maghi:
    * **Mago Generatore**: Genera elisir periodicamente invece di attaccare. 
    * **Mago del Vento**: Attacco con statistiche equilibrate tra danno, raggio e tempo di ricarica.
    * **Mago del Fuoco**: Infligge alto danno per singolo colpo, efficace contro nemici con molta salute.
    * **Mago del Ghiaccio**: Rallenta temporaneamente i nemici colpiti, utile per guadagnare tempo.
    * **Mago Barriera**: Alta salute ma nessun attacco. Funziona come muro difensivo per bloccare o rallentare i troll.

### Troll
Unità nemica che avanza verso la torre.
* Si muove automaticamente lungo un percorso predefinito verso la torre
* Ha statistiche: salute, velocità, danno, raggio d'attacco
* Attacca i maghi che incontra sul percorso
* Se raggiunge la torre, causa la sconfitta immediata del giocatore
* Viene rimosso quando la salute raggiunge zero
* Rilascia elisir come ricompensa quando eliminato
* Esistono quattro tipi di troll:
    * **Troll Base**: Statistiche equilibrate, rappresenta il nemico standard.
    * **Troll Guerriero**: Alta salute e alto danno ma movimento lento. Resistente e pericoloso in mischia.
    * **Troll Assassino**: Altissima velocità ma bassa salute. Può superare rapidamente le difese se non fermato.
    * **Troll Lanciatore**: Attacca i maghi a distanza con proiettili, rimanendo fuori dal raggio di alcuni difensori.

### Elixir
Risorsa economica gestita dal giocatore.
* Ha un valore corrente e un limite massimo
* Si rigenera automaticamente ogni N secondi
* Viene consumato per posizionare i maghi
* Viene guadagnato sconfiggendo i troll
* Può essere generato dai Maghi Generatori
* Determina quali maghi possono essere posizionati in un dato momento

### Tower
Obiettivo da difendere.
* Rappresenta la meta finale del percorso dei troll
* Ha una posizione fissa sulla mappa
* Se un troll la raggiunge, la partita termina immediatamente con la sconfitta
* Non subisce danni, è puramente un trigger di game over

### Wave
Ondata di troll che appare periodicamente.
* Ha un numero identificativo progressivo
* Contiene una composizione specifica di diversi tipi di troll
* La difficoltà aumenta ad ogni ondata successiva:
    * Più troll vengono generati
    * Troll più forti appaiono con maggiore frequenza
    * Le statistiche dei troll aumentano progressivamente
* Si attiva automaticamente dopo un intervallo di tempo dalla fine dell'ondata precedente
* Non ha limite massimo: il gioco continua all'infinito fino alla sconfitta

### Projectile
Proiettile lanciato dai maghi o dai troll lanciatori.
* Ha una posizione e si muove verso il bersaglio designato
* Ha un tipo che determina il danno e gli effetti visivi (fuoco, ghiaccio, base, troll)
* Colpisce il bersaglio quando raggiunge la sua posizione
* Viene rimosso dopo aver colpito o se il bersaglio è stato eliminato
* I proiettili del Mago del Ghiaccio applicano un effetto di rallentamento temporaneo

## Requisiti funzionali

### Requisiti di utente

Dal punto di vista dell'utente, il sistema deve consentire:

* **Il setup della partita**:
    * Visualizzazione del menu principale
    * Avvio di una nuova partita
    * Accesso alle informazioni di gioco

* **L'interazione di gioco**:
    * Selezionare un tipo di mago dal pannello laterale visualizzando costo, icona e nome
    * Posizionare i maghi cliccando su celle valide della griglia
    * Visualizzare in tempo reale:
        * Quantità di elisir disponibile
        * Numero dell'ondata corrente
        * Barre della vita sopra maghi e troll
        * Proiettili in movimento
    * Ricevere feedback immediato per:
        * Tentativi di posizionamento non validi (cella occupata, elisir insufficiente)
        * Inizio di nuove ondate
    * Mettere in pausa il gioco e riprendere

* **La fine della partita**:
    * Ricevere notifica chiara di game over quando un troll raggiunge la torre
    * Possibilità di tornare al menu principale

### Requisiti di sistema

Il sistema dovrà occuparsi di:

* **Gestione delle entità di gioco**:
    * Creare e mantenere tutte le entità (maghi, troll, proiettili) con identificatori unici
    * Associare a ogni entità le sue caratteristiche (posizione, salute, statistiche)
    * Permettere ricerche efficienti di entità con specifiche caratteristiche
    * Rimuovere automaticamente entità quando vengono eliminate

* **Gestione dell'elisir**:
    * Inizializzare l'elisir al valore predefinito all'inizio della partita
    * Rigenerare elisir automaticamente a intervalli regolari
    * Rispettare il limite massimo di elisir accumulabile
    * Verificare che il giocatore abbia elisir sufficiente prima di permettere il posizionamento
    * Sottrarre il costo corretto quando un mago viene posizionato
    * Aggiungere elisir quando un troll viene eliminato
    * Gestire la generazione periodica di elisir dai Maghi Generatori

* **Validazione del posizionamento**:
    * Verificare che la cella selezionata sia all'interno della griglia
    * Verificare che la cella non sia già occupata da un altro mago
    * Verificare che il giocatore abbia elisir sufficiente per il mago selezionato
    * Fornire feedback immediato in caso di tentativo non valido

* **Gestione del movimento**:
    * Aggiornare continuamente le posizioni dei troll lungo il percorso predefinito
    * Rispettare la velocità di movimento specifica di ogni tipo di troll
    * Applicare gli effetti di rallentamento quando un troll viene colpito dal ghiaccio
    * Muovere i proiettili verso i loro bersagli
    * Rilevare quando un troll raggiunge la torre

* **Gestione del combattimento**:
    * Implementare il targeting automatico: ogni mago identifica un nemico nel suo range e spara
    * Gestire i tempi di ricarica individuali per ogni mago
    * Impedire che un mago attacchi mentre è in ricarica
    * Creare proiettili quando un mago effettua un attacco
    * Gestire gli attacchi dei Troll Lanciatori verso i maghi
    * Rilevare le collisioni tra proiettili e bersagli
    * Calcolare e applicare i danni quando un'entità viene colpita
    * Applicare effetti speciali (rallentamento del ghiaccio)

* **Gestione della salute**:
    * Aggiornare la salute delle entità quando subiscono danni
    * Rilevare quando un'entità raggiunge salute zero o inferiore
    * Rimuovere le entità morte dal gioco
    * Assegnare ricompense in elisir quando un troll viene eliminato

* **Gestione delle ondate**:
    * Generare ondate di troll a intervalli temporali regolari
    * Determinare la composizione di ogni ondata (numero e tipi di troll)
    * Aumentare progressivamente la difficoltà:
        * Incrementare il numero totale di troll
        * Aumentare la frequenza di apparizione di troll forti (Guerriero, Assassino)
        * Incrementare gradualmente le statistiche base dei troll
    * Variare la composizione per evitare ripetitività
    * Continuare la generazione all'infinito senza limite massimo

* **Ciclo di gioco principale**:
    * Aggiornare costantemente lo stato del gioco
    * Processare tutti gli aggiornamenti necessari:
        * Rigenerazione elisir
        * Movimento di troll e proiettili
        * Attacchi
        * Collisioni e danni
        * Spawning di nuove ondate
    * Gestire correttamente la pausa e la ripresa senza perdita di stato
    * Rilevare la condizione di game over e terminare la partita

* **Rendering e interfaccia**:
    * Disegnare la griglia di gioco con le celle
    * Visualizzare tutti i maghi nelle loro posizioni
    * Visualizzare tutti i troll in movimento
    * Mostrare i proiettili in volo
    * Disegnare le barre della vita sopra ogni entità
    * Mostrare l'HUD con informazioni su elisir, ondata corrente, pannello di selezione maghi

## Requisiti non funzionali

### Requisiti esterni

* **Performance**:
    * Mantenere 60 FPS costanti
  

* **Affidabilità**:
    * Garantire stabilità durante sessioni di gioco di durata indefinita senza crash
    * Gestire robustamente input utente non validi senza interruzioni del gioco
    * Mantenere la consistenza dello stato anche con multiple pause e riprese
    * Gestire correttamente casi limite (tutti i maghi morti, elisir a zero, centinaia di entità)

* **Usabilità**:
    * Interfaccia comprensibile senza necessità di tutorial o manuali
    * Icone e simboli intuitivi per rappresentare i maghi e le loro abilità
    * Feedback visivo immediato per ogni azione utente 
    * Informazioni sempre visibili: elisir, ondata, salute
    * Messaggi chiari per azioni non valide



### Requisiti interni

* **Scalabilità**:
    * Possibilità di aggiungere nuovi tipi di maghi con minime modifiche al codice esistente
    * Possibilità di aggiungere nuovi tipi di troll senza impatti sui sistemi esistenti
    * Capacità di introdurre nuove meccaniche di gioco (powerup, mappe) senza ristrutturazioni massive
    * Gestione efficiente di un numero crescente di entità simultanee

* **Manutenibilità**:
    * Separazione netta tra logica di gioco e interfaccia grafica
    * Codice modulare organizzato in componenti con responsabilità chiare
    * Utilizzo di immutabilità per ridurre bug legati a stato condiviso
    * Documentazione completa delle API pubbliche
    * Nomi descrittivi e convenzioni di codice consistenti nel team

* **Testabilità**:
    * Logiche di gioco testabili in isolamento dal rendering
    * Comportamento deterministico: stessi input producono stessi output
    * Possibilità di creare scenari di gioco specifici per i test
    * Configurazioni iniettabili per semplificare i test
    * Copertura test sulle logiche critiche


## Requisiti di implementazione

* **Metodologia di sviluppo**  
  Sviluppo con metodologia **SCRUM-inspired**, basata su iterazioni brevi, integrazione continua e revisione periodica dei progressi.

* **Architettura**  
  Adozione di un’architettura con **netta separazione tra logica di gioco e interfaccia utente**, per garantire modularità e manutenibilità.

* **Tecnologie e linguaggio**  
  Utilizzo di **Scala 3.x**, sfruttando costrutti e paradigmi di **programmazione funzionale avanzata**.

* **Testing**  
  Sperimentazione dell’approccio **Test-Driven Development (TDD)**.

* **Versioning e collaborazione**  
  Gestione del codice tramite **versionamento Git** e workflow collaborativo basato su branch dedicati.
