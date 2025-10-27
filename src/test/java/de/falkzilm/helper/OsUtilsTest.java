package de.falkzilm.helper;

import org.apache.commons.exec.CommandLine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OsUtilsTest {

    @Test
    void testOperatingSystemDetection() {
        // At least one of these should be true
        assertTrue(OsUtils.isWindows() || OsUtils.isMac() || OsUtils.isLinux());
        
        // Unix is either Mac or Linux
        assertEquals(OsUtils.isMac() || OsUtils.isLinux(), OsUtils.isUnix());
        
        // OS description should not be null or empty
        assertNotNull(OsUtils.getOsDescription());
        assertFalse(OsUtils.getOsDescription().isEmpty());
        
        // Architecture should not be null
        assertNotNull(OsUtils.getArchitecture());
        assertFalse(OsUtils.getArchitecture().isEmpty());
    }

    @Test
    void testShellCommandGeneration() {
        String shell = OsUtils.getShell();
        String flag = OsUtils.getShellFlag();
        
        assertNotNull(shell);
        assertNotNull(flag);
        
        if (OsUtils.isWindows()) {
            assertEquals("cmd", shell);
            assertEquals("/c", flag);
        } else {
            assertEquals("bash", shell);
            assertEquals("-lc", flag);
        }
    }

    @Test
    void testCreateShellCommand() {
        String testCommand = "echo hello";
        CommandLine cmd = OsUtils.createShellCommand(testCommand);
        
        assertNotNull(cmd);
        assertEquals(OsUtils.getShell(), cmd.getExecutable());
        
        String[] args = cmd.getArguments();
        assertEquals(2, args.length);
        assertEquals(OsUtils.getShellFlag(), args[0]);
        assertEquals(testCommand, args[1]);
    }

    @Test
    void testCreateMkdirCommand() {
        String testPath = "/test/path";
        CommandLine cmd = OsUtils.createMkdirCommand(testPath);
        
        assertNotNull(cmd);
        assertEquals(OsUtils.getShell(), cmd.getExecutable());
        
        String[] args = cmd.getArguments();
        assertEquals(2, args.length);
        assertEquals(OsUtils.getShellFlag(), args[0]);
        
        // Command should contain the path
        assertTrue(args[1].contains(testPath));
    }

    @Test
    void testFileExtensions() {
        String execExt = OsUtils.getExecutableExtension();
        String scriptExt = OsUtils.getScriptExtension();
        
        assertNotNull(execExt);
        assertNotNull(scriptExt);
        
        if (OsUtils.isWindows()) {
            assertEquals(".exe", execExt);
            assertEquals(".cmd", scriptExt);
        } else {
            assertEquals("", execExt);
            assertEquals(".sh", scriptExt);
        }
    }

    @Test
    void testNormalizeCommandName() {
        String result = OsUtils.normalizeCommandName("mvn");
        
        if (OsUtils.isWindows()) {
            assertEquals("mvn.cmd", result);
        } else {
            assertEquals("mvn", result);
        }
        
        // Should not double-add extensions
        if (OsUtils.isWindows()) {
            assertEquals("mvn.exe", OsUtils.normalizeCommandName("mvn.exe"));
            assertEquals("script.bat", OsUtils.normalizeCommandName("script.bat"));
        }
    }

    @Test
    void testCreateMavenCommand() {
        String args = "clean install";
        CommandLine cmd = OsUtils.createMavenCommand(args);
        
        assertNotNull(cmd);
        assertEquals(OsUtils.getShell(), cmd.getExecutable());
        
        String[] cmdArgs = cmd.getArguments();
        assertEquals(2, cmdArgs.length);
        assertEquals(OsUtils.getShellFlag(), cmdArgs[0]);
        
        String fullCommand = cmdArgs[1];
        assertTrue(fullCommand.contains(args));
        
        if (OsUtils.isWindows()) {
            assertTrue(fullCommand.contains("mvnw.cmd"));
        } else {
            assertTrue(fullCommand.contains("./mvnw"));
        }
    }

    @Test
    void testCreateNpmCommand() {
        String args = "install --save";
        CommandLine cmd = OsUtils.createNpmCommand(args);
        
        assertNotNull(cmd);
        assertEquals(OsUtils.getShell(), cmd.getExecutable());
        
        String[] cmdArgs = cmd.getArguments();
        assertEquals(2, cmdArgs.length);
        assertEquals(OsUtils.getShellFlag(), cmdArgs[0]);
        
        String fullCommand = cmdArgs[1];
        assertTrue(fullCommand.contains(args));
        
        if (OsUtils.isWindows()) {
            assertTrue(fullCommand.contains("npm.cmd"));
        } else {
            assertTrue(fullCommand.contains("npm"));
        }
    }

    @Test
    void testCreateAngularCommand() {
        String args = "new my-app";
        CommandLine cmd = OsUtils.createAngularCommand(args);
        
        assertNotNull(cmd);
        assertEquals(OsUtils.getShell(), cmd.getExecutable());
        
        String[] cmdArgs = cmd.getArguments();
        assertEquals(2, cmdArgs.length);
        assertEquals(OsUtils.getShellFlag(), cmdArgs[0]);
        
        String fullCommand = cmdArgs[1];
        assertTrue(fullCommand.contains(args));
        
        if (OsUtils.isWindows()) {
            assertTrue(fullCommand.contains("ng.cmd"));
        } else {
            assertTrue(fullCommand.contains("ng"));
        }
    }

    @Test
    void testCommandWithSpecialCharacters() {
        String testCommand = "echo 'hello world'";
        CommandLine cmd = OsUtils.createShellCommand(testCommand);
        
        assertNotNull(cmd);
        String[] args = cmd.getArguments();
        assertEquals(testCommand, args[1]);
    }

    @Test
    void testMkdirWithSpaces() {
        String pathWithSpaces = "/path with spaces/test";
        CommandLine cmd = OsUtils.createMkdirCommand(pathWithSpaces);
        
        assertNotNull(cmd);
        String[] args = cmd.getArguments();
        assertTrue(args[1].contains(pathWithSpaces));
    }
}