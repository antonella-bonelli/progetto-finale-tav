package it.unibas.ids.analyzer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibas.common.model.Event;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Singleton
public class AnalysisContext {
    private IEventAnalyzer currentStrategy;
    private final Map<EAnalysisType, IEventAnalyzer> availableStrategies;

    @Inject
    public AnalysisContext(AdvancedAnalyzer advancedAnalyzer, SimpleAnalyzer simpleAnalyzer) {
        // Inizializza mappa delle strategie disponibili
        this.availableStrategies = Map.of(
                EAnalysisType.RULE_BASED, advancedAnalyzer,
                EAnalysisType.SIMPLE, simpleAnalyzer
        );

        // Strategia di default
        this.currentStrategy = advancedAnalyzer;
        log.info("📊 AnalysisContext inizializzato con la strategia di default: {}", currentStrategy.getAnalyzerName());
    }

    public void setStrategy(EAnalysisType analysisType) {
        IEventAnalyzer newStrategy = availableStrategies.get(analysisType);
        if (newStrategy != null) {
            IEventAnalyzer oldStrategy = currentStrategy;
            currentStrategy = newStrategy;
            log.info("🔄 Strategia cambiata: {} → {}", oldStrategy.getAnalyzerName(), currentStrategy.getAnalyzerName());
        } else {
            log.warn("❌ Strategia {} non disponibile", analysisType);
        }
    }

    public void analyzeEvent(Event event) {
        currentStrategy.analyzeEvent(event);
    }

}
