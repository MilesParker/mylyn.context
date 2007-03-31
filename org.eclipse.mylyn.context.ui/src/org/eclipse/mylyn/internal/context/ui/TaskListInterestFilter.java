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

import java.util.Calendar;

import org.eclipse.mylar.core.MylarStatusHandler;
import org.eclipse.mylar.internal.tasks.ui.AbstractTaskListFilter;
import org.eclipse.mylar.internal.tasks.ui.actions.NewLocalTaskAction;
import org.eclipse.mylar.tasks.core.AbstractQueryHit;
import org.eclipse.mylar.tasks.core.AbstractRepositoryTask;
import org.eclipse.mylar.tasks.core.DateRangeContainer;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.core.AbstractRepositoryTask.RepositoryTaskSyncState;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;

/**
 * Goal is to have this reuse as much of the super as possible.
 * 
 * @author Mik Kersten
 */
public class TaskListInterestFilter extends AbstractTaskListFilter {

	@Override
	public boolean select(Object parent, Object object) {
		try {
			if (object instanceof DateRangeContainer) {
				DateRangeContainer dateRangeTaskContainer = (DateRangeContainer) object;
				return isDateRangeInteresting(dateRangeTaskContainer);
			}
			if (object instanceof ITask || object instanceof AbstractQueryHit) {
				ITask task = null;
				if (object instanceof ITask) {
					task = (ITask) object;
				} else if (object instanceof AbstractQueryHit) {
					task = ((AbstractQueryHit) object).getCorrespondingTask();
				}
				if (task != null) {
					if (isUninteresting(parent, task)) {
						return false;
					} else if (isInteresting(parent, task)) {
						return true;
					}
				} else if (object instanceof AbstractQueryHit) {
					return true;
				}
			}
		} catch (Throwable t) {
			MylarStatusHandler.fail(t, "interest filter failed", false);
		}
		return false;
	}

	private boolean isDateRangeInteresting(DateRangeContainer container) {
		return (TasksUiPlugin.getTaskListManager().isWeekDay(container));// ||dateRangeTaskContainer.isFuture();
	}

	protected boolean isUninteresting(Object parent, ITask task) {
		return !task.isActive()
				&& ((task.isCompleted() && !TasksUiPlugin.getTaskListManager().isCompletedToday(task) && !hasChanges(
						parent, task)) || (TasksUiPlugin.getTaskListManager().isScheduledAfterThisWeek(task))
						&& !hasChanges(parent, task));
	}

	// TODO: make meta-context more explicit
	protected boolean isInteresting(Object parent, ITask task) {
		return shouldAlwaysShow(parent, task);
	}

	@Override
	public boolean shouldAlwaysShow(Object parent, ITask task) {
		return super.shouldAlwaysShow(parent, task) || hasChanges(parent, task)
				|| (TasksUiPlugin.getTaskListManager().isCompletedToday(task))
				||  shouldShowInFocusedWorkweekDateContainer(parent, task)
				|| (isInterestingForThisWeek(parent, task) && !task.isCompleted())
				|| (TasksUiPlugin.getTaskListManager().isOverdue(task))
				|| NewLocalTaskAction.DESCRIPTION_DEFAULT.equals(task.getSummary());
		// || isCurrentlySelectedInEditor(task);
	}

	private static boolean shouldShowInFocusedWorkweekDateContainer(Object parent, ITask task) {
		if (parent instanceof DateRangeContainer) {
			// if(task.isCompleted()) {
			// return false;
			// }
			// boolean overdue =
			// TasksUiPlugin.getTaskListManager().isOverdue(task);
			// if (overdue || task.isPastReminder()) {
			// return true;
			// }

			DateRangeContainer container = (DateRangeContainer) parent;
			Calendar previousCal = TasksUiPlugin.getTaskListManager().getActivityPrevious().getEnd();
			Calendar nextCal = TasksUiPlugin.getTaskListManager().getActivityNextWeek().getStart();
			if (container.getEnd().compareTo(previousCal) <= 0 || container.getStart().compareTo(nextCal) >= 0) {
				// not within workweek
				return false;
			} else {
				return true;
			}
		}

		return false;
	}

	public static boolean isInterestingForThisWeek(Object parent, ITask task) {
		if (parent instanceof DateRangeContainer) {
			return shouldShowInFocusedWorkweekDateContainer(parent, task);
		} else {
			return TasksUiPlugin.getTaskListManager().isScheduledForThisWeek(task)
					|| TasksUiPlugin.getTaskListManager().isScheduledForToday(task) || task.isPastReminder();
		}
	}

	public static boolean hasChanges(Object parent, ITask task) {
		if (parent instanceof DateRangeContainer) {
			if (!shouldShowInFocusedWorkweekDateContainer(parent, task)) {
				return false;
			}
		}
		if (task instanceof AbstractRepositoryTask) {
			AbstractRepositoryTask repositoryTask = (AbstractRepositoryTask) task;
			if (repositoryTask.getSyncState() == RepositoryTaskSyncState.OUTGOING) {
				return true;
			} else if (repositoryTask.getSyncState() == RepositoryTaskSyncState.INCOMING
					&& !(parent instanceof DateRangeContainer)) {
				return true;
			} else if (repositoryTask.getSyncState() == RepositoryTaskSyncState.CONFLICT) {
				return true;
			}
		}
		return false;
	}
}
