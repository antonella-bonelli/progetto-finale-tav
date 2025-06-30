package it.unibas.ids.analyzer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibas.common.model.Event;
import it.unibas.common.model.EventSeverity;
import it.unibas.common.model.EventType;
import it.unibas.ids.alert.AlertManager;
import it.unibas.ids.model.Alert;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Singleton
public class AdvancedAnalyzer extends AAnalyzer {

    @Inject
    public AdvancedAnalyzer(AlertManager alertManager) {
        super(alertManager);
        this.initRules();
    }

    private void initRules() {
        Map<EventType, Integer> defaultThresholds = new HashMap<>();
        defaultThresholds.put(EventType.FAILED_LOGIN, 5);
        defaultThresholds.put(EventType.SENSITIVE_FILE_ACCESS, 10);
        defaultThresholds.put(EventType.SUSPICIOUS_LOGIN, 1);

        Set<EventSeverity> levels = Set.of(EventSeverity.CRITICAL);

        super.rules = AnalysisRules.builder()
                .eventThresholds(defaultThresholds)
                .levels(levels)
                .build();
    }

    private boolean shouldGenerateAlert(Event event) {
        EventType eventType = event.getType();
        if (rules.getLevels().contains(event.getSeverity())) {
            return true;
        }
        Long count = stats.getEventTypeCount().get(eventType);
        return count > rules.getThreshold(eventType);
    }

    private Alert createAlert(Event event) {

        return Alert.builder()
                .eventSeverity(event.getSeverity())
                .alertType(event.getType().toString())
                .description(generateDescription(event))
                .userId(event.getUserId())
                .confidenceScore(calculateConfidence(event))
                .build();
    }

    private String generateDescription(Event event) {
        return String.format("Suspicious activity detected: %s from %s",
                event.getType(), event.getUserId()
        );
    }

    private double calculateConfidence(Event event) {
        return switch (event.getSeverity()) {
            case LOW -> 0.3;
            case MEDIUM -> 0.6;
            case HIGH -> 0.8;
            case CRITICAL -> 0.95;
        };
    }

    @Override
    public void analyzeEvent(Event event) {
        long startTime = System.currentTimeMillis();

        eventsAnalyzed.incrementAndGet();
        stats.setEventsAnalyzed(eventsAnalyzed.get());
        stats.setLastEventTime(LocalDateTime.now());
        stats.incrementEventType(event.getType());

        // Analisi basata su regole
        if (shouldGenerateAlert(event)) {
            Alert alert = createAlert(event);
            alertManager.addAlert(alert);
            alertsGenerated.incrementAndGet();
            stats.setAlertsGenerated(alertsGenerated.get());
            stats.incrementEventSeverity(alert.getEventSeverity());
        }

        // Update performance stats
        long analysisTime = System.currentTimeMillis() - startTime;
        stats.updateAnalysisTime(analysisTime);
    }

    @Override
    public EAnalysisType getAnalysisType() {
        return EAnalysisType.ADVANCED;
    }

    @Override
    public IEventAnalyzer clone() {
        IEventAnalyzer cloned = super.clone();
        cloned.updateRules(this.rules.clone());
        return cloned;
    }

}
