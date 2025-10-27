package de.falkzilm.exec;

import de.falkzilm.helper.ConsoleFormatter;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

@Builder
@Data
public class RunWrapper {
    private CommandLine cmd;

    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream errorStream;
    private Duration timeout;

    public int run(Path workingDir, boolean debug) {
        try {
            if (this.cmd == null) {
                return -1;
            }
            if (debug) {
                ConsoleFormatter.debug("Executing CMD", workingDir.toString() + " -> " + cmd);
            }
            var executor =
                DefaultExecutor.builder()
                    .setWorkingDirectory(workingDir)
                    .get();

            outputStream = new ByteArrayOutputStream(1024);
            errorStream = new ByteArrayOutputStream(512);

            executor.setExitValues(null);
            executor.setStreamHandler(new PumpStreamHandler(
                    new PrintStream(outputStream, true, StandardCharsets.UTF_8),
                    new PrintStream(errorStream, true, StandardCharsets.UTF_8)
            ));

            if (this.timeout != null) {
                executor.setWatchdog(ExecuteWatchdog.builder().setTimeout(timeout).get());
            }

            return executor.execute(cmd);
        } catch (IOException e) {
            return -1;
        } finally {
            if (debug) {
                ConsoleFormatter.debug("Output", getOutput());
            }
        }
    }

    public String getOutput() {
        var stdout = Optional.ofNullable(outputStream).map(ByteArrayOutputStream::toString).orElse("");
        var stderr = Optional.ofNullable(errorStream).map(ByteArrayOutputStream::toString).orElse("");

        return stdout.isEmpty() ? stderr : stdout;
    }
}
