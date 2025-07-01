package it.unibas.ids.aspect;

import com.google.inject.Inject;
import it.unibas.common.model.Event;
import it.unibas.common.model.EventSeverity;
import it.unibas.common.util.AnalysisContextHolder;
import it.unibas.ids.alert.EmailService;
import it.unibas.ids.model.Alert;
import it.unibas.ids.util.ViewUtil;
import it.unibas.ids.vista.IMainView;
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
    @Inject
    private IMainView mainView;

    // Pointcut che intercetta ogni chiamata a analyzeEvent(Event)
    @Pointcut("execution(* it.unibas.ids.analyzer.IEventAnalyzer.analyzeEvent(it.unibas.common.model.Event)) && args(event)")
    public void analyzeEventCall(Event event) {
    }

    @After(value = "analyzeEventCall(event)", argNames = "event")
    public void logSuspiciousEvent(Event event) {
        if (AnalysisContextHolder.isModalAnalysis()) return;

        // Logging nella JTextArea di MainView
        if (mainView != null) {
            String message = ViewUtil.formatLogMessage(event);
            mainView.appendEventLog(event, message);
        }

        log.warn("Evento sospetto rilevato: {} dall'utente {} (dettagli: {})", event.getType(), event.getUserId(), event);

    }

    @AfterReturning("execution(* it.unibas.ids.alert.AlertManager.addAlert(..))")
    public void sendCriticalNotifications(JoinPoint joinPoint) {
        Alert alert = (Alert) joinPoint.getArgs()[0];
        if (mainView != null && !AnalysisContextHolder.isModalAnalysis()) {
            mainView.addAlert(alert);
            if (alert.getEventSeverity() == EventSeverity.CRITICAL) {
                log.error("🚨🚨 CRITICAL ALERT TRIGGERED 🚨🚨");
                // Invia email mock
                emailService.sendCriticalAlert(alert);
            }
        }
    }

}
