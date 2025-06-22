package it.unibas.simulator;

import it.unibas.simulator.generator.RandomEventGenerator;
import it.unibas.simulator.publisher.ConsoleEventSubscriber;
import it.unibas.simulator.publisher.EventPublisher;
import it.unibas.simulator.generator.GeneratorConfig;
import it.unibas.simulator.publisher.TCPSocketPublisher;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
public class Main {
    private static EventPublisher publisher;
    private static RandomEventGenerator generator;
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static final CountDownLatch initLatch = new CountDownLatch(0);
    private static Thread mainThread;
    private static TCPSocketPublisher tcpPublisher;

    public static boolean isInitialized() {
        return initialized.get();
    }

    public static boolean isRunning() {
        return initialized.get() &&
                publisher != null && publisher.isRunning() &&
                generator != null && generator.isActive();
    }

    public static void shutdown() {
        log.info("Shutdow requested...");
        if (generator != null) {
            generator.stop();
        }
        if (publisher != null) {
            publisher.stop();
        }
        if (tcpPublisher != null) {
            tcpPublisher.stop();
        }
        if (mainThread != null && mainThread.isAlive()) {
            mainThread.interrupt();
        }
    }

    public static void main(String[] args) {
        mainThread = Thread.currentThread();

        try {

            GeneratorConfig config;
            try {
                config = GeneratorConfig.loadFromProperties("simulator.properties");
                log.info("Loaded configutation from simulator properties");
            } catch (Exception e) {
                log.error("Properties file not found, {}", e.getMessage());
                log.info("Using default config");
                config = GeneratorConfig.defaultConfig();
            }
            config.validate();
            config.printSummary();

            publisher = EventPublisher.builder()
                    .maxQueueSize(1000)
                    .publishIntervalMs(100)
                    .build();
            log.info("Publisher has been set");

            // TCP
            tcpPublisher = new TCPSocketPublisher(config.getPort());
            tcpPublisher.start();

            ConsoleEventSubscriber consoleEventSubscriber = ConsoleEventSubscriber.showAll();
            publisher.subscribe(consoleEventSubscriber);

            publisher.start();

            generator = new RandomEventGenerator(config);

            generator.setEventConsumer(event -> {
                boolean published = publisher.publishEvent(event);
                tcpPublisher.broadcast(event);
                if (!published) {
                    log.error("Failed to publish event: {}", event.getType());
                }
            });

            log.info("Starting event generation...");
            generator.start();

            initialized.set(true);
            initLatch.countDown();
            log.info("Simulator fully initialized and ready for connections");

            if (args.length > 0 && args[0].equals("daemon")) {
                runDaemonMode();
            } else if (args.length > 0) {
                log.error("Error: wrong args");
            } else {
                runMonitoringMode(30);
            }
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            e.printStackTrace();
            initLatch.countDown();
        } finally {
            shutdown();
        }
    }

    private static void runDaemonMode() throws InterruptedException {
        log.info("Running in daemon mode. Press cmd + c to stop");

        while (!Thread.currentThread().isInterrupted()) {
            Thread.sleep(30000);
            printStatistics();
        }
    }

    private static void runMonitoringMode(int durationSeconds) throws InterruptedException {
        log.info("Running in monitoring mode for {} seconds", durationSeconds);

        for (int i = 0; i < durationSeconds; i += 5) {
            Thread.sleep(5000);

            log.info("\n📊 Statistiche dopo " + (i + 5) + " secondi:");
            printStatistics();
        }

        log.info("\n📈 Statistiche Finali:");
        printFinalStatistics();
    }

    private static void printStatistics() {
        EventPublisher.PublisherStats stats = publisher.getStats();
        log.info("   Generator: " + generator.getEventsGenerated() + " eventi generati");
        log.info("   Publisher: " + stats.getPublishedEvents() + " eventi pubblicati");
        log.info("   Queue: " + stats.getQueueSize() + " eventi in coda");
        log.info("   Subscribers: " + stats.getSubscriberCount());
        log.info("   Dropped: " + stats.getDroppedEvents() + " eventi persi");
    }

    private static void printFinalStatistics() {
        EventPublisher.PublisherStats finalStats = publisher.getStats();
        log.info("   Eventi generati: " + generator.getEventsGenerated());
        log.info("   Eventi pubblicati: " + finalStats.getPublishedEvents());
        log.info("   Eventi persi: " + finalStats.getDroppedEvents());

        double efficiency = generator.getEventsGenerated() > 0 ?
                (finalStats.getPublishedEvents() * 100.0) / generator.getEventsGenerated() : 100.0;

        log.info("   Efficienza: " + String.format("%.1f%%", efficiency));
    }
}