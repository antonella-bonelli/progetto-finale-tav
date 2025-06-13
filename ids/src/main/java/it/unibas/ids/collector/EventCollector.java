package it.unibas.ids.collector;

import com.google.inject.Inject;
import it.unibas.ids.analyzer.EventAnalyzer;
import it.unibas.common.model.Event;
import it.unibas.common.interfaces.EventSubscriber;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Singleton
public class EventCollector implements EventSubscriber {
    private final EventAnalyzer eventAnalyzer;
    private final AtomicLong eventsReceived = new AtomicLong(0);

    @Inject
    public EventCollector(EventAnalyzer eventAnalyzer) {
        this.eventAnalyzer = eventAnalyzer;
    }

    @Override
    public void onEvent(Event event) {
        try {
            log.debug("Received event: {} from {}", event.getType());

            eventsReceived.incrementAndGet();

            eventAnalyzer.analyzeEvent(event);

        } catch (Exception e) {
            log.error("Error processing event: {}", event.getType(), e);
        }
    }

    @Override
    public String getSubscriberName() {
        return "IDS-EventCollector";
    }

    public long getEventsReceived() {
        return eventsReceived.get();
    }
}
