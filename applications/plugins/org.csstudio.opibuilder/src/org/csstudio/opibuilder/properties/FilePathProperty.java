/* 
 * Copyright (c) 2006 Stiftung Deutsches Elektronen-Synchroton, 
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY.
 *
 * THIS SOFTWARE IS PROVIDED UNDER THIS LICENSE ON AN "../AS IS" BASIS. 
 * WITHOUT WARRANTY OF ANY KIND, EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED 
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR PARTICULAR PURPOSE AND 
 * NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR 
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE. SHOULD THE SOFTWARE PROVE DEFECTIVE 
 * IN ANY RESPECT, THE USER ASSUMES THE COST OF ANY NECESSARY SERVICING, REPAIR OR 
 * CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL PART OF THIS LICENSE. 
 * NO USE OF ANY SOFTWARE IS AUTHORIZED HEREUNDER EXCEPT UNDER THIS DISCLAIMER.
 * DESY HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, 
 * OR MODIFICATIONS.
 * THE FULL LICENSE SPECIFYING FOR THE SOFTWARE THE REDISTRIBUTION, MODIFICATION, 
 * USAGE AND OTHER RIGHTS AND OBLIGATIONS IS INCLUDED WITH THE DISTRIBUTION OF THIS 
 * PROJECT IN THE FILE LICENSE.HTML. IF THE LICENSE IS NOT INCLUDED YOU MAY FIND A COPY 
 * AT HTTP://WWW.DESY.DE/LEGAL/LICENSE.HTM
 */

package org.csstudio.opibuilder.properties;

import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.properties.support.FilePathPropertyDescriptor;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jdom.Element;



/**
 * The property for file path, which is represented as an {@link IPath}.
 * @author Xihui Chen, Kai Meyer (similar class as in SDS)
 *
 */
public class FilePathProperty extends AbstractWidgetProperty {

	/**
	 * The file extension, which should be accepted.
	 */
	private String[] fileExtensions;
	
	public FilePathProperty(String prop_id, String description,
			WidgetPropertyCategory category, IPath defaultValue,
			String[] fileExtensions) {
		super(prop_id, description, category, defaultValue);
		this.fileExtensions = fileExtensions;
	}

	@Override
	public Object checkValue(Object value) {
		if(value == null)
			return null;
		Object acceptedValue = null;
		
		if (value instanceof IPath || value instanceof String) {
			IPath path;
			if(value instanceof String)
				path = ResourceUtil.getPathFromString((String) value);
			else 
				path = (IPath) value;
			if (fileExtensions!=null && fileExtensions.length>0) {
				for (String extension : fileExtensions) {
					if (extension.equalsIgnoreCase(path.getFileExtension())) {
						acceptedValue = path; 
					}
					if(extension.equals("*"))
						acceptedValue = path; 
				}
			} else {
				acceptedValue = path;
			}
			if (path.isEmpty()) {
				acceptedValue = path;
			}
		}
		
		return acceptedValue;
	}

	@Override
	protected PropertyDescriptor createPropertyDescriptor() {
		return new FilePathPropertyDescriptor(prop_id, 
				description,
				widgetModel, 
				fileExtensions);
	}
	
	@Override
	public Object getPropertyValue() {
		if(widgetModel !=null && widgetModel.getExecutionMode() == ExecutionMode.RUN_MODE
				&& propertyValue != null &&
				!((IPath)propertyValue).isEmpty() && !((IPath)propertyValue).isAbsolute()){
				return ResourceUtil.buildAbsolutePath(widgetModel, (IPath) propertyValue);
		}			
		return super.getPropertyValue();
	}
	

	@Override
	public Object readValueFromXML(Element propElement) {
		return Path.fromPortableString(propElement.getText());
	}

	@Override
	public void writeToXML(Element propElement) {
		propElement.setText(((IPath)getPropertyValue()).toPortableString());
	}

}
