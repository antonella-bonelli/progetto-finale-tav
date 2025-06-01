package it.unibas.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FileAccessEvent extends Event{
    @NonNull
    private String filePath;

    @Builder.Default
    //ToDo: trasformare accessType in un enum con casi READ, WRITE,DELETE, EXECUTE, CREATE, MODIFY
    private String accessType = "READ";

    @Builder.Default
    private boolean isAuthorized = true;

    public static FileAccessEventBuilder unauthorizedAccess(String filePath) {
        return FileAccessEvent.builder()
                .type(EventType.UNAUTHORIZED_FILE_ACCESS)
                .filePath(filePath)
                .isAuthorized(false)
                .severity(EventSeverity.MEDIUM);
    }

    public static FileAccessEventBuilder normalAccess(String filePath) {
        return FileAccessEvent.builder()
                .type(EventType.FILE_ACCESS)
                .filePath(filePath)
                .isAuthorized(true)
                .severity(EventSeverity.LOW);
    }

    public static FileAccessEventBuilder sensitiveFileAccess(String filePath) {
        return FileAccessEvent.builder()
                .type(EventType.UNAUTHORIZED_FILE_ACCESS)
                .filePath(filePath)
                .isAuthorized(false)
                .severity(EventSeverity.HIGH);
    }

    public static FileAccessEventBuilder criticalFileAccess(String filePath) {
        return FileAccessEvent.builder()
                .type(EventType.UNAUTHORIZED_FILE_ACCESS)
                .filePath(filePath)
                .isAuthorized(false)
                .severity(EventSeverity.CRITICAL);
    }
}
