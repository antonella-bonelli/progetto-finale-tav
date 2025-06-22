package it.unibas.ids.analyzer;

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

    // Tipi di eventi che generano sempre alert
    @Builder.Default
    private Set<EventType> alwaysAlertEvents = Set.of(
            EventType.UNAUTHORIZED_FILE_ACCESS,
            EventType.SENSITIVE_FILE_ACCESS,
            EventType.SUSPICIOUS_NETWORK_ACTIVITY
    );

    public boolean shouldAlwaysAlert(EventType eventType) {
        return alwaysAlertEvents.contains(eventType);
    }

    public int getThreshold(EventType eventType) {
        return eventThresholds.getOrDefault(eventType, Integer.MAX_VALUE);
    }

    @Override
    public AnalysisRules clone() {
        try {
            AnalysisRules cloned = (AnalysisRules) super.clone();

            // Deep clone delle mappe
            cloned.eventThresholds = new HashMap<>(this.eventThresholds);
            cloned.alwaysAlertEvents = Set.copyOf(this.alwaysAlertEvents);

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone AnalysisRules", e);
        }
    }
}
