package de.falkzilm.gen;

import de.falkzilm.helper.ConsoleFormatter;
import de.falkzilm.template.ChangeSet;
import de.falkzilm.template.PathSpec;
import de.falkzilm.template.Structure;
import lombok.Builder;
import lombok.Data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

@Builder
@Data
public class ChangesetHandler {
    private Structure structure;

    public void run(GenParameters genParameters) {
        if ("custom".equals(structure.value)) {
            ConsoleFormatter.section("Structural Changes");
            Path projectPath = genParameters.target().resolve(Path.of(genParameters.name()));
            for (ChangeSet changeset : structure.changeset.stream().filter(c -> c.type.equals("add")).toList()) {
                ConsoleFormatter.bullet("Additive");
                for (PathSpec pathSpec : changeset.paths) {
                    if (pathSpec.autocreate != null && pathSpec.autocreate) {
                        ConsoleFormatter.subbullet("Creating path " + pathSpec.name);
                        new File(projectPath.toFile(), pathSpec.name).mkdirs();
                    }

                    if (pathSpec.content != null && !pathSpec.content.isBlank()) {
                        ConsoleFormatter.subbullet("Editing content in path " + pathSpec.name);
                        File outputFile = new File(projectPath.toFile(), pathSpec.name);
                        try (FileWriter writer = new FileWriter(outputFile)) {
                            writer.write(pathSpec.content);
                        } catch (IOException e) {
                            ConsoleFormatter.error(
                                    "Write failure",
                                    "Could not write " + pathSpec.name + ": " + e.getMessage(), e.getCause(),
                                    "Make sure destination path is writeable",
                                    "Run with debug flag for verbose information"
                            );
                        }
                    }
                }
            }
            for (ChangeSet changeset : structure.changeset.stream().filter(c -> c.type.equals("remove")).toList()) {
                ConsoleFormatter.bullet("Removing");
                for (PathSpec pathSpec : changeset.paths) {
                    File outputFile = new File(projectPath.toFile(), pathSpec.name);

                    if (outputFile.exists()) {
                        ConsoleFormatter.subbullet("Removing file in path " + pathSpec.name);
                        outputFile.delete();
                    }  else {
                        ConsoleFormatter.subbullet("Removing file in path " + pathSpec.name + " not possible, does not exist.");
                    }
                }
            }
            System.out.println();
        }
    }
}
