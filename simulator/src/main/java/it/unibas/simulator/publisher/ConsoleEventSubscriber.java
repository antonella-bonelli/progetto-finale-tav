package it.unibas.simulator.publisher;


import it.unibas.common.interfaces.IEventSubscriber;
import it.unibas.common.model.Event;
import it.unibas.common.model.EventSeverity;

import java.time.format.DateTimeFormatter;

public class ConsoleEventSubscriber implements IEventSubscriber {

    private final boolean showAllEvents;
    private final EventSeverity minimumSeverity;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public ConsoleEventSubscriber(boolean showAllEvents, EventSeverity minimumSeverity) {
        this.showAllEvents = showAllEvents;
        this.minimumSeverity = minimumSeverity;
    }

    public static ConsoleEventSubscriber showAll() {
        return new ConsoleEventSubscriber(true, EventSeverity.LOW);
    }

    @Override
    public void onEvent(Event event) {
        if (shouldDisplay(event)) {
            displayEvent(event);
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

        System.out.printf("[%s] %s %s %s%n",
                timestamp, severity, event.getType(), eventInfo);
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

        info.append("user=").append(event.getUserId()).append(" ");

        String specificInfo = getEventSpecificInfo(event);
        info.append(specificInfo);

        return info.toString().trim();
    }

    private String getEventSpecificInfo(Event event) {
        return "src=" + event.getUserId();
    }

    @Override
    public String getSubscriberName() {
        return "ConsoleSubscriber";
    }
}