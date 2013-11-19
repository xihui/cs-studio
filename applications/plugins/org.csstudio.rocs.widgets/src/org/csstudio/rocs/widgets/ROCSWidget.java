package org.csstudio.rocs.widgets;

import gov.bnl.channelfinder.api.Channel;
import gov.bnl.channelfinder.api.ChannelQuery.Result;
import gov.bnl.channelfinder.api.ChannelUtil;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.csstudio.utility.file.IFileUtil;
import org.csstudio.openfile.DisplayUtil;
import org.csstudio.ui.util.widgets.ErrorBar;
import org.csstudio.utility.pvmanager.widgets.ConfigurableWidget;
import org.eclipse.core.resources.IFile;
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


public class ROCSWidget extends AbstractChannelQueryResultWidget
		implements ISelectionProvider, ConfigurableWidget {

	private ErrorBar errorBar;

	// Simple Model
	private Collection<Channel> channels = new ArrayList<Channel>();
	private AbstractSelectionProviderWrapper selectionProvider;

	private List<String> properties;
	private List<String> propertyValues;
	private List<String> tags;
	
	private enum RunOpiOptions{
		DETACHED("Detached"),EDIT("Edit"),ATTACHED("Attached");
		
		private String option;
		private RunOpiOptions(String s){
			option = s;
		}
	}
	
	private RunOpiOptions option = RunOpiOptions.DETACHED;
	
	private Map<String, HashMap<String,Channel>> cmap;

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
			this.cmap =  new TreeMap<String, HashMap<String,Channel>>();
			
			for (String propertyValue: propertyValues){
				HashMap<String,Channel> ids = new HashMap<String,Channel>();
				Collection<Channel> chans = filterByProperty(channels, "Device", propertyValue);
				Set<String> unique = new HashSet<String>(ChannelUtil.getPropValues(chans, "D"));
				for (String id : unique){
					ids.put(id, filterByProperty(chans, "D", id).iterator().next());
				}
				cmap.put(propertyValue, ids);
			}
		} else {
			this.properties = Collections.emptyList();
			this.tags = Collections.emptyList();
			this.propertyValues = Collections.emptyList();
			this.cmap = Collections.emptyMap();
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

		if (cmap != null) {
			for (Map.Entry<String, HashMap<String,Channel>> entry : cmap.entrySet()) {

				Template template = TemplateRegistry.getDefault().findTemplate(entry.getKey());
				try{
					
					//IFile resource = "get resource somehow";
					//  if (resource != null) resource.refreshLocal(IResource.DEPTH_INFINITE, monitor);
					  
					InputStream inputStream = template.getExecutorService().submit(new DisplayTemplate(entry,template)).get();
					IFileUtil fileUtil = IFileUtil.getInstance();
					IWorkbench workbench = PlatformUI.getWorkbench();
					String fileName = entry.getKey()+".opi";
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
		private Map.Entry<String, HashMap<String,Channel>> entry;
		private Template template;

		DisplayTemplate(Map.Entry<String, HashMap<String,Channel>> entry, Template template){
			this.entry = entry;
			this.template = template;
		}

		@Override
		public InputStream call() throws Exception {
			XMLDisplay xmlDisplay = new XMLDisplay();
			xmlDisplay.addTemplate(TemplateRegistry.getDefault().findTemplate(entry.getKey()+"_header"),null);
			Iterator itr = entry.getValue().keySet().iterator(); 
			for (Integer i = 0; i < entry.getValue().keySet().size(); i++) {
				Channel chan = entry.getValue().get(itr.next());
				xmlDisplay.addTemplate(template,chan.getProperties());
			}

			OutputStream output = new ByteArrayOutputStream();
			JAXBContext jc = JAXBContext.newInstance(XMLDisplay.class, XMLWidget.class);
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(xmlDisplay, output);
			
			return new ByteArrayInputStream(((ByteArrayOutputStream) output).toByteArray());
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
