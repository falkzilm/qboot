package de.falkzilm.gen.java;

import de.falkzilm.exec.RunWrapper;
import de.falkzilm.gen.*;
import de.falkzilm.helper.ConsoleFormatter;
import de.falkzilm.helper.OsUtils;
import de.falkzilm.template.Dependency;
import de.falkzilm.template.Workspace;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.exec.CommandLine;

import java.nio.file.Path;
import java.util.List;

@ApplicationScoped
@FrameworkUsage(Framework.SPRINGBOOT)
public class SpringBootEngine implements GenerationEngine {

    @Override
    public Framework framework() {
        return Framework.SPRINGBOOT;
    }

    @Override
    public void generate(Workspace template, GenParameters genParameters) throws Exception {
        ConsoleFormatter.section("Bootstrapping Spring Boot project via " + OsUtils.getOsDescription());

        // Create target directory
        ConsoleFormatter.bullet("Creating destination dir: " + genParameters.target().toString());
        CommandLine mkdirCmd = OsUtils.createMkdirCommand(genParameters.target().toString());
        RunWrapper.builder()
                .cmd(mkdirCmd)
                .build()
                .run(Path.of("."), genParameters.debug());

        ConsoleFormatter.bullet("Creating Spring Boot project from Spring Initializr (" + OsUtils.getOsDescription() + ")");
        System.out.println();

        // Generate Spring Boot project using Spring Initializr
        String curlCommand = buildSpringInitializrCommand(genParameters);
        CommandLine springCmd = OsUtils.createShellCommand(curlCommand);
        
        RunWrapper.builder()
                .cmd(springCmd)
                .build()
                .run(genParameters.target(), genParameters.debug());

        // Extract the downloaded zip file
        String extractCommand = buildExtractCommand(genParameters);
        CommandLine extractCmd = OsUtils.createShellCommand(extractCommand);
        
        ConsoleFormatter.bullet("Extracting project files...");
        RunWrapper.builder()
                .cmd(extractCmd)
                .build()
                .run(genParameters.target(), genParameters.debug());
    }

    private String buildSpringInitializrCommand(GenParameters genParameters) {
        String projectName = genParameters.name();
        String packageName = genParameters.packageName() != null ? 
            genParameters.packageName() : "com.example." + projectName.toLowerCase();
        String cliArgs = genParameters.cliArgs() != null ? genParameters.cliArgs() : "";
        String targetDir = genParameters.target().toString();
        
        // Parse dependencies from CLI args
        String dependencies = parseDependencies(cliArgs);
        String javaVersion = parseJavaVersion(cliArgs);
        String springBootVersion = parseSpringBootVersion(cliArgs);
        String packaging = parsePackaging(cliArgs);
        
        // Build Spring Initializr URL
        StringBuilder url = new StringBuilder("https://start.spring.io/starter.zip?");
        url.append("type=maven-project");
        url.append("&language=java");
        url.append("&bootVersion=").append(springBootVersion);
        url.append("&baseDir=").append(projectName);
        url.append("&groupId=").append(extractGroupId(packageName));
        url.append("&artifactId=").append(projectName.toLowerCase());
        url.append("&name=").append(projectName);
        url.append("&description=Spring%20Boot%20project%20for%20").append(projectName);
        url.append("&packageName=").append(packageName);
        url.append("&packaging=").append(packaging);
        url.append("&javaVersion=").append(javaVersion);
        url.append("&dependencies=").append(dependencies);
        
        // Curl command to download and save
        return String.format("curl -o %s/%s.zip '%s'", 
            targetDir, projectName, url.toString());
    }

    private String buildExtractCommand(GenParameters genParameters) {
        String projectName = genParameters.name();
        String targetDir = genParameters.target().toString();
        
        if (OsUtils.isWindows()) {
            // Windows PowerShell command
            return String.format("powershell Expand-Archive -Path %s\\%s.zip -DestinationPath %s && del %s\\%s.zip", 
                targetDir, projectName, targetDir, targetDir, projectName);
        } else {
            // Unix/Linux command
            return String.format("cd %s && unzip %s.zip && rm %s.zip", 
                targetDir, projectName, projectName);
        }
    }

    private String parseDependencies(String cliArgs) {
        if (cliArgs == null || cliArgs.trim().isEmpty()) {
            return "web,data-jpa,h2"; // Default dependencies
        }
        
        // Extract dependencies from --deps= parameter
        if (cliArgs.contains("--deps=")) {
            String deps = cliArgs.substring(cliArgs.indexOf("--deps=") + 7);
            int endIndex = deps.indexOf(" ");
            if (endIndex > 0) {
                deps = deps.substring(0, endIndex);
            }
            return deps.replace(" ", "");
        }
        
        return "web,data-jpa,h2";
    }

    private String parseJavaVersion(String cliArgs) {
        if (cliArgs != null && cliArgs.contains("--java=")) {
            String version = cliArgs.substring(cliArgs.indexOf("--java=") + 7);
            int endIndex = version.indexOf(" ");
            if (endIndex > 0) {
                version = version.substring(0, endIndex);
            }
            return version;
        }
        return "21"; // Default to Java 21
    }

    private String parseSpringBootVersion(String cliArgs) {
        if (cliArgs != null && cliArgs.contains("--boot-version=")) {
            String version = cliArgs.substring(cliArgs.indexOf("--boot-version=") + 15);
            int endIndex = version.indexOf(" ");
            if (endIndex > 0) {
                version = version.substring(0, endIndex);
            }
            return version;
        }
        return "3.4.1"; // Latest stable version
    }

    private String parsePackaging(String cliArgs) {
        if (cliArgs != null && cliArgs.contains("--packaging=")) {
            String packaging = cliArgs.substring(cliArgs.indexOf("--packaging=") + 12);
            int endIndex = packaging.indexOf(" ");
            if (endIndex > 0) {
                packaging = packaging.substring(0, endIndex);
            }
            return packaging;
        }
        return "jar"; // Default packaging
    }

    private String extractGroupId(String packageName) {
        // Extract group ID from package name (e.g., com.example.myapp -> com.example)
        int lastDot = packageName.lastIndexOf('.');
        if (lastDot > 0) {
            return packageName.substring(0, lastDot);
        }
        return packageName;
    }

    @Override
    public DependencyHandler createDependencyHandlerFor(GenParameters genParameters, List<Dependency> dependencyList) {
        return MavenDependencyHandler.builder()
                .dependencies(dependencyList)
                .genParameters(genParameters)
                .build();
    }
}