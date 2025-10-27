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
@FrameworkUsage(Framework.REACT)
public class ReactEngine implements GenerationEngine {

    @Override
    public Framework framework() {
        return Framework.REACT;
    }

    @Override
    public void generate(Workspace template, GenParameters genParameters) throws Exception {
        ConsoleFormatter.section("Bootstrapping React project via " + OsUtils.getOsDescription());

        // Create target directory
        ConsoleFormatter.bullet("Creating destination dir: " + genParameters.target().toString());
        CommandLine mkdirCmd = OsUtils.createMkdirCommand(genParameters.target().toString());
        RunWrapper.builder()
                .cmd(mkdirCmd)
                .build()
                .run(Path.of("."), genParameters.debug());

        // Determine React framework type from CLI args or default
        String reactType = determineReactType(genParameters.cliArgs());
        
        ConsoleFormatter.bullet("Creating " + reactType + " project (" + OsUtils.getOsDescription() + ")");
        System.out.println();

        // Generate React project
        String createCommand = buildCreateCommand(reactType, genParameters);
        CommandLine reactCmd = OsUtils.createNpmCommand(createCommand);
        
        RunWrapper.builder()
                .cmd(reactCmd)
                .build()
                .run(genParameters.target(), genParameters.debug());
    }

    private String determineReactType(String cliArgs) {
        if (cliArgs != null && cliArgs.contains("--nextjs")) {
            return "Next.js";
        } else if (cliArgs != null && cliArgs.contains("--vite")) {
            return "React + Vite";
        }
        return "React";
    }

    private String buildCreateCommand(String reactType, GenParameters genParameters) {
        String projectName = genParameters.name();
        String cliArgs = genParameters.cliArgs() != null ? genParameters.cliArgs() : "";
        
        if (reactType.equals("Next.js")) {
            // Next.js project
            return "create next-app@latest " + projectName + " " + 
                   filterNextJsArgs(cliArgs);
        } else if (reactType.equals("React + Vite")) {
            // Vite React project  
            return "create vite@latest " + projectName + " -- --template react " +
                   filterViteArgs(cliArgs);
        } else {
            // Standard Create React App
            return "create-react-app " + projectName + " " + 
                   filterCreateReactAppArgs(cliArgs);
        }
    }

    private String filterNextJsArgs(String cliArgs) {
        if (cliArgs == null) return "--typescript --tailwind --eslint --app";
        
        // Remove our custom flags and pass through valid Next.js options
        return cliArgs.replace("--nextjs", "")
                     .trim();
    }

    private String filterViteArgs(String cliArgs) {
        if (cliArgs == null) return "";
        
        // Remove our custom flags and pass through valid Vite options
        return cliArgs.replace("--vite", "")
                     .trim();
    }

    private String filterCreateReactAppArgs(String cliArgs) {
        if (cliArgs == null) return "--template typescript";
        
        // Remove our custom flags and pass through valid CRA options
        return cliArgs.replace("--nextjs", "")
                     .replace("--vite", "")
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