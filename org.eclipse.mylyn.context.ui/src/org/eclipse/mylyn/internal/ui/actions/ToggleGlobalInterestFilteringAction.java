/*******************************************************************************
 * Copyright (c) 2004 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.mylar.internal.ui.MylarImages;
import org.eclipse.mylar.provisional.core.IMylarContextListener;
import org.eclipse.mylar.provisional.core.MylarPlugin;
import org.eclipse.mylar.provisional.ui.MylarUiPlugin;

/**
 * @author Mik Kersten
 */
public class ToggleGlobalInterestFilteringAction extends Action {

	public static final String ID = "org.eclipse.mylar.ui.interest.filter.global";

	public ToggleGlobalInterestFilteringAction() {
		super();
		setText("Apply Mylar to All Views");
		setToolTipText("Apply Mylar to All Views");
		setImageDescriptor(MylarImages.INTEREST_FILTERING);
		setActionDefinitionId(ID);
		// setChecked(MylarUiPlugin.getDefault().isGlobalFilteringEnabled());
	}

	@Override
	public void run() {
		setChecked(isChecked());
		MylarUiPlugin.getDefault().setGlobalFilteringEnabled(isChecked());
		MylarPlugin.getContextManager().notifyPostPresentationSettingsChange(IMylarContextListener.UpdateKind.UPDATE);
		// MylarPlugin.getTaskscapeManager().notifyPostPresentationSettingsChange(
		// ITaskscapeListener.UpdateKind.UPDATE);
	}
}
