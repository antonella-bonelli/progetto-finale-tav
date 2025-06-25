package it.unibas.analyzer;

import it.unibas.common.model.EventGroup;
import it.unibas.common.model.EventSeverity;
import it.unibas.common.model.EventType;
import it.unibas.ids.analyzer.AnalysisRules;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AnalysisRulesTest {
    @Test
    void shouldCreateWithDefaults() {
        AnalysisRules rules = AnalysisRules.builder().build();

        assertNotNull(rules.getEventThresholds());
        assertNotNull(rules.getLevels());
        assertNotNull(rules.getGroups());
    }

    @Test
    void shouldGetThresholdCorrectly() {
        Map<EventType, Integer> thresholds = new HashMap<>();
        thresholds.put(EventType.FAILED_LOGIN, 5);

        AnalysisRules rules = AnalysisRules.builder()
                .eventThresholds(thresholds)
                .build();

        assertEquals(5, rules.getThreshold(EventType.FAILED_LOGIN));
        assertEquals(Integer.MAX_VALUE, rules.getThreshold(EventType.SUCCESSFUL_LOGIN));
    }

    @Test
    void shouldCloneCorrectly() {
        Map<EventType, Integer> thresholds = new HashMap<>();
        thresholds.put(EventType.FAILED_LOGIN, 3);

        AnalysisRules original = AnalysisRules.builder()
                .eventThresholds(thresholds)
                .levels(Set.of(EventSeverity.HIGH))
                .groups(Set.of(EventGroup.LOGIN))
                .build();

        AnalysisRules cloned = original.clone();

        assertNotSame(original, cloned);
        assertNotSame(original.getEventThresholds(), cloned.getEventThresholds());
        assertEquals(original.getThreshold(EventType.FAILED_LOGIN),
                cloned.getThreshold(EventType.FAILED_LOGIN));
        assertEquals(original.getLevels(), cloned.getLevels());
        assertEquals(original.getGroups(), cloned.getGroups());
    }

    @Test
    void shouldHandleModificationOfClonedRules() {
        AnalysisRules original = AnalysisRules.builder()
                .eventThresholds(new HashMap<>())
                .build();

        AnalysisRules cloned = original.clone();
        cloned.getEventThresholds().put(EventType.FAILED_LOGIN, 10);

        assertFalse(original.getEventThresholds().containsKey(EventType.FAILED_LOGIN));
        assertTrue(cloned.getEventThresholds().containsKey(EventType.FAILED_LOGIN));
    }
}
