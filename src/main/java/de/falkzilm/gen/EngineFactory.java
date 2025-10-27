package de.falkzilm.gen;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.util.*;

@ApplicationScoped
public class EngineFactory {
    private final Map<Framework, GenerationEngine> byFramework = new EnumMap<>(Framework.class);

    @Inject
    public EngineFactory(@Any Instance<GenerationEngine> engines) {
        for (var e : engines) byFramework.put(e.framework(), e);
    }

    public GenerationEngine get(Framework framework) {
        var e = byFramework.get(framework);
        if (e == null) throw new IllegalArgumentException("No engine for " + framework);
        return e;
    }

    public List<Framework> supported() {
        return List.copyOf(byFramework.keySet());
    }
}
