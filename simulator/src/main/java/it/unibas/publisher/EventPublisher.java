package it.unibas.publisher;

import it.unibas.model.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class EventPublisher {
    private final BlockingQueue<Event> eventQueue;
    private final List<EventSubscriber> subscribers;
    private final ExecutorService publisherExecutor;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicLong publishedEvents = new AtomicLong(0);
    private final AtomicLong droppedEvents = new AtomicLong(0);

    private final int maxQueueSize;
    private final long publishIntervalMs;

    public EventPublisher(int maxQueueSize, long publishIntervalMs) {
        this.maxQueueSize = maxQueueSize;
        this.publishIntervalMs = publishIntervalMs;
        this.eventQueue = new ArrayBlockingQueue<>(maxQueueSize);
        this.subscribers = new CopyOnWriteArrayList<>();
        this.publisherExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "EventPublisher");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            publisherExecutor.submit(this::publishingLoop);
            System.out.println("EventPublisher started with queue size: " + maxQueueSize);
        } else {
            System.out.println("EventPublisher is already running");
        }
    }

    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            publisherExecutor.shutdown();
            try {
                if (!publisherExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    publisherExecutor.shutdownNow();
                }
                System.out.println("EventPublisher stopped. Published: " + publishedEvents.get() +
                        ", Dropped: " + droppedEvents.get());
            } catch (InterruptedException e) {
                publisherExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public boolean publishEvent(Event event) {
        if (!isRunning.get()) {
            System.err.println("Cannot publish event: EventPublisher is not running");
            return false;
        }

        boolean added = eventQueue.offer(event);
        if (!added) {
            droppedEvents.incrementAndGet();
            System.err.println("Event queue is full! Dropped event: " + event.getType());
        }
        return added;
    }

    public void subscribe(EventSubscriber subscriber) {
        if (subscriber != null && !subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
            System.out.println("Subscriber added: " + subscriber.getClass().getSimpleName());
        }
    }

    public void unsubscribe(EventSubscriber subscriber) {
        if (subscribers.remove(subscriber)) {
            System.out.println("Subscriber removed: " + subscriber.getClass().getSimpleName());
        }
    }

    public int getSubscriberCount() {
        return subscribers.size();
    }

    public int getQueueSize() {
        return eventQueue.size();
    }

    public long getPublishedEventCount() {
        return publishedEvents.get();
    }

    public long getDroppedEventCount() {
        return droppedEvents.get();
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    private void publishingLoop() {
        System.out.println("EventPublisher publishing loop started");

        while (isRunning.get() || !eventQueue.isEmpty()) {
            try {
                // Take event from queue (blocks if empty)
                Event event = eventQueue.poll(publishIntervalMs, TimeUnit.MILLISECONDS);

                if (event != null) {
                    notifySubscribers(event);
                    publishedEvents.incrementAndGet();

                    if (publishedEvents.get() % 100 == 0) {
                        System.out.println("Published " + publishedEvents.get() + " events. Queue size: " + eventQueue.size());
                    }
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("EventPublisher was interrupted");
                break;
            } catch (Exception e) {
                System.err.println("Error in publishing loop: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Process remaining events in queue
        processRemainingEvents();

        System.out.println("EventPublisher publishing loop finished");
    }

    private void notifySubscribers(Event event) {
        if (subscribers.isEmpty()) {
            System.err.println("No subscribers registered for event: " + event.getType());
            return;
        }

        for (EventSubscriber subscriber : subscribers) {
            try {
                subscriber.onEvent(event);
            } catch (Exception e) {
                System.err.println("Error notifying subscriber " + subscriber.getClass().getSimpleName() +
                        ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void processRemainingEvents() {
        Event event;
        int processed = 0;

        while ((event = eventQueue.poll()) != null) {
            notifySubscribers(event);
            publishedEvents.incrementAndGet();
            processed++;
        }

        if (processed > 0) {
            System.out.println("Processed " + processed + " remaining events during shutdown");
        }
    }

    public static EventPublisherBuilder builder() {
        return new EventPublisherBuilder();
    }

    public static class EventPublisherBuilder {
        private int maxQueueSize = 1000;
        private long publishIntervalMs = 100;

        public EventPublisherBuilder maxQueueSize(int maxQueueSize) {
            this.maxQueueSize = maxQueueSize;
            return this;
        }

        public EventPublisherBuilder publishIntervalMs(long publishIntervalMs) {
            this.publishIntervalMs = publishIntervalMs;
            return this;
        }

        public EventPublisher build() {
            return new EventPublisher(maxQueueSize, publishIntervalMs);
        }
    }

    public PublisherStats getStats() {
        return new PublisherStats(
                publishedEvents.get(),
                droppedEvents.get(),
                eventQueue.size(),
                subscribers.size(),
                isRunning.get()
        );
    }

    @Data
    @AllArgsConstructor
    public static class PublisherStats {
        private final long publishedEvents;
        private final long droppedEvents;
        private final int queueSize;
        private final int subscriberCount;
        private final boolean isRunning;

        @Override
        public String toString() {
            return String.format(
                    "PublisherStats{published=%d, dropped=%d, queueSize=%d, subscribers=%d, running=%s}",
                    publishedEvents, droppedEvents, queueSize, subscriberCount, isRunning
            );
        }
    }
}
