package org.csstudio.alarm.table.preferences;

import java.text.SimpleDateFormat;

import org.csstudio.alarm.table.JmsLogsPlugin;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;


/**
 * Class used to initialize default preference values.
 */
public class LogArchiveViewerPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = JmsLogsPlugin.getDefault().getPreferenceStore();
		store.setDefault(LogArchiveViewerPreferenceConstants.P_STRINGArch,
				"TYPE" + ";" + //$NON-NLS-1$ //$NON-NLS-2$
				"EVENTTIME" + ";" + //$NON-NLS-1$ //$NON-NLS-2$
				"TEXT" + ";" + //$NON-NLS-1$ //$NON-NLS-2$
				"USER" + ";" + //$NON-NLS-1$ //$NON-NLS-2$
				"HOST" + ";" + //$NON-NLS-1$ //$NON-NLS-2$
				"APPLICATION-ID" + ";" + //$NON-NLS-1$ //$NON-NLS-2$
				"PROCESS-ID" + ";" + //$NON-NLS-1$ //$NON-NLS-2$
				"NAME" + ";" + //$NON-NLS-1$ //$NON-NLS-2$
				"CLASS" + ";" + //$NON-NLS-1$ //$NON-NLS-2$
				"DOMAIN" + ";" + //$NON-NLS-1$ //$NON-NLS-2$
				"FACILITY" + ";" + //$NON-NLS-1$ //$NON-NLS-2$
				"LOCATION" + ";" + //$NON-NLS-1$ //$NON-NLS-2$
				"SEVERITY" + ";" + //$NON-NLS-1$ //$NON-NLS-2$
				"STATUS" + ";" + //$NON-NLS-1$ //$NON-NLS-2$
				"VALUE" + ";" + //$NON-NLS-1$ //$NON-NLS-2$
				"DESTINATION" //$NON-NLS-1$
		);
        store.setDefault(LogArchiveViewerPreferenceConstants.DATE_FORMAT,"yyyy-MM-dd HH:mm:ss.S"); //$NON-NLS-1$
//		store.setDefault(LogArchiveViewerPreferenceConstants.DATE_FORMAT,"E d-MMMM-yyyy H:mm:ss"); //$NON-NLS-1$
	}

}
