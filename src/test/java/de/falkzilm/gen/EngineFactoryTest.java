package de.falkzilm.gen;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class EngineFactoryTest {

    @Inject
    EngineFactory engineFactory;

    @Test
    void testGetUnsupportedFramework() {
        // Since we can't create new enum values, we'll test with null handling
        // This tests the IllegalArgumentException path indirectly
        assertThrows(IllegalArgumentException.class, () -> {
            // This will fail because null is not a valid key in EnumMap
            engineFactory.get(null);
        });
    }

    @Test
    void testSupported() {
        List<Framework> supportedFrameworks = engineFactory.supported();
        
        assertNotNull(supportedFrameworks);
        assertFalse(supportedFrameworks.isEmpty());
        for (Framework framework : Framework.values()) {
            assertTrue(supportedFrameworks.contains(framework));
        }
        
        // Verify it's an immutable copy
        assertThrows(UnsupportedOperationException.class, () -> {
            supportedFrameworks.add(Framework.QUARKUS);
        });
    }

    @Test
    void testSupportedContainsAllAvailableEngines() {
        List<Framework> supported = engineFactory.supported();
        
        // Test that we can get an engine for each supported framework
        for (Framework framework : supported) {
            GenerationEngine engine = engineFactory.get(framework);
            assertNotNull(engine);
            assertEquals(framework, engine.framework());
        }
    }
}