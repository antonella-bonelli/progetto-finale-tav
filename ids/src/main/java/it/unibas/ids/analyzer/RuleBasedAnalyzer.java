package it.unibas.ids.analyzer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibas.common.model.Event;
import it.unibas.common.model.EventSeverity;
import it.unibas.common.model.EventType;
import it.unibas.ids.alert.AlertManager;
import it.unibas.ids.model.Alert;
import it.unibas.ids.model.ThreatLevel;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Singleton
public class RuleBasedAnalyzer extends AAnalyzer {

    @Inject
    public RuleBasedAnalyzer(AlertManager alertManager) {
        super(alertManager);
        this.initRules();
    }

    private void initRules() {
        Map<EventType, Integer> defaultThresholds = new HashMap<>();
        defaultThresholds.put(EventType.FAILED_LOGIN, 5);
        defaultThresholds.put(EventType.FILE_ACCESS, 50);
        defaultThresholds.put(EventType.SUSPICIOUS_LOGIN, 1);

        super.rules = AnalysisRules.builder()
                .eventThresholds(defaultThresholds)
                .build();
    }

    private boolean shouldGenerateAlert(Event event) {
        EventType eventType = event.getType();
        if (rules.shouldAlwaysAlert(eventType)) return true;

        if (event.getSeverity() == EventSeverity.CRITICAL) return true;

        Long count = stats.getEventTypeCount().get(eventType);
        return count > rules.getThreshold(eventType);
    }

    private Alert createAlert(Event event) {
        ThreatLevel threatLevel = super.mapSeverityToThreatLevel(event.getSeverity());

        return Alert.builder()
                .threatLevel(threatLevel)
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
            stats.incrementThreatLevel(alert.getThreatLevel());
        }

        // Update performance stats
        long analysisTime = System.currentTimeMillis() - startTime;
        stats.updateAnalysisTime(analysisTime);
    }

    @Override
    public EAnalysisType getAnalysisType() {
        return EAnalysisType.RULE_BASED;
    }

    @Override
    public IEventAnalyzer clone() {
        RuleBasedAnalyzer cloned = new RuleBasedAnalyzer(alertManager);
        cloned.rules = this.rules.clone();
        return cloned;
    }

}
