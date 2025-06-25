package it.unibas.analyzer;

import it.unibas.common.model.Event;
import it.unibas.common.model.EventGroup;
import it.unibas.common.model.EventSeverity;
import it.unibas.common.model.LoginEvent;
import it.unibas.ids.alert.AlertManager;
import it.unibas.ids.analyzer.AnalysisRules;
import it.unibas.ids.analyzer.EAnalysisType;
import it.unibas.ids.analyzer.SimpleAnalyzer;
import it.unibas.ids.model.Alert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SimpleAnalyzerTest {
    @Mock
    private AlertManager alertManager;

    private SimpleAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.analyzer = new SimpleAnalyzer(alertManager);
    }

    @Test
    void shouldInitializeWithAllLevelsAndGroups() {
        AnalysisRules rules = analyzer.getRules();

        assertEquals(EventSeverity.values().length, rules.getLevels().size());
        assertEquals(EventGroup.values().length, rules.getGroups().size());
    }

    @Test
    void shouldGenerateAlertForAnyEvent() {
        Event event = LoginEvent.successfulLogin("testUser").build();

        analyzer.analyzeEvent(event);

        verify(alertManager, times(1)).addAlert(any(Alert.class));
        assertEquals(1, analyzer.getStats().getEventsAnalyzed());
        assertEquals(1, analyzer.getStats().getAlertsGenerated());
    }

    @Test
    void shouldReturnCorrectAnalysisType() {
        assertEquals(EAnalysisType.SIMPLE, analyzer.getAnalysisType());
    }
}