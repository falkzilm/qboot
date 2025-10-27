package de.falkzilm;

import de.falkzilm.cmds.CreateCmd;
import de.falkzilm.gen.EngineFactory;
import de.falkzilm.helper.ConsoleFormatter;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import jakarta.inject.Inject;
import picocli.CommandLine;

@TopCommand
@CommandLine.Command(
    name = "qboot", 
    subcommands = {CreateCmd.class}, 
    description = "Bootstrap projects from templates - A CLI tool for rapid project creation",
    mixinStandardHelpOptions = true,
    version = "qBoot ${quarkus.application.version:dev}"
)
public class BootstrapCmd implements Runnable {

    @CommandLine.Option(
        names = {"-v", "--version"}, 
        description = "Print version information and exit"
    )
    private boolean version;

    @CommandLine.Option(
        names = {"-l", "--list"}, 
        description = "List all available framework engines"
    )
    private boolean listFrameworks;

    @CommandLine.Option(
        names = {"-h", "--help"}, 
        usageHelp = true, 
        description = "Print help usage"
    )
    private boolean help;

    @Inject
    EngineFactory factory;

    @Override
    public void run() {
        if (version) {
            System.out.println(ConsoleFormatter.detectVersion());
            System.exit(0);
        }

        if (listFrameworks) {
            System.out.println("Available frameworks:");
            factory.supported().forEach(framework -> 
                System.out.println("  • " + framework.label + " (" + framework.name().toLowerCase() + ")"));
            System.exit(0);
        }

        ConsoleFormatter.bannerRocketBox();
        CommandLine.usage(this, System.out);
    }
}
