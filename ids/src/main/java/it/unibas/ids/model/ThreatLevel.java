package it.unibas.ids.model;

public enum ThreatLevel {
    INFO(1),
    LOW(2),
    MEDIUM(3),
    HIGH(4),
    CRITICAL(5);

    private final int priority;

    ThreatLevel(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isHigherThan(ThreatLevel other) {
        return this.priority > other.priority;
    }
}
