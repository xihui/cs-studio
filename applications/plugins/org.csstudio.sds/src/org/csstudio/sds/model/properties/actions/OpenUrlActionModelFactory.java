package org.csstudio.sds.model.properties.actions;


public class OpenUrlActionModelFactory implements IActionModelFactory {

	public AbstractWidgetActionModel createWidgetActionModel() {
		return new OpenUrlActionModel();
	}

}
