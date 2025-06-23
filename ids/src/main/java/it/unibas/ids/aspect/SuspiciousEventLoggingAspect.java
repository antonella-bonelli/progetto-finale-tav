package it.unibas.ids.aspect;

import com.google.inject.Inject;
import it.unibas.common.model.Event;
import it.unibas.common.model.EventType;
import it.unibas.ids.alert.EmailService;
import it.unibas.ids.model.Alert;
import it.unibas.ids.model.ThreatLevel;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Slf4j
@Aspect
public class SuspiciousEventLoggingAspect {

    @Inject
    private EmailService emailService;

    // Lista degli EventType sospetti
    private static final java.util.Set<EventType> SUSPICIOUS_EVENTS = java.util.Set.of(
            EventType.UNAUTHORIZED_FILE_ACCESS,
            EventType.SENSITIVE_FILE_ACCESS,
            EventType.SUSPICIOUS_NETWORK_ACTIVITY,
            EventType.SUSPICIOUS_LOGIN,
            EventType.OFF_HOURS_LOGIN
    );

    // Pointcut che intercetta ogni chiamata a analyzeEvent(Event)
    @Pointcut("execution(* it.unibas.ids.analyzer.IEventAnalyzer.analyzeEvent(it.unibas.common.model.Event)) && args(event)")
    public void analyzeEventCall(Event event) {
    }

    @After(value = "analyzeEventCall(event)", argNames = "event")
    public void logSuspiciousEvent(Event event) {
        if (SUSPICIOUS_EVENTS.contains(event.getType())) {
            log.warn("Evento sospetto rilevato: {} dall'utente {} (dettagli: {})",
                    event.getType(), event.getUserId(), event);
        }
    }

    @AfterReturning("execution(* it.unibas.ids.alert.AlertManager.addAlert(..))")
    public void sendCriticalNotifications(JoinPoint joinPoint) {
        Alert alert = (Alert) joinPoint.getArgs()[0];

        if (alert.getThreatLevel() == ThreatLevel.CRITICAL) {
            log.error("🚨🚨 CRITICAL ALERT TRIGGERED 🚨🚨");

            // ✅ INVIA EMAIL MOCK
            emailService.sendCriticalAlert(alert);
        }
    }
}
