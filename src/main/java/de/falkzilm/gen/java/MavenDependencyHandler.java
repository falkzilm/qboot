package de.falkzilm.gen.java;

import de.falkzilm.exec.RunWrapper;
import de.falkzilm.gen.DependencyHandler;
import de.falkzilm.helper.ConsoleFormatter;
import de.falkzilm.helper.OsUtils;
import de.falkzilm.template.Dependency;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.exec.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuperBuilder
@Getter
public class MavenDependencyHandler extends DependencyHandler {

    @Override
    protected void frameworkInstall(Dependency dep) {
        Path projectPath = getGenParameters().target().resolve(Path.of(getGenParameters().name()));
        if (dep.extension()) {
            ConsoleFormatter.bullet("Installing " + Optional.ofNullable(dep.packageName()).orElse(dep.name()) + " with quarkus:add-extension");
            
            String mavenArgs = "quarkus:add-extension -Dextensions=" + dep.name();
            CommandLine mvnCmd = OsUtils.createMavenCommand(mavenArgs);
            
            RunWrapper.builder()
                    .cmd(mvnCmd)
                    .build()
                    .run(projectPath, getGenParameters().debug());
        } else {
            Path pomPath = projectPath.resolve("pom.xml");
            String[] depGroupArtifcat = Optional.ofNullable(dep.packageName()).orElse(dep.name()).split(":");
            String dependencyEntry = "        <dependency>\n" +
                    "            <groupId>" + depGroupArtifcat[0] + "</groupId>\n" +
                    "            <artifactId>" + depGroupArtifcat[1] + "</artifactId>\n" +
                    "            <version>" + dep.version() + "</version>\n" +
                    "        </dependency>";

            try {
                String pomContent = new String(java.nio.file.Files.readAllBytes(pomPath));
                Pattern dependencyPattern = Pattern.compile(
                        "<dependencies>\\s*<dependency>\\s*<groupId>io.quarkus</groupId>\\s*<artifactId>quarkus-\\w*</artifactId>\\s*</dependency>"
                );
                Matcher depMatcher = dependencyPattern.matcher(pomContent);
                if (depMatcher.find()) {
                    // should always be the case
                    if (!pomContent.contains("<artifactId>" + depGroupArtifcat[1] + "</artifactId")) {
                        String replaceDep = depMatcher.group() + "\n" + dependencyEntry;
                        pomContent = depMatcher.replaceFirst(replaceDep);
                        ConsoleFormatter.bullet("Dependency " + depGroupArtifcat[0] + ":" + depGroupArtifcat[1] + " added to pom.xml");
                    } else {
                        ConsoleFormatter.bullet("Dependency " + depGroupArtifcat[0] + ":" + depGroupArtifcat[1] + " already exists in pom.xml");
                        return;
                    }
                } else {
                    ConsoleFormatter.error("POM.xml misformat", "Could not reliable detect dependency section", null);
                }

                // Define the pattern to match the first occurrence of maven-compiler-plugin
                if (dep.name().contains("lombok") || dep.name().contains("mapstruct-processor") || dep.name().contains("processor")) {
                    String newApPaths = "                        <path>\n" +
                            "                            <groupId>" + depGroupArtifcat[0] + "</groupId>\n" +
                            "                            <artifactId>" + depGroupArtifcat[1] + "</artifactId>\n" +
                            "                            <version>" + dep.version() + "</version>\n" +
                            "                        </path>";
                    // --- 1) Case A: append into existing <annotationProcessorPaths> ---
                    // Capture groups:
                    // 1 = prefix up to <annotationProcessorPaths>
                    // 2 = existing content inside <annotationProcessorPaths>
                    // 3 = tail from </annotationProcessorPaths> to the end of the plugin
                    Pattern hasApt = Pattern.compile(
                            "(?s)(<plugin>\\s*(?:<groupId>.*?</groupId>\\s*)?" +
                                    "<artifactId>\\s*maven-compiler-plugin\\s*</artifactId>.*?<configuration>.*?" +
                                    "<annotationProcessorPaths>)(.*?)(</annotationProcessorPaths>.*?</configuration>.*?</plugin>)"
                    );
                    Matcher m1 = hasApt.matcher(pomContent);
                    if (m1.find()) {
                        // Try to detect indentation from existing content; if empty, use 10 spaces as a safe default
                        String existing = m1.group(2);
                        // Build replacement: $1 + existing + newPath + $3
                        String replacement = m1.group(1) + existing + "\n" + newApPaths + "\n" + m1.group(3);
                        pomContent = m1.replaceFirst(Matcher.quoteReplacement(replacement));
                    }

                    // --- 2) Case B: create a new <annotationProcessorPaths> block inside <configuration> ---
                    // Capture groups:
                    // 1 = prefix up to <configuration>
                    // 2 = content inside <configuration>
                    // 3 = whitespace before </configuration>
                    // 4 = tail from </configuration> to </plugin>
                    Pattern noApt = Pattern.compile(
                            "(?s)(<plugin>\\s*(?:<groupId>.*?</groupId>\\s*)?" +
                                    "<artifactId>\\s*maven-compiler-plugin\\s*</artifactId>.*?<configuration>)(.*?)" +
                                    "(\\s*)(</configuration>\\s*.*?</plugin>)"
                    );
                    Matcher m2 = noApt.matcher(pomContent);
                    if (m2.find()) {
                        String block = String.join("\n",
                                "                    <annotationProcessorPaths>",
                                newApPaths,
                                "                    </annotationProcessorPaths>"
                        );

                        String replacement = Matcher.quoteReplacement(m2.group(1) + m2.group(2) + "\n" + block + m2.group(3) + m2.group(4));
                        pomContent = m2.replaceFirst(replacement);
                    }
                }

                java.nio.file.Files.write(pomPath, pomContent.getBytes());

            } catch (IOException e) {
                ConsoleFormatter.error(
                        "Write failure",
                        "Failed to edit pom.xml: " + e.getMessage(), e.getCause(),
                        "Make sure destination path is writeable",
                        "Run with debug flag for verbose information"
                );
            }
        }
    }

    // Very small helper: try to infer indent level from existing inner content
    private String detectInnerIndent(String existing, String tag, String fallback) {
        // find a line that already contains <path> (or any inner line),
        // else derive from the end of <annotationProcessorPaths>
        Matcher line = Pattern.compile("(?m)^(\\s*)<" + tag + ">").matcher(existing);
        if (line.find()) return line.group(1);
        Matcher any = Pattern.compile("(?m)^(\\s*)\\S").matcher(existing);
        if (any.find()) return any.group(1);
        return fallback;
    }
}
