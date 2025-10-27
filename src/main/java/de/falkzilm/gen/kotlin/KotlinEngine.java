package de.falkzilm.gen.kotlin;

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
@FrameworkUsage(Framework.KOTLIN)
public class KotlinEngine implements GenerationEngine {

    @Override
    public Framework framework() {
        return Framework.KOTLIN;
    }

    @Override
    public void generate(Workspace template, GenParameters genParameters) throws Exception {
        ConsoleFormatter.section("Bootstrapping Kotlin project via " + OsUtils.getOsDescription());

        // Create target directory
        ConsoleFormatter.bullet("Creating destination dir: " + genParameters.target().toString());
        CommandLine mkdirCmd = OsUtils.createMkdirCommand(genParameters.target().toString());
        RunWrapper.builder()
                .cmd(mkdirCmd)
                .build()
                .run(Path.of("."), genParameters.debug());

        ConsoleFormatter.bullet("Creating Kotlin project (" + OsUtils.getOsDescription() + ")");
        System.out.println();

        // Determine Kotlin framework type from CLI args
        String kotlinType = determineKotlinType(genParameters.cliArgs());
        
        if (kotlinType.equals("Ktor")) {
            generateKtorProject(genParameters);
        } else if (kotlinType.equals("Spring Boot")) {
            generateKotlinSpringBootProject(genParameters);
        } else {
            generateBasicKotlinProject(genParameters);
        }
    }

    private String determineKotlinType(String cliArgs) {
        if (cliArgs != null && cliArgs.contains("--ktor")) {
            return "Ktor";
        } else if (cliArgs != null && cliArgs.contains("--spring")) {
            return "Spring Boot";
        }
        return "Basic"; // Default to basic Kotlin project
    }

    private void generateKtorProject(GenParameters genParameters) throws Exception {
        // Create Kotlin project directory structure
        createKotlinProjectStructure(genParameters);
        
        // Generate basic Gradle build file for Ktor
        generateKtorGradleBuild(genParameters);
        
        // Generate basic Ktor application
        generateKtorApplication(genParameters);
        
        // Generate configuration files
        generateKtorConfigFiles(genParameters);
    }

    private void generateKotlinSpringBootProject(GenParameters genParameters) throws Exception {
        // Use Spring Initializr for Kotlin + Spring Boot
        String initializrCommand = buildKotlinSpringInitializrCommand(genParameters);
        CommandLine springCmd = OsUtils.createShellCommand(initializrCommand);
        
        RunWrapper.builder()
                .cmd(springCmd)
                .build()
                .run(genParameters.target(), genParameters.debug());

        // Extract the downloaded zip file
        String extractCommand = buildExtractCommand(genParameters);
        CommandLine extractCmd = OsUtils.createShellCommand(extractCommand);
        
        RunWrapper.builder()
                .cmd(extractCmd)
                .build()
                .run(genParameters.target(), genParameters.debug());
    }

    private void generateBasicKotlinProject(GenParameters genParameters) throws Exception {
        // Create basic Kotlin project with Gradle
        createKotlinProjectStructure(genParameters);
        generateBasicGradleBuild(genParameters);
        generateBasicKotlinMain(genParameters);
    }

    private void createKotlinProjectStructure(GenParameters genParameters) {
        String projectPath = genParameters.target().resolve(genParameters.name()).toString();
        
        // Create standard Kotlin/Gradle directory structure
        String[] dirs = {
            "src/main/kotlin",
            "src/main/resources", 
            "src/test/kotlin",
            "src/test/resources",
            "gradle/wrapper"
        };
        
        for (String dir : dirs) {
            CommandLine mkdirCmd = OsUtils.createMkdirCommand(projectPath + "/" + dir);
            RunWrapper.builder()
                    .cmd(mkdirCmd)
                    .build()
                    .run(genParameters.target(), genParameters.debug());
        }
    }

    private void generateKtorGradleBuild(GenParameters genParameters) {
        String projectPath = genParameters.target().resolve(genParameters.name()).toString();
        String buildGradleContent = createKtorBuildGradleContent(genParameters);
        
        String createBuildCmd = createFileCommand(projectPath + "/build.gradle.kts", buildGradleContent);
        CommandLine buildCmd = OsUtils.createShellCommand(createBuildCmd);
        RunWrapper.builder()
                .cmd(buildCmd)
                .build()
                .run(genParameters.target(), genParameters.debug());
    }

    private void generateBasicGradleBuild(GenParameters genParameters) {
        String projectPath = genParameters.target().resolve(genParameters.name()).toString();
        String buildGradleContent = createBasicBuildGradleContent(genParameters);
        
        String createBuildCmd = createFileCommand(projectPath + "/build.gradle.kts", buildGradleContent);
        CommandLine buildCmd = OsUtils.createShellCommand(createBuildCmd);
        RunWrapper.builder()
                .cmd(buildCmd)
                .build()
                .run(genParameters.target(), genParameters.debug());
    }

