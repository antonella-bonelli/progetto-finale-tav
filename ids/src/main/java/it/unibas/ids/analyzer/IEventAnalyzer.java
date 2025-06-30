package it.unibas.ids.analyzer;

import it.unibas.common.model.Event;
import it.unibas.ids.alert.AlertManager;

public interface IEventAnalyzer {

    IEventAnalyzer clone();

    void analyzeEvent(Event event);

    AnalysisRules getRules();

    void updateRules(AnalysisRules rules);

    AnalysisStats getStats();

    AlertManager getManager();

    String getAnalyzerName();

    EAnalysisType getAnalysisType();
}
