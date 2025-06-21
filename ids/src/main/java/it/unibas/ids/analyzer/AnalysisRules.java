package it.unibas.ids.analyzer;

import it.unibas.common.model.EventSeverity;
import it.unibas.common.model.EventType;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@Builder
public class AnalysisRules implements Cloneable {
    // Soglie per diversi tipi di eventi
    @Builder.Default
    private Map<EventType, Integer> eventThresholds = new HashMap<>();

    // Tipi di eventi che generano sempre alert
    @Builder.Default
    private Set<EventType> alwaysAlertEvents = Set.of(
            EventType.UNAUTHORIZED_FILE_ACCESS,
            EventType.SENSITIVE_FILE_ACCESS,
            EventType.SUSPICIOUS_NETWORK_ACTIVITY
    );

    // Soglie di severità per generare alert
    @Builder.Default
    private Map<EventType, EventSeverity> severityThresholds = new HashMap<>();

    // Finestra temporale per conteggio eventi (in millisecondi)
    @Builder.Default
    private long timeWindowMs = 300000; // 5 minuti

    // Soglia per login falliti consecutivi
    @Builder.Default
    private int maxFailedLogins = 5;

    // Soglia per accessi file sospetti
    @Builder.Default
    private int maxSuspiciousFileAccess = 3;

    // IP address whitelist (non generano alert)
//    @Builder.Default
//    private Set<String> whitelistedIps = Set.of(
//            "127.0.0.1",
//            "localhost"
//    );

    // Utenti privilegiati che richiedono soglie più basse
//    @Builder.Default
//    private Set<String> privilegedUsers = Set.of(
//            "admin",
//            "root",
//            "administrator"
//    );

    public static AnalysisRules defaultRules() {
        Map<EventType, Integer> defaultThresholds = new HashMap<>();
        defaultThresholds.put(EventType.FAILED_LOGIN, 5);
        defaultThresholds.put(EventType.FILE_ACCESS, 50);
        defaultThresholds.put(EventType.SUSPICIOUS_LOGIN, 1);

        Map<EventType, EventSeverity> defaultSeverityThresholds = new HashMap<>();
        defaultSeverityThresholds.put(EventType.FAILED_LOGIN, EventSeverity.MEDIUM);
        defaultSeverityThresholds.put(EventType.SUSPICIOUS_LOGIN, EventSeverity.LOW);

        return AnalysisRules.builder()
                .eventThresholds(defaultThresholds)
                .severityThresholds(defaultSeverityThresholds)
                .build();
    }

    public static AnalysisRules strictRules() {
        Map<EventType, Integer> strictThresholds = new HashMap<>();
        strictThresholds.put(EventType.FAILED_LOGIN, 3);
        strictThresholds.put(EventType.FILE_ACCESS, 20);
        strictThresholds.put(EventType.SUSPICIOUS_LOGIN, 1);

        Set<EventType> strictAlertEvents = Set.of(
                EventType.UNAUTHORIZED_FILE_ACCESS,
                EventType.SENSITIVE_FILE_ACCESS,
                EventType.SUSPICIOUS_NETWORK_ACTIVITY,
                EventType.SUSPICIOUS_LOGIN,
                EventType.OFF_HOURS_LOGIN
        );

        return AnalysisRules.builder()
                .eventThresholds(strictThresholds)
                .alwaysAlertEvents(strictAlertEvents)
                .maxFailedLogins(3)
                .maxSuspiciousFileAccess(1)
                .timeWindowMs(180000) // 3 minuti
                .build();
    }

    public boolean shouldAlwaysAlert(EventType eventType) {
        return alwaysAlertEvents.contains(eventType);
    }

//    public boolean isWhitelisted(String ipAddress) {
//        return whitelistedIps.contains(ipAddress);
//    }

//    public boolean isPrivilegedUser(String userId) {
//        return privilegedUsers.contains(userId);
//    }

    public int getThreshold(EventType eventType) {
        return eventThresholds.getOrDefault(eventType, Integer.MAX_VALUE);
    }

    public EventSeverity getSeverityThreshold(EventType eventType) {
        return severityThresholds.getOrDefault(eventType, EventSeverity.LOW);
    }

    @Override
    public AnalysisRules clone() {
        try {
            AnalysisRules cloned = (AnalysisRules) super.clone();

            // Deep clone delle mappe
            cloned.eventThresholds = new HashMap<>(this.eventThresholds);
            cloned.severityThresholds = new HashMap<>(this.severityThresholds);
            cloned.alwaysAlertEvents = Set.copyOf(this.alwaysAlertEvents);
            //cloned.whitelistedIps = Set.copyOf(this.whitelistedIps);
            //cloned.privilegedUsers = Set.copyOf(this.privilegedUsers);

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone AnalysisRules", e);
        }
    }
}
