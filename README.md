# 🧙‍♂️ Wizards vs Trolls 👹

[![Scala Version](https://img.shields.io/badge/scala-3.3.0-red.svg)](https://www.scala-lang.org/)
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

- **Scala** 3.3.0+
- **SBT** 1.9.0+
- **Java** 11+

### Installazione

```bash
# Clona il repository
git clone https://github.com/yourusername/Wizards_vs_Trolls_PPS.git
cd PPS-24-mnt

# Compila il progetto
sbt compile

# Esegui i test
sbt test

# Avvia il gioco
sbt run
```

### 🎮 Come Giocare

1. **Avvia il gioco** dal menu principale
2. **Posiziona i maghi** sulla griglia usando l'elisir disponibile
3. **Respingi le ondate** di trolls impedendo loro di raggiungere il castello
4. **Gestisci le risorse** generando elisir con i maghi generatori
5. **Progredisci** attraverso ondate sempre più difficili

## 🧙‍♂️ I Maghi

| 🧙‍♂️ Mago               | 💎 Costo | ❤️ Vita | ⚔️ Danno | 🎯 Abilità Speciale |
|--------------------------|---------|---------|----------|-------------------|
| **🔮 Generatore**        | 50 | Bassa | - | Genera 25 elisir ogni 2 secondi |
| **💨 Mago del Vento**    | 100 | Media | Basso | Attacchi rapidi a distanza |
| **🛡️ Mago Barriera**    | 50 | Molto Alta | - | Blocca l'avanzata dei trolls |
| **🔥 Mago del Fuoco**    | 150 | Media | Alto | Danni devastanti ad area |
| **❄️ Mago del Ghiaccio** | 175 | Media | Basso | Rallenta i nemici colpiti |

### 🔓 Progressione Sblocco Maghi
- **Ondata 1**: 🔮 Generatore, 💨 Vento
- **Ondata 2**: 🛡️ Barriera
- **Ondata 3**: 🔥 Fuoco
- **Ondata 4+**: ❄️ Ghiaccio

## 👹 I Trolls

| 👹Troll | ❤️ Vita | 🏃 Velocità | ⚔️ Danno | 🎯 Caratteristica |
|-------|---------|------------|----------|------------------|
| **👤 Base** | Normale | Normale | Basso | Troll standard bilanciato |
| **⚔️ Guerriero** | Alta | Bassa | Medio | Tank resistente |
| **🗡️ Sicario** | Molto Bassa | Molto Alta | Alto | Assassino veloce |
| **🏹 Lanciatore** | Bassa | Ferma | Medio | Attacca dalla distanza |

### 🔓 Progressione Apparizione Trolls
- **Ondata 1**: 👤 Base, ⚔️ Guerriero
- **Ondata 2**: 🏹 Lanciatore
- **Ondata 3+**: 🗡️ Sicario

## 💎 Sistema Economico

### Generazione Elisir
- **⏰ Automatica**: +100 elisir ogni 10 secondi
- **🔮 Maghi Generatori**: +25 elisir ogni 2 secondi
- **💰 Capitale iniziale**: Quantità predefinita per ondata

### Gestione Risorse
- Pianifica attentamente i tuoi investimenti
- Bilancia difesa e generazione di risorse
- Adatta la strategia in base all'ondata

## 🎮 Controlli

## TODO correctly

Example:

| Tasto/Azione | Funzione |
|--------------|----------|
| **Click sinistro** | Seleziona/Posiziona mago |
| **Click destro** | Annulla selezione |
| **P** | Pausa/Riprendi |
| **ESC** | Menu pausa |
| **1-5** | Selezione rapida maghi |

## 📦 Build & Deploy

### Crea JAR Eseguibile
```bash
sbt assembly
# Il JAR sarà in target/scala-3.3.0/
```

### Esegui il JAR
```bash
java -jar magicians-vs-trolls.jar
```

## 👥 Team di Sviluppo

| Ruolo                | Membro               | GitHub                                                           |
|----------------------|----------------------|------------------------------------------------------------------|
| 💡 **Commissioner**  | [Giovanni Pisoni]    | [@GiovanniPisoni](https://github.com/GiovanniPisoni)             |
| 🎯 **Product Owner** | [Gioavnni Rinchiuso] | [@giovannirinchiuso02](https://github.com/giovannirinchiuso02)   |
| ⚙️ **Developer**     | [Giacomo Foschi]     | [@giacomofoschii](https://github.com/giacomofoschii)             |

## 📚 Documentazione

- 📖 [Requisiti di Sistema](docs/requisiti.md)
- 🏗️ [Architettura](docs/architettura.md)
- 🎮 [Game Design Document](docs/gdd.md)
- 📊 [Relazione Tecnica](docs/relazione/)
- 🔄 [Sprint Reports](process/)


## 📄 Licenza

Questo progetto è distribuito sotto licenza MIT - vedi il file [LICENSE](LICENSE) per i dettagli.

---

<div align="center">

**[🎮 Gioca Ora](https://github.com/)**

Made with ❤️ and ☕ by Piso, Jack and Gino. 

*"La magia è l'unica difesa contro l'invasione!"* ✨

</div>