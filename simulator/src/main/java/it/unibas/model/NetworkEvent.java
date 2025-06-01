package it.unibas.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkEvent extends Event{
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

    public static NetworkEventBuilder normalTraffic(String sourceIp, String destinationIp) {
        return NetworkEvent.builder()
                .type(EventType.NORMAL_NETWORK_ACTIVITY)
                .sourceIp(sourceIp)
                .destinationIp(destinationIp)
                .destinationPort(80)
                .severity(EventSeverity.LOW);
    }

    public static NetworkEventBuilder suspiciousTraffic(String sourceIp,String destinationIp) {
        return NetworkEvent.builder()
                .type(EventType.SUSPICIOUS_NETWORK_ACTIVITY)
                .sourceIp(sourceIp)
                .destinationIp(destinationIp)
                .severity(EventSeverity.HIGH);
    }

    public static NetworkEventBuilder dataExfiltration(String sourceIp, String destinationIp) {
        return NetworkEvent.builder()
                .type(EventType.DATA_EXFILTRATION)
                .sourceIp(sourceIp)
                .destinationIp(destinationIp)
                .severity(EventSeverity.CRITICAL);
    }
}
