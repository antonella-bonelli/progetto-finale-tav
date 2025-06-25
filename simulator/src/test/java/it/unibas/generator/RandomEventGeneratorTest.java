package it.unibas.generator;

import it.unibas.simulator.generator.GeneratorConfig;
import it.unibas.simulator.generator.RandomEventGenerator;
import it.unibas.common.model.Event;
import it.unibas.common.model.EventSeverity;
import it.unibas.common.model.EventType;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.*;
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
                .numberOfSources(3)
                .build();

        generator = new RandomEventGenerator(testConfig);
        generator.setEventConsumer(event -> {System.out.println("[test event consumer] "+ event.getType());});
    }

    @AfterEach
    public void teardown() {
        generator.updateConfig(testConfig);
        generator.resetEventCounter();

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
                        event.getSeverity() == EventSeverity.MEDIUM ||
                                event.getSeverity() == EventSeverity.HIGH ||
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
    void shouldStartAndStopGenerator() throws InterruptedException {
        assertFalse(generator.isActive());

        generator.start();
        Thread.sleep(100);
        assertTrue(generator.isActive());

        generator.stop();
        Thread.sleep(100);
        assertFalse(generator.isActive());
    }

    @Test
    void shouldNotStartGeneratorTwice() throws InterruptedException {
        generator.start();
        Thread.sleep(100);
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
    @Timeout(10)
    void shouldStopGracefullyWithinTimeout() throws InterruptedException {
        generator.start();
        Thread.sleep(500);

        long startTime = System.currentTimeMillis();
        generator.stop();
        long stopTime = System.currentTimeMillis();

        assertTrue(stopTime - startTime < 7000, "Stop should complete within timeout");
        assertFalse(generator.isActive());
    }

    @Test
    void shouldDeliverEventsToConsumer() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(5);

        generator.setEventConsumer(event -> {
            latch.countDown();
        });

        generator.start();

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Should receive events within timeout");

        generator.stop();

        assertTrue(generator.getEventsGenerated() >= 5, "Should receive at least 5 events");

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

        assertTrue(latch.await(3, TimeUnit.SECONDS));

        generator.stop();

        assertTrue(eventCount.get() >= 3, "Should continue after consumer exception");
    }

    @Test
    void shouldRespectMaxEventsLimit() throws InterruptedException {
        GeneratorConfig limitedConfig = GeneratorConfig.builder()
                .generatorName("LimitedGenerator")
                .intervalMs(50)
                .maxEvents(10)
                .numberOfSources(3)
                .build();

        generator.updateConfig(limitedConfig);
        AtomicInteger eventCount = new AtomicInteger(0);
        CountDownLatch finishLatch = new CountDownLatch(10);

        generator.resetEventCounter();

        generator.setEventConsumer(event -> {
            eventCount.incrementAndGet();
            finishLatch.countDown();
        });

        generator.start();

        assertTrue(finishLatch.await(10, TimeUnit.SECONDS), "Should receive all events");

        Thread.sleep(500);

        System.out.println("Count: " + eventCount.get() + " - event: " + generator.getEventsGenerated());
        generator.stop();



        assertEquals(10, eventCount.get(), "Should generate exactly maxEvents");
        assertEquals(10, generator.getEventsGenerated());
    }

    @Test
    void shouldUpdateConfiguration() {
        GeneratorConfig newConfig = GeneratorConfig.builder()
                .generatorName("UpdatedGenerator")
                .intervalMs(200)
                .suspiciousEventProbability(0.8)
                .numberOfSources(2)
                .build();

        generator.updateConfig(newConfig);

        assertEquals(newConfig, generator.getConfig());
        assertEquals("UpdatedGenerator", generator.getGeneratorName());
    }

    @Test
    void shouldTrackGeneratedEventsCount() throws InterruptedException {
        AtomicInteger receivedCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(15);

        generator.setEventConsumer(event -> {
            receivedCount.incrementAndGet();
            latch.countDown();
        });

        assertEquals(0, generator.getEventsGenerated());

        generator.start();
        latch.await(5, TimeUnit.SECONDS);
        generator.stop();

        assertTrue(generator.getEventsGenerated() >= 10);
        assertEquals(receivedCount.get(), generator.getEventsGenerated());
    }

    @Test
    void shouldResetEventCounter() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        generator.setEventConsumer(event -> {
            if (latch.getCount() > 0) {
                latch.countDown();
            }
        });

        generator.start();
        latch.await(2, TimeUnit.SECONDS);
        generator.stop();

        assertTrue(generator.getEventsGenerated() > 0);

        generator.resetEventCounter();
        assertEquals(0, generator.getEventsGenerated());
    }

    @Test
    void shouldGenerateEventsFromMultipleSources() throws InterruptedException {
        GeneratorConfig multiSourceConfig = GeneratorConfig.builder()
                .generatorName("MultiSourceGenerator")
                .intervalMs(50)
                .numberOfSources(3)
                .build();

        System.out.println("*** "+multiSourceConfig.getMaxEvents());
        generator.updateConfig(multiSourceConfig);
        Set<Integer> activeThreadIds = ConcurrentHashMap.newKeySet();
        CountDownLatch latch = new CountDownLatch(10);

        generator.setEventConsumer(event -> {
            activeThreadIds.add(Thread.currentThread().hashCode());
            if (latch.getCount() > 0) {
                latch.countDown();
            }
        });

        generator.start();
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Should generate events from all sources");
        generator.stop();

        assertTrue(activeThreadIds.size() >= 2,
                "Should have events from multiple threads, but got: " + activeThreadIds.size());
    }

    @Test
    void shouldRespectEventDistributionProbabilities() throws InterruptedException {
        GeneratorConfig probConfig = GeneratorConfig.builder()
                .generatorName("ProbabilityTestGenerator")
                .intervalMs(5)  // Ancora più veloce
                .maxEvents(200)
                .numberOfSources(3)
                .suspiciousEventProbability(0.3)
                .build();

        generator.updateConfig(probConfig);
        List<Event> events = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(200);

        generator.setEventConsumer(event -> {
            events.add(event);
            latch.countDown();
        });

        generator.start();

        assertTrue(latch.await(30, TimeUnit.SECONDS), "Should generate all events within timeout");

        generator.stop();

        Thread.sleep(100);

        assertEquals(200, events.size(), "Should receive exactly maxEvents");

        long suspiciousEvents = events.stream()
                .filter(event ->
                        event.getSeverity() == EventSeverity.MEDIUM ||
                                event.getSeverity() == EventSeverity.HIGH ||
                                event.getSeverity() == EventSeverity.CRITICAL)
                .count();

        double actualSuspiciousRatio = (double) suspiciousEvents / events.size();

        assertTrue(actualSuspiciousRatio >= 0.2 && actualSuspiciousRatio <= 0.4,
                String.format("Suspicious event ratio should be around 0.3, but was: %.2f",
                        actualSuspiciousRatio));
    }
    
}