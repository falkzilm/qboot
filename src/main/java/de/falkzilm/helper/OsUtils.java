package de.falkzilm.helper;

import org.apache.commons.exec.CommandLine;

/**
 * Utility class for operating system detection and command execution.
 * Provides cross-platform support for Windows, macOS, and Linux.
 */
public class OsUtils {
    
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final boolean IS_WINDOWS = OS_NAME.contains("windows");
    private static final boolean IS_MAC = OS_NAME.contains("mac");
    private static final boolean IS_LINUX = OS_NAME.contains("linux");

    /**
     * Determines if the current operating system is Windows.
     * @return true if running on Windows
     */
    public static boolean isWindows() {
        return IS_WINDOWS;
    }

    /**
     * Determines if the current operating system is macOS.
     * @return true if running on macOS
     */
    public static boolean isMac() {
        return IS_MAC;
    }

    /**
     * Determines if the current operating system is Linux.
     * @return true if running on Linux
     */
    public static boolean isLinux() {
        return IS_LINUX;
    }

    /**
     * Determines if the current operating system is Unix-like (Linux or macOS).
     * @return true if running on Unix-like system
     */
    public static boolean isUnix() {
        return IS_LINUX || IS_MAC;
    }

    /**
     * Gets the appropriate shell command for the current operating system.
     * @return "cmd" for Windows, "bash" for Unix-like systems
     */
    public static String getShell() {
        return IS_WINDOWS ? "cmd" : "bash";
    }

    /**
     * Gets the appropriate shell flag for executing commands.
     * @return "/c" for Windows cmd, "-lc" for Unix bash
     */
    public static String getShellFlag() {
        return IS_WINDOWS ? "/c" : "-lc";
    }

    /**
     * Creates a platform-appropriate command line for executing shell commands.
     * On Windows: cmd /c "command"
     * On Unix: bash -lc "command"
     * 
     * @param command The command to execute
     * @return CommandLine configured for the current platform
     */
    public static CommandLine createShellCommand(String command) {
        return new CommandLine(getShell())
                .addArgument(getShellFlag())
                .addArgument(command, false);
    }

    /**
     * Creates a platform-appropriate directory creation command.
     * @param path The directory path to create
     * @return CommandLine for creating directories
     */
    public static CommandLine createMkdirCommand(String path) {
        String command = IS_WINDOWS ? "mkdir \"" + path + "\" 2>nul || echo Directory exists" 
                                   : "mkdir -p \"" + path + "\"";
        return createShellCommand(command);
    }

    /**
     * Gets the executable extension for the current platform.
     * @return ".exe" for Windows, "" for Unix-like systems
     */
    public static String getExecutableExtension() {
        return IS_WINDOWS ? ".exe" : "";
    }

    /**
     * Gets the batch/script file extension for the current platform.
     * @return ".bat" or ".cmd" for Windows, ".sh" for Unix-like systems
     */
    public static String getScriptExtension() {
        return IS_WINDOWS ? ".cmd" : ".sh";
    }

    /**
     * Normalizes a command name to include platform-specific extensions if needed.
     * @param commandName The base command name (e.g., "mvn", "npm")
     * @return The command name with appropriate extension for Windows
     */
    public static String normalizeCommandName(String commandName) {
        if (IS_WINDOWS && !commandName.endsWith(".exe") && !commandName.endsWith(".cmd") && !commandName.endsWith(".bat")) {
            // Try .cmd first, then .bat for Windows batch files
            return commandName + ".cmd";
        }
        return commandName;
    }

    /**
     * Gets a human-readable description of the current operating system.
     * @return String describing the current OS
     */
    public static String getOsDescription() {
        if (IS_WINDOWS) {
            return "Windows";
        } else if (IS_MAC) {
            return "macOS";
        } else if (IS_LINUX) {
            return "Linux";
        } else {
            return "Unknown (" + OS_NAME + ")";
        }
    }

    /**
     * Gets the current OS architecture.
     * @return The system architecture (e.g., "x86_64", "aarch64")
     */
    public static String getArchitecture() {
        return System.getProperty("os.arch");
    }

    /**
     * Creates a Maven command with platform-appropriate wrapper.
     * @param mavenArgs The Maven arguments
     * @return CommandLine for Maven execution
     */
    public static CommandLine createMavenCommand(String mavenArgs) {
        String mvnCommand = IS_WINDOWS ? "mvnw.cmd" : "mvn";
        return createShellCommand(mvnCommand + " " + mavenArgs);
    }

    /**
     * Creates an npm command with platform-appropriate handling.
     * @param npmArgs The npm arguments
     * @return CommandLine for npm execution
     */
    public static CommandLine createNpmCommand(String npmArgs) {
        String npmCommand = IS_WINDOWS ? "npm.cmd" : "npm";
        return createShellCommand(npmCommand + " " + npmArgs);
    }

    /**
     * Creates an Angular CLI command with platform-appropriate handling.
     * @param ngArgs The Angular CLI arguments
     * @return CommandLine for ng execution
     */
    public static CommandLine createAngularCommand(String ngArgs) {
        String ngCommand = IS_WINDOWS ? "ng.cmd" : "ng";
        return createShellCommand(ngCommand + " " + ngArgs);
    }
}