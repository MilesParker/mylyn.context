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

package org.eclipse.mylar.internal.ui;

import org.eclipse.mylar.internal.core.MylarContextManager;
import org.eclipse.mylar.internal.core.util.MylarStatusHandler;
import org.eclipse.mylar.internal.tasklist.ui.AbstractTaskListFilter;
import org.eclipse.mylar.provisional.core.IMylarElement;
import org.eclipse.mylar.provisional.core.IMylarStructureBridge;
import org.eclipse.mylar.provisional.core.MylarPlugin;
import org.eclipse.mylar.provisional.tasklist.AbstractQueryHit;
import org.eclipse.mylar.provisional.tasklist.ITask;
import org.eclipse.mylar.provisional.tasklist.MylarTaskListPlugin;

/**
 * Goal is to have this reuse as much of the super as possible.
 * 
 * @author Mik Kersten
 */
public class TaskListInterestFilter extends AbstractTaskListFilter {

	// private InterestFilter interestFilter = new InterestFilter();

	@Override
	public boolean select(Object object) {
		try {
			// if (element instanceof AbstractTaskContainer) {
			// AbstractTaskContainer container = (AbstractTaskContainer)
			// element;
			// // TODO: get rid of this work-around to look down?
			// for (ITask task : container.getChildren()) {
			// if (select(viewer, this, task)) {
			// return true;
			// }
			// }
			IMylarElement element = null;
			if (object instanceof ITask || object instanceof AbstractQueryHit) {
				ITask task = null;
				if (object instanceof ITask) {
					task = (ITask) object;
				} else if (object instanceof AbstractQueryHit) {
					task = ((AbstractQueryHit) object).getCorrespondingTask();
				}
				if (task != null) {
					if (isImplicitlyInteresting(task)) {
						return true;
					}
					IMylarStructureBridge bridge = MylarPlugin.getDefault().getStructureBridge(task);
					if (!bridge.canFilter(task)) {
						return true;
					}
					String handle = bridge.getHandleIdentifier(task.getHandleIdentifier());
					element = MylarPlugin.getContextManager().getActivityHistoryMetaContext().get(handle);
				}
			}
			if (element != null) {
				if (element.getInterest().isPredicted()) {
					return false;
				} else {
					return element.getInterest().getValue() > MylarContextManager.getScalingFactors().getInteresting();
				}
			}
		} catch (Throwable t) {
			MylarStatusHandler.fail(t, "interest filter failed", false);
		}
		return false;
	}

	// protected boolean isImplicitlyUninteresting(ITask task) {
	// if (task.isCompleted()) {
	// return true;
	// }
	// return false;
	// }

	protected boolean isImplicitlyInteresting(ITask task) {
		return task.isActive() || task.isPastReminder()
				|| MylarTaskListPlugin.getTaskListManager().isActiveThisWeek(task);
	}
}
