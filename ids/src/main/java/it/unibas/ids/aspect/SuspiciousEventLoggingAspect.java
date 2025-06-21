package it.unibas.ids.aspect;

import it.unibas.common.model.Event;
import it.unibas.common.model.EventType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Slf4j
@Aspect
public class SuspiciousEventLoggingAspect {

    // Lista degli EventType sospetti
    private static final java.util.Set<EventType> SUSPICIOUS_EVENTS = java.util.Set.of(
            EventType.UNAUTHORIZED_FILE_ACCESS,
            EventType.SENSITIVE_FILE_ACCESS,
            EventType.SUSPICIOUS_NETWORK_ACTIVITY,
            EventType.SUSPICIOUS_LOGIN,
            EventType.OFF_HOURS_LOGIN
    );

    // Pointcut che intercetta ogni chiamata a analyzeEvent(Event)
    @Pointcut("execution(* it.unibas.ids.analyzer.EventAnalyzer.analyzeEvent(it.unibas.common.model.Event)) && args(event)")
    public void analyzeEventCall(Event event) {
    }

    // Advice che viene eseguito dopo la chiamata
    @After(value = "analyzeEventCall(event)", argNames = "event")
    public void logSuspiciousEvent(Event event) {
        log.warn("🟢🟢🟢 logSuspiciousEvent [{}] dall'utente {} (dettagli: {})",
                event.getType(), event.getUserId(), event);

        if (SUSPICIOUS_EVENTS.contains(event.getType())) {
            log.warn("🔴🔴🔴🔴🔴🔴 ******************* Evento sospetto rilevato: {} dall'utente {} (dettagli: {})",
                    event.getType(), event.getUserId(), event);
        }
    }
}