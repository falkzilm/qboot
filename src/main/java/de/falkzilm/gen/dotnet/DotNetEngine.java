package de.falkzilm.gen.dotnet;

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
@FrameworkUsage(Framework.DOTNET)
public class DotNetEngine implements GenerationEngine {

    @Override
    public Framework framework() {
        return Framework.DOTNET;
    }

    @Override
    public void generate(Workspace template, GenParameters genParameters) throws Exception {
        ConsoleFormatter.section("Bootstrapping .NET Core project via " + OsUtils.getOsDescription());

        // Create target directory
        ConsoleFormatter.bullet("Creating destination dir: " + genParameters.target().toString());
        CommandLine mkdirCmd = OsUtils.createMkdirCommand(genParameters.target().toString());
        RunWrapper.builder()
                .cmd(mkdirCmd)
                .build()
                .run(Path.of("."), genParameters.debug());

        ConsoleFormatter.bullet("Creating .NET Core project (" + OsUtils.getOsDescription() + ")");
        System.out.println();

        // Generate .NET project using dotnet CLI
        String createCommand = buildCreateCommand(genParameters);
        CommandLine dotnetCmd = OsUtils.createShellCommand(createCommand);
        
        RunWrapper.builder()
                .cmd(dotnetCmd)
                .build()
                .run(genParameters.target(), genParameters.debug());

        // Add additional packages if specified
        addAdditionalPackages(genParameters);

    }

    private String buildCreateCommand(GenParameters genParameters) {
        String projectName = genParameters.name();
        String cliArgs = genParameters.cliArgs() != null ? genParameters.cliArgs() : "";
        String targetDir = genParameters.target().toString();
        
        // Determine project template from CLI args
        String template = parseTemplate(cliArgs);
        String framework = parseFramework(cliArgs);
        
        StringBuilder command = new StringBuilder("dotnet new ");
        command.append(template);
        command.append(" --name ").append(projectName);
        command.append(" --output ").append(targetDir).append("/").append(projectName);
        
        if (!framework.isEmpty()) {
            command.append(" --framework ").append(framework);
        }
        
        // Add additional template-specific options
        if (cliArgs.contains("--auth")) {
            command.append(" --auth Individual");
        }
        
        if (cliArgs.contains("--https")) {
            command.append(" --use-https");
        }
        
        if (cliArgs.contains("--no-restore")) {
            command.append(" --no-restore");
        }
        
        return command.toString();
    }

    private String parseTemplate(String cliArgs) {
        if (cliArgs == null) return "webapi";
        
        if (cliArgs.contains("--template=")) {
            String template = cliArgs.substring(cliArgs.indexOf("--template=") + 11);
            int endIndex = template.indexOf(" ");
            if (endIndex > 0) {
                template = template.substring(0, endIndex);
            }
            return template;
        }
        
        // Determine template from keywords
        if (cliArgs.contains("--mvc")) return "mvc";
        if (cliArgs.contains("--blazor")) return "blazorserver";
        if (cliArgs.contains("--razor")) return "razor";
        if (cliArgs.contains("--console")) return "console";
        if (cliArgs.contains("--classlib")) return "classlib";
        if (cliArgs.contains("--worker")) return "worker";
        if (cliArgs.contains("--grpc")) return "grpc";
        
        return "webapi"; // Default to Web API
    }

    private String parseFramework(String cliArgs) {
        if (cliArgs != null && cliArgs.contains("--framework=")) {
            String framework = cliArgs.substring(cliArgs.indexOf("--framework=") + 12);
            int endIndex = framework.indexOf(" ");
            if (endIndex > 0) {
                framework = framework.substring(0, endIndex);
            }
            return framework;
        }
        
        // Determine framework version from keywords
        if (cliArgs != null && cliArgs.contains("--net6")) return "net6.0";
        if (cliArgs != null && cliArgs.contains("--net7")) return "net7.0";
        if (cliArgs != null && cliArgs.contains("--net8")) return "net8.0";
        if (cliArgs != null && cliArgs.contains("--net9")) return "net9.0";
        
        return "net8.0"; // Default to .NET 8
    }

    private void addAdditionalPackages(GenParameters genParameters) {
        String cliArgs = genParameters.cliArgs() != null ? genParameters.cliArgs() : "";
        String projectPath = genParameters.target().resolve(genParameters.name()).toString();
        
        if (cliArgs.contains("--ef") || cliArgs.contains("--entity-framework")) {
            addNuGetPackage("Microsoft.EntityFrameworkCore.SqlServer", projectPath, genParameters);
            addNuGetPackage("Microsoft.EntityFrameworkCore.Tools", projectPath, genParameters);
        }
        
        if (cliArgs.contains("--swagger")) {
            addNuGetPackage("Swashbuckle.AspNetCore", projectPath, genParameters);
        }
        
        if (cliArgs.contains("--serilog")) {
            addNuGetPackage("Serilog.AspNetCore", projectPath, genParameters);
        }
        
        if (cliArgs.contains("--automapper")) {
            addNuGetPackage("AutoMapper.Extensions.Microsoft.DependencyInjection", projectPath, genParameters);
        }
        
        if (cliArgs.contains("--jwt")) {
            addNuGetPackage("Microsoft.AspNetCore.Authentication.JwtBearer", projectPath, genParameters);
        }
    }

    private void addNuGetPackage(String packageName, String projectPath, GenParameters genParameters) {
        ConsoleFormatter.bullet("Adding NuGet package: " + packageName);
        
        String addPackageCommand = String.format("dotnet add %s package %s", projectPath, packageName);
        CommandLine packageCmd = OsUtils.createShellCommand(addPackageCommand);
        RunWrapper.builder()
                .cmd(packageCmd)
                .build()
                .run(genParameters.target(), genParameters.debug());
    }

    @Override
    public DependencyHandler createDependencyHandlerFor(GenParameters genParameters, List<Dependency> dependencyList) {
        return DotNetDependencyHandler.builder()
                .dependencies(dependencyList)
                .genParameters(genParameters)
                .build();
    }
}