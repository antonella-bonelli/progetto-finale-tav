package it.unibas.ids.model;

import it.unibas.common.model.EventSeverity;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class Alert {

    @Builder.Default
    private final String alertId = generateAlertId();

    @NonNull
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @NonNull
    private EventSeverity eventSeverity;

    @NonNull
    private String alertType;

    @NonNull
    private String description;

    @NonNull
    private String userId;


    @Builder.Default
    private AlertStatus status = AlertStatus.ACTIVE;

    @Builder.Default
    private Double confidenceScore = 0.0;

    private static String generateAlertId() {
        return "ALR-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
