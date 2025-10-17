---
title: Sprint 3
layout: default
nav_order: 2
parent: Processo di Sviluppo
---

# Sprint 3 - Economy & Combat Systems

## Obiettivo

L'obiettivo di questo terzo Sprint è quello di completare i sistemi di combattimento e di economia del gioco, migliorare il sistema di movimento passando da celle a pixel, e implementare le condizioni di vittoria e sconfitta. Al termine dello sprint, il gioco dovrà avere un sistema di progressione delle ondate funzionante con difficoltà crescente, un HUD informativo completo, condizioni di fine partita chiare, e un sistema di movimento fluido basato su pixel. Il bilanciamento del gioco dovrà essere ottimizzato per garantire un'esperienza di gioco equilibrata e coinvolgente.

## Deadline

La scadenza dello sprint è il 6 ottobre.

## Backlog

| Nome | Descrizione | Sprint Task | Volontario |
|------|-------------|-------------|------------|
| **Movement System** | Transizione dal movimento a celle al movimento in pixel | Implementazione movimento pixel per troll | Giovanni Pisoni |
| | | Refactoring logica movimento esistente | Giovanni Pisoni |
| **Win and Lose Condition** | Implementazione delle condizioni di fine partita | Implementazione gestione condizioni di vittoria | Giovanni Pisoni |
| | | Implementazione gestione condizioni di sconfitta | Giovanni Pisoni |
| | | Implementazione transizioni fine partita | Giovanni Pisoni |
| **Win/Lose Panel** | Interfaccia per la fine della partita | Implementazione pannello vittoria | Giovanni Pisoni |
| | | Implementazione pannello sconfitta | Giovanni Pisoni |
| | | Implementazione statistiche fine partita | Giovanni Pisoni |
| **Balancing** | Bilanciamento dei parametri di gioco | Analisi metriche di gioco | Giovanni Rinchiuso |
| | | Tuning parametri maghi | Giovanni Rinchiuso |
| | | Tuning parametri troll | Giovanni Rinchiuso |
| | | Tuning costi shop | Giovanni Rinchiuso |
| **Wave Management** | Sistema di progressione delle ondate | Implementazione progressione ondate | Giovanni Rinchiuso |
| | | Implementazione difficoltà crescente | Giovanni Rinchiuso |
| | | Implementazione scaling nemici | Giovanni Rinchiuso |
| **Game HUD** | Interfaccia informativa durante il gioco | Implementazione contatore elisir | Giovanni Rinchiuso |
| | | Implementazione informazioni ondata | Giovanni Rinchiuso |
| | | Implementazione indicatori progresso | Giovanni Rinchiuso |
| **Collision System** | Implementazione del sistema di collisioni | Implementazione collision detection | Giacomo Foschi |
| | | Implementazione collision response | Giacomo Foschi |
| | | Ottimizzazione performance collisioni | Giacomo Foschi |
| **Projectile Movement** | Sistema di movimento dei proiettili | Implementazione movimento pixel per proiettili | Giacomo Foschi |
| | | Implementazione traiettoria proiettili | Giacomo Foschi |
| **Effetto Gelo Logic** | Implementazione dell'effetto rallentante | Implementazione meccanica rallentamento | Giacomo Foschi |
| | | Implementazione durata effetto | Giacomo Foschi |
| | | Implementazione stack effetti gelo | Giacomo Foschi |
| **Effetto Gelo Visual** | Visualizzazione grafica del congelamento | Implementazione effetto visivo congelamento | Giacomo Foschi |
| | | Implementazione indicatore durata effetto | Giacomo Foschi |
| | | Implementazione animazione transizione | Giacomo Foschi |

## Sprint Review

Lo stakeholder si dichiara soddisfatto dei risultati ottenuti in questo terzo sprint. Il sistema di movimento basato su pixel ha migliorato significativamente la fluidità del gioco, rendendo l'esperienza molto più piacevole rispetto al movimento a celle. Le condizioni di vittoria e sconfitta sono implementate correttamente e i pannelli di fine partita mostrano statistiche dettagliate che danno soddisfazione al giocatore. Il sistema di wave management con difficoltà crescente funziona, creando una curva di difficoltà ben bilanciata che mantiene il gioco sfidante ma non frustrante. Il bilanciamento generale del gioco è stato notevolmente migliorato: i costi dei maghi, i loro danni e la resistenza dei troll sono ora ben equilibrati. L'HUD fornisce tutte le informazioni necessarie in modo chiaro e non invasivo. Il sistema di collisioni è finalmente completo e funzionante, permettendo interazioni accurate tra tutte le entità del gioco. Il movimento dei proiettili in pixel aggiunge un livello di precisione e soddisfazione al combattimento. L'effetto gelo aggiunge una dimensione strategica importante al gameplay, con un feedback visivo chiaro e intuitivo. Il gioco è ora sostanzialmente completo dal punto di vista meccanico e necessita solo di rifinitura finale.

## Sprint Retrospective

Lo sprint ha avuto una durata di nove giorni e ha rappresentato una fase intensiva di completamento dei sistemi core del gioco. La complessità è stata gestita meglio rispetto allo Sprint 2 grazie alle lesson learned e alle action items implementate. Il refactoring del sistema di movimento ha richiesto un impegno significativo ma ha portato a risultati eccellenti. La collaborazione tra i membri del team ha raggiunto un ottimo livello di maturità.

### Cosa è andato bene

- Il bilanciamento è stato fatto in modo iterativo grazie all'allocazione esplicita di tempo nel backlog, risultando in un gioco più equilibrato
- L'implementazione completa del collision system ha risolto i problemi rimasti in sospeso dallo Sprint 2, permettendo finalmente interazioni complete tra entità
- Il sistema di movimento dei proiettili in pixel ha migliorato notevolmente la precisione del combattimento

### Cosa può essere migliorato

- Il passaggio dal movimento a celle a pixel ha avuto un impatto maggiore del previsto, richiedendo modifiche non pianificate
- Alcuni task di balancing sono stati sottostimati: trovare i valori ottimali ha richiesto più iterazioni del previsto

### Action items per il prossimo sprint

- Nel planning del prossimo sprint, considerare esplicitamente l'impatto dei refactoring su altri sistemi dipendenti


