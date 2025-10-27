package de.falkzilm.gen;

import de.falkzilm.exec.RunWrapper;
import de.falkzilm.helper.ConsoleFormatter;
import de.falkzilm.template.Dependency;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.apache.commons.exec.CommandLine;

import java.nio.file.Path;
import java.util.List;

@SuperBuilder
@Data
public class DependencyHandler {
    private List<Dependency> dependencies;
    private GenParameters genParameters;

    public void check() {
        ConsoleFormatter.section("PreRequisites:");
        for(Dependency dep : this.dependencies) {
            var checkItemConsole = ConsoleFormatter.beginCheck(dep.name(), dep.optional(), dep.version());
            checkItemConsole.printStart();
            var cmd = new CommandLine("bash")
                    .addArgument("-lc")
                    .addArgument(dep.name() + " --version", false);
            var runner = RunWrapper.builder().cmd(cmd).build();
            var result = runner.run(Path.of("."), genParameters.debug());

            if (genParameters.debug()) {
                System.out.println();
                ConsoleFormatter.debug("Execution return", dep.name() + " --version [" + result + "]");
                ConsoleFormatter.debug("Output", runner.getOutput());
            }

            if (result > 0 && !dep.optional()) {
                checkItemConsole.fail("Not found");
                System.exit(1);
            } else if (result > 0) {
                checkItemConsole.fail("Not found");
            } else {
                var output = runner.getOutput().trim();
                String requiredVersion = dep.version();

                String actualVersion = getActualStringForDep(dep.name(), output);
                checkItemConsole.detected(actualVersion);

                if (requiredVersion.endsWith("+")) {
                    if (actualVersion != null && !actualVersion.isBlank()) {
                        int requiredMajorVersion = Integer.parseInt(requiredVersion.replace("+", ""));
                        int actualMajorVersion = Integer.parseInt(actualVersion);

                        if (requiredMajorVersion > actualMajorVersion) {
                            checkItemConsole.fail("Greater version is needed");
                            System.exit(1);
                        } else {
                            checkItemConsole.ok();
                        }
                    } else {
                        checkItemConsole.fail("Failed to extract version information");
                    }
                } else {
                    if (!output.contains(dep.version()) && !dep.optional()) {
                        checkItemConsole.fail("Specific version was requested and is not meet");
                        System.exit(1);
                    } else if (dep.optional()) {
                        checkItemConsole.fail("Not right version");
                    } else {
                        checkItemConsole.ok();
                    }
                }
            }
            System.out.println();
        }
    }

    public void install() {
        ConsoleFormatter.section("Installing dependencies");
        for(Dependency dep : this.dependencies) {
            this.frameworkInstall(dep);
        }
        System.out.println();
    }

    protected void frameworkInstall(Dependency dep){
        throw new IllegalArgumentException("Needs implementation");
    }

    private String getActualStringForDep(String dep, String output) {
        return switch (dep) {
            case "java" -> output.split(" ")[1].split("\\.")[0];
            case "mvn" -> output.split(" ")[2].split("\\.")[0];
            case "node" -> output.replace("v", "").split("\\.")[0];
            default -> output.split("\\.")[0];
        };
    }
}
