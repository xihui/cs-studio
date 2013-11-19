package org.csstudio.rocs.views;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.csstudio.rocs.widgets.Template;
import org.csstudio.rocs.widgets.TemplateRegistry;
import org.csstudio.rocs.widgets.TemplateXMLFactory;
import org.csstudio.rocs.widgets.Templates;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.csstudio.rocs.views"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private static final TemplateXMLFactory templateXMLFactory = new TemplateXMLFactory();
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		ExecutorService defaultExecutor = Executors.newSingleThreadExecutor(Templates.namedPool("ROCS Template"));
		defaultExecutor.submit(new Runnable(){

			@Override
			public void run() {
				for(Template template : templateXMLFactory.createTemplates()){
					TemplateRegistry.getDefault().registerTemplate(template);
				}
			}});

		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
