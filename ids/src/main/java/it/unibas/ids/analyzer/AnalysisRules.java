package it.unibas.ids.analyzer;

import it.unibas.common.model.EventGroup;
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
            EventSeverity.values()
    );

    // Gruppi di eventi che generano sempre alert
    @Builder.Default
    private Set<EventGroup> groups = Set.of(
            EventGroup.values()
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
            cloned.groups = Set.copyOf(this.groups);
            cloned.levels = Set.copyOf(this.levels);

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone AnalysisRules", e);
        }
    }
}
