package it.unibas.common.util;

import it.unibas.common.model.Event;
import it.unibas.common.model.FileAccessEvent;
import it.unibas.common.model.LoginEvent;
import it.unibas.common.model.NetworkEvent;

public class EventUtil {

    public static boolean eventTypeMatches(String filter, Event event) {
        return switch (filter) {
            case "Login" -> event instanceof LoginEvent;
            case "File" -> event instanceof FileAccessEvent;
            case "Network" -> event instanceof NetworkEvent;
            default -> true;
        };
    }

}
