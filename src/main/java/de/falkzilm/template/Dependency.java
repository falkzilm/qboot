package de.falkzilm.template;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record Dependency(String name, String packageName, Boolean optional, String version, Boolean extension) {
}
