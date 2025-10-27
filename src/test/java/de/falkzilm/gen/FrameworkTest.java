package de.falkzilm.gen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FrameworkTest {

    @Test
    void testQuarkusFramework() {
        assertEquals("Quarkus", Framework.QUARKUS.label);
        assertTrue(Framework.QUARKUS.aliases.contains("quarkus"));
        assertEquals("quarkus", Framework.QUARKUS.toJson());
    }

    @Test
    void testAngularFramework() {
        assertEquals("Angular", Framework.ANGULAR.label);
        assertTrue(Framework.ANGULAR.aliases.contains("angular"));
        assertTrue(Framework.ANGULAR.aliases.contains("ng"));
        assertEquals("angular", Framework.ANGULAR.toJson());
    }

    @ParameterizedTest
    @ValueSource(strings = {"quarkus", "QUARKUS", "Quarkus", "QuArKuS"})
    void testParseQuarkusVariations(String input) {
        Optional<Framework> result = Framework.parse(input);
        assertTrue(result.isPresent());
        assertEquals(Framework.QUARKUS, result.get());
    }

    @ParameterizedTest
    @ValueSource(strings = {"angular", "ANGULAR", "Angular", "ng", "NG", "Ng"})
    void testParseAngularVariations(String input) {
        Optional<Framework> result = Framework.parse(input);
        assertTrue(result.isPresent());
        assertEquals(Framework.ANGULAR, result.get());
    }

    @Test
    void testParseNullInput() {
        Optional<Framework> result = Framework.parse(null);
        assertFalse(result.isPresent());
    }

    @Test
    void testParseEmptyInput() {
        Optional<Framework> result = Framework.parse("");
        assertFalse(result.isPresent());
    }

    @Test
    void testParseWhitespaceInput() {
        Optional<Framework> result = Framework.parse("   ");
        assertFalse(result.isPresent());
    }

    @Test
    void testParseInvalidInput() {
        Optional<Framework> result = Framework.parse("invalid");
        assertFalse(result.isPresent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"quarkus", "qs"})
    void testFromQuarkusVariations(String input) {
        Framework result = Framework.from(input);
        assertEquals(Framework.QUARKUS, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"angular", "ng"})
    void testFromAngularVariations(String input) {
        Framework result = Framework.from(input);
        assertEquals(Framework.ANGULAR, result);
    }

    @Test
    void testFromNullInput() {
        Framework result = Framework.from(null);
        assertNull(result);
    }

    @Test
    void testFromInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> {
            Framework.from("invalid");
        });
    }

    @Test
    void testFromCaseInsensitive() {
        assertEquals(Framework.QUARKUS, Framework.from("QUARKUS"));
        assertEquals(Framework.QUARKUS, Framework.from("QS"));
        assertEquals(Framework.ANGULAR, Framework.from("ANGULAR"));
        assertEquals(Framework.ANGULAR, Framework.from("NG"));
    }

    @Test
    void testFromWithWhitespace() {
        assertEquals(Framework.QUARKUS, Framework.from("  quarkus  "));
        assertEquals(Framework.ANGULAR, Framework.from("  ng  "));
    }
}