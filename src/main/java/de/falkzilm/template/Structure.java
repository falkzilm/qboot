package de.falkzilm.template;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

@RegisterForReflection
public class Structure {
    @JacksonXmlProperty(isAttribute = true, localName = "value")
    public String value;

    @JacksonXmlElementWrapper(useWrapping = false)
    public List<ChangeSet> changeset;
}
