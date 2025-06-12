package it.unibas;

import it.unibas.generator.GeneratorConfig;
import it.unibas.generator.RandomEventGenerator;
import it.unibas.model.Event;
import it.unibas.model.EventSeverity;
import it.unibas.publisher.EventPublisher;
import it.unibas.publisher.EventSubscriber;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;


public class SimulatorIntegrationTest {
    private EventPublisher publisher;
    private RandomEventGenerator generator;
    private GeneratorConfig config;
    private TestIntegrationSubscriber subscriber;

    @BeforeEach
    void setUp() {
        config = GeneratorConfig.builder()
                .generatorName("IntegrationTestGenerator")
                .intervalMs(50)
                .suspiciousEventProbability(0.4)
                .maxEvents(20)
                .sourceId("integration-test")
                .debugMode(true)
                .build();

        publisher = EventPublisher.builder()
                .maxQueueSize(100)
                .publishIntervalMs(10)
                .build();

        generator = new RandomEventGenerator(config);
        subscriber = new TestIntegrationSubscriber();
    }

    @AfterEach
    void tearDown() {
        if (generator.isActive()) {
            generator.stop();
        }
        if (publisher.isRunning()) {
            publisher.stop();
        }
    }

    @Test
    @Timeout(10)
    void shouldCompleteFullEventGenerationAndPublishingFlow() throws InterruptedException {
        publisher.subscribe(subscriber);
        publisher.start();

        generator.setEventConsumer(event -> {
            boolean published = publisher.publishEvent(event);
            assertTrue(published, "All events should be published successfully");
        });

        generator.start();

        subscriber.waitForEvents(20, 8, TimeUnit.SECONDS);

        generator.stop();
        publisher.stop();

        assertEquals(20, generator.getEventsGenerated(), "Should generate exactly maxEvents");
        assertEquals(20, subscriber.getReceivedEvents().size(), "Should receive all generated events");
        assertEquals(20, publisher.getPublishedEventCount(), "Should publish all events");
        assertEquals(0, publisher.getDroppedEventCount(), "Should not drop any events");

        List<Event> receivedEvents = subscriber.getReceivedEvents();
        for (Event event : receivedEvents) {
            assertNotNull(event.getType(), "Event should have type");
            assertNotNull(event.getSeverity(), "Event should have severity");
            assertNotNull(event.getTimestamp(), "Event should have timestamp");
            assertNotNull(event.getUserId(), "Event should have userId");
        }
    }

    @Test
    @Timeout(15)
    void shouldHandleHighVolumeEventProcessing() throws InterruptedException {
        GeneratorConfig highVolumeConfig = GeneratorConfig.builder()
                .generatorName("HighVolumeGenerator")
                .intervalMs(10)
                .maxEvents(100)
                .suspiciousEventProbability(0.3)
                .sourceId("high-volume-test")
                .build();

        EventPublisher highVolumePublisher = EventPublisher.builder()
                .maxQueueSize(200)
                .publishIntervalMs(5)
                .build();

        RandomEventGenerator highVolumeGenerator = new RandomEventGenerator(highVolumeConfig);
        TestIntegrationSubscriber highVolumeSubscriber = new TestIntegrationSubscriber();

        try {
            highVolumePublisher.subscribe(highVolumeSubscriber);
            highVolumePublisher.start();

            highVolumeGenerator.setEventConsumer(highVolumePublisher::publishEvent);
            highVolumeGenerator.start();

            highVolumeSubscriber.waitForEvents(100, 10, TimeUnit.SECONDS);

            highVolumeGenerator.stop();
            Thread.sleep(500);
            highVolumePublisher.stop();

            assertEquals(100, highVolumeGenerator.getEventsGenerated());
            assertTrue(highVolumeSubscriber.getReceivedEvents().size() >= 95,
                    "Should receive at least 95% of events in high-volume scenario");
            assertTrue(highVolumePublisher.getDroppedEventCount() < 5,
                    "Should drop very few events");

        } finally {
            highVolumeGenerator.stop();
            highVolumePublisher.stop();
        }
    }

    @Test
    @Timeout(10)
    void shouldHandleMultipleSubscribersCorrectly() throws InterruptedException {
        TestIntegrationSubscriber subscriber1 = new TestIntegrationSubscriber();
        TestIntegrationSubscriber subscriber2 = new TestIntegrationSubscriber();
        TestIntegrationSubscriber subscriber3 = new TestIntegrationSubscriber();

        publisher.subscribe(subscriber1);
        publisher.subscribe(subscriber2);
        publisher.subscribe(subscriber3);
        publisher.start();

        generator.setEventConsumer(publisher::publishEvent);
        generator.start();

        subscriber1.waitForEvents(20, 8, TimeUnit.SECONDS);
        subscriber2.waitForEvents(20, 8, TimeUnit.SECONDS);
        subscriber3.waitForEvents(20, 8, TimeUnit.SECONDS);

        generator.stop();
        publisher.stop();

        assertEquals(20, subscriber1.getReceivedEvents().size());
        assertEquals(20, subscriber2.getReceivedEvents().size());
        assertEquals(20, subscriber3.getReceivedEvents().size());
        assertEquals(3, publisher.getSubscriberCount());
    }

