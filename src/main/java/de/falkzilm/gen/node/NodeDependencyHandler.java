package de.falkzilm.gen.node;

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
public class NodeDependencyHandler extends DependencyHandler {

    @Override
    protected void frameworkInstall(Dependency dep) {
        Path projectPath = getGenParameters().target().resolve(Path.of(getGenParameters().name()));
        if (dep.version() != null && (dep.name() != null || dep.packageName() != null)) {
            String flag = Boolean.TRUE.equals(dep.extension()) ? " --save-dev " : " --save ";
            String packageName = Optional.ofNullable(dep.packageName()).orElse(dep.name());
            
            ConsoleFormatter.bullet("Installing " + packageName + " with npm install" + flag);
            
            // Build npm install command with proper package@version syntax
            String npmArgs = "install" + flag + packageName + "@" + dep.version();
            CommandLine npmCmd = OsUtils.createNpmCommand(npmArgs);
            
            RunWrapper.builder()
                    .cmd(npmCmd)
                    .build()
                    .run(projectPath, getGenParameters().debug());
        }
    }
}