    private void generateKtorApplication(GenParameters genParameters) {
        String projectPath = genParameters.target().resolve(genParameters.name()).toString();
        String packagePath = genParameters.packageName() != null ? 
            genParameters.packageName().replace(".", "/") : "com/example";
        
        String applicationContent = createKtorApplicationContent(genParameters);
        String appPath = String.format("%s/src/main/kotlin/%s/Application.kt", projectPath, packagePath);
        
        // Create package directories
        CommandLine mkdirCmd = OsUtils.createMkdirCommand(projectPath + "/src/main/kotlin/" + packagePath);
        RunWrapper.builder()
                .cmd(mkdirCmd)
                .build()
                .run(genParameters.target(), genParameters.debug());
        
        String createAppCmd = createFileCommand(appPath, applicationContent);
        CommandLine appCmd = OsUtils.createShellCommand(createAppCmd);
        RunWrapper.builder()
                .cmd(appCmd)
                .build()
                .run(genParameters.target(), genParameters.debug());
    }

    private void generateBasicKotlinMain(GenParameters genParameters) {
        String projectPath = genParameters.target().resolve(genParameters.name()).toString();
        String packagePath = genParameters.packageName() != null ? 
            genParameters.packageName().replace(".", "/") : "com/example";
        
        String mainContent = createBasicKotlinMainContent(genParameters);
        String mainPath = String.format("%s/src/main/kotlin/%s/Main.kt", projectPath, packagePath);
        
        // Create package directories
        CommandLine mkdirCmd = OsUtils.createMkdirCommand(projectPath + "/src/main/kotlin/" + packagePath);
        RunWrapper.builder()
                .cmd(mkdirCmd)
                .build()
                .run(genParameters.target(), genParameters.debug());
        
        String createMainCmd = createFileCommand(mainPath, mainContent);
        CommandLine mainCmd = OsUtils.createShellCommand(createMainCmd);
        RunWrapper.builder()
                .cmd(mainCmd)
                .build()
                .run(genParameters.target(), genParameters.debug());
    }

    private void generateKtorConfigFiles(GenParameters genParameters) {
        String projectPath = genParameters.target().resolve(genParameters.name()).toString();
        
        // Generate application.conf
        String configContent = createKtorConfigContent(genParameters);
        String createConfigCmd = createFileCommand(projectPath + "/src/main/resources/application.conf", configContent);
        CommandLine configCmd = OsUtils.createShellCommand(createConfigCmd);
        RunWrapper.builder()
                .cmd(configCmd)
                .build()
                .run(genParameters.target(), genParameters.debug());
        
        // Generate logback.xml
        String logbackContent = createLogbackContent();
        String createLogbackCmd = createFileCommand(projectPath + "/src/main/resources/logback.xml", logbackContent);
        CommandLine logbackCmd = OsUtils.createShellCommand(createLogbackCmd);
        RunWrapper.builder()
                .cmd(logbackCmd)
                .build()
                .run(genParameters.target(), genParameters.debug());
    }

    private String createKtorBuildGradleContent(GenParameters genParameters) {
        String cliArgs = genParameters.cliArgs() != null ? genParameters.cliArgs() : "";
        String kotlinVersion = parseKotlinVersion(cliArgs);
        String ktorVersion = parseKtorVersion(cliArgs);
        
        return String.format("""
val kotlin_version: String by project
val ktor_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "%s"
    id("io.ktor.plugin") version "%s"
    id("org.jetbrains.kotlin.plugin.serialization") version "%s"
}

group = "%s"
version = "0.0.1"

application {
    mainClass.set("%s.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${'$'}isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-host-common-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:${'$'}logback_version")
    implementation("io.ktor:ktor-server-config-yaml")
    
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:${'$'}kotlin_version")
}
                """, 
                kotlinVersion, ktorVersion, kotlinVersion, 
                genParameters.packageName() != null ? genParameters.packageName() : "com.example",
                genParameters.packageName() != null ? genParameters.packageName() : "com.example");
    }

    private String createBasicBuildGradleContent(GenParameters genParameters) {
        String kotlinVersion = parseKotlinVersion(genParameters.cliArgs());
        
        return String.format("""
plugins {
    kotlin("jvm") version "%s"
    application
}

group = "%s"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    mainClass.set("%s.MainKt")
}

kotlin {
    jvmToolchain(21)
}
                """, 
                kotlinVersion,
                genParameters.packageName() != null ? genParameters.packageName() : "com.example",
                genParameters.packageName() != null ? genParameters.packageName() : "com.example");
    }

