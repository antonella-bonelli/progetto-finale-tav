package it.unibas;

import it.unibas.generator.GeneratorConfig;
import it.unibas.generator.RandomEventGenerator;

public class Main {
        public static void main(String[] args) {
            try {
                // Carica configurazione da file properties
                GeneratorConfig config = GeneratorConfig.loadFromProperties("simulator.properties");

                // Oppure usa configurazione di default
                // GeneratorConfig config = GeneratorConfig.defaultConfig();

                // Valida la configurazione
                config.validate();

                // Stampa riepilogo
                config.printSummary();

                // Crea e avvia il generatore
                RandomEventGenerator generator = new RandomEventGenerator(config);

                // Imposta il consumer per ricevere gli eventi
                generator.setEventConsumer(event -> {
                    System.out.println("Generated event: " + event.getType() +
                            " at " + event.getTimestamp());
                });

                // Avvia la generazione
                generator.start();

                // Ferma dopo 30 secondi
                Thread.sleep(30000);
                generator.stop();

            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
}