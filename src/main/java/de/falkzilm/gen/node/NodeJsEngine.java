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
@FrameworkUsage(Framework.NODEJS)
public class NodeJsEngine implements GenerationEngine {

    @Override
    public Framework framework() {
        return Framework.NODEJS;
    }

    @Override
    public void generate(Workspace template, GenParameters genParameters) throws Exception {
        ConsoleFormatter.section("Bootstrapping Node.js project via " + OsUtils.getOsDescription());

        // Create target directory
        ConsoleFormatter.bullet("Creating destination dir: " + genParameters.target().toString());
        CommandLine mkdirCmd = OsUtils.createMkdirCommand(genParameters.target().toString());
        RunWrapper.builder()
                .cmd(mkdirCmd)
                .build()
                .run(Path.of("."), genParameters.debug());

        ConsoleFormatter.bullet("Initializing Node.js project (" + OsUtils.getOsDescription() + ")");
        System.out.println();

        // Initialize package.json
        String initCommand = buildInitCommand(genParameters);
        CommandLine initCmd = OsUtils.createNpmCommand(initCommand);
        
        RunWrapper.builder()
                .cmd(initCmd)
                .build()
                .run(genParameters.target(), genParameters.debug());

        // Install Express and basic dependencies
        String installCommand = buildInstallCommand(genParameters);
        CommandLine installCmd = OsUtils.createNpmCommand(installCommand);
        
        ConsoleFormatter.bullet("Installing Node.js dependencies...");
        RunWrapper.builder()
                .cmd(installCmd)
                .build()
                .run(genParameters.target(), genParameters.debug());

        // Create basic project structure
        createProjectStructure(genParameters);

    }

    private String buildInitCommand(GenParameters genParameters) {
        String projectName = genParameters.name();
        String targetDir = genParameters.target().resolve(projectName).toString();
        
        // Change to project directory and initialize npm
        return String.format("init -y --name %s --version 1.0.0 --main index.js && cd %s", 
            projectName, targetDir);
    }

    private String buildInstallCommand(GenParameters genParameters) {
        String cliArgs = genParameters.cliArgs() != null ? genParameters.cliArgs() : "";
        String targetDir = genParameters.target().resolve(genParameters.name()).toString();
        
        // Parse framework type from CLI args
        StringBuilder packages = new StringBuilder("express");
        
        // Add common middleware and utilities
        if (cliArgs.contains("--typescript") || cliArgs.contains("--ts")) {
            packages.append(" typescript @types/node @types/express ts-node nodemon");
        }
        
        if (cliArgs.contains("--mongodb")) {
            packages.append(" mongoose");
        }
        
        if (cliArgs.contains("--postgres") || cliArgs.contains("--postgresql")) {
            packages.append(" pg");
            if (cliArgs.contains("--typescript") || cliArgs.contains("--ts")) {
                packages.append(" @types/pg");
            }
        }
        
        if (cliArgs.contains("--cors")) {
            packages.append(" cors");
            if (cliArgs.contains("--typescript") || cliArgs.contains("--ts")) {
                packages.append(" @types/cors");
            }
        }
        
        if (cliArgs.contains("--morgan")) {
            packages.append(" morgan");
            if (cliArgs.contains("--typescript") || cliArgs.contains("--ts")) {
                packages.append(" @types/morgan");
            }
        }
        
        // Default packages if none specified
        if (!cliArgs.contains("--minimal")) {
            packages.append(" helmet dotenv");
        }
        
        return String.format("install %s && cd %s", packages.toString(), targetDir);
    }

