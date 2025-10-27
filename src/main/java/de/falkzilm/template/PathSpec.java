package de.falkzilm.template;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class PathSpec {
    @JacksonXmlProperty(isAttribute = true, localName = "name")
    public String name;

    @JacksonXmlProperty(isAttribute = true, localName = "autocreate")
    public Boolean autocreate;

    @JacksonXmlText
    public String content;
}