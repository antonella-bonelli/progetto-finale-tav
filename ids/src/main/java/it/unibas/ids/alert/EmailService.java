package it.unibas.ids.alert;

import com.google.inject.Singleton;
import it.unibas.ids.model.Alert;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Singleton
public class EmailService {
    private final AtomicLong emailsSent = new AtomicLong(0);

    public EmailService() {
    }

    public void sendCriticalAlert(Alert alert) {
        long emailId = emailsSent.incrementAndGet();

        log.error("📧 ==================== EMAIL NOTIFICATION #{} ====================", emailId);
        log.error("📧 FROM: ids-system@company.com");
        log.error("📧 TO: security@company.com, admin@company.com");
        log.error("📧 SUBJECT: 🚨 CRITICAL ALERT - {} - Alert #{}",
                alert.getAlertType(), alert.getAlertId());
        log.error("📧 ");
        log.error("📧 CRITICAL SECURITY ALERT DETECTED");
        log.error("📧 ├── Alert ID: {}", alert.getAlertId());
        log.error("📧 ├── Threat Level: {}", alert.getEventSeverity());
        log.error("📧 ├── User: {}", alert.getUserId());
        log.error("📧 ├── Description: {}", alert.getDescription());
        log.error("📧 └── Confidence: {}%", Double.valueOf(alert.getConfidenceScore() * 100).intValue());
        log.error("📧 ");
        log.error("📧 IMMEDIATE ACTION REQUIRED!");
        log.error("📧 ================================================================");
    }
}
