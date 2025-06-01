package it.unibas.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
    SUSPICIOUS_LOGIN("Tentativo di login sospetto"),
    UNAUTHORIZED_FILE_ACCESS("Accesso non autorizzato a file"),
    OFF_HOURS_LOGIN("Login fuori orario lavorativo"),
    MULTIPLE_FAILED_LOGINS("Multipli tentativi di login falliti"),
    PRIVILEGE_ESCALATION("Tentativo di escalation privilegi"),
    SUSPICIOUS_NETWORK_ACTIVITY("Attività di rete sospetta"),
    DATA_EXFILTRATION("Possibile esfiltrazione dati"),
    FILE_ACCESS("Accesso a file"),
    SUCCESSFUL_LOGIN("Login avvenuto con successo"),
    LOGOUT("Logout avvenuto con successo"),
    NORMAL_NETWORK_ACTIVITY("Attività di rete normale");

    private final String description;
}
