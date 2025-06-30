package it.unibas.simulator.generator;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Data
@Builder
public class GeneratorConfig {

    @NonNull
    private String generatorName;

    @Builder.Default
    private long intervalMs = 1000;

    @Builder.Default
    private double suspiciousEventProbability = 0.15;

    @Builder.Default
    private String sourceId = "simulator";

    @Builder.Default
    private int maxEvents = 1000;

    @Builder.Default
    private int numberOfSources = 2;

    @Builder.Default
    private int port = 9876;

    public static GeneratorConfig loadFromProperties(String propertiesFile) {
        Properties props = new Properties();

        try (InputStream inputStream = GeneratorConfig.class.getClassLoader()
                .getResourceAsStream(propertiesFile)) {

            if (inputStream == null) {
                throw new IllegalArgumentException("Properties file not found: " + propertiesFile);
            }

            props.load(inputStream);
            return parseConfiguration(props);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration from: " + propertiesFile, e);
        }
    }

    private static GeneratorConfig parseConfiguration(Properties props) {
        long interval = Long.parseLong(props.getProperty("simulator.event.interval", "1000"));
        double suspiciousProbability = Double.parseDouble(
                props.getProperty("simulator.suspicious.probability", "0.15"));
        int numberOfSources = Integer.parseInt(props.getProperty("simulator.sources", "2"));
        int maxEvents = Integer.parseInt(props.getProperty("simulator.max.events", "10000"));
        String sourceId = props.getProperty("simulator.source.id", "simulator");
        int port = Integer.parseInt(props.getProperty("simulator.port", "9876"));
        return GeneratorConfig.builder()
                .generatorName("EventGenerator")
                .intervalMs(interval)
                .suspiciousEventProbability(suspiciousProbability)
                .maxEvents(maxEvents)
                .sourceId(sourceId)
                .numberOfSources(numberOfSources)
                .port(port)
                .build();
    }

    public static GeneratorConfig defaultConfig() {
        return GeneratorConfig.builder()
                .generatorName("DefaultGenerator")
                .intervalMs(2000)
                .suspiciousEventProbability(0.20)
                .port(9876)
                .build();
    }

    public void validate() {
        if (intervalMs <= 0) {
            throw new IllegalArgumentException("Interval must be positive");
        }

        if (suspiciousEventProbability < 0.0 || suspiciousEventProbability > 1.0) {
            throw new IllegalArgumentException("Suspicious event probability must be between 0.0 and 1.0");
        }

        if (maxEvents <= 0) {
            throw new IllegalArgumentException("Max event limit must be positive");
        }
    }

    public void printSummary() {
        System.out.println("=== Generator Configuration ===");
        System.out.println("Name: " + generatorName);
        System.out.println("Interval: " + intervalMs + "ms");
        System.out.println("Suspicious probability: " + suspiciousEventProbability);
        System.out.println("Source ID: " + sourceId);
        System.out.println("Max events: " + (maxEvents == -1 ? "unlimited" : maxEvents));
    }
}
