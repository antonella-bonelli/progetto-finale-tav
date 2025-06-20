package it.unibas.ids.collector;

import com.google.inject.Inject;
import it.unibas.common.util.ObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper objectMapper = ObjectMapperFactory.create();
    @Inject
    public EventCollector(EventAnalyzer eventAnalyzer) {
        this.eventAnalyzer = eventAnalyzer;
    }

    @Override
    public void onEvent(Event event) {
        try {
            log.debug("Received event: {}", event.getType());

            eventsReceived.incrementAndGet();

            eventAnalyzer.analyzeEvent(event);

        } catch (Exception e) {
            log.error("Error processing event: {}", event.getType(), e);
        }
    }
    public void onEventJson(String eventJson) {
        try {
            Event event = objectMapper.readValue(eventJson, Event.class);
            log.info("Received event: {}", event);
            this.onEvent(event);
        } catch (Exception e) {
            log.error("Error: parsing event JSON: {}", eventJson, e);
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
