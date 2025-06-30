package it.unibas.ids.analyzer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibas.common.model.Event;
import it.unibas.common.model.EventGroup;
import it.unibas.common.model.EventSeverity;
import it.unibas.ids.alert.AlertManager;
import it.unibas.ids.model.Alert;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
@Singleton
public class SimpleAnalyzer extends AAnalyzer {

    @Inject
    public SimpleAnalyzer(AlertManager alertManager) {
        super(alertManager);
        this.initRules();
    }

    public void initRules() {
        Set<EventGroup> groups = Set.of(EventGroup.values());
        Set<EventSeverity> levels = Set.of(EventSeverity.values());

        super.rules = AnalysisRules.builder()
                .groups(groups)
                .levels(levels)
                .build();
    }

    private boolean shouldGenerateAlert(Event event) {
        return rules.getLevels().contains(event.getSeverity())
                && rules.getGroups().contains(event.getGroup());
    }

    private Alert createSimpleAlert(Event event) {

        return Alert.builder()
                .eventSeverity(event.getSeverity())
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
            stats.incrementEventSeverity(alert.getEventSeverity());
        }
    }

    @Override
    public EAnalysisType getAnalysisType() {
        return EAnalysisType.SIMPLE;
    }

    @Override
    public IEventAnalyzer clone() {
        IEventAnalyzer cloned = super.clone();
        cloned.updateRules(this.rules.clone());
        return cloned;
    }

}
