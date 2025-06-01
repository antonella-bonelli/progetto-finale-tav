package it.unibas.model;

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

    public boolean isHigherThan(EventSeverity other) {
        return this.level > other.level;
    }
    public boolean isLowerThan(EventSeverity other) {
        if (other == null) return false;
        return this.level < other.level;
    }

    public boolean isEqualTo(EventSeverity other) {
        if (other == null) return false;
        return this.level == other.level;
    }
}
