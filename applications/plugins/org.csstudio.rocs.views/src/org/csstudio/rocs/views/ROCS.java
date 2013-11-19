package org.csstudio.rocs.views;

import org.csstudio.rocs.widgets.ROCSWidget;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;

public class ROCS extends AbstractChannelQueryView<ROCSWidget> {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.csstudio.rocs.views.ROCS";

	@Override
	public void saveWidgetState(ROCSWidget widget, IMemento memento) {
	}

	@Override
	public void loadWidgetState(ROCSWidget widget, IMemento memento) {
	}

	@Override
	protected ROCSWidget createChannelQueryWidget(Composite parent,
			int style) {
		return new ROCSWidget(parent, style);
	}
}
