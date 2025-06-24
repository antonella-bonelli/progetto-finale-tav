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
public class FileAccessEvent extends Event {
    private EventGroup group = EventGroup.FILE_ACCESS;
    @NonNull
    private String filePath;

    @Builder.Default
    //ToDo: trasformare accessType in un enum con casi READ, WRITE,DELETE, EXECUTE, CREATE, MODIFY
    private String accessType = "READ";

    @Builder.Default
    private boolean isAuthorized = true;

    public static FileAccessEventBuilder unauthorizedAccess(String filePath, String userId) {
        return FileAccessEvent.builder()
                .type(EventType.UNAUTHORIZED_FILE_ACCESS)
                .filePath(filePath)
                .userId(userId)
                .isAuthorized(false)
                .timestamp(LocalDateTime.now())
                .severity(EventSeverity.MEDIUM);
    }

    public static FileAccessEventBuilder normalAccess(String filePath, String userId) {
        return FileAccessEvent.builder()
                .type(EventType.FILE_ACCESS)
                .filePath(filePath)
                .userId(userId)
                .isAuthorized(true)
                .timestamp(LocalDateTime.now())
                .severity(EventSeverity.LOW);
    }

    public static FileAccessEventBuilder sensitiveFileAccess(String filePath, String userId) {
        return FileAccessEvent.builder()
                .type(EventType.SENSITIVE_FILE_ACCESS)
                .filePath(filePath)
                .userId(userId)
                .isAuthorized(false)
                .timestamp(LocalDateTime.now())
                .severity(EventSeverity.HIGH);
    }
}
