package it.unibas.ids.analyzer;

import it.unibas.common.model.Event;

public interface EventAnalyzer {

    void analyzeEvent(Event event);


    void updateRules(AnalysisRules rules);

    AnalysisStats getStats();

    EventAnalyzer clone();
}
