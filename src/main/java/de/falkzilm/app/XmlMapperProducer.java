package de.falkzilm.app;

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
class XmlMapperProducer {
    @Produces @ApplicationScoped
    XmlMapper xmlMapper() {
        var mod = new JacksonXmlModule();
        mod.setDefaultUseWrapper(false);
        return new XmlMapper(mod);
    }
}
