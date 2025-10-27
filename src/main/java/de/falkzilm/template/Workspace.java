package de.falkzilm.template;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

import java.util.List;

@RegisterForReflection
@Data
public class Workspace {
    @JacksonXmlProperty(isAttribute = true, localName = "path")
    public String path;

    private General general;

    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Dependencies> dependencies;

    private Structure structure;
}
