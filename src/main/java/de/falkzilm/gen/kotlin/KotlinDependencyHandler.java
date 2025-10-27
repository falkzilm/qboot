package de.falkzilm.gen.kotlin;

import de.falkzilm.exec.RunWrapper;
import de.falkzilm.gen.DependencyHandler;
import de.falkzilm.helper.ConsoleFormatter;
import de.falkzilm.helper.OsUtils;
import de.falkzilm.template.Dependency;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.exec.CommandLine;

import java.nio.file.Path;
import java.util.Optional;

@SuperBuilder
@Getter
public class KotlinDependencyHandler extends DependencyHandler {

    @Override
    protected void frameworkInstall(Dependency dep) {
        Path projectPath = getGenParameters().target().resolve(Path.of(getGenParameters().name()));
        
        if (dep.version() != null && (dep.name() != null || dep.packageName() != null)) {
            String packageName = Optional.ofNullable(dep.packageName()).orElse(dep.name());
            
            ConsoleFormatter.bullet("Adding Gradle dependency: " + packageName);
            
            // Determine if it's a Gradle or Maven project by checking for build files
            boolean isGradleProject = isGradleProject(projectPath);
            boolean isMavenProject = isMavenProject(projectPath);
            
            if (isGradleProject) {
                addGradleDependency(dep, projectPath);
            } else if (isMavenProject) {
                addMavenDependency(dep, projectPath);
            } else {
                // Default to Gradle for Kotlin projects
                ConsoleFormatter.bullet("No build file detected, assuming Gradle project");
                addGradleDependency(dep, projectPath);
            }
        }
    }

    private boolean isGradleProject(Path projectPath) {
        return projectPath.resolve("build.gradle").toFile().exists() || 
               projectPath.resolve("build.gradle.kts").toFile().exists();
    }

    private boolean isMavenProject(Path projectPath) {
        return projectPath.resolve("pom.xml").toFile().exists();
    }

    private void addGradleDependency(Dependency dep, Path projectPath) {
        String packageName = Optional.ofNullable(dep.packageName()).orElse(dep.name());
        String scope = Boolean.TRUE.equals(dep.extension()) ? "testImplementation" : "implementation";
        
        // Build dependency string for Gradle
        String dependencyLine = String.format("%s(\"%s:%s\")", 
            scope, packageName, dep.version());
        
        ConsoleFormatter.bullet("Adding to Gradle: " + dependencyLine);
        
        // For now, we'll use a shell command to append to build.gradle.kts
        // In a real implementation, you might want to parse and modify the build file properly
        String buildFile = projectPath.resolve("build.gradle.kts").toFile().exists() ? 
            "build.gradle.kts" : "build.gradle";
            
        String addDependencyCmd = createAppendToBuildFileCommand(
            projectPath.resolve(buildFile).toString(),
            "    " + dependencyLine
        );
        
        CommandLine gradleCmd = OsUtils.createShellCommand(addDependencyCmd);
        
        RunWrapper.builder()
                .cmd(gradleCmd)
                .build()
                .run(projectPath, getGenParameters().debug());
    }

    private void addMavenDependency(Dependency dep, Path projectPath) {
        String packageName = Optional.ofNullable(dep.packageName()).orElse(dep.name());
        String scope = Boolean.TRUE.equals(dep.extension()) ? "test" : "compile";
        
        // Extract group and artifact from package name
        String groupId = extractGroupId(packageName);
        String artifactId = extractArtifactId(packageName);
        
        ConsoleFormatter.bullet("Adding to Maven: " + groupId + ":" + artifactId + ":" + dep.version());
        
        // Use Maven dependency:resolve to add dependency
        String mavenArgs = String.format(
            "dependency:resolve -Ddependency.groupId=%s -Ddependency.artifactId=%s -Ddependency.version=%s -Ddependency.scope=%s",
            groupId, artifactId, dep.version(), scope
        );
        
        CommandLine mvnCmd = OsUtils.createMavenCommand(mavenArgs);
        
        RunWrapper.builder()
                .cmd(mvnCmd)
                .build()
                .run(projectPath, getGenParameters().debug());
    }

    private String extractGroupId(String packageName) {
        // Handle Maven coordinate format: groupId:artifactId
        if (packageName.contains(":")) {
            return packageName.split(":")[0];
        }
        
        // For Kotlin libraries, try to infer common group IDs
        if (packageName.startsWith("ktor")) {
            return "io.ktor";
        } else if (packageName.startsWith("kotlinx")) {
            return "org.jetbrains.kotlinx";
        } else if (packageName.startsWith("kotlin")) {
            return "org.jetbrains.kotlin";
        }
        
        // Default fallback
        return "org.jetbrains.kotlin";
    }

    private String extractArtifactId(String packageName) {
        // Handle Maven coordinate format: groupId:artifactId
        if (packageName.contains(":")) {
            String[] parts = packageName.split(":");
            return parts.length > 1 ? parts[1] : parts[0];
        }
        
        return packageName;
    }

    private String createAppendToBuildFileCommand(String buildFilePath, String dependencyLine) {
        if (OsUtils.isWindows()) {
            // Windows: append to dependencies block
            return String.format("echo %s >> \"%s\"", dependencyLine, buildFilePath);
        } else {
            // Unix/Linux: append to dependencies block
            return String.format("echo '%s' >> \"%s\"", dependencyLine, buildFilePath);
        }
    }
}