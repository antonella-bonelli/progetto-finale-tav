package it.unibas.ids.alert;

import com.google.inject.Singleton;
import it.unibas.ids.model.Alert;
import it.unibas.ids.model.AlertStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Singleton
public class AlertManager {
    private final ConcurrentHashMap<String, Alert> activeAlerts = new ConcurrentHashMap<>();
    private final List<Alert> alertHistory = new CopyOnWriteArrayList<>();
    private final AtomicLong totalAlerts = new AtomicLong(0);

    public synchronized void addAlert(Alert alert) {
        log.info("New alert generated: {} - {}", alert.getThreatLevel(), alert.getDescription());

        activeAlerts.put(alert.getAlertId(), alert);
        alertHistory.add(alert);
        totalAlerts.incrementAndGet();
    }

    public List<Alert> getActiveAlerts() {
        return new ArrayList<>(activeAlerts.values());
    }

    public List<Alert> getAlertHistory() {
        return new ArrayList<>(alertHistory);
    }

    public long getTotalAlertCount() {
        return totalAlerts.get();
    }

    public void resolveAlert(String alertId) {
        Alert alert = activeAlerts.remove(alertId);
        if (alert != null) {
            alert.setStatus(AlertStatus.RESOLVED);
            log.info("Alert resolved: {}", alertId);
        }
    }

    public void clearAll() {
        activeAlerts.clear();
        alertHistory.clear();
        totalAlerts.set(0);
    }

}
