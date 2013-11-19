package org.csstudio.rocs.widgets;

import gov.bnl.channelfinder.api.Property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.*;

@XmlRootElement(name="display")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Display", propOrder = {
    "typeId","version","height","width","widgets","any"
})
public class XMLDisplay {
	
	@XmlAttribute
	protected String typeId;
	
	@XmlAttribute
	protected Double version;
	
	@XmlElement(name="widget")
	protected List<XMLWidget> widgets = new ArrayList<XMLWidget>();
	
	@XmlAnyElement(lax = true)
    protected List<Object> any = new ArrayList<Object>();
	
	@XmlElement
	private Integer width = 0;
	
	@XmlElement
	private Integer height = 0;
	
	public XMLDisplay(){
	}
	
	public XMLDisplay(String typeId, Double version, List<XMLWidget> widgets, List<Object> any){
		this.typeId = typeId;
		this.version = version;
		this.widgets = widgets;
		this.any = any;
	}

	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public Double getVersion() {
		return version;
	}

	public void setVersion(Double version) {
		this.version = version;
	}

	public List<XMLWidget> getWidgets() {
		return widgets;
	}

	public void setWidgets(List<XMLWidget> widgets) {
		this.widgets = widgets;
	}
	
	public void addWidgets(List<XMLWidget> widgets) {

		this.widgets.addAll(widgets);
	}
	
	public void addTemplate(Template template,Collection<Property> properties){
		for(XMLWidget xmlWidget: template.getWidgets()){
			XMLWidget newWidget = new XMLWidget(xmlWidget);
			newWidget.y = height;
	    	if(width< newWidget.x+newWidget.width)
	    		width = newWidget.x+newWidget.width;
			if (properties!=null){
				for(Property prop : properties){
					if(newWidget.pv_name!=null)
						newWidget.pv_name = newWidget.pv_name.replace("${"+prop.getName()+"}",prop.getValue());
					if(newWidget.text!=null)
						newWidget.text = newWidget.text.replace("${"+prop.getName()+"}",prop.getValue());
				}
			}
			this.widgets.add(newWidget);
		}
		height = template.getHeight()+height;
	}

	public List<Object> getAny() {
		return any;
	}

	public void setAny(List<Object> any) {
		this.any = any;
	}
	
	
}
