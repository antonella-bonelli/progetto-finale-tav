# Setup Iniziale

1. **Esegui lo script di setup:**
   ```bash
   ./setup-config.sh
   ```
   
# Avvio moduli
## Avvio modulo simulator
1. modalità monitoraggio
   ```bash
   ./gradlew :simulator:run
   ```
2. modalità demone
   ```bash
   ./gradlew :simulator:run --args='daemon'
   ```

## Avvio modulo IDS
> Note!
> 

   ```bash
   ./gradlew :ids:run
   ```
