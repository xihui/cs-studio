package org.csstudio.rocs.widgets;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;



public class TemplateDescription {
	
    final String name;
    ExecutorService executorService;
    private Integer x;
    private Integer y;
    Integer width;
    Integer height;
    private List<XMLWidget> widgets = new ArrayList<>();
    static Pattern namePattern = Pattern.compile("[a-zA-Z_]\\w*");

    public TemplateDescription(String name) {
    	this.x = 0;
        this.y = 0;
        this.width = 0;
        this.height = 0;
        this.name = name;
        if (!namePattern.matcher(name).matches()) {
            throw new IllegalArgumentException("Name must start by a letter and only consist of letters and numbers");
        }
    }
    
    public TemplateDescription addWidget(XMLWidget widget) {
    	widgets.add(widget);
    	if(x+width< widget.x+widget.width){
    		width = widget.x+widget.width;
    	}
    	if(y+height< widget.y+widget.height){
    		height = widget.y+widget.height;
    	}
        return this;
    }
    
    public List<XMLWidget> getWidgets(){
    	return widgets;
    }
    
}
