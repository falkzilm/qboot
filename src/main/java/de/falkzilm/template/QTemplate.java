package de.falkzilm.template;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@RegisterForReflection
@JacksonXmlRootElement(localName = "qtemplate")
@Data
public class QTemplate {
    private Workspaces workspaces;
}