    private void createProjectStructure(GenParameters genParameters) {
        String projectPath = genParameters.target().resolve(genParameters.name()).toString();
        String cliArgs = genParameters.cliArgs() != null ? genParameters.cliArgs() : "";
        
        // Create basic directory structure
        String[] dirs = {"src", "src/routes", "src/middleware", "src/controllers", "public", "tests"};
        for (String dir : dirs) {
            CommandLine mkdirCmd = OsUtils.createMkdirCommand(projectPath + "/" + dir);
            RunWrapper.builder()
                    .cmd(mkdirCmd)
                    .build()
                    .run(genParameters.target(), genParameters.debug());
        }
        
        // Create basic server file
        boolean isTypeScript = cliArgs.contains("--typescript") || cliArgs.contains("--ts");
        String serverFile = isTypeScript ? "src/index.ts" : "src/index.js";
        String serverContent = createServerContent(genParameters, isTypeScript);
        
        String createServerCmd = createFileCommand(projectPath + "/" + serverFile, serverContent);
        CommandLine serverCmd = OsUtils.createShellCommand(createServerCmd);
        RunWrapper.builder()
                .cmd(serverCmd)
                .build()
                .run(genParameters.target(), genParameters.debug());
        
        // Create package.json scripts
        String updatePackageCmd = createPackageJsonUpdateCommand(projectPath, isTypeScript);
        CommandLine packageCmd = OsUtils.createShellCommand(updatePackageCmd);
        RunWrapper.builder()
                .cmd(packageCmd)
                .build()
                .run(genParameters.target(), genParameters.debug());
    }

    private String createServerContent(GenParameters genParameters, boolean isTypeScript) {
        String cliArgs = genParameters.cliArgs() != null ? genParameters.cliArgs() : "";
        
        if (isTypeScript) {
            return """
                import express, { Request, Response } from 'express';
                import helmet from 'helmet';
                import dotenv from 'dotenv';
                
                dotenv.config();
                
                const app = express();
                const PORT = process.env.PORT || 3000;
                
                // Middleware
                app.use(helmet());
                app.use(express.json());
                app.use(express.urlencoded({ extended: true }));
                
                // Routes
                app.get('/', (req: Request, res: Response) => {
                  res.json({ message: 'Hello from %s!' });
                });
                
                app.listen(PORT, () => {
                  console.log(`Server running on port ${PORT}`);
                });
                """.formatted(genParameters.name());
        } else {
            return """
                const express = require('express');
                const helmet = require('helmet');
                require('dotenv').config();
                
                const app = express();
                const PORT = process.env.PORT || 3000;
                
                // Middleware
                app.use(helmet());
                app.use(express.json());
                app.use(express.urlencoded({ extended: true }));
                
                // Routes
                app.get('/', (req, res) => {
                  res.json({ message: 'Hello from %s!' });
                });
                
                app.listen(PORT, () => {
                  console.log(`Server running on port ${PORT}`);
                });
                """.formatted(genParameters.name());
        }
    }

    private String createFileCommand(String filePath, String content) {
        if (OsUtils.isWindows()) {
            return String.format("echo \"%s\" > \"%s\"", content.replace("\"", "\\\""), filePath);
        } else {
            return String.format("cat > \"%s\" << 'EOF'\n%s\nEOF", filePath, content);
        }
    }

    private String createPackageJsonUpdateCommand(String projectPath, boolean isTypeScript) {
        String packageJsonPath = projectPath + "/package.json";
        
        if (isTypeScript) {
            return String.format(
                "cd %s && npm pkg set scripts.start=\"node dist/index.js\" && " +
                "npm pkg set scripts.dev=\"ts-node src/index.ts\" && " +
                "npm pkg set scripts.build=\"tsc\" && " +
                "npm pkg set scripts.watch=\"nodemon src/index.ts\"",
                projectPath);
        } else {
            return String.format(
                "cd %s && npm pkg set scripts.start=\"node src/index.js\" && " +
                "npm pkg set scripts.dev=\"nodemon src/index.js\"",
                projectPath);
        }
    }

    @Override
    public DependencyHandler createDependencyHandlerFor(GenParameters genParameters, List<Dependency> dependencyList) {
        return NodeDependencyHandler.builder()
                .dependencies(dependencyList)
                .genParameters(genParameters)
                .build();
    }
}