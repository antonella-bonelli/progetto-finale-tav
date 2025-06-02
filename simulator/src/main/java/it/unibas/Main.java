package it.unibas;

import it.unibas.generator.GeneratorConfig;
import it.unibas.generator.RandomEventGenerator;
import it.unibas.publisher.ConsoleEventSubscriber;
import it.unibas.publisher.EventPublisher;

public class Main {
    public static void main(String[] args) {
        try {
            // 1. Carica configurazione
            GeneratorConfig config;
            try {
                config = GeneratorConfig.loadFromProperties("simulator.properties");
                System.out.println("✅ Loaded configuration from simulator.properties");
            } catch (Exception e) {
                System.out.println("⚠️ Properties file not found, using default configuration");
                config = GeneratorConfig.defaultConfig();
            }

            // 2. Valida la configurazione
            config.validate();
            config.printSummary();

            // 3. Crea e configura EventPublisher
            EventPublisher publisher = EventPublisher.builder()
                    .maxQueueSize(1000)
                    .publishIntervalMs(100)
                    .build();

            // 4. Aggiungi subscriber per vedere gli eventi
            ConsoleEventSubscriber consoleSubscriber = ConsoleEventSubscriber.showAll();
            publisher.subscribe(consoleSubscriber);

            // 5. Avvia il publisher
            publisher.start();

            // 6. Crea e configura il generatore
            RandomEventGenerator generator = new RandomEventGenerator(config);

            // 7. Connetti generatore al publisher (invece del consumer diretto)
            generator.setEventConsumer(event -> {
                boolean published = publisher.publishEvent(event);
                if (!published) {
                    System.err.println("❌ Failed to publish event: " + event.getType());
                }
            });

            // 8. Avvia la generazione
            System.out.println("🚀 Starting event generator...");
            generator.start();

            // 9. Monitora per 30 secondi
            for (int i = 0; i < 30; i += 5) {
                Thread.sleep(5000);

                // Stampa statistiche ogni 5 secondi
                EventPublisher.PublisherStats stats = publisher.getStats();
                System.out.println("\n📊 Statistiche dopo " + (i + 5) + " secondi:");
                System.out.println("   Generator: " + generator.getEventsGenerated() + " eventi generati");
                System.out.println("   Publisher: " + stats.getPublishedEvents() + " eventi pubblicati");
                System.out.println("   Queue: " + stats.getQueueSize() + " eventi in coda");
                System.out.println("   Dropped: " + stats.getDroppedEvents() + " eventi persi");
            }

            // 10. Ferma tutto
            System.out.println("\n🛑 Stopping simulation...");
            generator.stop();
            publisher.stop();

            // 11. Statistiche finali
            EventPublisher.PublisherStats finalStats = publisher.getStats();
            System.out.println("\n📈 Statistiche Finali:");
            System.out.println("   Eventi generati: " + generator.getEventsGenerated());
            System.out.println("   Eventi pubblicati: " + finalStats.getPublishedEvents());
            System.out.println("   Eventi persi: " + finalStats.getDroppedEvents());
            System.out.println("   Efficienza: " +
                    String.format("%.1f%%", (finalStats.getPublishedEvents() * 100.0) / generator.getEventsGenerated()));

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}