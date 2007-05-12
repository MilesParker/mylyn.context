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

package org.eclipse.mylar.internal.context.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.mylar.context.core.IMylarContext;
import org.eclipse.mylar.context.core.IMylarContextListener;
import org.eclipse.mylar.context.core.IMylarElement;
import org.eclipse.mylar.context.core.AbstractContextStructureBridge;
import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.mylar.context.ui.ContextUiPlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetUpdater;

/**
 * @author Shawn Minto
 * @author Mik Kersten
 */
public class MylarWorkingSetManager implements IWorkingSetUpdater, IMylarContextListener {

	/** Should only ever have 1 working set */
	private List<IWorkingSet> workingSets = new ArrayList<IWorkingSet>();

	public MylarWorkingSetManager() {
		ContextUiPlugin.getDefault().addWorkingSetManager(this);
	}

	public void add(IWorkingSet workingSet) {
		workingSets.add(workingSet);
	}

	public boolean remove(IWorkingSet workingSet) {
		return workingSets.remove(workingSet);

	}

	public boolean contains(IWorkingSet workingSet) {
		return workingSets.contains(workingSet);
	}

	public void dispose() {
		// nothing to do here
	}

	public void contextActivated(IMylarContext context) {
		updateWorkingSet();
	}

	public void contextDeactivated(IMylarContext context) {
		updateWorkingSet();
	}
	
	public void contextCleared(IMylarContext context) {
		updateWorkingSet();
	}

	public void interestChanged(List<IMylarElement> nodes) {
		updateWorkingSet();

	}

	public void elementDeleted(IMylarElement node) {
		updateWorkingSet();
	}

	public void landmarkAdded(IMylarElement node) {
		updateWorkingSet();

	}

	public void landmarkRemoved(IMylarElement node) {
		updateWorkingSet();

	}

	public void relationsChanged(IMylarElement node) {
		// don't care about this relationship

	}

	private void updateWorkingSet() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (workingSets.size() <= 0)
					return;
				IWorkingSet set = workingSets.get(0);
				set.setElements(new IAdaptable[] {});
				List<IAdaptable> elements = new ArrayList<IAdaptable>();
				getElementsFromTaskscape(elements);
				set.setElements(elements.toArray(new IAdaptable[elements.size()]));
			}
		});
	}

	public static void getElementsFromTaskscape(List<IAdaptable> elements) {
		// IMylarContext t = ContextCorePlugin.getContextManager().getActiveContext();
		for (IMylarElement node : ContextCorePlugin.getContextManager().getInterestingDocuments()) {
			AbstractContextStructureBridge bridge = ContextCorePlugin.getDefault().getStructureBridge(node.getContentType());

			// HACK comparing extension to string
			// No need to add bugzilla resources to the taskscape
			// search...really slow and eclipese doesn't know about them
			if (bridge.getContentType().equals("bugzilla"))
				continue;

			Object o = bridge.getObjectForHandle(node.getHandleIdentifier());
			if (o instanceof IAdaptable) {
				elements.add((IAdaptable) o);
			}

		}
	}

	public IWorkingSet getWorkingSet() {
		return workingSets.get(0);
	}
}
