package it.unibas.common.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LoginEvent extends Event{
    @Builder.Default
    private String loginMethod = "password";

    @Builder.Default
    private LoginResult result = LoginResult.SUCCESS;

    @Builder.Default
    private int attemptCount = 1;

    public static LoginEventBuilder successfulLogin(String userId) {
        return LoginEvent.builder()
                .type(EventType.SUCCESSFUL_LOGIN)
                .userId(userId)
                .result(LoginResult.SUCCESS)
                .timestamp(LocalDateTime.now())
                .severity(EventSeverity.LOW);
    }
    public static LoginEventBuilder suspiciousLogin(String userId) {
        return LoginEvent.builder()
                .type(EventType.SUSPICIOUS_LOGIN)
                .userId(userId)
                .result(LoginResult.FAILURE)
                .timestamp(LocalDateTime.now())
                .severity(EventSeverity.MEDIUM);
    }

    public static LoginEventBuilder offHoursLogin(String userId) {
        return LoginEvent.builder()
                .type(EventType.OFF_HOURS_LOGIN)
                .userId(userId)
                .result(LoginResult.SYSTEM_UNAVAILABLE)
                .timestamp(LocalDateTime.now())
                .severity(EventSeverity.MEDIUM);
    }

    public static LoginEventBuilder bruteForceAttempt(String userId, int attemptNumber) {
        EventSeverity severity = attemptNumber > 5 ? EventSeverity.CRITICAL : EventSeverity.HIGH;
        return LoginEvent.builder()
                .type(EventType.MULTIPLE_FAILED_LOGINS)
                .userId(userId)
                .result(LoginResult.FAILURE)
                .attemptCount(attemptNumber)
                .timestamp(LocalDateTime.now())
                .severity(severity);
    }

    public static LoginEventBuilder logout(String userId) {
        return LoginEvent.builder()
                .type(EventType.LOGOUT)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .severity(EventSeverity.LOW);
    }
}
