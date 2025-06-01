package it.unibas.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

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
                .severity(EventSeverity.LOW);
    }
    public static LoginEventBuilder suspiciousLogin(String userId) {
        return LoginEvent.builder()
                .type(EventType.SUSPICIOUS_LOGIN)
                .userId(userId)
                .result(LoginResult.FAILURE)
                .severity(EventSeverity.MEDIUM);
    }

    public static LoginEventBuilder offHoursLogin(String userId) {
        return LoginEvent.builder()
                .type(EventType.OFF_HOURS_LOGIN)
                .userId(userId)
                .result(LoginResult.SYSTEM_UNAVAILABLE)
                .severity(EventSeverity.MEDIUM);
    }

    public static LoginEventBuilder bruteForceAttempt(String userId, int attemptNumber) {
        EventSeverity severity = attemptNumber > 5 ? EventSeverity.CRITICAL : EventSeverity.HIGH;
        return LoginEvent.builder()
                .type(EventType.SUSPICIOUS_LOGIN)
                .userId(userId)
                .result(LoginResult.FAILURE)
                .attemptCount(attemptNumber)
                .severity(severity);
    }
}
