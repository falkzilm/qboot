package de.falkzilm.helper;

import de.falkzilm.gen.Framework;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleFormatterTest {

    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void testDetectVersion() {
        String version = ConsoleFormatter.detectVersion();
        assertNotNull(version);
        assertFalse(version.trim().isEmpty());
        // Version should be either dev or contain version info
        assertTrue(version.equals("dev") || version.contains("1.0.0") || 
                  version.contains("SNAPSHOT"));
    }

    @Test
    void testBannerRocketBox() {
        ConsoleFormatter.bannerRocketBox();
        
        String output = outputStream.toString();
        assertFalse(output.isEmpty());
        // Should contain some visual elements for banner
        assertTrue(output.length() > 10); // Banner should be more than just a few chars
    }

    @Test
    void testHeader() {
        ConsoleFormatter.header(Framework.QUARKUS, "3.28.4", "/tmp/test");
        
        String output = outputStream.toString();
        assertTrue(output.contains("Quarkus"));
        assertTrue(output.contains("3.28.4"));
        assertTrue(output.contains("/tmp/test"));
    }

    @Test
    void testSection() {
        ConsoleFormatter.section("Test Section");
        
        String output = outputStream.toString();
        assertTrue(output.contains("Test Section"));
    }

    @Test
    void testBullet() {
        ConsoleFormatter.bullet("Test bullet point");
        
        String output = outputStream.toString();
        assertTrue(output.contains("Test bullet point"));
        // Should have some bullet character or indentation
        assertTrue(output.trim().length() > "Test bullet point".length());
    }

    @Test
    void testFooter() {
        Duration duration = Duration.ofMillis(1500); // 1.5 seconds
        ConsoleFormatter.footer("/tmp/output", duration);
        
        String output = outputStream.toString();
        assertTrue(output.contains("/tmp/output"));
        // Should contain timing information
        assertTrue(output.contains("1") || output.contains("second") || output.contains("ms"));
    }

    @Test
    void testError() {
        ConsoleFormatter.error("Test Error", "Error message", null);
        
        String output = outputStream.toString();
        assertTrue(output.contains("Test Error"));
        assertTrue(output.contains("Error message"));
    }

    @Test
    void testErrorWithCause() {
        Exception cause = new RuntimeException("Root cause");
        ConsoleFormatter.error("Test Error", "Error message", cause);
        
        String output = outputStream.toString();
        assertTrue(output.contains("Test Error"));
        assertTrue(output.contains("Error message"));
    }

    @Test
    void testErrorWithSuggestions() {
        ConsoleFormatter.error("Test Error", "Error message", null, 
                              "Suggestion 1", "Suggestion 2");
        
        String output = outputStream.toString();
        assertTrue(output.contains("Test Error"));
        assertTrue(output.contains("Error message"));
        assertTrue(output.contains("Suggestion 1"));
        assertTrue(output.contains("Suggestion 2"));
    }

    @Test
    void testEmptyStringHandling() {
        ConsoleFormatter.header(Framework.QUARKUS, "", "");
        ConsoleFormatter.section("");
        ConsoleFormatter.bullet("");
        ConsoleFormatter.error("", "", null);
        
        // Should not crash with empty strings
        String output = outputStream.toString();
        assertNotNull(output);
    }

    @Test
    void testNullStringHandling() {
        // These methods should handle null gracefully
        assertDoesNotThrow(() -> {
            ConsoleFormatter.section(null);
            ConsoleFormatter.bullet(null);
            ConsoleFormatter.error(null, null, null);
        });
    }

    @Test
    void testFooterWithZeroDuration() {
        Duration zeroDuration = Duration.ZERO;
        ConsoleFormatter.footer("/tmp/output", zeroDuration);
        
        String output = outputStream.toString();
        assertTrue(output.contains("/tmp/output"));
        // Should handle zero duration gracefully
        assertFalse(output.isEmpty());
    }

    @Test
    void testFooterWithLongDuration() {
        Duration longDuration = Duration.ofMinutes(5).plusSeconds(30);
        ConsoleFormatter.footer("/tmp/output", longDuration);
        
        String output = outputStream.toString();
        assertTrue(output.contains("/tmp/output"));
        // Should format long durations appropriately
        assertTrue(output.contains("5") || output.contains("30") || 
                  output.contains("minute") || output.contains("second"));
    }
}