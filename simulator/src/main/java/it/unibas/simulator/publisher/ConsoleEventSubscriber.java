package it.unibas.simulator.publisher;


import it.unibas.common.interfaces.EventSubscriber;
import it.unibas.common.model.Event;
import it.unibas.common.model.EventSeverity;

import java.time.format.DateTimeFormatter;

public class ConsoleEventSubscriber implements EventSubscriber {

    private final boolean showAllEvents;
    private final EventSeverity minimumSeverity;
    private long eventsDisplayed = 0;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public ConsoleEventSubscriber(boolean showAllEvents, EventSeverity minimumSeverity) {
        this.showAllEvents = showAllEvents;
        this.minimumSeverity = minimumSeverity;
    }

    public static ConsoleEventSubscriber showAll() {
        return new ConsoleEventSubscriber(true, EventSeverity.LOW);
    }

    public static ConsoleEventSubscriber criticalOnly() {
        return new ConsoleEventSubscriber(false, EventSeverity.CRITICAL);
    }

    public static ConsoleEventSubscriber highAndCritical() {
        return new ConsoleEventSubscriber(false, EventSeverity.HIGH);
    }

    @Override
    public void onEvent(Event event) {
        if (shouldDisplay(event)) {
            displayEvent(event);
            eventsDisplayed++;
        }
    }

    private boolean shouldDisplay(Event event) {
        if (showAllEvents) {
            return true;
        }

        return event.getSeverity().ordinal() >= minimumSeverity.ordinal();
    }

    private void displayEvent(Event event) {
        String timestamp = event.getTimestamp().format(TIME_FORMATTER);
        String severity = formatSeverity(event.getSeverity());
        String eventInfo = formatEventInfo(event);

        System.out.println(String.format("[%s] %s %s %s",
                timestamp, severity, event.getType(), eventInfo));
    }

    private String formatSeverity(EventSeverity severity) {
        return switch (severity) {
            case CRITICAL -> "🔴 CRIT";
            case HIGH -> "🟠 HIGH";
            case MEDIUM -> "🟡 MED ";
            case LOW -> "🟢 LOW ";
        };
    }

    private String formatEventInfo(Event event) {
        StringBuilder info = new StringBuilder();

        if (event.getUserId() != null) {
            info.append("user=").append(event.getUserId()).append(" ");
        }

        // Add specific info based on event type
        String specificInfo = getEventSpecificInfo(event);
        if (specificInfo != null) {
            info.append(specificInfo);
        }

        return info.toString().trim();
    }

    private String getEventSpecificInfo(Event event) {
        // This could be expanded to show specific fields for different event types
        // For now, just show the source ID
        return "src=" + event.getUserId();
    }

    @Override
    public String getSubscriberName() {
        return "ConsoleSubscriber";
    }

    @Override
    public boolean isInterestedIn(Event event) {
        return shouldDisplay(event);
    }

    public long getEventsDisplayed() {
        return eventsDisplayed;
    }

    public void printStats() {
        System.out.println("📊 Console Subscriber Stats: " + eventsDisplayed + " events displayed");
    }
}