package de.falkzilm.gen.node;

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
@FrameworkUsage(Framework.ANGULAR)
public class AngularEngine implements GenerationEngine {

    @Override
    public Framework framework() {
        return Framework.ANGULAR;
    }

    @Override
    public void generate(Workspace template, GenParameters genParameters) throws Exception {
        ConsoleFormatter.section("Bootstrapping Angular project via " + OsUtils.getOsDescription());

        Path tempNpmDir = genParameters.target().resolve("temp-npm");

        // Create temporary directory for npm install
        ConsoleFormatter.bullet("Creating temporary directory: " + tempNpmDir);
        CommandLine mkdirCmd = OsUtils.createMkdirCommand(tempNpmDir.toString());
        RunWrapper.builder()
                .cmd(mkdirCmd)
                .build()
                .run(Path.of("."), genParameters.debug());

        // Install Angular CLI in temp directory
        ConsoleFormatter.bullet("Installing @angular/cli@" + genParameters.frameworkVersion());
        String angularCliVersion = genParameters.frameworkVersion();
        String npmInstallArgs = "install @angular/cli@" + angularCliVersion;
        
        CommandLine npmInstallCmd = OsUtils.createNpmCommand(npmInstallArgs);
        RunWrapper.builder()
                .cmd(npmInstallCmd)
                .build()
                .run(tempNpmDir, genParameters.debug());

        // Create Angular project using the locally installed CLI
        ConsoleFormatter.bullet("Creating Angular project with ng new");
        String ngArgs = "new --create-application --defaults " + genParameters.name();
        if (genParameters.cliArgs() != null && !genParameters.cliArgs().trim().isEmpty()) {
            ngArgs += " " + genParameters.cliArgs();
        }

        // Use the locally installed ng binary
        String ngPath = OsUtils.isWindows() 
            ? "temp-npm\\node_modules\\.bin\\ng.cmd"
            : "temp-npm/node_modules/.bin/ng";
        
        CommandLine ngCmd = OsUtils.createShellCommand(ngPath + " " + ngArgs);
        RunWrapper.builder()
                .cmd(ngCmd)
                .build()
                .run(genParameters.target(), genParameters.debug());

        // Clean up temporary directory
        ConsoleFormatter.bullet("Cleaning up temporary directory");
        String cleanupCommand = OsUtils.isWindows() 
            ? "rmdir /s /q \"" + tempNpmDir + "\""
            : "rm -rf \"" + tempNpmDir + "\"";
        
        CommandLine cleanupCmd = OsUtils.createShellCommand(cleanupCommand);
        RunWrapper.builder()
                .cmd(cleanupCmd)
                .build()
                .run(genParameters.target(), genParameters.debug());
        
        System.out.println();
    }

    @Override
    public DependencyHandler createDependencyHandlerFor(GenParameters genParameters, List<Dependency> dependencyList) {
        return NodeDependencyHandler.builder()
                .dependencies(dependencyList)
                .genParameters(genParameters)
                .build();
    }
}
