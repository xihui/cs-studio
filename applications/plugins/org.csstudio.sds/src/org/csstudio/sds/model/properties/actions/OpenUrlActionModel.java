package org.csstudio.sds.model.properties.actions;

import org.csstudio.sds.internal.model.ResourceProperty;
import org.csstudio.sds.internal.model.StringProperty;
import org.csstudio.sds.model.ActionType;
import org.csstudio.sds.model.WidgetPropertyCategory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * A {@link AbstractWidgetActionModel}, which opens an Url in default browser.
 * 
 * @author Kai Meyer
 */
public class OpenUrlActionModel extends AbstractWidgetActionModel {
	/**
	 * The ID for the <i>resource</i> property.
	 */
	public static final String PROP_URL = "url";
	
	/**
	 * The ID for the <i>description</i> property.
	 */
	public static final String PROP_DESCRIPTION = "description";

	public OpenUrlActionModel() {
		super(ActionType.OPEN_URL.getTitle(), ActionType.OPEN_URL);
	}

	@Override
	protected void createProperties() {
		StringProperty url = new StringProperty("Url",
				WidgetPropertyCategory.BEHAVIOR, "");
		addProperty(PROP_URL, url);
		StringProperty description = new StringProperty("Description",
				WidgetPropertyCategory.BEHAVIOR, "");
		addProperty(PROP_DESCRIPTION, description);
	}
	
	/**
	 * Returns the {@link IPath} to the display.
	 * 
	 * @return The {@link IPath} to the display
	 */
	public String getResource() {
		return getProperty(PROP_URL).getPropertyValue();
	}
	
	/**
	 * Returns the description.
	 * 
	 * @return The description
	 */
	public String getDescription() {
		return getProperty(PROP_DESCRIPTION).getPropertyValue();
	}

	@Override
	public String getActionLabel() {
		return getDescription();
	}

}
