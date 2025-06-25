package it.unibas.analyzer;

import it.unibas.common.model.EventType;
import it.unibas.ids.analyzer.AnalysisStats;
import it.unibas.ids.model.ThreatLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AnalysisStatsTest {

    private AnalysisStats stats;

    @BeforeEach
    void setUp() {
        stats = AnalysisStats.builder().build();
    }

    @Test
    void shouldCalculateAlertPercentageCorrectly() {
        stats.setEventsAnalyzed(100);
        stats.setAlertsGenerated(25);

        assertEquals(25.0, stats.getAlertPercentage(), 0.01);
    }

    @Test
    void shouldHandleZeroEventsForPercentage() {
        stats.setEventsAnalyzed(0);
        stats.setAlertsGenerated(0);

        assertEquals(0.0, stats.getAlertPercentage());
    }

    @Test
    void shouldIncrementEventTypeCorrectly() {
        stats.incrementEventType(EventType.FAILED_LOGIN);
        stats.incrementEventType(EventType.FAILED_LOGIN);
        stats.incrementEventType(EventType.SUCCESSFUL_LOGIN);

        assertEquals(2, stats.getEventTypeCount().get(EventType.FAILED_LOGIN));
        assertEquals(1, stats.getEventTypeCount().get(EventType.SUCCESSFUL_LOGIN));
    }

    @Test
    void shouldIncrementThreatLevelCorrectly() {
        stats.incrementThreatLevel(ThreatLevel.HIGH);
        stats.incrementThreatLevel(ThreatLevel.HIGH);
        stats.incrementThreatLevel(ThreatLevel.CRITICAL);

        assertEquals(2, stats.getThreatLevelCount().get(ThreatLevel.HIGH));
        assertEquals(1, stats.getThreatLevelCount().get(ThreatLevel.CRITICAL));
    }

    @Test
    void shouldUpdateAnalysisTimeCorrectly() {
        stats.setEventsAnalyzed(2);
        stats.updateAnalysisTime(100);
        stats.updateAnalysisTime(200);

        assertEquals(300, stats.getTotalAnalysisTimeMs());
        assertEquals(150.0, stats.getAverageAnalysisTimeMs(), 0.01);
    }

    @Test
    void shouldResetCorrectly() {
        stats.setEventsAnalyzed(100);
        stats.setAlertsGenerated(25);
        stats.incrementEventType(EventType.FAILED_LOGIN);

        stats.reset();

        assertEquals(0, stats.getEventsAnalyzed());
        assertEquals(0, stats.getAlertsGenerated());
        assertEquals(0.0, stats.getAverageAnalysisTimeMs());
        assertTrue(stats.getEventTypeCount().isEmpty());
        assertTrue(stats.getThreatLevelCount().isEmpty());
    }
}
