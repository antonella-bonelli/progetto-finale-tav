package it.unibas.generator;

import it.unibas.simulator.generator.GeneratorConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GeneratorConfigTest {
    @Test
    void shouldCreateConfigWithDefaultValues() {
        GeneratorConfig config = GeneratorConfig.builder()
                .generatorName("TestGenerator")
                .build();

        assertNotNull(config);
        assertEquals("TestGenerator", config.getGeneratorName());
        assertEquals(1000, config.getIntervalMs());
        assertEquals(0.15, config.getSuspiciousEventProbability(), 0.001);
        assertEquals("simulator", config.getSourceId());
        assertEquals(1000, config.getMaxEvents());
    }

    @Test
    void shouldCreateConfigWithCustomValues() {
        GeneratorConfig config = GeneratorConfig.builder()
                .generatorName("CustomGenerator")
                .intervalMs(500)
                .suspiciousEventProbability(0.3)
                .sourceId("custom-source")
                .maxEvents(100)
                .build();

        assertEquals("CustomGenerator", config.getGeneratorName());
        assertEquals(500, config.getIntervalMs());
        assertEquals(0.3, config.getSuspiciousEventProbability(), 0.001);
        assertEquals("custom-source", config.getSourceId());
        assertEquals(100, config.getMaxEvents());
    }

    @Test
    void shouldThrowExceptionWhenGeneratorNameIsNull() {
        assertThrows(NullPointerException.class, () -> {
            GeneratorConfig.builder()
                    .generatorName(null)
                    .build();
        });
    }

    @Test
    void shouldThrowExceptionForNonExistentPropertiesFile() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> GeneratorConfig.loadFromProperties("non-existent.properties")
        );
        assertTrue(exception.getMessage().contains("Properties file not found"));
    }

    @Test
    void shouldValidateCorrectConfiguration() {
        GeneratorConfig config = GeneratorConfig.builder()
                .generatorName("ValidGenerator")
                .intervalMs(1000)
                .suspiciousEventProbability(0.5)
                .build();

        assertDoesNotThrow(config::validate);
    }

    @Test
    void shouldThrowExceptionForNegativeInterval() {
        GeneratorConfig config = GeneratorConfig.builder()
                .generatorName("TestGenerator")
                .intervalMs(-100)
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                config::validate
        );
        assertTrue(exception.getMessage().contains("Interval must be positive"));
    }

    @Test
    void shouldThrowExceptionForZeroInterval() {
        GeneratorConfig config = GeneratorConfig.builder()
                .generatorName("TestGenerator")
                .intervalMs(0)
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                config::validate
        );
        assertTrue(exception.getMessage().contains("Interval must be positive"));
    }

    @Test
    void shouldThrowExceptionForNegativeSuspiciousProbability() {
        GeneratorConfig config = GeneratorConfig.builder()
                .generatorName("TestGenerator")
                .suspiciousEventProbability(-0.1)
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                config::validate
        );
        assertTrue(exception.getMessage().contains("Suspicious event probability must be between 0.0 and 1.0"));
    }

    @Test
    void shouldThrowExceptionForSuspiciousProbabilityGreaterThanOne() {
        GeneratorConfig config = GeneratorConfig.builder()
                .generatorName("TestGenerator")
                .suspiciousEventProbability(1.5)
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                config::validate
        );
        assertTrue(exception.getMessage().contains("Suspicious event probability must be between 0.0 and 1.0"));
    }

    @Test
    void shouldAcceptBoundaryValues() {
        GeneratorConfig config1 = GeneratorConfig.builder()
                .generatorName("TestGenerator")
                .suspiciousEventProbability(0.0)
                .intervalMs(1)
                .build();

        GeneratorConfig config2 = GeneratorConfig.builder()
                .generatorName("TestGenerator")
                .suspiciousEventProbability(1.0)
                .intervalMs(Integer.MAX_VALUE)
                .build();

        assertDoesNotThrow(config1::validate);
        assertDoesNotThrow(config2::validate);
    }

    @Test
    void shouldCreateDefaultConfiguration() {
        GeneratorConfig config = GeneratorConfig.defaultConfig();

        assertNotNull(config);
        assertEquals("DefaultGenerator", config.getGeneratorName());
        assertEquals(2000, config.getIntervalMs());
        assertEquals(0.20, config.getSuspiciousEventProbability(), 0.001);
        assertEquals("simulator", config.getSourceId());
        assertEquals(1000, config.getMaxEvents());
    }

    @Test
    void shouldBeEqualWhenAllPropertiesAreTheSame() {
        GeneratorConfig config1 = GeneratorConfig.builder()
                .generatorName("TestGenerator")
                .intervalMs(1000)
                .suspiciousEventProbability(0.3)
                .maxEvents(100)
                .sourceId("test")
                .build();

        GeneratorConfig config2 = GeneratorConfig.builder()
                .generatorName("TestGenerator")
                .intervalMs(1000)
                .suspiciousEventProbability(0.3)
                .maxEvents(100)
                .sourceId("test")
                .build();

        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenPropertiesDiffer() {
        GeneratorConfig config1 = GeneratorConfig.builder()
                .generatorName("TestGenerator1")
                .build();

        GeneratorConfig config2 = GeneratorConfig.builder()
                .generatorName("TestGenerator2")
                .build();

        assertNotEquals(config1, config2);
    }
}
