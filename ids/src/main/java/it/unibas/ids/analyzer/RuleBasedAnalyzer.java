package it.unibas.ids.analyzer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibas.ids.alert.AlertManager;
import it.unibas.ids.model.Alert;
import it.unibas.common.model.Event;
import it.unibas.common.model.EventSeverity;
import it.unibas.ids.model.ThreatLevel;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Singleton
public class RuleBasedAnalyzer implements EventAnalyzer {
    private final AnalysisStats stats = AnalysisStats.builder().build();
    private final AlertManager alertManager;
    private final AtomicLong eventsAnalyzed = new AtomicLong(0);
    private final AtomicLong alertsGenerated = new AtomicLong(0);

    private AnalysisRules rules;

    @Inject
    public RuleBasedAnalyzer(AlertManager alertManager) {
        this.alertManager = alertManager;
        this.rules = AnalysisRules.defaultRules();
    }

    private boolean shouldGenerateAlert(Event event) {
        // Logica di analisi semplificata
        return switch (event.getType()) {
            case UNAUTHORIZED_FILE_ACCESS, SENSITIVE_FILE_ACCESS, SUSPICIOUS_NETWORK_ACTIVITY -> true;
            case FAILED_LOGIN -> event.getSeverity() == EventSeverity.HIGH;
            default -> false;
        };
    }

    private Alert createAlert(Event event) {
        ThreatLevel threatLevel = mapSeverityToThreatLevel(event.getSeverity());

        return Alert.builder()
                .threatLevel(threatLevel)
                .alertType(event.getType().toString())
                .description(generateDescription(event))
                .userId(event.getUserId())
                .relatedEvents(List.of(event))
                .confidenceScore(calculateConfidence(event))
                .build();
    }

    private ThreatLevel mapSeverityToThreatLevel(EventSeverity severity) {
        return switch (severity) {
            case LOW -> ThreatLevel.LOW;
            case MEDIUM -> ThreatLevel.MEDIUM;
            case HIGH -> ThreatLevel.HIGH;
            case CRITICAL -> ThreatLevel.CRITICAL;
        };
    }

    private String generateDescription(Event event) {
        return String.format("Suspicious activity detected: %s from %s",
                event.getType(), event.getUserId()
        );
    }

    private double calculateConfidence(Event event) {
        // Logica semplificata per confidence score
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
        stats.incrementEventType(event.getType().toString());

//        // Skip whitelisted IPs
//        if (rules.isWhitelisted(event.getUserId())) {
//            log.debug("Skipping whitelisted IP: {}", event.getUserId());
//            return;
//        }

        // Analisi basata su regole
        if (shouldGenerateAlert(event)) {
            Alert alert = createAlert(event);
            alertManager.addAlert(alert);
            alertsGenerated.incrementAndGet();
            stats.setAlertsGenerated(alertsGenerated.get());
            stats.incrementThreatLevel(alert.getThreatLevel().toString());
        }

        // Update performance stats
        long analysisTime = System.currentTimeMillis() - startTime;
        stats.updateAnalysisTime(analysisTime);

    }

    @Override
    public void updateRules(AnalysisRules rules) {
        this.rules = rules;
        log.info("Analysis rules updated");
    }

    @Override
    public AnalysisStats getStats() {
        return AnalysisStats.builder()
                .eventsAnalyzed(eventsAnalyzed.get())
                .alertsGenerated(alertsGenerated.get())
                .build();
    }

    @Override
    public EventAnalyzer clone() {
        RuleBasedAnalyzer cloned = new RuleBasedAnalyzer(alertManager);
        cloned.rules = this.rules.clone();
        return cloned;
    }
}