    @Test
    @Timeout(10)
    void shouldMaintainStatisticsAccuracyThroughoutOperation() throws InterruptedException {
        AtomicInteger publishAttempts = new AtomicInteger(0);
        AtomicInteger publishSuccesses = new AtomicInteger(0);

        publisher.subscribe(subscriber);
        publisher.start();

        generator.setEventConsumer(event -> {
            publishAttempts.incrementAndGet();
            boolean success = publisher.publishEvent(event);
            if (success) {
                publishSuccesses.incrementAndGet();
            }
        });

        generator.start();

        for (int i = 0; i < 5; i++) {
            Thread.sleep(1000);

            EventPublisher.PublisherStats stats = publisher.getStats();

            assertTrue(stats.getPublishedEvents() <= publishSuccesses.get(),
                    "Published count should not exceed successful publishes");
            assertTrue(stats.getQueueSize() >= 0, "Queue size should be non-negative");
            assertTrue(stats.getDroppedEvents() >= 0, "Dropped count should be non-negative");
            assertEquals(1, stats.getSubscriberCount(), "Should have one subscriber");
            assertTrue(stats.isRunning(), "Publisher should be running");
        }

        generator.stop();
        Thread.sleep(500);
        publisher.stop();

        EventPublisher.PublisherStats finalStats = publisher.getStats();
        assertEquals(publishSuccesses.get(), finalStats.getPublishedEvents(),
                "Final published count should match successful publishes");
        assertFalse(finalStats.isRunning(), "Publisher should be stopped");
    }

    @Test
    void shouldHandleGracefulShutdownUnderLoad() throws InterruptedException {
        GeneratorConfig rapidConfig = GeneratorConfig.builder()
                .generatorName("RapidGenerator")
                .intervalMs(5)
                .maxEvents(-1)
                .suspiciousEventProbability(0.5)
                .build();

        RandomEventGenerator rapidGenerator = new RandomEventGenerator(rapidConfig);

        publisher.subscribe(subscriber);
        publisher.start();

        rapidGenerator.setEventConsumer(publisher::publishEvent);
        rapidGenerator.start();

        Thread.sleep(2000);

        long stopStartTime = System.currentTimeMillis();
        rapidGenerator.stop();
        publisher.stop();
        long stopEndTime = System.currentTimeMillis();

        assertTrue(stopEndTime - stopStartTime < 3000, "Shutdown should complete within 3 seconds");
        assertFalse(rapidGenerator.isActive(), "Generator should be stopped");
        assertFalse(publisher.isRunning(), "Publisher should be stopped");

        assertTrue(rapidGenerator.getEventsGenerated() > 0, "Should have generated some events");
        assertTrue(subscriber.getReceivedEvents().size() > 0, "Should have received some events");
    }

    @Test
    void shouldRespectEventDistributionProbabilities() throws InterruptedException {
        GeneratorConfig probConfig = GeneratorConfig.builder()
                .generatorName("ProbabilityTestGenerator")
                .intervalMs(20)
                .maxEvents(200)
                .suspiciousEventProbability(0.3)
                .build();

        RandomEventGenerator probGenerator = new RandomEventGenerator(probConfig);

        publisher.subscribe(subscriber);
        publisher.start();

        probGenerator.setEventConsumer(publisher::publishEvent);
        probGenerator.start();

        subscriber.waitForEvents(200, 15, TimeUnit.SECONDS);

        probGenerator.stop();
        publisher.stop();

        List<Event> events = subscriber.getReceivedEvents();
        assertEquals(200, events.size(), "Should receive all events");

        long suspiciousEvents = events.stream()
                .filter(event -> event.getSeverity() == EventSeverity.MEDIUM || event.getSeverity() == EventSeverity.HIGH ||
                        event.getSeverity() == EventSeverity.CRITICAL)
                .count();

        double actualSuspiciousRatio = (double) suspiciousEvents / events.size();

        // Should be approximately 30% ± 10% (statistical tolerance)
        assertTrue(actualSuspiciousRatio >= 0.2 && actualSuspiciousRatio <= 0.4,
                String.format("Suspicious event ratio should be around 0.3, but was: %.2f", actualSuspiciousRatio));
    }

    // Helper class for integration testing
    private static class TestIntegrationSubscriber implements EventSubscriber {
        private final List<Event> receivedEvents = Collections.synchronizedList(new ArrayList<>());
        private final AtomicInteger eventCount = new AtomicInteger(0);

        @Override
        public void onEvent(Event event) {
            receivedEvents.add(event);
            eventCount.incrementAndGet();
        }

        public List<Event> getReceivedEvents() {
            return new ArrayList<>(receivedEvents);
        }

        public void waitForEvents(int expectedCount, long timeout, TimeUnit unit) throws InterruptedException {
            long endTime = System.currentTimeMillis() + unit.toMillis(timeout);

            while (eventCount.get() < expectedCount && System.currentTimeMillis() < endTime) {
                Thread.sleep(100);
            }

            if (eventCount.get() < expectedCount) {
                throw new AssertionError(String.format(
                        "Expected %d events but only received %d within timeout",
                        expectedCount, eventCount.get()));
            }
        }

        @Override
        public String getSubscriberName() {
            return "TestIntegrationSubscriber";
        }
    }
}
