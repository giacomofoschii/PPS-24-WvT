---
title: Processo di sviluppo
nav_order: 1
parent: Report
---

# Processo di sviluppo adottato

Il gruppo ha adottato una metodologia **Agile** per lo sviluppo del progetto.

In particolare, la scelta è ricaduta su un approccio **SCRUM-inspired** per la sua flessibilità e capacità di adattarsi alle esigenze del team e del progetto, producendo ad ogni iterazione nuove funzionalità del sistema o miglioramenti a quelle esistenti.

Per la coordinazione del team e la gestione del progetto sono stati utilizzati:

* **Google Docs**: per la gestione delle attività, la pianificazione dei task nelle iterazioni e la documentazione condivisa
* **GitHub**: per la collaborazione tra i membri, il versionamento del codice e la gestione dei branch
* **Discord**: per le comunicazioni quotidiane e le call tra i membri del team
* **IntelliJ IDEA**: come ambiente di sviluppo integrato (IDE) per la scrittura del codice

Il team è stato così suddiviso:

* **Product Owner**: si occupa di redigere il Product Backlog, definire le priorità delle funzionalità e verificare il corretto funzionamento del sistema realizzato. Il ruolo è stato assunto da **Rinchiuso Giovanni**
* **Committente**: esperto del dominio, garantisce l'usabilità e qualità del risultato. Il ruolo è stato assunto da **Pisoni Giovanni**
* **Sviluppatori**: tutti i membri del team hanno assunto il ruolo di sviluppatori:
    * Pisoni Giovanni
    * Rinchiuso Giovanni
    * Foschi Giacomo

## Modalità di divisione in itinere dei task

La suddivisione dei task è stata gestita in modo collaborativo durante le riunioni di pianificazione degli sprint, per permettere a tutti i membri del team di contribuire alla definizione delle attività da svolgere.

Nel primo incontro (**Sprint Planning**) sono stati individuati i task principali del progetto e sono state assegnate delle priorità in base alla rilevanza delle funzionalità, andando così a redigere il **Product Backlog**. In questa fase è stata inoltre stabilita la **Definition of Done** secondo cui una funzionalità può considerarsi completata quando:

* è stata implementata e testata con esito positivo
* rispetta quanto richiesto dall'utente
* il codice è stato revisionato da almeno un altro membro del team
* la documentazione è stata aggiornata

Nelle successive riunioni di pianificazione degli sprint, i task sono stati ulteriormente suddivisi in attività più piccole (**Sprint Backlog**) per permettere un'equa distribuzione del lavoro tra i membri del team e semplificare la gestione operativa delle attività. Al termine di ogni sprint vengono inoltre redatti una **Sprint Review** e una **Sprint Retrospective**, per valutare, rispettivamente, sia il progresso a livello di funzionalità, sia il processo di sviluppo, individuando possibili aree di miglioramento.

## Meeting/iterazioni pianificate

In una prima fase di analisi e modellazione, il gruppo ha partecipato a un meeting iniziale con l'obiettivo di definire l'architettura del progetto e stabilire le tecnologie da utilizzare. In quella stessa sede sono stati inoltre stabiliti la durata degli sprint e le modalità delle successive iterazioni.

Il team ha deciso di adottare **sprint settimanali** per permettere un rilascio rapido di funzionalità e ottenere un feedback frequente sullo stato di avanzamento del progetto.

La decisione di organizzare sprint brevi è stata motivata dall'esigenza di:

* sviluppare funzionalità in tempi brevi e mantenerle verificabili
* ottenere feedback rapido dal committente
* mantenere alta la reattività del team di fronte a eventuali problemi o cambiamenti nei requisiti

Oltre alle riunioni settimanali di pianificazione, il team ha previsto brevi confronti regolari per discutere dello stato di avanzamento e affrontare eventuali criticità o problemi tecnici emersi durante l'implementazione.

## Modalità di revisione in itinere dei task

Per la gestione del codice, è stato adottato un approccio basato su **branch dedicati per sprint**. All'inizio di ogni sprint veniva creato un branch specifico (ad esempio `sprint-1`, `sprint-2`, ecc.) sul quale tutti i membri del team lavoravano in parallelo alle diverse funzionalità previste per quella iterazione.

Al termine di ogni sprint, durante la Sprint Review, il codice consolidato nel branch dello sprint veniva integrato nel branch `main` tramite merge, dopo aver verificato che tutte le funzionalità fossero completate secondo la Definition of Done e che i test automatici fossero superati con successo. Questo approccio ha permesso di avere:

* una chiara separazione tra il codice in sviluppo e quello stabile in produzione
* milestone ben definite corrispondenti ai vari sprint
* una cronologia pulita e organizzata del progetto

## Scelta degli strumenti di test, build e Continuous Integration (CI)
Per il testing si è scelto di utilizzare **ScalaTest** come framework di automazione, essendo una tecnologia matura e ben integrata nell'ecosistema Scala, mentre come build tool è stato scelto **sbt**, in quanto nasce specificatamente per Scala e offre un'ottima gestione delle dipendenze. Inoltre, è stato utilizzato **scalafmt** per formattare automaticamente il codice sorgente rendendolo coerente e standardizzato all'interno del team.

L'intero progetto è stato gestito tramite **GitHub**. Per automatizzare i processi di test e controllo qualità, è stata implementata una pipeline di **continuous integration** (CI) su **GitHub Actions**. Questa pipeline si attiva automaticamente a ogni nuova push o pull request sui branch principali (escluso il contenuto della cartella docs), garantendo che il codice rispetti gli standard prefissati prima di essere integrato.

Il workflow configurato esegue le seguenti azioni principali:

* **Controllo della formattazione**: Verifica che il codice sorgente rispetti gli standard di formattazione definiti nel file _.scalafmt.conf_, utilizzando il comando sbt `scalafmtCheckAll` per mantenere una codebase coerente e leggibile.
* **Generazione report PDF**: Questa pipeline si attiva specificatamente quando vengono modificati i file markdown del report nella cartella `docs/report` (o le immagini associate). Utilizza *Pandoc* per convertire i file markdown aggiornati in un unico file PDF (`docs/report.pdf`). Se il PDF generato è diverso dalla versione precedente presente nel repository, il workflow effettua automaticamente il commit del nuovo file PDF nel branch principale, mantenendo così la documentazione PDF sempre allineata con i sorgenti markdown.
* **Build e test**: Compila il codice ed esegue la suite di test automatici (sbt test) per prevenire regressioni e assicurare la correttezza del software. Questo processo viene eseguito su diverse piattaforme (Ubuntu, Windows, macOS) utilizzando JDK 21 per garantire la compatibilità cross-platform.