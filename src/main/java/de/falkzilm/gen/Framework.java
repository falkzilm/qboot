package de.falkzilm.gen;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.*;

public enum Framework {
    QUARKUS("Quarkus", List.of("quarkus")),
    ANGULAR("Angular", List.of("angular", "ng")),
    REACT("React", List.of("react", "reactjs", "nextjs", "next")),
    VUE("Vue.js", List.of("vue", "vuejs")),
    SPRINGBOOT("Spring Boot", List.of("springboot", "spring", "spring-boot")),
    NODEJS("Node.js", List.of("nodejs", "node", "express")),
    DOTNET("ASP.NET Core", List.of("dotnet", "aspnet", "aspnetcore", "net")),
    KOTLIN("Kotlin", List.of("kotlin", "ktor"));

    public final String label;
    public final List<String> aliases;
    Framework(String label, List<String> aliases){ this.label=label; this.aliases=aliases; }

    public static Optional<Framework> parse(String in) {
        if (in == null) return Optional.empty();
        String x = in.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(l -> l.name().equalsIgnoreCase(x) || l.aliases.contains(x))
                .findFirst();
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Framework from(String in) {
        if (in == null) return null;
        String x = in.trim().toLowerCase();
        return switch (x) {
            case "quarkus","qs" -> QUARKUS;
            case "angular","ng" -> ANGULAR;
            case "react","reactjs","nextjs","next" -> REACT;
            case "vue","vuejs" -> VUE;
            case "springboot","spring","spring-boot" -> SPRINGBOOT;
            case "nodejs","node","express" -> NODEJS;
            case "dotnet","aspnet","aspnetcore","net" -> DOTNET;
            case "kotlin","ktor" -> KOTLIN;
            default -> throw new IllegalArgumentException("Unknown framework: " + in);
        };
    }

    @JsonValue
    public String toJson() { return name().toLowerCase(); }
}
