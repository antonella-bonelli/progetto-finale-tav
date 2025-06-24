package it.unibas.ids.analyzer;

import it.unibas.common.model.EventSeverity;
import it.unibas.common.model.EventType;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@Builder
public class AnalysisRules implements Cloneable {
    // Soglie per diversi tipi di eventi
    @Builder.Default
    private Map<EventType, Integer> eventThresholds = new HashMap<>();

    // Severità di eventi che generano sempre alert
    @Builder.Default
    private Set<EventSeverity> levels = Set.of(
            EventSeverity.LOW,
            EventSeverity.MEDIUM,
            EventSeverity.HIGH,
            EventSeverity.CRITICAL
    );

    // Tipi di eventi che generano sempre alert
    @Builder.Default
    private Set<EventType> types = Set.of(
            EventType.UNAUTHORIZED_FILE_ACCESS,
            EventType.SENSITIVE_FILE_ACCESS,
            EventType.SUSPICIOUS_NETWORK_ACTIVITY
    );

    public int getThreshold(EventType eventType) {
        return eventThresholds.getOrDefault(eventType, Integer.MAX_VALUE);
    }

    @Override
    public AnalysisRules clone() {
        try {
            AnalysisRules cloned = (AnalysisRules) super.clone();

            // Deep clone delle mappe
            cloned.eventThresholds = new HashMap<>(this.eventThresholds);
            cloned.types = Set.copyOf(this.types);
            cloned.levels = Set.copyOf(this.levels);

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone AnalysisRules", e);
        }
    }
}
