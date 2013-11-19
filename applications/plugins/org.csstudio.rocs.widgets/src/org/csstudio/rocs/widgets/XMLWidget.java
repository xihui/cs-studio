package org.csstudio.rocs.widgets;

import java.util.List;

import javax.xml.bind.annotation.*;

@XmlRootElement(name="widget")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Widget", propOrder = {
    "typeId","version","width","height","x","y","pv_name","text","any"
})
public class XMLWidget {

	@XmlAttribute
	protected String typeId;
	
	@XmlAttribute
	protected Double version;
	
	@XmlElement
	protected Integer width;
	
	@XmlElement
	protected Integer height;
	
	@XmlElement
	protected Integer x;
	
	@XmlElement
	protected Integer y;
	
	@XmlElement
	protected String pv_name;
	
	@XmlElement
	protected String text;
	
	@XmlAnyElement(lax = true)
    protected List<Object> any;
	
	public XMLWidget(){
		
	}
	public XMLWidget(XMLWidget xmlWidget) {
		this.typeId = xmlWidget.typeId;
		this.version = xmlWidget.version;
		this.width = xmlWidget.width;
		this.height = xmlWidget.height;
		this.x = xmlWidget.x;
		this.y = xmlWidget.y;
		this.pv_name = xmlWidget.pv_name;
		this.text =xmlWidget.text;
		this.any = xmlWidget.any;
	}
	
}
