package de.falkzilm.template;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import de.falkzilm.gen.Framework;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record General(
        @JacksonXmlProperty(localName = "framework") Framework framework,
        @JacksonXmlProperty(localName = "frameworkVersion") String frameworkVersion,
        @JacksonXmlProperty(localName = "cliArgs") String cliArgs,
        @JacksonXmlProperty(localName = "projectName") String projectName,
        @JacksonXmlProperty(localName = "projectPackage") String projectPackage) {
}
