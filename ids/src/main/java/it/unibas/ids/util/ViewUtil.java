package it.unibas.ids.util;

import it.unibas.common.model.Event;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

public class ViewUtil {

    @Getter
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static String formatLogMessage(Event event) {
        String formattedTimestamp = event.getTimestamp() != null
                ? event.getTimestamp().format(formatter)
                : "data sconosciuta";
        return String.format(
                "[%s] %s - %s (%s)",
                formattedTimestamp,
                event.getSeverity(),
                event.getType().getDescription(),
                event.getUserId()
        );
    }

}
