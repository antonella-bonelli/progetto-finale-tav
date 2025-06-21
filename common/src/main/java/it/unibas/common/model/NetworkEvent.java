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
public class NetworkEvent extends Event {
    @NonNull
    private String sourceIp;

    @NonNull
    private String destinationIp;

    @Builder.Default
    private int destinationPort = 8080;

    @Builder.Default
    private String protocol = "TCP";

    @Builder.Default
    private long bytesTransferred = 0;

    @Builder.Default
    private boolean isEncrypted = false;

    public static NetworkEventBuilder normalTraffic(String sourceIp, String destinationIp, String userId) {
        return NetworkEvent.builder()
                .type(EventType.NORMAL_NETWORK_ACTIVITY)
                .userId(userId)
                .sourceIp(sourceIp)
                .destinationIp(destinationIp)
                .destinationPort(80)
                .timestamp(LocalDateTime.now())
                .severity(EventSeverity.LOW);
    }

    public static NetworkEventBuilder suspiciousTraffic(String sourceIp, String destinationIp, String userId) {
        return NetworkEvent.builder()
                .type(EventType.SUSPICIOUS_NETWORK_ACTIVITY)
                .userId(userId)
                .sourceIp(sourceIp)
                .destinationIp(destinationIp)
                .timestamp(LocalDateTime.now())
                .severity(EventSeverity.HIGH);
    }

    public static NetworkEventBuilder dataExfiltration(String sourceIp, String destinationIp, String userId) {
        return NetworkEvent.builder()
                .type(EventType.DATA_EXFILTRATION)
                .userId(userId)
                .sourceIp(sourceIp)
                .destinationIp(destinationIp)
                .timestamp(LocalDateTime.now())
                .severity(EventSeverity.CRITICAL);
    }
}
