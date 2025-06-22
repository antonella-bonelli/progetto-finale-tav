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

import java.util.*;

@Slf4j
@Singleton
public class SimpleAnalyzer extends AAnalyzer {

    private List<EventSeverity> levels = new ArrayList<>(List.of(EventSeverity.LOW, EventSeverity.MEDIUM, EventSeverity.HIGH, EventSeverity.CRITICAL));

    @Inject
    public SimpleAnalyzer(AlertManager alertManager) {
        super(alertManager);
        this.initRules();
    }

    public void initRules() {
        Set<EventType> alertEvents = Set.of(
                EventType.UNAUTHORIZED_FILE_ACCESS,
                EventType.SENSITIVE_FILE_ACCESS,
                EventType.SUSPICIOUS_NETWORK_ACTIVITY,
                EventType.SUSPICIOUS_LOGIN,
                EventType.OFF_HOURS_LOGIN
        );

        super.rules = AnalysisRules.builder()
                .alwaysAlertEvents(alertEvents)
                .build();
    }

    private boolean shouldGenerateAlert(Event event) {
        return levels.contains(event.getSeverity()) && rules.shouldAlwaysAlert(event.getType());
    }

    private Alert createSimpleAlert(Event event) {
        ThreatLevel threatLevel = super.mapSeverityToThreatLevel(event.getSeverity());

        return Alert.builder()
                .threatLevel(threatLevel)
                .alertType(event.getType().toString())
                .description(event.getType().getDescription())
                .userId(event.getUserId())
                .build();
    }

    @Override
    public void analyzeEvent(Event event) {
        eventsAnalyzed.incrementAndGet();
        stats.setEventsAnalyzed(eventsAnalyzed.get());
        stats.incrementEventType(event.getType());

        if (shouldGenerateAlert(event)) {
            Alert alert = createSimpleAlert(event);
            alertManager.addAlert(alert);
            alertsGenerated.incrementAndGet();
            stats.setAlertsGenerated(alertsGenerated.get());
            stats.incrementThreatLevel(alert.getThreatLevel());
        }
    }

    @Override
    public EAnalysisType getAnalysisType() {
        return EAnalysisType.SIMPLE;
    }

    @Override
    public IEventAnalyzer clone() {
        SimpleAnalyzer cloned = new SimpleAnalyzer(super.alertManager);
        cloned.rules = this.rules.clone();
        cloned.levels = new ArrayList<>(levels);
        return cloned;
    }

}
