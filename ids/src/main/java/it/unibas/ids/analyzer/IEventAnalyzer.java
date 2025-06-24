package it.unibas.ids.analyzer;

import it.unibas.common.model.Event;

public interface IEventAnalyzer {

    void analyzeEvent(Event event);
    AnalysisRules getRules();
    void updateRules(AnalysisRules rules);
    AnalysisStats getStats();
    String getAnalyzerName();
    String getDescription();
    EAnalysisType getAnalysisType();

}
