package it.unibas.simulator.generator;


import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import it.unibas.common.model.Event;
@Slf4j
public abstract class AbstractEventGenerator implements EventGenerator{
    protected final Random random = new Random();
    protected GeneratorConfig config;
    protected Consumer<Event> eventConsumer;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicInteger eventsGenerated = new AtomicInteger(0);
    private Thread generatorThread;

    protected AbstractEventGenerator(GeneratorConfig config) {
        this.config = config;
        log.info("Created generator: {}", config.getGeneratorName());
    }

    @Override
    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            generatorThread = new Thread(this::runGenerator, config.getGeneratorName());
            generatorThread.setDaemon(true);
            generatorThread.start();
            log.info("Started generator: {}", config.getGeneratorName());
        } else {
            log.warn("Generator {} is already running", config.getGeneratorName());
        }
    }

    @Override
    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            if (generatorThread != null) {
                generatorThread.interrupt();
                try {
                    generatorThread.join(5000); // Wait up to 5 seconds for graceful shutdown
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Interrupted while waiting for generator {} to stop", config.getGeneratorName());
                }
            }
            log.info("Stopped generator: {} (generated {} events)",
                    config.getGeneratorName(), eventsGenerated.get());
        }
    }

    @Override
    public boolean isActive() {
        return isRunning.get() && generatorThread != null && generatorThread.isAlive();
    }

    @Override
    public void setEventConsumer(Consumer<Event> eventConsumer) {
        this.eventConsumer = eventConsumer;
    }

    @Override
    public String getGeneratorName() {
        return config.getGeneratorName();
    }

    @Override
    public GeneratorConfig getConfig() {
        return config;
    }

    @Override
    public void updateConfig(GeneratorConfig config) {
        this.config = config;
        log.info("Updated configuration for generator: {}", config.getGeneratorName());
    }

    private void runGenerator() {
        log.info("Generator thread started: {}", config.getGeneratorName());

        try {
            while (isRunning.get() && !Thread.currentThread().isInterrupted()) {

                // Check if we've reached the maximum number of events
                if (config.getMaxEvents() > 0 && eventsGenerated.get() >= config.getMaxEvents()) {
                    log.info("Generator {} reached max events limit: {}",
                            config.getGeneratorName(), config.getMaxEvents());
                    break;
                }

                generateSingleEvent();
                waitForNextCycle();
            }
        } catch (InterruptedException e) {
            log.info("Generator {} was interrupted", config.getGeneratorName());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Error in generator {}: {}", config.getGeneratorName(), e.getMessage(), e);
        } finally {
            isRunning.set(false);
            log.info("Generator thread finished: {}", config.getGeneratorName());
        }
    }

    private void generateSingleEvent() {
        try {
            Event event = generateEvent();
            if (event != null) {
                dispatchEvent(event);
                eventsGenerated.incrementAndGet();
            }
        } catch (Exception e) {
            log.error("Error generating event in {}: {}", config.getGeneratorName(), e.getMessage(), e);
        }
    }

    private void dispatchEvent(Event event) {
        if (eventConsumer != null) {
            try {
                eventConsumer.accept(event);
                log.debug("Dispatched event: {} from {}", event.getType(), config.getGeneratorName());
            } catch (Exception e) {
                log.error("Error dispatching event from {}: {}", config.getGeneratorName(), e.getMessage(), e);
            }
        } else {
            log.warn("No event consumer registered for generator: {}", config.getGeneratorName());
        }
    }

    private void waitForNextCycle() throws InterruptedException {
        long baseInterval = config.getIntervalMs();

        // Add random variation of ±20% to make events more realistic
        double variation = 0.8 + (random.nextDouble() * 0.4); // 0.8 to 1.2
        long waitTime = (long) (baseInterval * variation);

        Thread.sleep(waitTime);
    }

    protected boolean shouldGenerateSuspiciousEvent() {
        return random.nextDouble() < config.getSuspiciousEventProbability();
    }

    public int getEventsGenerated() {
        return eventsGenerated.get();
    }

    public void resetEventCounter() {
        eventsGenerated.set(0);
    }
}
