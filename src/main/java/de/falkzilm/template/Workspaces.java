package de.falkzilm.template;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

import java.util.List;

@RegisterForReflection
@Data
public class Workspaces {
    @JacksonXmlProperty(localName = "workspace")
    @JacksonXmlElementWrapper(useWrapping = false)
    List<Workspace> items;
}
