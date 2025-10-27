package de.falkzilm.gen;

import de.falkzilm.template.Dependency;
import de.falkzilm.template.Workspace;

import java.util.List;

public interface GenerationEngine {
    Framework framework();
    /** Erzeugt Projektartefakte für die gegebene Sprache. */
    void generate(Workspace template, GenParameters genParameters) throws Exception;

    DependencyHandler createDependencyHandlerFor(GenParameters genParameters, List<Dependency> dependencyList);
}
