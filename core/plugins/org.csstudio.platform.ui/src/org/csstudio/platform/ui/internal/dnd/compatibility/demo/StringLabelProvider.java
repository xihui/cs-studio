package org.csstudio.platform.ui.internal.dnd.compatibility.demo;

import org.csstudio.platform.model.IProcessVariable;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * <code>LabelProvider</code> for <class>ProcessVariableName</class> data.
 * 
 * @author Kay Kasemir
 */
public class StringLabelProvider extends LabelProvider implements
		ITableLabelProvider {
	public String getColumnText(final Object obj, final int index) {
		return ((IProcessVariable) obj).getName();
	}

	public Image getColumnImage(final Object obj, final int index) {
		return null;
	}
}
