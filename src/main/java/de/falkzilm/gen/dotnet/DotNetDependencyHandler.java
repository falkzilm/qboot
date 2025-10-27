package de.falkzilm.gen.dotnet;

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
public class DotNetDependencyHandler extends DependencyHandler {

    @Override
    protected void frameworkInstall(Dependency dep) {
        Path projectPath = getGenParameters().target().resolve(Path.of(getGenParameters().name()));
        
        if (dep.version() != null && (dep.name() != null || dep.packageName() != null)) {
            String packageName = Optional.ofNullable(dep.packageName()).orElse(dep.name());
            
            ConsoleFormatter.bullet("Installing " + packageName + " with dotnet add package");
            
            // Build dotnet add package command
            String dotnetArgs = String.format("add package %s --version %s", 
                packageName, dep.version());
            
            CommandLine dotnetCmd = OsUtils.createShellCommand("dotnet " + dotnetArgs);
            
            RunWrapper.builder()
                    .cmd(dotnetCmd)
                    .build()
                    .run(projectPath, getGenParameters().debug());
        }
    }
}