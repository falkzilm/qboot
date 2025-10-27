package de.falkzilm.gen;

import java.nio.file.Path;

public record GenParameters(Framework framework, String name, String packageName, String frameworkVersion, Boolean debug, Path target, String cliArgs) {
}
