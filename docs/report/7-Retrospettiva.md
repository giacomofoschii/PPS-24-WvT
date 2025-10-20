---
title: Retrospettiva
nav_order: 7
parent: Report
---

# Retrospettiva

## Analisi del processo di sviluppo e dello stato attuale

Il processo di sviluppo adottato ha garantito una buona organizzazione e coordinazione tra i membri del team. Complessivamente, siamo soddisfatti del processo adottato in quanto le scadenze settimanali sono state per lo più soddisfatte.

La criticità maggiore riscontrata è stata la suddivisione dei task negli sprint, in modo da garantire un carico di lavoro equo e permettere lo sviluppo indipendente tra i membri, soprattutto nelle fasi iniziali del progetto. L'architettura ECS (Entity Component System) scelta ha richiesto un periodo di adattamento iniziale per comprendere appieno le interazioni tra entità, componenti e sistemi. Tuttavia, una volta acquisita familiarità con questo pattern, lo sviluppo è proceduto in modo più fluido ed efficiente.

Questa metodologia ci ha permesso di gestire le tempistiche in modo accurato e i frequenti confronti hanno permesso di evitare incongruenze o ambiguità. L'adozione di un approccio funzionale con Scala ha facilitato la gestione dello stato immutabile del gioco, riducendo significativamente i bug legati alla concorrenza e agli effetti collaterali.

## Migliorie e lavori futuri

Le funzionalità principali previste sono state tutte realizzate: cinque tipi di wizard e quattro tipi di troll con comportamenti differenziati, oltre a un'interfaccia utente completa con menu, shop e indicatori di gioco. Alcune migliorie future potrebbero riguardare l'interfaccia grafica, che sebbene funzionale potrebbe essere ulteriormente arricchita, e le funzionalità opzionali rimaste in sospeso: l'inserimento di ostacoli nella mappa, l'aggiunta di colpi speciali o potenziamenti per i wizard, e l'implementazione di nuove mappe con layout diversi.

Data la struttura modulare del progetto, potrebbe essere molto semplice inserire in futuro ulteriori difficoltà di gioco e strategie, in modo da rendere l'esperienza più stimolante. L'aggiunta di nuovi tipi di entità, boss fight a fine wave, o un sistema di upgrade persistente tra le partite potrebbero aumentare significativamente la rigiocabilità del gioco.

## Conclusioni

In conclusione, il progetto Wizards vs Trolls ha rappresentato un'ottima occasione per sperimentare concretamente tecniche e processi di sviluppo studiati durante il corso. Inoltre, ha permesso di affrontare la progettazione del software con un approccio differente, a partire dalle prime fasi fino alla conclusione, ponendo l'attenzione più sulla metodologia e sulla qualità del codice che sulla realizzazione di grandi funzionalità. L'utilizzo di Scala e del paradigma funzionale ci ha spinto a ragionare in termini di immutabilità e composizione, portando a un codice più robusto e manutenibile.

Durante lo sviluppo, abbiamo integrato la scrittura dei test in parallelo all'implementazione del codice. Questo approccio, sebbene non seguisse il ciclo TDD in modo rigoroso, si è rivelato prezioso: ci ha spinto a chiarire i requisiti in anticipo e ha garantito una maggiore correttezza del software. In particolare, la creazione di una DSL custom per i test di scenario ha migliorato notevolmente la leggibilità e l'efficacia delle verifiche sulle meccaniche di gioco.

Tuttavia, questo metodo ha presentato delle sfide. Poiché i test venivano scritti insieme al codice, a volte risultavano strettamente legati ai dettagli implementativi, richiedendo ristrutturazioni quando il codice veniva sottoposto a refactoring.

Con il senno di poi, un'applicazione più formale del TDD avrebbe probabilmente ridotto la necessità di riscrivere i test, guidando un design più stabile fin dall'inizio. Nonostante le difficoltà, l'esperienza è stata un'importante lezione sul valore di una metodologia di test rigorosa e ci ha fornito una maggiore consapevolezza per i progetti futuri.