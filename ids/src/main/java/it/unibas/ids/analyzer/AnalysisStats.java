package it.unibas.ids.analyzer;

import it.unibas.common.model.EventType;
import it.unibas.ids.model.ThreatLevel;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class AnalysisStats {
    // Contatori generali
    private long eventsAnalyzed;
    private long alertsGenerated;

    @Builder.Default
    private LocalDateTime startTime = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime lastEventTime = LocalDateTime.now();

    // Statistiche per tipo di evento
    @Builder.Default
    private Map<EventType, Long> eventTypeCount = new HashMap<>();

    // Statistiche per livello di minaccia
    @Builder.Default
    private Map<ThreatLevel, Long> threatLevelCount = new HashMap<>();

    // Performance metrics
    @Builder.Default
    private double averageAnalysisTimeMs = 0.0;

    @Builder.Default
    private long totalAnalysisTimeMs = 0;

    public double getAlertPercentage() {
        if (eventsAnalyzed == 0) return 0.0;
        return (double) alertsGenerated / eventsAnalyzed * 100.0;
    }

    public double getEventsPerSecond() {
        long duration = java.time.Duration.between(startTime, lastEventTime).getSeconds();
        if (duration == 0) return 0.0;
        return (double) eventsAnalyzed / duration;
    }

    public double getAlertsPerMinute() {
        long durationMinutes = java.time.Duration.between(startTime, lastEventTime).toMinutes();
        if (durationMinutes == 0) return 0.0;
        return (double) alertsGenerated / durationMinutes;
    }

    public void updateAnalysisTime(long analysisTimeMs) {
        totalAnalysisTimeMs += analysisTimeMs;
        averageAnalysisTimeMs = (double) totalAnalysisTimeMs / eventsAnalyzed;
    }

    public void incrementEventType(EventType eventType) {
        eventTypeCount.merge(eventType, 1L, Long::sum);
    }

    public void incrementThreatLevel(ThreatLevel threatLevel) {
        threatLevelCount.merge(threatLevel, 1L, Long::sum);
    }

    public void reset() {
        eventsAnalyzed = 0;
        alertsGenerated = 0;
        startTime = LocalDateTime.now();
        lastEventTime = LocalDateTime.now();
        eventTypeCount.clear();
        threatLevelCount.clear();
        averageAnalysisTimeMs = 0.0;
        totalAnalysisTimeMs = 0;
    }

    @Override
    public String toString() {
        return String.format(
                "AnalysisStats{events=%d, alerts=%d, alertRate=%.2f%%, throughput=%.2f events/sec}",
                eventsAnalyzed, alertsGenerated, getAlertPercentage(), getEventsPerSecond()
        );
    }
}
