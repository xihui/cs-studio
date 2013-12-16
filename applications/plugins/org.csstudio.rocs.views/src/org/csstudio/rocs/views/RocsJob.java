package org.csstudio.rocs.views;

import org.csstudio.rocs.widgets.Template;
import org.csstudio.rocs.widgets.TemplateRegistry;
import org.csstudio.rocs.widgets.TemplateXMLFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class RocsJob extends Job {
	public static final String JOB_FAMILY_NAME = "ROCS Templates";
	private static final TemplateXMLFactory templateXMLFactory = new TemplateXMLFactory();
	
	public RocsJob(String name) {
		super(name);
	}

    public boolean belongsTo(Object family) {
       return JOB_FAMILY_NAME.equals(family);
    }

	@Override
	protected IStatus run(IProgressMonitor monitor) {

		try{
			monitor.beginTask("Registering templates", 100);
			for(Template template : templateXMLFactory.createTemplates()){
				monitor.subTask("Loading ...");
				TemplateRegistry.getDefault().registerTemplate(template);
				monitor.worked(1/templateXMLFactory.getFiles().size()*100);
				if (monitor.isCanceled()) return Status.CANCEL_STATUS;
			}
			} catch (Exception ex){
				ex.printStackTrace();
				return Status.CANCEL_STATUS;
			} finally {
				monitor.done();
			}
			return Status.OK_STATUS;
		}
		
	
}
