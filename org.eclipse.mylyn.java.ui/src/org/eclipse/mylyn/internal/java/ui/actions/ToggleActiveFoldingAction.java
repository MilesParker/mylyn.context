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
/*
 * Created on Jul 29, 2004
 */
package org.eclipse.mylar.internal.java.ui.actions;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.mylar.context.ui.ContextUiPlugin;
import org.eclipse.mylar.core.MylarStatusHandler;
import org.eclipse.mylar.internal.context.ui.ContextUiPrefContstants;
import org.eclipse.mylar.internal.context.ui.ContextUiImages;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * @author Mik Kersten
 * 
 * TODO: move to UI
 */
public class ToggleActiveFoldingAction extends Action implements IWorkbenchWindowActionDelegate, IActionDelegate2 {

	private static ToggleActiveFoldingAction INSTANCE;

	private IAction parentAction = null;

	public ToggleActiveFoldingAction() {
		super();
		INSTANCE = this;
		setText("Active folding");
		setImageDescriptor(ContextUiImages.INTEREST_FOLDING);
	}

	public static void toggleFolding(boolean on) {
		if (INSTANCE.parentAction != null) {
			INSTANCE.valueChanged(INSTANCE.parentAction, on);
		}
	}

	public void run(IAction action) {
		valueChanged(action, action.isChecked());
	}

	private void valueChanged(IAction action, final boolean on) {
		try {
			if (on) {
				JavaPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.EDITOR_FOLDING_ENABLED, true);
			}
			action.setChecked(on);
			ContextUiPlugin.getDefault().getPreferenceStore().setValue(ContextUiPrefContstants.ACTIVE_FOLDING_ENABLED, on);
		} catch (Throwable t) {
			MylarStatusHandler.fail(t, "Could not enable editor management", true);
		}
	} 

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		// don't care when the active editor changes
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// don't care when the selection changes
	}

	public void init(IAction action) {
		this.parentAction = action;
		valueChanged(action, ContextUiPlugin.getDefault().getPreferenceStore().getBoolean(
				ContextUiPrefContstants.ACTIVE_FOLDING_ENABLED));
	}

	public void dispose() {
		// don't need to do anything

	}

	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	public void init(IWorkbenchWindow window) {
	}
}
