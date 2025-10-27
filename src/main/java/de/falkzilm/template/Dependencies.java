package de.falkzilm.template;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RegisterForReflection
public class Dependencies {
    @JacksonXmlProperty(isAttribute = true, localName = "name")
    public String blockName; // z. B. "pre" oder "post"

    public List<Dependency> items = new ArrayList<>();

    @JsonAnySetter
    public void collect(String tagName, Object value) {
        boolean optional = false;
        boolean extension = false;
        String version = null;
        String packageName = null;

        if (value instanceof Map<?, ?> m) {
            Object opt = m.get("optional");
            if (opt != null) optional = Boolean.parseBoolean(opt.toString());
            Object ext = m.get("extension");
            if (ext != null) extension = Boolean.parseBoolean(ext.toString());
            Object pName = m.get("packageName");
            if (pName != null) packageName = pName.toString();

            Object text = m.get("");                 // üblicher Platz für Element-Text
            if (text == null) text = m.get("value"); // Fallback (selten)
            if (text != null) version = text.toString();
        } else if (value != null) {
            version = value.toString(); // z. B. <java>21+</java>
        }

        items.add(new Dependency(tagName, packageName, optional, version, extension));
    }
}
