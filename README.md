# Sistema IDS

Sistema di rilevamento intrusioni con simulatore integrato e interfaccia grafica.

## Architettura

Il sistema è composto da tre moduli principali:
- **simulator**: Genera eventi di sicurezza simulati
- **ids**: Sistema di rilevamento intrusioni con interfaccia grafica
- **common**: Modelli condivisi e interfacce
## Setup

1. **Configurazione iniziale**
   ```bash
   chmod +x setup-config.sh
   ./setup-config.sh
   ```

2. **Build del progetto**
   ```bash
   ./gradlew build -x test
   ```

## Avvio

**Avvio automatico (raccomandato):**
```bash
chmod +x start.sh
./start.sh
```

Lo script aprirà automaticamente:
- Un terminale con il Simulator
- Un terminale con l'IDS (interfaccia grafica)

**Avvio manuale:**
```bash
# Terminale 1: Simulator
./gradlew :simulator:run --args='daemon'

# Terminale 2: IDS
./gradlew :ids:run
```

## Utilizzo

1. Il **Simulator** genera eventi di sicurezza simulati
2. L'**IDS** li analizza e mostra:
   - Log eventi in tempo reale
   - Alert di sicurezza
   - Statistiche del sistema

3. Nell'interfaccia IDS:
   - **Start/Stop**: Controlli per la simulazione
   - **Filtri**: Per tipo evento e severità
   - **Clone Config**: Per testare configurazioni diverse

### ⚙️ Funzione Clone Configuration
La funzione Clone Configuration crea una copia degli eventi catturati al momento del clone:

Se non ci sono eventi catturati, la dialog mostrerà una log area vuota
È comunque possibile modificare la configurazione dell'analyzer
Permette di testare diverse configurazioni senza impattare il sistema principale
Gli eventi clonati sono isolati e non influenzano l'analisi in tempo reale

## Test

```bash
# Esegui tutti i test
./gradlew test

# Test specifici per modulo
./gradlew :simulator:test
./gradlew :ids:test
```
## Requisiti

- Java 17+
- macOS (per gli script di avvio)

---

*Per arrestare il sistema, chiudi le finestre dei terminali o premi Ctrl+C*