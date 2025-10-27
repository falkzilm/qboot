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
@FrameworkUsage(Framework.VUE)
public class VueEngine implements GenerationEngine {

    @Override
    public Framework framework() {
        return Framework.VUE;
    }

    @Override
    public void generate(Workspace template, GenParameters genParameters) throws Exception {
        ConsoleFormatter.section("Bootstrapping Vue.js project via " + OsUtils.getOsDescription());

        // Create target directory
        ConsoleFormatter.bullet("Creating destination dir: " + genParameters.target().toString());
        CommandLine mkdirCmd = OsUtils.createMkdirCommand(genParameters.target().toString());
        RunWrapper.builder()
                .cmd(mkdirCmd)
                .build()
                .run(Path.of("."), genParameters.debug());

        ConsoleFormatter.bullet("Creating Vue.js project (" + OsUtils.getOsDescription() + ")");
        System.out.println();

        // Generate Vue project using Vue CLI or Vite
        String createCommand = buildCreateCommand(genParameters);
        CommandLine vueCmd = OsUtils.createNpmCommand(createCommand);
        
        RunWrapper.builder()
                .cmd(vueCmd)
                .build()
                .run(genParameters.target(), genParameters.debug());
    }

    private String buildCreateCommand(GenParameters genParameters) {
        String projectName = genParameters.name();
        String cliArgs = genParameters.cliArgs() != null ? genParameters.cliArgs() : "";
        
        // Determine if using Vite or Vue CLI
        if (cliArgs.contains("--vite") || cliArgs.contains("--vue3")) {
            // Use create-vue (official Vue 3 + Vite template)
            return "create-vue@latest " + projectName + " " + 
                   filterCreateVueArgs(cliArgs);
        } else {
            // Use Vue CLI
            return "vue create " + projectName + " " + 
                   filterVueCliArgs(cliArgs);
        }
    }

    private String filterCreateVueArgs(String cliArgs) {
        if (cliArgs == null) return "--typescript --router --pinia --eslint";
        
        // Remove our custom flags and pass through valid create-vue options
        return cliArgs.replace("--vite", "")
                     .replace("--vue3", "")
                     .trim();
    }

    private String filterVueCliArgs(String cliArgs) {
        if (cliArgs == null) return "--default";
        
        // Remove our custom flags and pass through valid Vue CLI options
        return cliArgs.replace("--vite", "")
                     .replace("--vue3", "")
                     .trim();
    }

    @Override
    public DependencyHandler createDependencyHandlerFor(GenParameters genParameters, List<Dependency> dependencyList) {
        return NodeDependencyHandler.builder()
                .dependencies(dependencyList)
                .genParameters(genParameters)
                .build();
    }
}