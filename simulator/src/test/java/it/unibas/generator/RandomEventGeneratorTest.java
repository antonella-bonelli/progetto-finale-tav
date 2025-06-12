package it.unibas.generator;

import it.unibas.simulator.generator.GeneratorConfig;
import it.unibas.simulator.generator.RandomEventGenerator;
import it.unibas.common.model.Event;
import it.unibas.common.model.EventSeverity;
import it.unibas.common.model.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class RandomEventGeneratorTest {
    private GeneratorConfig testConfig;
    private RandomEventGenerator generator;

    @BeforeEach
    void setUp() {
        testConfig = GeneratorConfig.builder()
                .generatorName("TestGenerator")
                .intervalMs(100)
                .suspiciousEventProbability(0.5)
                .debugMode(true)
                .maxEvents(10)
                .sourceId("test-source")
                .build();

        generator = new RandomEventGenerator(testConfig);
    }


    @Test
    void shouldCreateGeneratorWithValidConfig() {
        assertNotNull(generator);
        assertEquals("TestGenerator", generator.getGeneratorName());
        assertEquals(testConfig, generator.getConfig());
        assertFalse(generator.isActive());
    }

    @Test
    void shouldThrowExceptionWithNullConfig() {
        assertThrows(NullPointerException.class, () -> {
            new RandomEventGenerator(null);
        });
    }


    @Test
    void shouldGenerateValidEvents() {
        Event event = generator.generateEvent();

        assertNotNull(event);
        assertNotNull(event.getType());
        assertNotNull(event.getSeverity());
        assertNotNull(event.getTimestamp());
    }

    @RepeatedTest(10)
    void shouldGenerateDifferentEventTypes() {
        Event event = generator.generateEvent();
        assertNotNull(event.getType());

        assertTrue(
                event.getType() == EventType.SUCCESSFUL_LOGIN ||
                        event.getType() == EventType.FAILED_LOGIN ||
                        event.getType() == EventType.LOGOUT ||
                        event.getType() == EventType.FILE_ACCESS ||
                        event.getType() == EventType.UNAUTHORIZED_FILE_ACCESS ||
                        event.getType() == EventType.SENSITIVE_FILE_ACCESS ||
                        event.getType() == EventType.SUSPICIOUS_LOGIN ||
                        event.getType() == EventType.OFF_HOURS_LOGIN ||
                        event.getType() == EventType.SUSPICIOUS_NETWORK_ACTIVITY ||
                        event.getType() == EventType.MULTIPLE_FAILED_LOGINS ||
                        event.getType() == EventType.DATA_EXFILTRATION ||
                        event.getType() == EventType.NORMAL_NETWORK_ACTIVITY

        );
    }

    @Test
    void shouldRespectSuspiciousEventProbability() {
        List<Event> events = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            events.add(generator.generateEvent());
        }

        long suspiciousCount = events.stream()
                .filter(event ->
                        event.getSeverity() == EventSeverity.MEDIUM || event.getSeverity() == EventSeverity.HIGH ||
                                event.getSeverity() == EventSeverity.CRITICAL)
                .count();

        double actualRatio = (double) suspiciousCount / events.size();

        assertTrue(actualRatio >= 0.3 && actualRatio <= 0.7,
                "Suspicious event ratio should be around 0.5, but was: " + actualRatio);
    }

    @Test
    void shouldGenerateEventsWithRequiredFields() {
        Event event = generator.generateEvent();

        assertNotNull(event.getTimestamp());
        assertNotNull(event.getType());
        assertNotNull(event.getUserId());
        assertNotNull(event.getSeverity());
    }

    @Test
    void shouldStartAndStopGenerator() {
        assertFalse(generator.isActive());

        generator.start();
        assertTrue(generator.isActive());

        generator.stop();
        assertFalse(generator.isActive());
    }

    @Test
    void shouldNotStartGeneratorTwice() {
        generator.start();
        assertTrue(generator.isActive());

        generator.start();
        assertTrue(generator.isActive());

        generator.stop();
    }

    @Test
    void shouldHandleStopWithoutStart() {
        assertFalse(generator.isActive());

        assertDoesNotThrow(() -> generator.stop());

        assertFalse(generator.isActive());
    }

    @Test
    @Timeout(5)
    void shouldStopGracefullyWithinTimeout() throws InterruptedException {
        generator.start();
        Thread.sleep(500);

        long startTime = System.currentTimeMillis();
        generator.stop();
        long stopTime = System.currentTimeMillis();

        assertTrue(stopTime - startTime < 5000, "Stop should complete quickly");
        assertFalse(generator.isActive());
    }


    @Test
    void shouldDeliverEventsToConsumer() throws InterruptedException {
        List<Event> receivedEvents = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(5);

        generator.setEventConsumer(event -> {
            receivedEvents.add(event);
            latch.countDown();
        });

        generator.start();

        assertTrue(latch.await(3, TimeUnit.SECONDS), "Should receive events within timeout");

        generator.stop();

        assertTrue(receivedEvents.size() >= 5, "Should receive at least 5 events");

        for (Event event : receivedEvents) {
            assertNotNull(event);
            assertNotNull(event.getType());
            assertNotNull(event.getSeverity());
            assertNotNull(event.getUserId());
        }
    }

    @Test
    void shouldHandleNullConsumerGracefully() {
        generator.setEventConsumer(null);

        assertDoesNotThrow(() -> {
            generator.start();
            Thread.sleep(200);
            generator.stop();
        });
    }

    @Test
    void shouldHandleConsumerExceptions() throws InterruptedException {
        AtomicInteger eventCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(3);

        generator.setEventConsumer(event -> {
            eventCount.incrementAndGet();
            latch.countDown();
            if (eventCount.get() == 2) {
                throw new RuntimeException("Test exception");
            }
        });

        generator.start();

        assertTrue(latch.await(2, TimeUnit.SECONDS));

        generator.stop();

        assertTrue(eventCount.get() >= 3, "Should continue after consumer exception");
    }


    @Test
    void shouldRespectMaxEventsLimit() throws InterruptedException {
        GeneratorConfig limitedConfig = GeneratorConfig.builder()
                .generatorName("LimitedGenerator")
                .intervalMs(50)
                .maxEvents(3)
                .build();

        RandomEventGenerator limitedGenerator = new RandomEventGenerator(limitedConfig);
        AtomicInteger eventCount = new AtomicInteger(0);
        CountDownLatch finishLatch = new CountDownLatch(1);

        limitedGenerator.setEventConsumer(event -> {
            eventCount.incrementAndGet();

            if (eventCount.get() == 3) {
                finishLatch.countDown(); // ← Segnala: "Obiettivo raggiunto!"
            }
        });

        limitedGenerator.start();

        Thread.sleep(1000);

        limitedGenerator.stop();

        assertEquals(3, eventCount.get(), "Should generate exactly maxEvents");
        assertEquals(3, limitedGenerator.getEventsGenerated());
    }

    @Test
    void shouldUpdateConfiguration() {
        GeneratorConfig newConfig = GeneratorConfig.builder()
                .generatorName("UpdatedGenerator")
                .intervalMs(200)
                .suspiciousEventProbability(0.8)
                .build();

        generator.updateConfig(newConfig);

        assertEquals(newConfig, generator.getConfig());
        assertEquals("UpdatedGenerator", generator.getGeneratorName());
    }


    @Test
    void shouldTrackGeneratedEventsCount() throws InterruptedException {
        AtomicInteger receivedCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(5);

        generator.setEventConsumer(event -> {
            receivedCount.incrementAndGet();
            latch.countDown();
        });

        assertEquals(0, generator.getEventsGenerated());

        generator.start();
        latch.await(2, TimeUnit.SECONDS);
        generator.stop();

        assertTrue(generator.getEventsGenerated() >= 5);
        assertEquals(receivedCount.get(), generator.getEventsGenerated());
    }

    @Test
    void shouldResetEventCounter() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        generator.setEventConsumer(event -> latch.countDown());

        generator.start();
        latch.await(1, TimeUnit.SECONDS);
        generator.stop();

        assertTrue(generator.getEventsGenerated() > 0);

        generator.resetEventCounter();
        assertEquals(0, generator.getEventsGenerated());
    }


    @Test
    void shouldHandleConcurrentStartStopOperations() throws InterruptedException {
        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(threadCount);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            new Thread(() -> {
                try {
                    startLatch.countDown();
                    startLatch.await();

                    if (threadIndex % 2 == 0) {
                        generator.start();
                    } else {
                        generator.stop();
                    }

                    finishLatch.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        assertTrue(finishLatch.await(5, TimeUnit.SECONDS));

        generator.stop();
        assertFalse(generator.isActive());
    }
}

