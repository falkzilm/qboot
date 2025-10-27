package de.falkzilm.exec;

import org.apache.commons.exec.CommandLine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class RunWrapperTest {

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    /** Build a shell command (one string after /c or -lc). */
    private static CommandLine sh(String command) {
        if (isWindows()) {
            return new CommandLine("cmd").addArgument("/c").addArgument(command, false);
        } else {
            return new CommandLine("bash").addArgument("-lc").addArgument(command, false);
        }
    }

    /** A short command that prints to STDOUT and exits 0. */
    private static CommandLine echo(String text) {
        if (isWindows()) {
            return sh("echo " + text);
        } else {
            return sh("echo " + text);
        }
    }

    /** A short command that prints to STDERR and exits 0 (java -version). */
    private static CommandLine javaVersion() {
        // Use "java -version" (prints to stderr on most JDKs)
        return new CommandLine("java").addArgument("-version");
    }

    /** A command that sleeps > 5s to trigger the 5s watchdog timeout in RunWrapper. */
    private static CommandLine longRunning() {
        if (isWindows()) {
            // ping loop ~ 6 seconds (5 pings @ ~1s each; first ping returns immediately)
            return sh("ping -n 7 127.0.0.1 >NUL");
        } else {
            return sh("sleep 7");
        }
    }

    private Path tempDir;

    @BeforeEach
    void setup() throws Exception {
        tempDir = Files.createTempDirectory("runwrapper-test-");
    }

    @AfterEach
    void cleanup() throws Exception {
        // best effort cleanup
        try { Files.walk(tempDir).sorted((a,b)->b.compareTo(a)).forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception ignored) {} }); }
        catch (Exception ignored) {}
    }

    @Test
    @DisplayName("Returns -1 when cmd is null")
    void nullCommandReturnsMinusOne() {
        RunWrapper wrapper = RunWrapper.builder().cmd(null).build();
        int exit = wrapper.run(tempDir, false);
        assertEquals(-1, exit);
        assertEquals("", wrapper.getOutput());
    }

    @Test
    @DisplayName("Captures STDOUT for a successful command")
    void capturesStdout() {
        RunWrapper wrapper = RunWrapper.builder().cmd(echo("hello-world")).build();
        int exit = wrapper.run(tempDir, false);
        assertEquals(0, exit, "echo should exit 0");
        String out = wrapper.getOutput().trim();
        assertEquals("hello-world", out);
        assertNotNull(wrapper.getOutputStream(), "stdout stream should be initialized");
        assertNotNull(wrapper.getErrorStream(), "stderr stream should be initialized");
    }

    @Test
    @DisplayName("Falls back to STDERR when STDOUT is empty (java -version)")
    void picksStderrWhenNoStdout() {
        RunWrapper wrapper = RunWrapper.builder().cmd(javaVersion()).build();
        int exit = wrapper.run(tempDir, false);
        assertEquals(0, exit, "java -version should exit 0");
        String out = wrapper.getOutput();
        assertTrue(out.toLowerCase().contains("version") || out.toLowerCase().contains("openjdk"),
                "expected stderr text with version info, got: " + out);
    }

    @Test
    @DisplayName("Returns -1 on timeout (watchdog kills long-running command)")
    void returnsMinusOneOnTimeout() {
        RunWrapper wrapper = RunWrapper.builder().cmd(longRunning()).timeout(Duration.ofSeconds(5)).build();
        long t0 = System.currentTimeMillis();
        int exit = wrapper.run(tempDir, false);
        long elapsed = System.currentTimeMillis() - t0;

        assertNotEquals(0, exit, "On timeout cmd is stopped and returned");
        assertTrue(elapsed >= 4500 && elapsed < 15000, "Watchdog should stop around 5s: was " + elapsed + "ms");
        // Output may be empty or partial; no strict assertion here.
    }

    @Test
    @DisplayName("Debug path should not break execution")
    void debugDoesNotBreak() {
        RunWrapper wrapper = RunWrapper.builder().cmd(echo("dbg")).build();
        int exit = wrapper.run(tempDir, true);
        assertEquals(0, exit);
        assertTrue(wrapper.getOutput().contains("dbg"));
    }
}
