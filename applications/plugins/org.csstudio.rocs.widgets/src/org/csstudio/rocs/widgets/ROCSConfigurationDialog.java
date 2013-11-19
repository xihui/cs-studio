package org.csstudio.rocs.widgets;

import gov.bnl.channelfinder.api.ChannelUtil;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.csstudio.utility.pvmanager.widgets.AbstractConfigurationDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

public class ROCSConfigurationDialog
		extends
		AbstractConfigurationDialog<ROCSWidget, ROCSConfigurationPanel> {

	protected ROCSConfigurationDialog(ROCSWidget widget) {
		super(widget, SWT.DIALOG_TRIM, "Configure Channel Viewer");
		addInitialValues("possibleProperties", new ArrayList<String>(
				ChannelUtil.getPropertyNames(widget.getChannels())));
		addInitialValues("selectedProperties", widget.getProperties());
		addInitialValues(
				"possibleTags",
				new ArrayList<String>(ChannelUtil.getAllTagNames(widget
						.getChannels())));
		addInitialValues("selectedTags", widget.getTags());
		// addInitialValues("showChannelNames", widget.isShowChannelNames());
	}

	@Override
	protected void onPropertyChange(PropertyChangeEvent evt) {
		getWidget().setProperties(
				getConfigurationComposite().getSelectedProperties());
		getWidget().setTags(getConfigurationComposite().getSelectedTags());
		// getWidget().setShowChannelNames(getConfigurationComposite().isShowChannelNames());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void populateInitialValues() {
		getConfigurationComposite().setPossibleProperties(
				(List<String>) getInitialValues().get("possibleProperties"));
		getConfigurationComposite().setSelectedProperties(
				(List<String>) getInitialValues().get("selectedProperties"));
		getConfigurationComposite().setPossibleTags(
				(List<String>) getInitialValues().get("possibleTags"));
		getConfigurationComposite().setSelectedTags(
				(List<String>) getInitialValues().get("selectedTags"));
		// getConfigurationComposite().setShowChannelNames((Boolean)
		// getInitialValues().get("showChannelNames"));
	}

	@Override
	protected ROCSConfigurationPanel createConfigurationComposite(
			Shell shell) {
		return new ROCSConfigurationPanel(shell, SWT.None);
	}

}
