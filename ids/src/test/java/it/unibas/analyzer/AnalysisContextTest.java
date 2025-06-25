package it.unibas.analyzer;

import it.unibas.common.model.Event;
import it.unibas.common.model.LoginEvent;
import it.unibas.ids.analyzer.AdvancedAnalyzer;
import it.unibas.ids.analyzer.AnalysisContext;
import it.unibas.ids.analyzer.EAnalysisType;
import it.unibas.ids.analyzer.SimpleAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class AnalysisContextTest {
    @Mock
    private AdvancedAnalyzer advancedAnalyzer;

    @Mock
    private SimpleAnalyzer simpleAnalyzer;

    private AnalysisContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(advancedAnalyzer.getAnalyzerName()).thenReturn("Advanced");
        when(simpleAnalyzer.getAnalyzerName()).thenReturn("Simple");

        context = new AnalysisContext(advancedAnalyzer, simpleAnalyzer);
    }

    @Test
    void shouldInitializeWithAdvancedAnalyzerAsDefault() {
        Event event = LoginEvent.successfulLogin("user").build();

        context.analyzeEvent(event);

        verify(advancedAnalyzer, times(1)).analyzeEvent(event);
        verify(simpleAnalyzer, never()).analyzeEvent(event);
    }

    @Test
    void shouldSwitchToSimpleAnalyzer() {
        SimpleAnalyzer simpleAnalyzer = mock(SimpleAnalyzer.class);
        context.setStrategy(simpleAnalyzer);
        Event event = LoginEvent.successfulLogin("user").build();

        context.analyzeEvent(event);

        verify(simpleAnalyzer, times(1)).analyzeEvent(event);
        verify(advancedAnalyzer, never()).analyzeEvent(event);
    }

    @Test
    void shouldIgnoreInvalidStrategy() {
        context.setStrategy(null);
        Event event = LoginEvent.successfulLogin("user").build();

        context.analyzeEvent(event);

        // Should still use default (advanced)
        verify(advancedAnalyzer, times(1)).analyzeEvent(event);
    }
}
