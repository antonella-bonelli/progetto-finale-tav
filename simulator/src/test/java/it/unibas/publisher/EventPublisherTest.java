package it.unibas.publisher;

import it.unibas.common.interfaces.EventSubscriber;
import it.unibas.simulator.publisher.EventPublisher;
import it.unibas.common.model.Event;
import it.unibas.common.model.LoginEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class EventPublisherTest {
    private EventPublisher publisher;
    private TestEventSubscriber testSubscriber;

    @BeforeEach
    void setUp() {
        publisher = EventPublisher.builder()
                .maxQueueSize(100)
                .publishIntervalMs(10) // Fast for testing
                .build();

        testSubscriber = new TestEventSubscriber();
    }

    @AfterEach
    void tearDown() {
        if (publisher.isRunning()) {
            publisher.stop();
        }
    }


    @Test
    void shouldCreatePublisherWithDefaultValues() {
        EventPublisher defaultPublisher = EventPublisher.builder().build();

        assertNotNull(defaultPublisher);
        assertFalse(defaultPublisher.isRunning());
        assertEquals(0, defaultPublisher.getSubscriberCount());
        assertEquals(0, defaultPublisher.getQueueSize());
        assertEquals(0, defaultPublisher.getPublishedEventCount());
        assertEquals(0, defaultPublisher.getDroppedEventCount());
    }

    @Test
    void shouldCreatePublisherWithCustomValues() {
        EventPublisher customPublisher = EventPublisher.builder()
                .maxQueueSize(500)
                .publishIntervalMs(50)
                .build();

        assertNotNull(customPublisher);
        assertFalse(customPublisher.isRunning());
        assertEquals(0, customPublisher.getSubscriberCount());
    }


    @Test
    void shouldStartAndStopPublisher() {
        assertFalse(publisher.isRunning());

        publisher.start();
        assertTrue(publisher.isRunning());

        publisher.stop();
        assertFalse(publisher.isRunning());
    }

    @Test
    void shouldHandleMultipleStartCalls() {
        publisher.start();
        assertTrue(publisher.isRunning());

        // Should not cause issues
        publisher.start();
        assertTrue(publisher.isRunning());

        publisher.stop();
        assertFalse(publisher.isRunning());
    }

    @Test
    void shouldHandleStopWithoutStart() {
        assertFalse(publisher.isRunning());

        assertDoesNotThrow(() -> publisher.stop());

        assertFalse(publisher.isRunning());
    }

    @Test
    @Timeout(5)
    void shouldStopGracefullyWithinTimeout() throws InterruptedException {
        publisher.start();
        Thread.sleep(100);

        long startTime = System.currentTimeMillis();
        publisher.stop();
        long stopTime = System.currentTimeMillis();

        assertTrue(stopTime - startTime < 5000,
                "Stop should complete within 5 seconds but took " + (stopTime - startTime) + "ms");
        assertFalse(publisher.isRunning());
    }


    @Test
    void shouldAddAndRemoveSubscribers() {
        assertEquals(0, publisher.getSubscriberCount());

        publisher.subscribe(testSubscriber);
        assertEquals(1, publisher.getSubscriberCount());

        publisher.unsubscribe(testSubscriber);
        assertEquals(0, publisher.getSubscriberCount());
    }

    @Test
    void shouldHandleNullSubscriber() {
        assertEquals(0, publisher.getSubscriberCount());

        publisher.subscribe(null);
        assertEquals(0, publisher.getSubscriberCount());
    }

    @Test
    void shouldNotAddDuplicateSubscribers() {
        publisher.subscribe(testSubscriber);
        publisher.subscribe(testSubscriber);

        assertEquals(1, publisher.getSubscriberCount());
    }

    @Test
    void shouldHandleRemovingNonExistentSubscriber() {
        TestEventSubscriber otherSubscriber = new TestEventSubscriber();

        publisher.subscribe(testSubscriber);
        assertEquals(1, publisher.getSubscriberCount());

        publisher.unsubscribe(otherSubscriber);
        assertEquals(1, publisher.getSubscriberCount());
    }

    @Test
    void shouldManageMultipleSubscribersCorrectly() {
        TestEventSubscriber subscriber1 = new TestEventSubscriber();
        TestEventSubscriber subscriber2 = new TestEventSubscriber();
        TestEventSubscriber subscriber3 = new TestEventSubscriber();

        // Add subscribers
        publisher.subscribe(subscriber1);
        publisher.subscribe(subscriber2);
        publisher.subscribe(subscriber3);
        assertEquals(3, publisher.getSubscriberCount());

        // Remove one
        publisher.unsubscribe(subscriber2);
        assertEquals(2, publisher.getSubscriberCount());

        // Remove all
        publisher.unsubscribe(subscriber1);
        publisher.unsubscribe(subscriber3);
        assertEquals(0, publisher.getSubscriberCount());
    }


    @Test
    void shouldPublishEventsToSubscribers() throws InterruptedException {
        CountDownLatch eventLatch = new CountDownLatch(1);
        AtomicReference<Event> receivedEvent = new AtomicReference<>();

        EventSubscriber waitingSubscriber = event -> {
            receivedEvent.set(event);
            eventLatch.countDown();
        };

        publisher.subscribe(waitingSubscriber);
        publisher.start();

        Event testEvent = createTestEvent();
        assertTrue(publisher.publishEvent(testEvent));

        // Wait for event to be processed with timeout
        assertTrue(eventLatch.await(5, TimeUnit.SECONDS),
                "Event should be processed within 5 seconds");

        assertNotNull(receivedEvent.get());
        assertEquals(testEvent.getType(), receivedEvent.get().getType());
        assertEquals(testEvent.getUserId(), receivedEvent.get().getUserId());
    }

    @Test
    void shouldNotPublishWhenNotRunning() {
        Event testEvent = createTestEvent();

        assertFalse(publisher.publishEvent(testEvent));
        assertEquals(0, publisher.getQueueSize());
    }

    @Test
    void shouldHandleQueueOverflow() throws InterruptedException {
        // Create publisher with very small queue
        EventPublisher smallPublisher = EventPublisher.builder()
                .maxQueueSize(2)
                .publishIntervalMs(1000) // Slow processing
                .build();

        smallPublisher.start();

        Event event1 = createTestEvent();
        Event event2 = createTestEvent();
        Event event3 = createTestEvent();

        assertTrue(smallPublisher.publishEvent(event1), "First event should be accepted");
        assertTrue(smallPublisher.publishEvent(event2), "Second event should be accepted");
        assertFalse(smallPublisher.publishEvent(event3), "Third event should be dropped");

        // Wait a bit for potential processing
        Thread.sleep(100);

        assertTrue(smallPublisher.getDroppedEventCount() > 0,
                "Should have dropped at least one event");
        assertEquals(1, smallPublisher.getDroppedEventCount());

        smallPublisher.stop();
    }

    @Test
    void shouldPublishToMultipleSubscribers() throws InterruptedException {
        CountDownLatch allSubscribersLatch = new CountDownLatch(3);

        TestEventSubscriber subscriber1 = new TestEventSubscriber(allSubscribersLatch);
        TestEventSubscriber subscriber2 = new TestEventSubscriber(allSubscribersLatch);
        TestEventSubscriber subscriber3 = new TestEventSubscriber(allSubscribersLatch);

        publisher.subscribe(subscriber1);
        publisher.subscribe(subscriber2);
        publisher.subscribe(subscriber3);
        publisher.start();

        Event testEvent = createTestEvent();
        assertTrue(publisher.publishEvent(testEvent));

        // Wait for all subscribers to receive the event
        assertTrue(allSubscribersLatch.await(5, TimeUnit.SECONDS),
                "All subscribers should receive the event within 5 seconds");

        assertEquals(1, subscriber1.getReceivedEvents().size());
        assertEquals(1, subscriber2.getReceivedEvents().size());
        assertEquals(1, subscriber3.getReceivedEvents().size());
    }

    @Test
    void shouldHandleSubscriberExceptions() throws InterruptedException {
        CountDownLatch goodSubscriberLatch = new CountDownLatch(1);
        AtomicInteger exceptionCount = new AtomicInteger(0);

        EventSubscriber faultySubscriber = event -> {
            exceptionCount.incrementAndGet();
            throw new RuntimeException("Test exception");
        };

        EventSubscriber goodSubscriber = event -> {
            goodSubscriberLatch.countDown();
        };

        publisher.subscribe(faultySubscriber);
        publisher.subscribe(goodSubscriber);
        publisher.start();

        Event testEvent = createTestEvent();
        assertTrue(publisher.publishEvent(testEvent));

        // Wait for processing
        assertTrue(goodSubscriberLatch.await(5, TimeUnit.SECONDS),
                "Good subscriber should receive event despite faulty subscriber");

        // Both subscribers should have been called
        assertEquals(1, exceptionCount.get());
    }

    @Test
    void shouldHandleNullEventsGracefully() {
        publisher.start();

        assertFalse(publisher.publishEvent(null), "Null event should not be published");
        assertEquals(0, publisher.getQueueSize());
        assertEquals(0, publisher.getPublishedEventCount());
    }


    @Test
    void shouldTrackPublishedEventsAccurately() throws InterruptedException {
        CountDownLatch eventsLatch = new CountDownLatch(5);
        publisher.subscribe(event -> eventsLatch.countDown());
        publisher.start();

        assertEquals(0, publisher.getPublishedEventCount());

        for (int i = 0; i < 5; i++) {
            assertTrue(publisher.publishEvent(createTestEvent()));
        }

        assertTrue(eventsLatch.await(5, TimeUnit.SECONDS),
                "All events should be processed within 5 seconds");

        assertEquals(5, publisher.getPublishedEventCount());
    }

    @Test
    void shouldTrackDroppedEvents() {
        EventPublisher smallPublisher = EventPublisher.builder()
                .maxQueueSize(1)
                .publishIntervalMs(10000) // Molto lento
                .build();

        // NON avviare ancora il publisher
        Event event1 = createTestEvent();
        Event event2 = createTestEvent();
        Event event3 = createTestEvent();
        Event event4 = createTestEvent();

        // Avvia DOPO aver preparato gli eventi
        smallPublisher.start();

        // Pubblica rapidamente per riempire la coda
        smallPublisher.publishEvent(event1);
        smallPublisher.publishEvent(event2);
        smallPublisher.publishEvent(event3);
        smallPublisher.publishEvent(event4);

        assertTrue(smallPublisher.getDroppedEventCount() > 1);
        smallPublisher.stop();
    }

    @Test
    void shouldProvideAccurateStatistics() throws InterruptedException {
        CountDownLatch processedLatch = new CountDownLatch(3);
        publisher.subscribe(event -> processedLatch.countDown());
        publisher.start();

        for (int i = 0; i < 3; i++) {
            assertTrue(publisher.publishEvent(createTestEvent()));
        }

        EventPublisher.PublisherStats stats = publisher.getStats();

        assertTrue(stats.getQueueSize() >= 0, "Queue size should be non-negative");
        assertEquals(1, stats.getSubscriberCount());
        assertTrue(stats.isRunning());

        assertTrue(processedLatch.await(5, TimeUnit.SECONDS),
                "All events should be processed within 5 seconds");

        EventPublisher.PublisherStats finalStats = publisher.getStats();
        assertEquals(3, finalStats.getPublishedEvents());
        assertEquals(0, finalStats.getDroppedEvents());
        assertEquals(0, finalStats.getQueueSize(), "Queue should be empty after processing");
    }

    @Test
    void shouldResetStatisticsCorrectly() throws InterruptedException {
        CountDownLatch eventsLatch = new CountDownLatch(3);
        publisher.subscribe(event -> eventsLatch.countDown());
        publisher.start();

        // Publish some events
        for (int i = 0; i < 3; i++) {
            publisher.publishEvent(createTestEvent());
        }

        eventsLatch.await(5, TimeUnit.SECONDS);

        assertTrue(publisher.getPublishedEventCount() > 0);

        // Stop and restart should maintain statistics
        publisher.stop();
        assertFalse(publisher.getStats().isRunning());

        // Published events should still be tracked
        assertTrue(publisher.getPublishedEventCount() > 0);
    }


    @Test
    @Timeout(15)
    void shouldHandleHighVolumeOfEvents() throws InterruptedException {
        int eventCount = 1000;
        CountDownLatch eventsLatch = new CountDownLatch(eventCount);
        AtomicInteger processedCount = new AtomicInteger(0);

        EventSubscriber countingSubscriber = event -> {
            processedCount.incrementAndGet();
            eventsLatch.countDown();
        };

        EventPublisher highVolumePublisher = EventPublisher.builder()
                .maxQueueSize(2000)
                .publishIntervalMs(1)
                .build();

        highVolumePublisher.subscribe(countingSubscriber);
        highVolumePublisher.start();

        // Publish events rapidly
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < eventCount; i++) {
            assertTrue(highVolumePublisher.publishEvent(createTestEvent()),
                    "Event " + i + " should be published successfully");
        }

        // Wait for processing
        assertTrue(eventsLatch.await(10, TimeUnit.SECONDS),
                "Should process all events within 10 seconds");

        long endTime = System.currentTimeMillis();

        highVolumePublisher.stop();

        assertEquals(eventCount, processedCount.get());
        assertEquals(eventCount, highVolumePublisher.getPublishedEventCount());
        assertEquals(0, highVolumePublisher.getDroppedEventCount());

        double throughput = eventCount / ((endTime - startTime) / 1000.0);
        System.out.println("Throughput: " + throughput + " events/second");
        assertTrue(throughput > 100, "Should achieve reasonable throughput");
    }

    @Test
    @Timeout(15)
    void shouldMaintainPerformanceUnderConcurrentAccess() throws InterruptedException {
        int threadCount = 2;
        int eventsPerThread = 100;
        int totalEvents = threadCount * eventsPerThread;

        CountDownLatch allEventsLatch = new CountDownLatch(totalEvents);
        CountDownLatch startLatch = new CountDownLatch(threadCount);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);
        AtomicInteger publishedCount = new AtomicInteger(0);

        publisher.subscribe(event -> allEventsLatch.countDown());
        publisher.start();

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    startLatch.countDown();
                    startLatch.await();

                    for (int j = 0; j < eventsPerThread; j++) {
                        if (publisher.publishEvent(createTestEventWithId("thread-" + threadId + "-event-" + j))) {
                            publishedCount.incrementAndGet();
                        }
                    }

                    finishLatch.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "PublisherThread-" + i).start();
        }

        assertTrue(finishLatch.await(12, TimeUnit.SECONDS),
                "All publishing threads should complete within 12 seconds");

        assertTrue(allEventsLatch.await(12, TimeUnit.SECONDS),
                "All events should be processed within 12 seconds");

        assertEquals(totalEvents, publishedCount.get());
        assertEquals(totalEvents, publisher.getPublishedEventCount());
        assertEquals(0, publisher.getDroppedEventCount());
    }

    @Test
    void shouldContinueAfterSubscriberExceptions() throws InterruptedException {
        int eventCount = 3;
        CountDownLatch goodSubscriberLatch = new CountDownLatch(eventCount);
        AtomicInteger exceptionCount = new AtomicInteger(0);

        EventSubscriber faultySubscriber = event -> {
            exceptionCount.incrementAndGet();
            throw new RuntimeException("Subscriber error #" + exceptionCount.get());
        };

        EventSubscriber goodSubscriber = event -> goodSubscriberLatch.countDown();

        publisher.subscribe(faultySubscriber);
        publisher.subscribe(goodSubscriber);
        publisher.start();

        // Send multiple events
        for (int i = 0; i < eventCount; i++) {
            assertTrue(publisher.publishEvent(createTestEvent()));
        }

        assertTrue(goodSubscriberLatch.await(5, TimeUnit.SECONDS),
                "Good subscriber should receive all events despite faulty subscriber");

        // Both subscribers should have been called for all events
        assertEquals(eventCount, exceptionCount.get());
    }

    @Test
    void shouldHandleRapidStartStopCycles() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            EventPublisher tempPublisher = EventPublisher.builder()
                    .maxQueueSize(100)
                    .publishIntervalMs(10)
                    .build();

            assertFalse(tempPublisher.isRunning());

            tempPublisher.start();
            assertTrue(tempPublisher.isRunning(), "Publisher should be running in cycle " + i);

            Thread.sleep(10);

            tempPublisher.stop();
            assertFalse(tempPublisher.isRunning());
        }
    }

    private Event createTestEvent() {
        return LoginEvent.successfulLogin("testUser")
                .build();
    }

    private Event createTestEventWithId(String userId) {
        return LoginEvent.successfulLogin(userId)
                .build();
    }

    private static class TestEventSubscriber implements EventSubscriber {
        private final List<Event> receivedEvents = Collections.synchronizedList(new ArrayList<>());
        private final CountDownLatch latch;

        public TestEventSubscriber() {
            this.latch = null;
        }

        public TestEventSubscriber(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onEvent(Event event) {
            receivedEvents.add(event);
            if (latch != null) {
                latch.countDown();
            }
        }

        public List<Event> getReceivedEvents() {
            return new ArrayList<>(receivedEvents);
        }

        @Override
        public String getSubscriberName() {
            return "TestSubscriber";
        }
    }

    private static class CountingEventSubscriber implements EventSubscriber {
        private final AtomicInteger count = new AtomicInteger(0);

        @Override
        public void onEvent(Event event) {
            count.incrementAndGet();
        }

        @Override
        public String getSubscriberName() {
            return "CountingSubscriber";
        }
    }
}
