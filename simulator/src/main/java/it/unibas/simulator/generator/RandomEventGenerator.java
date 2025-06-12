package it.unibas.simulator.generator;


import it.unibas.common.model.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class RandomEventGenerator extends AbstractEventGenerator{
    private static final List<String> SAMPLE_USERS = List.of(
            "john.doe", "jane.smith", "admin", "root", "guest",
            "alice.johnson", "bob.wilson", "charlie.brown", "diana.prince", "eve.adams"
    );


    private static final List<String> SAMPLE_FILES = List.of(
            "/home/user/documents/report.pdf", "/var/log/system.log", "/tmp/temp.txt",
            "/etc/passwd", "/etc/shadow", "/root/.ssh/id_rsa", "/var/www/config.php",
            "/home/admin/secrets.txt", "/opt/app/database.conf", "/usr/local/bin/script.sh"
    );

    public RandomEventGenerator(GeneratorConfig config) {
        super(config);
    }

    @Override
    public Event generateEvent() {
        try {
            // Determine event type based on probability
            if (shouldGenerateSuspiciousEvent()) {
                return generateSuspiciousEvent();
            } else {
                return generateNormalEvent();
            }
        } catch (Exception e) {
            log.error("Error generating random event: {}", e.getMessage(), e);
            return null;
        }
    }

    private Event generateNormalEvent() {
        EventType[] normalTypes = {
                EventType.SUCCESSFUL_LOGIN,
                EventType.LOGOUT,
                EventType.FILE_ACCESS,
                EventType.NORMAL_NETWORK_ACTIVITY,
        };

        EventType eventType = getRandomElement(normalTypes);

        return switch (eventType) {
            case SUCCESSFUL_LOGIN -> generateNormalLoginEvent();
            case LOGOUT -> generateLogoutEvent();
            case FILE_ACCESS -> generateNormalFileAccessEvent();
            case NORMAL_NETWORK_ACTIVITY -> generateNormalTrafficEvent();
            default -> generateNormalLoginEvent();
        };
    }

    private Event generateSuspiciousEvent() {
        EventType[] suspiciousTypes = {
                EventType.FAILED_LOGIN,
                EventType.SUSPICIOUS_LOGIN,
                EventType.MULTIPLE_FAILED_LOGINS,
                EventType.OFF_HOURS_LOGIN,
                EventType.UNAUTHORIZED_FILE_ACCESS,
                EventType.SENSITIVE_FILE_ACCESS,
                EventType.SUSPICIOUS_NETWORK_ACTIVITY,
                EventType.DATA_EXFILTRATION
        };

        EventType eventType = getRandomElement(suspiciousTypes);

        return switch (eventType) {
            case FAILED_LOGIN -> generateFailedLoginEvent();
            case SUSPICIOUS_LOGIN -> generateSuspiciousLoginEvent();
            case MULTIPLE_FAILED_LOGINS -> generateBruteForceEvent();
            case OFF_HOURS_LOGIN -> generateOffHoursLoginEvent();
            case UNAUTHORIZED_FILE_ACCESS -> generateUnauthorizedFileAccessEvent();
            case SENSITIVE_FILE_ACCESS -> generateSensitiveFileAccessEvent();
            case SUSPICIOUS_NETWORK_ACTIVITY -> generateSuspiciousNetworkEvent();
            case DATA_EXFILTRATION -> generateDataExfiltrationEvent();
            default -> generateFailedLoginEvent();
        };
    }

    // Login Event Generators

    private Event generateNormalLoginEvent() {
        return LoginEvent.successfulLogin(getRandomUser())
                .timestamp(LocalDateTime.now())
                .build();
    }

    private LoginEvent generateFailedLoginEvent() {
        return LoginEvent.suspiciousLogin(getRandomUser())
                .build();
    }

    private LoginEvent generateSuspiciousLoginEvent() {
        return LoginEvent.suspiciousLogin(getRandomUser())
                .build();
    }

    private LoginEvent generateBruteForceEvent() {
        return LoginEvent.bruteForceAttempt(getRandomUser(),
                        random.nextInt(15) + 5) // 5-20
                .build();
    }

    private LoginEvent generateOffHoursLoginEvent() {
        return LoginEvent.offHoursLogin(getRandomUser())
                .build();
    }

    private LoginEvent generateLogoutEvent() {
        return LoginEvent.logout(getRandomUser())
                .build();
    }

    // File Access Event Generators

    private FileAccessEvent generateNormalFileAccessEvent() {
        String normalFile = getRandomElement(List.of(
                "/home/user/documents/report.pdf",
                "/var/log/system.log",
                "/tmp/temp.txt"
        ));

        return FileAccessEvent.normalAccess(normalFile, getRandomUser())
                .build();
    }

    private FileAccessEvent generateUnauthorizedFileAccessEvent() {
        return FileAccessEvent.unauthorizedAccess(getRandomFile(), getRandomUser())
                .build();
    }

    private FileAccessEvent generateSensitiveFileAccessEvent() {
        String sensitiveFile = getRandomElement(List.of(
                "/etc/passwd", "/etc/shadow", "/root/.ssh/id_rsa",
                "/var/www/config.php", "/home/admin/secrets.txt"
        ));

        return FileAccessEvent.sensitiveFileAccess(sensitiveFile, getRandomUser())
                .build();
    }

    // Network Event Generators
    private NetworkEvent generateNormalTrafficEvent() {
        return NetworkEvent.normalTraffic(getRandomExternalIp(), getRandomInternalIp(), getRandomUser())
                .build();
    }

    private NetworkEvent generateSuspiciousNetworkEvent() {
        return NetworkEvent.suspiciousTraffic(getRandomExternalIp(), getRandomInternalIp(), getRandomUser())
                .destinationPort(8080 + random.nextInt(1000))
                .bytesTransferred(random.nextLong(1000000))
                .build();
    }

    private NetworkEvent generateDataExfiltrationEvent() {
        return NetworkEvent.dataExfiltration(getRandomExternalIp(), getRandomInternalIp(), getRandomUser())
                .build();
    }
    // Utility Methods

    private String getRandomUser() {
        return getRandomElement(SAMPLE_USERS);
    }

    private String getRandomInternalIp() {
        List<String> internalIps = List.of("192.168.1.100", "192.168.1.101", "10.0.0.50", "172.16.0.10");
        return getRandomElement(internalIps);
    }

    private String getRandomExternalIp() {
        List<String> externalIps = List.of("203.0.113.42", "198.51.100.25", "1.2.3.4", "8.8.8.8");
        return getRandomElement(externalIps);
    }
    private String getRandomFile() {
        return getRandomElement(SAMPLE_FILES);
    }
    private <T> T getRandomElement(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    private <T> T getRandomElement(T[] array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Array cannot be null or empty");
        }
        return array[random.nextInt(array.length)];
    }
}
