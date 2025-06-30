package it.unibas.analyzer;

import it.unibas.common.model.Event;
import it.unibas.common.model.EventSeverity;
import it.unibas.common.model.LoginEvent;
import it.unibas.ids.alert.AlertManager;
import it.unibas.ids.analyzer.AdvancedAnalyzer;
import it.unibas.ids.analyzer.AnalysisRules;
import it.unibas.ids.analyzer.IEventAnalyzer;
import it.unibas.ids.model.Alert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AdvancedAnalyzerTest {
    @Mock
    private AlertManager alertManager;

    private AdvancedAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        analyzer = new AdvancedAnalyzer(alertManager);
    }

    @Test
    void shouldInitializeWithDefaultRules() {
        AnalysisRules rules = analyzer.getRules();

        assertNotNull(rules);
        assertNotNull(rules.getEventThresholds());
        assertNotNull(rules.getLevels());
        assertTrue(rules.getLevels().contains(EventSeverity.CRITICAL));
    }

    @Test
    void shouldGenerateAlertForCriticalEvent() {
        Event criticalEvent = LoginEvent.bruteForceAttempt("testUser", 10)
                .severity(EventSeverity.CRITICAL)
                .build();

        analyzer.analyzeEvent(criticalEvent);

        verify(alertManager, times(1)).addAlert(any(Alert.class));
        assertEquals(1, analyzer.getStats().getEventsAnalyzed());
        assertEquals(1, analyzer.getStats().getAlertsGenerated());
    }

    @Test
    void shouldNotGenerateAlertForLowSeverityEvent() {
        Event lowEvent = LoginEvent.successfulLogin("testUser")
                .severity(EventSeverity.LOW)
                .build();

        analyzer.analyzeEvent(lowEvent);

        verify(alertManager, never()).addAlert(any(Alert.class));
        assertEquals(1, analyzer.getStats().getEventsAnalyzed());
        assertEquals(0, analyzer.getStats().getAlertsGenerated());
    }

    @Test
    void shouldUpdateRulesCorrectly() {
        AnalysisRules newRules = AnalysisRules.builder()
                .eventThresholds(new HashMap<>())
                .levels(Set.of(EventSeverity.HIGH))
                .build();

        analyzer.updateRules(newRules);

        assertEquals(newRules, analyzer.getRules());
        assertTrue(analyzer.getRules().getLevels().contains(EventSeverity.HIGH));
        assertFalse(analyzer.getRules().getLevels().contains(EventSeverity.LOW));
    }

    @Test
    void shouldCloneCorrectly() {
        IEventAnalyzer cloned = analyzer.clone();

        assertNotNull(cloned);
        assertNotSame(analyzer, cloned);
        assertEquals(analyzer.getAnalysisType(), cloned.getAnalysisType());
        assertEquals(analyzer.getAnalyzerName(), cloned.getAnalyzerName());
    }

    @Test
    void shouldMapSeverityToThreatLevelCorrectly() {
        Event criticalEvent = LoginEvent.bruteForceAttempt("user", 10).severity(EventSeverity.CRITICAL).build();

        analyzer.analyzeEvent(criticalEvent);

        verify(alertManager).addAlert(argThat(alert ->
                alert.getEventSeverity() == EventSeverity.CRITICAL
        ));
    }
}