    private String createKtorApplicationContent(GenParameters genParameters) {
        String packageName = genParameters.packageName() != null ? genParameters.packageName() : "com.example";
        
        return String.format("""
package %s

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    
    routing {
        get("/") {
            call.respond(mapOf("message" to "Hello from %s!", "status" to "running"))
        }
        
        get("/health") {
            call.respond(HealthResponse("OK", System.currentTimeMillis()))
        }
    }
}

@Serializable
data class HealthResponse(val status: String, val timestamp: Long)
                """, 
                packageName, genParameters.name());
    }

    private String createBasicKotlinMainContent(GenParameters genParameters) {
        String packageName = genParameters.packageName() != null ? genParameters.packageName() : "com.example";
        
        return String.format("""
package %s

fun main() {
    println("Hello from %s!")
    println("Welcome to Kotlin development with qBoot")
    
    // Example of Kotlin features
    val numbers = listOf(1, 2, 3, 4, 5)
    val doubled = numbers.map { it * 2 }
    
    println("Original numbers: ${'$'}numbers")
    println("Doubled numbers: ${'$'}doubled")
    
    // Null safety example
    val nullableString: String? = null
    println("Safe length: ${'$'}{nullableString?.length ?: 0}")
}
                """, 
                packageName, genParameters.name());
    }

    private String createKtorConfigContent(GenParameters genParameters) {
        return """
ktor {
    deployment {
        port = 8080
        port = ${?PORT}
        
        watch = [ classes, resources ]
    }
    
    application {
        modules = [ ApplicationKt.module ]
    }
}
                """;
    }

    private String createLogbackContent() {
        return """
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{YYYY-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="STDOUT"/>
    </root>
    
    <logger name="io.ktor" level="DEBUG"/>
    <logger name="io.netty" level="INFO"/>
</configuration>
                """;
    }

    // Helper methods for Spring Boot Kotlin support
    private String buildKotlinSpringInitializrCommand(GenParameters genParameters) {
        String projectName = genParameters.name();
        String packageName = genParameters.packageName() != null ? 
            genParameters.packageName() : "com.example." + projectName.toLowerCase();
        String targetDir = genParameters.target().toString();
        
        StringBuilder url = new StringBuilder("https://start.spring.io/starter.zip?");
        url.append("type=gradle-project");
        url.append("&language=kotlin");
        url.append("&bootVersion=3.4.1");
        url.append("&baseDir=").append(projectName);
        url.append("&groupId=").append(extractGroupId(packageName));
        url.append("&artifactId=").append(projectName.toLowerCase());
        url.append("&name=").append(projectName);
        url.append("&description=Kotlin%20Spring%20Boot%20project%20for%20").append(projectName);
        url.append("&packageName=").append(packageName);
        url.append("&packaging=jar");
        url.append("&javaVersion=21");
        url.append("&dependencies=web,data-jpa,h2");
        
        return String.format("curl -o %s/%s.zip '%s'", targetDir, projectName, url.toString());
    }

    private String buildExtractCommand(GenParameters genParameters) {
        String projectName = genParameters.name();
        String targetDir = genParameters.target().toString();
        
        if (OsUtils.isWindows()) {
            return String.format("powershell Expand-Archive -Path %s\\%s.zip -DestinationPath %s && del %s\\%s.zip", 
                targetDir, projectName, targetDir, targetDir, projectName);
        } else {
            return String.format("cd %s && unzip %s.zip && rm %s.zip", 
                targetDir, projectName, projectName);
        }
    }

    private String extractGroupId(String packageName) {
        int lastDot = packageName.lastIndexOf('.');
        if (lastDot > 0) {
            return packageName.substring(0, lastDot);
        }
        return packageName;
    }

    private String parseKotlinVersion(String cliArgs) {
        if (cliArgs != null && cliArgs.contains("--kotlin-version=")) {
            String version = cliArgs.substring(cliArgs.indexOf("--kotlin-version=") + 17);
            int endIndex = version.indexOf(" ");
            if (endIndex > 0) {
                version = version.substring(0, endIndex);
            }
            return version;
        }
        return "2.1.0"; // Latest stable Kotlin version
    }

    private String parseKtorVersion(String cliArgs) {
        if (cliArgs != null && cliArgs.contains("--ktor-version=")) {
            String version = cliArgs.substring(cliArgs.indexOf("--ktor-version=") + 15);
            int endIndex = version.indexOf(" ");
            if (endIndex > 0) {
                version = version.substring(0, endIndex);
            }
            return version;
        }
        return "3.0.3"; // Latest stable Ktor version
    }

    private String createFileCommand(String filePath, String content) {
        if (OsUtils.isWindows()) {
            return String.format("echo \"%s\" > \"%s\"", content.replace("\"", "\\\""), filePath);
        } else {
            return String.format("cat > \"%s\" << 'EOF'\n%s\nEOF", filePath, content);
        }
    }

    @Override
    public DependencyHandler createDependencyHandlerFor(GenParameters genParameters, List<Dependency> dependencyList) {
        return KotlinDependencyHandler.builder()
                .dependencies(dependencyList)
                .genParameters(genParameters)
                .build();
    }
}