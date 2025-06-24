# Sistema IDS

Sistema di rilevamento intrusioni con simulatore integrato e interfaccia grafica.

## Setup

1. **Configurazione iniziale**
   ```bash
   chmod +x setup-config.sh
   ./setup-config.sh
   ```

2. **Build del progetto**
   ```bash
   ./gradlew build
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

## Requisiti

- Java 17+
- macOS (per gli script di avvio)

---

*Per arrestare il sistema, chiudi le finestre dei terminali o premi Ctrl+C*