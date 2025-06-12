package it.unibas.common.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LoginResult {
    SUCCESS("Login was successful"),
    FAILURE("Login failed due to incorrect credentials"),
    LOCKED("Login failed due to account being locked"),
    TIMEOUT("Session ended due to timeout"),
    EXPIRED("Login failed due to expired credentials"),
    UNAUTHORIZED("Login failed due to insufficient privileges"),
    SYSTEM_UNAVAILABLE("Login failed due to system maintenance");

    private final String description;
}
