package it.unibas.common.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventGroup {
    LOGIN("Login"),
    FILE_ACCESS("File"),
    NETWORK_ACTIVITY("Network");

    private final String description;
}
