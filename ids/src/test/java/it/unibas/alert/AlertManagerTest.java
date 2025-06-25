package it.unibas.alert;

import it.unibas.ids.alert.AlertManager;
import it.unibas.ids.model.Alert;
import it.unibas.ids.model.AlertStatus;
import it.unibas.ids.model.ThreatLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AlertManagerTest {
    private AlertManager alertManager;

    @BeforeEach
    void setUp() {
        alertManager = new AlertManager();
    }

    @Test
    void shouldAddAlertCorrectly() {
        Alert alert = createTestAlert(ThreatLevel.HIGH);

        alertManager.addAlert(alert);

        assertEquals(1, alertManager.getTotalAlertCount());
        assertEquals(1, alertManager.getActiveAlerts().size());
        assertTrue(alertManager.getActiveAlerts().contains(alert));
    }

    @Test
    void shouldResolveAlertCorrectly() {
        Alert alert = createTestAlert(ThreatLevel.MEDIUM);
        alertManager.addAlert(alert);

        alertManager.resolveAlert(alert.getAlertId());

        assertEquals(0, alertManager.getActiveAlerts().size());
        assertEquals(AlertStatus.RESOLVED, alert.getStatus());
        assertEquals(1, alertManager.getAlertHistory().size());
    }

    @Test
    void shouldTrackAlertHistory() {
        Alert alert1 = createTestAlert(ThreatLevel.LOW);
        Alert alert2 = createTestAlert(ThreatLevel.HIGH);

        alertManager.addAlert(alert1);
        alertManager.addAlert(alert2);

        List<Alert> history = alertManager.getAlertHistory();
        assertEquals(2, history.size());
        assertTrue(history.contains(alert1));
        assertTrue(history.contains(alert2));
    }

    @Test
    void shouldClearAllCorrectly() {
        alertManager.addAlert(createTestAlert(ThreatLevel.HIGH));
        alertManager.addAlert(createTestAlert(ThreatLevel.LOW));

        alertManager.clearAll();

        assertEquals(0, alertManager.getActiveAlerts().size());
        assertEquals(0, alertManager.getAlertHistory().size());
        assertEquals(0, alertManager.getTotalAlertCount());
    }

    @Test
    void shouldHandleResolveNonExistentAlert() {
        assertDoesNotThrow(() -> alertManager.resolveAlert("non-existent"));
        assertEquals(0, alertManager.getActiveAlerts().size());
    }

    private Alert createTestAlert(ThreatLevel threatLevel) {
        return Alert.builder()
                .threatLevel(threatLevel)
                .alertType("TEST_ALERT")
                .description("Test alert")
                .userId("testUser")
                .build();
    }
}
