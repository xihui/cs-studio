package org.csstudio.rocs.views;

import java.util.List;

import gov.bnl.channelfinder.api.ChannelQuery;

import org.csstudio.ui.util.AbstractAdaptedHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.PartInitException;


public class OpenROCS extends AbstractAdaptedHandler<ChannelQuery> {

	public OpenROCS() {
		super(ChannelQuery.class);
	}
	
	@Override
	protected void execute(List<ChannelQuery> queries, ExecutionEvent event) throws PartInitException {
		if (!queries.isEmpty()) {
			findView(ROCS.class, ROCS.ID)
					.setChannelQuery(queries.get(0));
		}
	}

}
