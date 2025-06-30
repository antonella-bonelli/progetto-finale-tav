package it.unibas.alert;

import it.unibas.common.model.EventSeverity;
import it.unibas.ids.alert.AlertManager;
import it.unibas.ids.model.Alert;
import it.unibas.ids.model.AlertStatus;
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
        Alert alert = createTestAlert(EventSeverity.HIGH);

        alertManager.addAlert(alert);

        assertEquals(1, alertManager.getTotalAlertCount());
        assertEquals(1, alertManager.getActiveAlerts().size());
        assertTrue(alertManager.getActiveAlerts().contains(alert));
    }

    @Test
    void shouldResolveAlertCorrectly() {
        Alert alert = createTestAlert(EventSeverity.MEDIUM);
        alertManager.addAlert(alert);

        alertManager.resolveAlert(alert.getAlertId());

        assertEquals(0, alertManager.getActiveAlerts().size());
        assertEquals(AlertStatus.RESOLVED, alert.getStatus());
        assertEquals(1, alertManager.getAlertHistory().size());
    }

    @Test
    void shouldTrackAlertHistory() {
        Alert alert1 = createTestAlert(EventSeverity.LOW);
        Alert alert2 = createTestAlert(EventSeverity.HIGH);

        alertManager.addAlert(alert1);
        alertManager.addAlert(alert2);

        List<Alert> history = alertManager.getAlertHistory();
        assertEquals(2, history.size());
        assertTrue(history.contains(alert1));
        assertTrue(history.contains(alert2));
    }

    @Test
    void shouldClearAllCorrectly() {
        alertManager.addAlert(createTestAlert(EventSeverity.HIGH));
        alertManager.addAlert(createTestAlert(EventSeverity.LOW));

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

    private Alert createTestAlert(EventSeverity eventSeverity) {
        return Alert.builder()
                .eventSeverity(eventSeverity)
                .alertType("TEST_ALERT")
                .description("Test alert")
                .userId("testUser")
                .build();
    }
}
