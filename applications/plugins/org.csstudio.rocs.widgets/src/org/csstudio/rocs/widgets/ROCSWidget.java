package org.csstudio.rocs.widgets;

import gov.bnl.channelfinder.api.Channel;
import gov.bnl.channelfinder.api.ChannelQuery.Result;
import gov.bnl.channelfinder.api.ChannelUtil;
import gov.bnl.channelfinder.api.Property;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.csstudio.utility.file.IFileUtil;
import org.csstudio.openfile.DisplayUtil;
import org.csstudio.ui.util.widgets.ErrorBar;
import org.csstudio.utility.pvmanager.widgets.ConfigurableWidget;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;


public class ROCSWidget extends AbstractChannelQueryResultWidget
		implements ISelectionProvider, ConfigurableWidget {

	private ErrorBar errorBar;

	// Simple Model
	private Collection<Channel> channels = new ArrayList<Channel>();
	private AbstractSelectionProviderWrapper selectionProvider;

	private List<String> properties;
	private List<String> propertyValues;
	private List<String> tags;
	private IJobManager jobMan = Job.getJobManager();
	
	private enum RunOpiOptions{
		DETACHED("Detached"),EDIT("Edit"),ATTACHED("Attached");
		
		private String option;
		private RunOpiOptions(String s){
			option = s;
		}
	}
	
	private RunOpiOptions option = RunOpiOptions.DETACHED;
	
	private Collection<XMLPropertyCollectionSet> xmlset;

	public Collection<Channel> getChannels() {
		return channels;
	}
	
	Listener buttonListener = new Listener () { 
		@Override
		public void handleEvent(Event event) {
			buttonSelection((Button)event.widget);
		} 
	};

	private void buttonSelection(Button button) { 
		if (button.getSelection()){ 
			//for selection
			RunOpiOptions oldOption = option;
			option = RunOpiOptions.valueOf(button.getText());
			changeSupport.firePropertyChange("runOptions", oldOption, option);
		} else { 
			//for deselection 
		} 
	} 

	private void setChannels(Collection<Channel> channels) {
		Collection<Channel> oldChannels = this.channels;
		this.channels = channels;
		if (channels != null) {
			this.properties = new ArrayList<String>(
					ChannelUtil.getPropertyNames(channels));
			this.propertyValues = new ArrayList<String>(
					ChannelUtil.getPropValues(channels, "Device"));
			this.tags = new ArrayList<String>(
					ChannelUtil.getAllTagNames(channels));
			this.xmlset =  new ArrayList<XMLPropertyCollectionSet>();
			
			for (String propertyValue: propertyValues){
				Collection<Channel> chans = filterByProperty(channels, "Device", propertyValue);
				TreeSet<XMLPropertyCollection> propTreeSet =new TreeSet<XMLPropertyCollection>();
				for(Channel chan: chans){
					String id = chan.getProperty("System").getValue()
							+chan.getProperty("SubSystem").getValue()
							+chan.getProperty("D").getValue();
					propTreeSet.add(new XMLPropertyCollection(id,chan.getProperties()));
				}
				
				xmlset.add(new XMLPropertyCollectionSet(propertyValue,propTreeSet));
				
			}
		} else {
			this.properties = Collections.emptyList();
			this.tags = Collections.emptyList();
			this.propertyValues = Collections.emptyList();
			this.xmlset = Collections.emptyList();
		}
		changeSupport.firePropertyChange("channels", oldChannels, channels);
	}

	public Collection<String> getProperties() {
		return properties;
	}

	public void setProperties(List<String> properties) {
		List<String> oldProperties = this.properties;
		this.properties = properties;
		changeSupport.firePropertyChange("properties", oldProperties,
				properties);
	}
	
	private Collection<Channel> filterByProperty(Collection<Channel> channels, String propertyName, String propertyValue){
        Collection<Channel> result = new ArrayList<Channel>();
        for (Channel channel : channels) {
            if(channel.getPropertyNames().contains(propertyName)){
            	if(channel.getProperty(propertyName).getValue().equals(propertyValue)){
            		result.add(channel);
            	}
            }
        }
        return result;
    }

	public Collection<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		List<String> oldTags = this.tags;
		this.tags = tags;
		changeSupport.firePropertyChange("tags", oldTags, tags);
	}

	private void updateWidget() {

		if (xmlset != null) {
			for ( XMLPropertyCollectionSet entry : xmlset) {
				try{
					Job[] build = jobMan.find("ROCS Templates"); 
					if (build.length == 1)
						build[0].join();
					Template template = TemplateRegistry.getDefault().findTemplate(entry.getId());
					if(template==null)
						throw new IllegalArgumentException("Couldn't find template "+entry.getId());
					
					//IFile resource = "get resource somehow";
					//  if (resource != null) resource.refreshLocal(IResource.DEPTH_INFINITE, monitor);
					  
					InputStream inputStream = template.getExecutorService().submit(new DisplayTemplate(entry,template)).get();
					IFileUtil fileUtil = IFileUtil.getInstance();
					IWorkbench workbench = PlatformUI.getWorkbench();
					SecureRandom random = new SecureRandom();
					String fileName = entry.getId()+"_"+new BigInteger(32, random).toString(32)+".opi";
					
					IFile ifile = fileUtil.createFileResource(fileName, inputStream);
					IEditorDescriptor desc = workbench.getEditorRegistry().getDefaultEditor(ifile.getName());
					IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
					
					// default: desc.getId() org.csstudio.opibuilder.OPIRunner
					// org.csstudio.opibuilder.OPIEditor
					// org.csstudio.opibuilder.opiView
					IEditorPart part;
					switch (option) {
					case ATTACHED:
						part = page.openEditor(new FileEditorInput(ifile), "org.csstudio.opibuilder.OPIRunner");
						IFileUtil.getInstance().registerPart(part, ifile);
						break;
					case DETACHED:
						DisplayUtil.getInstance().openDisplay(ifile.getFullPath().toOSString(),"Position=Detached");
//						RunModeService.getInstance().runOPIInView(ifile.getFullPath(), 
//								new DisplayOpenManager(null), null, Position.DETACHED);
						break;
					case EDIT:
						part = page.openEditor(new FileEditorInput(ifile), "org.csstudio.opibuilder.OPIEditor");
						IFileUtil.getInstance().registerPart(part, ifile);
						break;
					default:
						part = page.openEditor(new FileEditorInput(ifile), desc.getId());
						IFileUtil.getInstance().registerPart(part, ifile);
						break;	
					}

					
				} catch (Exception e){
					// clean up opi file if it didn't get registered
					errorBar.setException(e);
				}
			}
		}		
	}
	
	private class DisplayTemplate implements Callable<InputStream> {
		private XMLPropertyCollectionSet entry;
		private Template template;

		DisplayTemplate(XMLPropertyCollectionSet entry, Template template){
			this.entry = entry;
			this.template = template;
		}

		@Override
		public InputStream call() throws Exception {

			JAXBContext jc = JAXBContext.newInstance(XMLPropertyCollectionSet.class, XMLPropertyCollection.class);
			
			Bundle bundle = Platform.getBundle("org.csstudio.rocs.widgets");
			Path path = new Path(
					"src/org/csstudio/rocs/widgets/stylesheet.xslt");
			URL fileURL = FileLocator.find(bundle, path, null);
			InputStream stylesheetFile = fileURL.openStream();
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder db = factory.newDocumentBuilder();
		    Document cachedTemplate = template.getTemplate();
		    Document cachedHeaderTemplate = TemplateRegistry.getDefault().findTemplate(template.getName()+"_header").getTemplate();
		    Document cachedTemplateStylesheet = db.parse(stylesheetFile);
			
			Document stylesheet = (Document) cachedTemplateStylesheet.cloneNode(true);
			Element template = (Element) stylesheet.getElementsByTagName("xsl:stylesheet").item(0);
			Node opiHeaderTemplate = cachedHeaderTemplate.getElementsByTagName("opitemplate").item(0);
			for(int i=0; i<opiHeaderTemplate.getChildNodes().getLength();i++){
				template.appendChild(stylesheet.importNode(opiHeaderTemplate.getChildNodes().item(i),true));
			}
			Node opiTemplate = cachedTemplate.getElementsByTagName("opitemplate").item(0);
			for(int i=0; i<opiTemplate.getChildNodes().getLength();i++){
				template.appendChild(stylesheet.importNode(opiTemplate.getChildNodes().item(i),true));
			}

			
			TransformerFactory tf = TransformerFactory.newInstance();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(os);
			
			Transformer transformer = tf.newTransformer(new DOMSource(stylesheet));
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	        transformer.transform(new JAXBSource(jc,entry),result);
			
			return new ByteArrayInputStream(os.toByteArray());
		}
	}

	public ROCSWidget(Composite parent, int style) {
		super(parent, style);

		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		setLayout(gridLayout);
		
		Group group = new Group(this, SWT.SHADOW_IN);
		group.setText("View Mode for OPI");
		group.setLayout(new RowLayout(SWT.HORIZONTAL));

		for (  RunOpiOptions option : RunOpiOptions.values()) {
			Button button=new Button(group,SWT.RADIO);
			button.setText(option.name());
			if (buttonListener != null)     button.addListener(SWT.Selection,buttonListener);
		}
		
		if (((Group)getChildren()[0]).getChildren().length > 0)   
			((Button)((Group)getChildren()[0]).getChildren()[0]).setSelection(true);

		errorBar = new ErrorBar(this, SWT.NONE);
		errorBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false,
				1, 1));
		errorBar.setMarginBottom(5);
	

		addPropertyChangeListener(new PropertyChangeListener() {

			List<String> properties = Arrays.asList("channels", "properties",
					"tags","runOptions");

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (properties.contains(evt.getPropertyName())) {
					updateWidget();
				}
			}

		});
	}

	@Override
	public void setMenu(Menu menu) {
		super.setMenu(menu);
		//table.setMenu(menu);
	}

	@Override
	protected void queryCleared() {
		this.channels = null;
		this.errorBar.setException(null);
		setChannels(null);
	}

	@Override
	protected void queryExecuted(Result result) {
		Exception e = result.exception;
		errorBar.setException(e);
		if (e == null) {
			setChannels(result.channels);
		}
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionProvider.addSelectionChangedListener(listener);

	}

	@Override
	public ISelection getSelection() {
		return selectionProvider.getSelection();
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		selectionProvider.removeSelectionChangedListener(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		selectionProvider.setSelection(selection);
	}

	private boolean configurable = true;

	private ROCSConfigurationDialog dialog;

	public void openConfigurationDialog() {
		if (dialog != null)
			return;
		dialog = new ROCSConfigurationDialog(this);
		dialog.open();
	}

	@Override
	public boolean isConfigurable() {
		return configurable;
	}

	@Override
	public void setConfigurable(boolean configurable) {
		boolean oldConfigurable = configurable;
		this.configurable = configurable;
		changeSupport.firePropertyChange("configurable", oldConfigurable,
				configurable);
	}

	@Override
	public boolean isConfigurationDialogOpen() {
		return dialog != null;
	}

	@Override
	public void configurationDialogClosed() {
		dialog = null;
	}

}
