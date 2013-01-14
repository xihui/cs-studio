/* 
 * Copyright (c) 2008 Stiftung Deutsches Elektronen-Synchrotron, 
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
 package org.csstudio.sds.ui.internal.editor;

import org.csstudio.sds.ui.SdsUiPlugin;
import org.csstudio.sds.ui.runmode.RunModeService;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * A {@link ControlContribution} to run the sds display in the active window.
 * 
 * @author jhatje
 *
 */
public final class RunDisplayContributionItem extends ControlContribution 
	implements SelectionListener {
		
	/**
	 * Constructor.
	 */
	public RunDisplayContributionItem() {
		super("RUN_DISPLAY");
	}
	
	/**
	 * Creates the control for this {@link ContributionItem}.
	 * @param parent The parent composite
	 * @return The Control
	 */
	protected Control createControl(final Composite parent) {
		Button runButton = new Button(parent, SWT.NONE);
		runButton.setImage(CustomMediaFactory.getInstance().getImageFromPlugin(SdsUiPlugin.PLUGIN_ID, "icons/run_exc.png"));
		runButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				//TODO (jhatje): Is there a better location to implement the run button in menu bar?
				//Maybe the context menu is incomplete.
		        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		        IEditorPart activeEditor = page.getActiveEditor();
		        if(activeEditor instanceof DisplayEditor) {
		            DisplayEditor editor = (DisplayEditor)activeEditor;
		            RunModeService.getInstance().openDisplayShellInRunMode(editor.getFilePath());
		        }
		        
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		return runButton;
	}
	
	/**
	 * Computes the width required by control.
	 * @param control The control to compute width
	 * @return int The width required
	 */
	protected int computeWidth(final Control control) {
		return control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
	}
	
	/**
	 * Sets the preference value.
	 */
	private void setPreferenceValue() {
	}

	/**
	 * {@inheritDoc}
	 */
	public void widgetDefaultSelected(final SelectionEvent e) {
		this.setPreferenceValue();
	}

	/**
	 * {@inheritDoc}
	 */
	public void widgetSelected(final SelectionEvent e) {
		this.setPreferenceValue();
	}

	/**
	 * {@inheritDoc}
	 */
	public void modifyText(final ModifyEvent e) {
		this.setPreferenceValue();
	}

	/**
	 * {@inheritDoc}
	 */
	public void propertyChange(final PropertyChangeEvent event) {
	}
}
