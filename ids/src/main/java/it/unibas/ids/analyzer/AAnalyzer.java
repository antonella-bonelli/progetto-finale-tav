package it.unibas.ids.analyzer;

import com.google.inject.Inject;
import it.unibas.common.model.EventSeverity;
import it.unibas.ids.alert.AlertManager;
import it.unibas.ids.model.ThreatLevel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public abstract class AAnalyzer implements IEventAnalyzer, Cloneable {

    protected final AnalysisStats stats = AnalysisStats.builder().build();
    protected final AlertManager alertManager;
    protected final AtomicLong eventsAnalyzed = new AtomicLong(0);
    protected final AtomicLong alertsGenerated = new AtomicLong(0);

    protected AnalysisRules rules;

    protected ThreatLevel mapSeverityToThreatLevel(EventSeverity severity) {
        return switch (severity) {
            case LOW -> ThreatLevel.LOW;
            case MEDIUM -> ThreatLevel.MEDIUM;
            case HIGH -> ThreatLevel.HIGH;
            case CRITICAL -> ThreatLevel.CRITICAL;
        };
    }

    @Inject
    public AAnalyzer(AlertManager alertManager) {
        this.alertManager = alertManager;
    }

    @Override
    public void updateRules(AnalysisRules rules) {
        this.rules = rules;
    }

    @Override
    public AnalysisRules getRules() {
        return this.rules;
    }

    @Override
    public AnalysisStats getStats() {
        return AnalysisStats.builder()
                .eventsAnalyzed(eventsAnalyzed.get())
                .alertsGenerated(alertsGenerated.get())
                .build();
    }

    @Override
    public String getAnalyzerName() {
        return getAnalysisType().getDisplayName();
    }

    @Override
    public String getDescription() {
        return getAnalysisType().getDescription();
    }

}
