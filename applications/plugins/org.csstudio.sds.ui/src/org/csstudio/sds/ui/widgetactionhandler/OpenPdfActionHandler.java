package org.csstudio.sds.ui.widgetactionhandler;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.csstudio.sds.model.AbstractWidgetModel;
import org.csstudio.sds.model.properties.actions.AbstractWidgetActionModel;
import org.csstudio.sds.model.properties.actions.OpenPdfActionModel;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenPdfActionHandler implements IWidgetActionHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(OpenPdfActionHandler.class);

	public void executeAction(AbstractWidgetModel widget,
			AbstractWidgetActionModel action) {
		assert action instanceof OpenPdfActionModel : "action instanceof OpenPdfActionModel";

		OpenPdfActionModel pdfModel = (OpenPdfActionModel) action;

		//TODO (jhatje): find a better way to get absolute path
		IPath path = pdfModel.getResource();
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		URI locationURI = file.getLocationURI();
		File file3 = new File(locationURI);
		LOG.debug("Open " + locationURI + " in external pdf viewer");
		try {
			Desktop.getDesktop().open(file3);
		} catch (IOException e) {
			LOG.error("Error opening " + locationURI);
		}
	}
}
