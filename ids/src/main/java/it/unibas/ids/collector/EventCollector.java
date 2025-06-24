package it.unibas.ids.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibas.common.interfaces.IEventSubscriber;
import it.unibas.common.model.Event;
import it.unibas.common.util.ObjectMapperFactory;
import it.unibas.ids.analyzer.AnalysisContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Singleton
public class EventCollector implements IEventSubscriber {
    private final AnalysisContext analysisContext;
    private final AtomicLong eventsReceived = new AtomicLong(0);
    private final ObjectMapper objectMapper = ObjectMapperFactory.create();
    private final List<Event> allEvents = new CopyOnWriteArrayList<>();

    @Inject
    public EventCollector(AnalysisContext analysisContext) {
        this.analysisContext = analysisContext;
    }

    @Override
    public void onEvent(Event event) {
        try {
            eventsReceived.incrementAndGet();
            allEvents.add(event.clone());
            // Analizzo l'evento in base alla strategia selezionata
            analysisContext.analyzeEvent(event);
        } catch (Exception e) {
            log.error("Error processing event: {}", event.getType(), e);
        }
    }

    public void onEventJson(String eventJson) {
        try {
            if (eventJson == null || eventJson.trim().isEmpty()) {
                log.warn("Received empty or null event JSON");
                return;
            }
            Event event = objectMapper.readValue(eventJson, Event.class);
            if (event == null) {
                log.warn("Parsed event is null");
                return;
            }
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

    public List<Event> getAllEvents() {
        return allEvents.stream().map(Event::clone).toList();
    }
}
