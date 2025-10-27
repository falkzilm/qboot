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
@FrameworkUsage(Framework.QUARKUS)
public class QuarkusEngine implements GenerationEngine {

    @Override
    public Framework framework() {
        return Framework.QUARKUS;
    }

    @Override
    public void generate(Workspace template, GenParameters genParameters) throws Exception {
        ConsoleFormatter.section("Bootstrapping default structure via " + OsUtils.getOsDescription());

        // Create target directory with OS-appropriate command
        ConsoleFormatter.bullet("Creating destination dir: " + genParameters.target().toString());
        CommandLine mkdirCmd = OsUtils.createMkdirCommand(genParameters.target().toString());
        RunWrapper.builder()
                .cmd(mkdirCmd)
                .build()
                .run(Path.of("."), genParameters.debug());

        ConsoleFormatter.bullet("Calling Maven Quarkus plugin (" + OsUtils.getOsDescription() + ")");
        System.out.println();

        // Build Maven command with OS-appropriate handling
        String mavenArgs = "io.quarkus.platform:quarkus-maven-plugin:" +
                genParameters.frameworkVersion() +
                ":create -DprojectGroupId=" +
                genParameters.packageName() +
                " -DprojectArtifactId=" + genParameters.name();

        if (genParameters.cliArgs() != null && !genParameters.cliArgs().trim().isEmpty()) {
            mavenArgs += " " + genParameters.cliArgs();
        }

        CommandLine mavenCmd = OsUtils.createMavenCommand(mavenArgs);
        RunWrapper.builder()
                .cmd(mavenCmd)
                .build()
                .run(genParameters.target(), genParameters.debug());
    }

    @Override
    public DependencyHandler createDependencyHandlerFor(GenParameters genParameters, List<Dependency> dependencyList) {
        return MavenDependencyHandler.builder()
                .dependencies(dependencyList)
                .genParameters(genParameters)
                .build();
    }
}
