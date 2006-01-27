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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * @author Mik Kersten
 */
public class OpenPrefsAction implements IWorkbenchWindowActionDelegate {

	public void dispose() {
		// don't care when we are disposed
	}

	public void init(IWorkbenchWindow window) {
		// don't have anything to initialize
	}

	public void run(IAction action) {
		// don't have anything to run
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// don't care about selection changes
	}
}
