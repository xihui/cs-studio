package org.csstudio.sds.ui.widgetactionhandler;

import java.net.MalformedURLException;
import java.net.URL;

import org.csstudio.sds.model.AbstractWidgetModel;
import org.csstudio.sds.model.properties.actions.AbstractWidgetActionModel;
import org.csstudio.sds.model.properties.actions.OpenUrlActionModel;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenUrlActionHandler implements IWidgetActionHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(OpenUrlActionHandler.class);

	public void executeAction(AbstractWidgetModel widget,
			AbstractWidgetActionModel action) {
		assert action instanceof OpenUrlActionModel : "action instanceof OpenUrlActionModel";

		OpenUrlActionModel urlModel = (OpenUrlActionModel) action;

		String resource = urlModel.getResource();
		//if there is no protocol use http
		if (!resource.contains("://")) {
			resource = "http://" + resource;
		}
		URL url = null;
		try {
			url = new URL(resource);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
        if (url != null) {
            try {
                // Note: we have to pass a browser id here to work
                // around a bug in eclipse. The method documentation
                // says that createBrowser accepts null but it will
                // throw a NullPointerException.
                // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=194988
                final IWebBrowser browser =
                    PlatformUI.getWorkbench().getBrowserSupport().createBrowser("workaround");
                browser.openURL(url);
            } catch (final PartInitException e) {
                LOG.error("Failed to initialize workbench browser.", e);
            }
        }
	}
}
