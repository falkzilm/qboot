package de.falkzilm.cmds;

import de.falkzilm.gen.ChangesetHandler;
import de.falkzilm.gen.EngineFactory;
import de.falkzilm.gen.GenParameters;
import de.falkzilm.helper.ConsoleFormatter;
import de.falkzilm.template.QTemplate;
import de.falkzilm.template.TemplateService;
import de.falkzilm.template.Workspace;
import jakarta.inject.Inject;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

@CommandLine.Command(name = "create", description = "Bootstrap a new project")
public class CreateCmd implements Runnable {

    @CommandLine.Option(
        names = {"-t", "--template"}, 
        description = "Template to use for project generation. Can be a local file path or HTTP/HTTPS URL", 
        required = true
    )
    private String template;

    @CommandLine.Option(names = {"-p", "--package"}, description = "Package name for the generated project")
    private String packageName;

    @CommandLine.Option(names = {"-n", "--name"}, description = "Name of the generated project")
    private String projectName;

    @CommandLine.Option(names = {"-c", "--cli-args"}, description = "Additional arguments for cli usage in generation")
    private String cliArgs;

    @CommandLine.Option(names = {"-o", "--output"}, description = "Target directory to create bootstrap in", defaultValue = ".")
    private String destinationDir;

    @CommandLine.Option(names = {"-d", "--debug"}, description = "Verbose/debug output")
    private boolean debug;

    @CommandLine.Option(names= {"-h", "--help"}, usageHelp = true, description="Print help usage")
    boolean help;

    @Inject
    TemplateService templateService;

    @Inject
    EngineFactory factory;

    @Override
    public void run() {
        ConsoleFormatter.bannerRocketBox();

        try {
            processTemplate();
        } catch (IOException exc) {
            ConsoleFormatter.error(
                "Template Error",
                "Could not load template: " + exc.getMessage(), 
                exc,
                "For URLs: Check network connectivity and URL validity",
                "For files: Ensure the template file exists and is readable",
                "Verify template XML syntax is valid"
            );
        } catch (InterruptedException exc) {
            ConsoleFormatter.error(
                "Network Timeout", 
                "Template download was interrupted", 
                exc,
                "Check your internet connection",
                "Try again with a more stable connection"
            );
            Thread.currentThread().interrupt();
        } catch (Exception exc) {
            ConsoleFormatter.error(
                "Generation Error",
                "Failed to generate project: " + exc.getMessage(), 
                exc,
                "Run with --debug flag for detailed information",
                "Check that all required tools are installed",
                "Verify template configuration is correct"
            );
            if (debug) {
                exc.printStackTrace();
            }
        }
    }

    private void processTemplate() throws Exception {
        long startTime = System.nanoTime();
        
        // Load template from file or URL
        QTemplate templateData = templateService.loadTemplate(template);
        
        // Validate template structure
        if (!templateService.validateTemplate(templateData)) {
            throw new IllegalArgumentException("Template validation failed - check template structure");
        }
        
        // Process all workspaces
        for (Workspace workspace : templateData.getWorkspaces().getItems()) {
            processWorkspace(workspace);
        }
        
        Duration elapsed = Duration.ofNanos(System.nanoTime() - startTime);
        ConsoleFormatter.footer(destinationDir, elapsed);
    }

    private void processWorkspace(Workspace workspace) throws Exception {
        GenParameters genParameters = createGenParameters(workspace);
        var engine = factory.get(workspace.getGeneral().framework());

        ConsoleFormatter.header(genParameters.framework(), genParameters.frameworkVersion(), genParameters.target().toString());
        System.out.println();

        // Handle pre-requisite dependencies
        var preRequisites = extractDependencies(workspace, "pre");
        engine.createDependencyHandlerFor(genParameters, preRequisites).check();

        // Generate project structure
        engine.generate(workspace, genParameters);

        // Handle post-generation dependencies
        var postDependencies = extractDependencies(workspace, "post");
        engine.createDependencyHandlerFor(genParameters, postDependencies).install();

        // Apply changesets
        ChangesetHandler.builder()
                .structure(workspace.getStructure())
                .build()
                .run(genParameters);

        System.out.println();
    }

    private GenParameters createGenParameters(Workspace workspace) {
        return new GenParameters(
            workspace.getGeneral().framework(),
            getTemplateOrArg(workspace.getGeneral().projectName(), projectName),
            getTemplateOrArg(workspace.getGeneral().projectPackage(), packageName),
            workspace.getGeneral().frameworkVersion(),
            debug,
            workspace.getPath() != null ? 
                Path.of(destinationDir).resolve(workspace.getPath()) : 
                Path.of(destinationDir),
            getTemplateOrArg(workspace.getGeneral().cliArgs(), cliArgs)
        );
    }

    private java.util.List<de.falkzilm.template.Dependency> extractDependencies(Workspace workspace, String blockName) {
        return workspace.getDependencies()
                .stream()
                .filter(d -> blockName.equals(d.blockName))
                .flatMap(dependencies -> dependencies.items.stream())
                .toList();
    }

    private String getTemplateOrArg(String templateData, String argData) {
        return Optional.ofNullable(
                Optional.ofNullable(templateData).orElse(argData)
        ).orElse("");
    }
}
