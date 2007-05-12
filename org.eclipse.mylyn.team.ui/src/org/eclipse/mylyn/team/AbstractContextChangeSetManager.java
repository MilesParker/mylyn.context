/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylar.team;

import java.util.List;

import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.mylar.context.core.IMylarContextListener;
import org.eclipse.mylar.context.core.IMylarElement;
import org.eclipse.mylar.tasks.core.AbstractTaskContainer;
import org.eclipse.mylar.tasks.core.DateRangeContainer;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.core.ITaskActivityListener;
import org.eclipse.mylar.tasks.core.ITaskListChangeListener;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;

/**
 * @author Mik Kersten
 */
public abstract class AbstractContextChangeSetManager implements IMylarContextListener {

	protected boolean isEnabled = false;
		
	public void enable() {
		if (!isEnabled) {
			ContextCorePlugin.getContextManager().addListener(this);
			TasksUiPlugin.getTaskListManager().getTaskList().addChangeListener(TASK_CHANGE_LISTENER);
			TasksUiPlugin.getTaskListManager().addActivityListener(TASK_ACTIVITY_LISTENER);
			if (TasksUiPlugin.getTaskListManager().isTaskListInitialized()) {
				initContextChangeSets(); // otherwise listener will do it
			}
			isEnabled = true;
		}
	}

	public void disable() {
		ContextCorePlugin.getContextManager().removeListener(this);
		TasksUiPlugin.getTaskListManager().removeActivityListener(TASK_ACTIVITY_LISTENER);
		TasksUiPlugin.getTaskListManager().getTaskList().removeChangeListener(TASK_CHANGE_LISTENER);
		isEnabled = false;
	}
	
	protected abstract void initContextChangeSets();
	
	protected abstract void updateChangeSetLabel(ITask task);
	
	private ITaskActivityListener TASK_ACTIVITY_LISTENER = new ITaskActivityListener() {

		public void taskListRead() {
			initContextChangeSets();
		}

		public void taskActivated(ITask task) {
			// ignore
		}

		public void tasksActivated(List<ITask> tasks) {
			// ignore
		}

		public void taskDeactivated(ITask task) {
			// ignore
		}

		public void activityChanged(DateRangeContainer week) {
			// ignore
		}

		public void calendarChanged() {
			// ignore
		}
	};

	private ITaskListChangeListener TASK_CHANGE_LISTENER = new ITaskListChangeListener() {

		public void localInfoChanged(ITask task) {
			updateChangeSetLabel(task);
		}

		public void repositoryInfoChanged(ITask task) {
			// ignore
		}

		public void taskMoved(ITask task, AbstractTaskContainer fromContainer, AbstractTaskContainer toContainer) {
			// ignore
		}

		public void taskDeleted(ITask task) {
			// ignore
		}

		public void containerAdded(AbstractTaskContainer container) {
			// ignore
		}

		public void containerDeleted(AbstractTaskContainer container) {
			// ignore
		}

		public void taskAdded(ITask task) {
			// ignore
		}

		public void containerInfoChanged(AbstractTaskContainer container) {
			// ignore
		}
	};

	public void elementDeleted(IMylarElement node) {
		// TODO: handle?
	}

	public void landmarkAdded(IMylarElement node) {
		// ignore
	}

	public void landmarkRemoved(IMylarElement node) {
		// ignore
	}

	public void relationsChanged(IMylarElement node) {
		// ignore
	}
}