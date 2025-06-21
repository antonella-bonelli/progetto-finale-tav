package it.unibas.ids.config;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class IdsProperties {
    private final Properties properties;

    public IdsProperties() {
        this.properties = loadProperties();
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream inputStream = getClass().getResourceAsStream("/ids.properties")) {
            if (inputStream != null) {
                props.load(inputStream);
                log.info("Loaded IDS configuration from ids.properties");
            } else {
                log.warn("ids.properties not found, using defaults");
            }
        } catch (IOException e) {
            log.error("Error loading ids.properties: {}", e.getMessage());
        }
        return props;
    }

    public String getTcpHost() {
        return properties.getProperty("tcp.host", "localhost");
    }

    public int getTcpPort() {
        return Integer.parseInt(properties.getProperty("tcp.port", "9876"));
    }
}
