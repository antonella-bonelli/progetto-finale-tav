package it.unibas.collector;

import it.unibas.common.model.Event;
import it.unibas.common.model.LoginEvent;
import it.unibas.ids.analyzer.AnalysisContext;
import it.unibas.ids.collector.EventCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EventCollectorTest {
    @Mock
    private AnalysisContext analysisContext;

    private EventCollector collector;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        collector = new EventCollector(analysisContext);
    }

    @Test
    void shouldProcessEventCorrectly() {
        Event event = LoginEvent.successfulLogin("testUser").build();

        collector.onEvent(event);

        verify(analysisContext, times(1)).analyzeEvent(event);
        assertEquals(1, collector.getEventsReceived());
        assertEquals(1, collector.getAllEvents().size());
    }

    @Test
    void shouldProcessJsonEventCorrectly() {
        String json = "{\"@class\":\"it.unibas.common.model.LoginEvent\",\"timestamp\":\"2025-01-01T10:00:00\",\"type\":\"SUCCESSFUL_LOGIN\",\"group\":\"LOGIN\",\"userId\":\"testUser\",\"severity\":\"LOW\",\"loginMethod\":\"password\",\"result\":\"SUCCESS\",\"attemptCount\":1}";

        collector.onEventJson(json);

        verify(analysisContext, times(1)).analyzeEvent(any(Event.class));
        assertEquals(1, collector.getEventsReceived());
    }

    @Test
    void shouldHandleInvalidJson() {
        String invalidJson = "invalid json";

        assertDoesNotThrow(() -> collector.onEventJson(invalidJson));
        assertEquals(0, collector.getEventsReceived());
        verify(analysisContext, never()).analyzeEvent(any(Event.class));
    }

    @Test
    void shouldHandleNullEvent() {
        assertDoesNotThrow(() -> collector.onEvent(null));
        assertEquals(0, collector.getEventsReceived());
    }

    @Test
    void shouldReturnCorrectSubscriberName() {
        assertEquals("IDS-EventCollector", collector.getSubscriberName());
    }

    @Test
    void shouldCloneEventsInHistory() {
        Event originalEvent = LoginEvent.successfulLogin("testUser").build();

        collector.onEvent(originalEvent);

        Event retrievedEvent = collector.getAllEvents().get(0);
        assertNotSame(originalEvent, retrievedEvent);
        assertEquals(originalEvent.getUserId(), retrievedEvent.getUserId());
        assertEquals(originalEvent.getType(), retrievedEvent.getType());
    }
}
