package it.unibas.common.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventSeverity {
    LOW(1, "Bassa"),
    MEDIUM(2, "Media"),
    HIGH(3, "Alta"),
    CRITICAL(4, "Critica");

    private final int level;
    private final String description;
}
