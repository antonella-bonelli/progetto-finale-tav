package it.unibas.simulator.generator;


import it.unibas.common.model.Event;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Slf4j
public abstract class AbstractEventGenerator implements EventGenerator {
    protected final Random random = new Random();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicInteger eventsGenerated = new AtomicInteger(0);
    private final List<EventSourceThread> sourceThreads = new ArrayList<>();
    protected GeneratorConfig config;
    protected Consumer<Event> eventConsumer;
    private ExecutorService executorService;

    private Semaphore eventSemaphore;

    protected AbstractEventGenerator(GeneratorConfig config) {
        this.config = config;
        log.info("Created generator: {}", config.getGeneratorName());
    }

    public int getAvailableEventPermits() {
        return eventSemaphore != null ? eventSemaphore.availablePermits() : -1;
    }

    private void createSourceThreads() {
        sourceThreads.clear();
        for (int i = 1; i <= config.getNumberOfSources(); i++) {
            String sourceId = String.format("source-%d", i);
            EventSourceThread source = new EventSourceThread(sourceId);
            sourceThreads.add(source);
        }
        log.debug("Created {} source threads", sourceThreads.size());
    }

    private void startAllSources() {
        for (EventSourceThread sourceThread : sourceThreads) {
            executorService.submit(sourceThread);
        }
        log.debug("Started all {} source threads", sourceThreads.size());
    }

    private void stopAllSources() {
        sourceThreads.forEach(EventSourceThread::stop);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void shutdownExecutorService() {
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("ExecutorService didn't terminate gracefully, forcing shutdown");
                    executorService.shutdownNow();

                    if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                        log.error("ExecutorService dind't terminate after forced shutdown");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executorService.shutdownNow();
                log.warn("Interrupted while waiting for ExecutorService shutdown");
            }
        }
    }

    @Override
    public void start() {
        log.info("Generator is starting...");
        if (isRunning.compareAndSet(false, true)) {

            try {
                eventSemaphore = new Semaphore(config.getMaxEvents());

                executorService = Executors.newFixedThreadPool(
                        config.getNumberOfSources(),
                        r -> {
                            Thread t = new Thread(r);
                            t.setDaemon(true);
                            return t;
                        }
                );

                createSourceThreads();
                startAllSources();
                log.info("Started generator: {} with {} source threads", config.getGeneratorName(), config.getNumberOfSources());

            } catch (Exception e) {
                isRunning.set(false);
                log.error("Failed to start generator: {}", config.getGeneratorName());
                throw new RuntimeException("Failed to start generator: ", e);
            }
            log.info("Started generator: {}", config.getGeneratorName());
        } else {
            log.warn("Generator {} is already running", config.getGeneratorName());
        }
    }

    @Override
    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            log.info("Stopping generator {}", config.getGeneratorName());
            stopAllSources();
            shutdownExecutorService();

            log.info("Stopped generator: {} (total events: {})", config.getGeneratorName(), eventsGenerated.get());
        }
    }

    @Override
    public boolean isActive() {
        return isRunning.get() && executorService != null && !executorService.isShutdown();
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

    public abstract Event generateEvent();

    public int getEventsGenerated() {
        return eventsGenerated.get();
    }

    public void resetEventCounter() {
        eventsGenerated.set(0);
        if (eventSemaphore != null) {
            eventSemaphore.drainPermits();
            eventSemaphore.release(config.getMaxEvents());
            log.info("Reset semaphore permits to maxEvents");
        }
    }

    public int getActiveSourcesCount() {
        return (int) sourceThreads.stream().filter(EventSourceThread::isActive).count();
    }


    private void dispatchEvent(Event event, String sourceId) {
        if (eventConsumer != null) {
            try {
                eventConsumer.accept(event);
                log.debug("Dispatched event: {} from {}", event.getType(), sourceId);
                eventsGenerated.incrementAndGet();
            } catch (Exception e) {
                log.error("Error dispatching event from {}: {}", sourceId, e.getMessage(), e);
            }
        } else {
            log.warn("No event consumer registered for generator: {}", sourceId);
        }
    }

    private void waitForNextCycle() throws InterruptedException {
        long baseInterval = config.getIntervalMs();
        long waitTime = baseInterval;
        Thread.sleep(waitTime);
    }

    protected boolean shouldGenerateSuspiciousEvent() {
        return random.nextDouble() < config.getSuspiciousEventProbability();
    }

    private boolean hasReachedGlobalEventLimit() {
        return config.getMaxEvents() > 0 && eventsGenerated.get() >= config.getMaxEvents();
    }

    private void generateAndDispatchEvent(String sourceId) {
        try {
            if (getAvailableEventPermits() <= 0) {
                log.debug("Source {} cannot generate event - limit reached", sourceId);
                return;
            }
            log.debug("Source {} acquired permit (remaining {})", sourceId, getAvailableEventPermits());
            Event event = generateEvent();
            if (event != null) {
                if (!eventSemaphore.tryAcquire()) {
                    return;
                }
                dispatchEvent(event, sourceId);
            } else {
                eventSemaphore.release();
                log.debug("{}", event);
                log.debug("Source {} released permit (event was null)", sourceId);
            }
        } catch (Exception e) {
            if (eventSemaphore != null) {
                eventSemaphore.release();
                log.debug("Source {} released permit due to error", sourceId);
            }
            log.error("Error generating event in source {}: {}", sourceId, e.getMessage(), e);
        }
    }

    private class EventSourceThread implements Runnable {
        private final String sourceId;
        private final AtomicBoolean isSourceRunning = new AtomicBoolean(false);
        private final AtomicInteger sourceEventsGenerated = new AtomicInteger(0);

        public EventSourceThread(String sourceId) {
            this.sourceId = sourceId;
        }

        @Override
        public void run() {
            isSourceRunning.set(true);
            log.info("Event source thread started: {}", sourceId);

            try {
                while (isSourceRunning.get() && isRunning.get() && !Thread.currentThread().isInterrupted()) {
                    if (hasReachedGlobalEventLimit()) {
                        log.info("Source {} stopping due to global event limit reached: {}", sourceId, config.getMaxEvents());
                        break;
                    }
                    generateAndDispatchEvent(sourceId);
                    sourceEventsGenerated.incrementAndGet();
                    waitForNextCycle();
                }

            } catch (InterruptedException e) {
                log.info("Source {} was interrupted", sourceId);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Error in source {}: {}", sourceId, e.getMessage());
            } finally {
                isSourceRunning.set(false);
                log.info("Event source thread finished: {} (generated {} events)", sourceId, sourceEventsGenerated.get());
            }
        }

        public void stop() {
            isSourceRunning.set(false);
        }

        public boolean isActive() {
            return isSourceRunning.get();
        }

        public void addSourceEventsGenerated() {
            sourceEventsGenerated.incrementAndGet();
        }
    }
}
