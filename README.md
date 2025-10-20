# 🧙‍♂️ Wizards vs Trolls 👹

[![Scala Version](https://img.shields.io/badge/scala-3.3.6-red.svg)](https://www.scala-lang.org/)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Coverage](https://img.shields.io/badge/coverage-85%25-green.svg)](https://github.com/)

> *Un epico tower defense game dove la magia incontra la strategia!* ✨

## 📖 Descrizione

**Wizards vs Trolls** è un avvincente gioco tower defense ispirato al classico *Plants vs Zombies*, reimaginato in un mondo fantasy dove potenti maghi difendono il loro castello da orde di trolls invasori.

### 🎯 Caratteristiche Principali

- 🏰 **Difendi il tuo castello** dall'invasione dei trolls
- 🗺️ **Griglia strategica 5x9** per il posizionamento tattico
- 🧙 **5 tipi di maghi unici** con abilità speciali
- 👹 **4 tipologie di trolls** con comportamenti diversi
- 💎 **Sistema economico** basato sull'elisir
- 🌊 **Ondate progressive** con difficoltà crescente
- 🎮 **Interfaccia intuitiva** con pausa e menu interattivi

## 🚀 Quick Start

### Prerequisiti

- **Java** 11+

### 🎮 Come Giocare

1.  **Scarica il gioco**: Vai alla [sezione Releases](https://github.com/giacomofoschii/PPS-24-WvT/releases) del repository GitHub e scarica l'ultimo file `WizardVsTrolls.jar`.
2.  **Avvia il gioco**: Apri un terminale o prompt dei comandi, naviga nella cartella dove hai scaricato il file JAR ed esegui il comando:
    ```bash
    java -jar WizardVsTrolls.jar
    ```
3.  **Inizia a giocare**:
    * Seleziona "Start Game" dal menu principale.
    * **Posiziona i maghi** sulla griglia usando l'elisir disponibile dallo shop laterale.
    * **Respingi le ondate** di trolls impedendo loro di raggiungere il castello sulla sinistra.
    * **Gestisci le risorse**: I Maghi Generatori producono elisir aggiuntivo. L'elisir si rigenera anche automaticamente.
    * **Progredisci** attraverso ondate sempre più difficili. Il gioco termina se un troll raggiunge il lato sinistro della griglia.

## 🧙‍♂️ I Maghi

| 🧙‍♂️ Mago            | 💎 Costo | ❤️ Vita | ⚔️ Danno | 🎯 Gittata | ⏳ Cooldown | 📜 Abilità Speciale                     |
| :------------------- | :------- | :------ | :------- | :--------- | :--------- | :-------------------------------------- |
| **🔮 Generatore** | 100      | 150     | -        | -          | 10s        | Genera 25 elisir ogni 10 secondi      |
| **💨 Mago del Vento** | 150      | 100     | 25       | 3.0        | 3s         | Attacco base a distanza             |
| **🛡️ Mago Barriera** | 200      | 300     | -        | -          | -          | Blocca l'avanzata dei trolls          |
| **🔥 Mago del Fuoco** | 250      | 100     | 50       | 2.0        | 2.5s       | Attacco potente a corto raggio        |
| **❄️ Mago del Ghiaccio**| 200      | 150     | 25       | 2.5        | 4s         | Rallenta i nemici colpiti             |

_Fonte Dati: GameConstants.scala, EntityFactory.scala_

## 👹 I Trolls

| 👹 Troll        | ❤️ Vita | 🏃 Velocità | ⚔️ Danno | 🎯 Gittata | ⏳ Cooldown | 📜 Caratteristica                                        |
| :-------------- | :------ | :---------- | :------- | :--------- | :--------- |:---------------------------------------------------------|
| **👤 Base** | 100     | 0.10        | 20       | 1.0        | 1s         | Troll standard                                           |
| **⚔️ Guerriero** | 130     | 0.15        | 30       | 0.5        | 1.5s       | Tank resistente, attacco ravvicinato                     |
| **🗡️ Sicario** | 70      | 0.20        | 60       | 1.5        | 0.8s       | Veloce, danno alto ma poca resistenza, si muove a zigzag |
| **🏹 Lanciatore**| 40      | 0.10        | 10       | 5.0        | 3s         | Attacca dalla distanza                                   |

_Fonte Dati: GameConstants.scala, EntityFactory.scala, MovementSystem.scala_

## 💎 Sistema Economico

### Generazione Elisir

-   **⏰ Automatica**: +100 elisir ogni 10 secondi.
-   **🔮 Maghi Generatori**: +25 elisir ogni 10 secondi.
-   **💰 Capitale iniziale**: 200 elisir.
-   **上限 Massimo**: 1000 elisir.

### Gestione Risorse

-   Pianifica attentamente i tuoi investimenti.
-   Bilancia difesa e generazione di risorse.
-   Adatta la strategia in base all'ondata.

## 👥 Team di Sviluppo

| Ruolo                | Membro               | GitHub                                                           |
| :------------------- | :------------------- | :--------------------------------------------------------------- |
| 💡 **Commissioner** | [Giovanni Pisoni]    | [@GiovanniPisoni](https://github.com/GiovanniPisoni)             |
| 🎯 **Product Owner** | [Giovanni Rinchiuso] | [@giovannirinchiuso02](https://github.com/giovannirinchiuso02)   |
| ⚙️ **Developer** | [Giacomo Foschi]     | [@giacomofoschii](https://github.com/giacomofoschii)             |

## 📚 Documentazione

-   📖 [Documentazione](https://giacomofoschii.github.io/PPS-24-WvT/)

## 📄 Licenza

Questo progetto è distribuito sotto licenza MIT - vedi il file [LICENSE](LICENSE) per i dettagli.

---

<div align="center">

**[🎮 Scarica l'Ultima Release](https://github.com/giacomofoschii/PPS-24-WvT/releases)**

Made with ❤️ and ☕ by Piso, Jack and Gino.

*"La magia è l'unica difesa contro l'invasione!"* ✨

</div>