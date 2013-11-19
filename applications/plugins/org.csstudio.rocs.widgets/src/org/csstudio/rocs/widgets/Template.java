package org.csstudio.rocs.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;




public class Template {
    
    private final String name;
    private ExecutorService executorService;
    private Integer width;
    private Integer height;
    private final List<XMLWidget> widgets;

    public Template(TemplateDescription templateDescription) {
        this.name = templateDescription.name;
        this.executorService = templateDescription.executorService;
        this.width = templateDescription.width;
        this.height = templateDescription.height;
        this.widgets = Collections.unmodifiableList(new ArrayList<>(templateDescription.getWidgets()));
    }
    
    public Template(Template template){
    	this.name = template.name;
    	this.executorService = template.executorService;
    	this.width = template.width;
    	this.height = template.height;
    	this.widgets = template.widgets;
    }

	public final String getName() {
        return name;
    }

    public ExecutorService getExecutorService() {
		return executorService;
	}

	public Integer getWidth() {
		return width;
	}

	public Integer getHeight() {
		return height;
	}

	public final List<XMLWidget> getWidgets() {
        return widgets;
    }
}
