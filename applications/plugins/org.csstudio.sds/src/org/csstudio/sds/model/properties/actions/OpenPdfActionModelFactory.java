package org.csstudio.sds.model.properties.actions;


public class OpenPdfActionModelFactory implements IActionModelFactory {

	public AbstractWidgetActionModel createWidgetActionModel() {
		return new OpenPdfActionModel();
	}

}
