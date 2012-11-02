package org.csstudio.domain.desy.epics.pvmanager;

import java.util.Collection;

import org.epics.pvmanager.DataSourceTypeAdapterSet;

public interface DesyJCATypeAdapterSet extends DataSourceTypeAdapterSet{

	 @Override
	    Collection<DesyJCATypeAdapter> getAdapters();

}
