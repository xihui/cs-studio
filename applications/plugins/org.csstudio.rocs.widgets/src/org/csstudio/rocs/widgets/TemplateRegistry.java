package org.csstudio.rocs.widgets;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TemplateRegistry {
    private final static TemplateRegistry registry = new TemplateRegistry();

    public static TemplateRegistry getDefault() {
        return registry;
    }
    
    private Map<String, Template> templates = new ConcurrentHashMap<>();
    
    public void registerTemplate(Template template) {
        templates.put(template.getName(), template);
    }
    
    
    public Set<String> listTemplates() {
        return Collections.unmodifiableSet(new HashSet<>(templates.keySet()));
    }
    
    public Template findTemplate(String name) {
        return templates.get(name);
    }
}
