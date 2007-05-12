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

package org.eclipse.mylar.internal.context.ui.actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.mylar.context.ui.InterestFilter;
import org.eclipse.mylar.internal.context.ui.TaskListInterestFilter;
import org.eclipse.mylar.internal.context.ui.TaskListInterestSorter;
import org.eclipse.mylar.internal.tasks.ui.AbstractTaskListFilter;
import org.eclipse.mylar.internal.tasks.ui.views.IFilteredTreeListener;
import org.eclipse.mylar.internal.tasks.ui.views.TaskListView;
import org.eclipse.ui.IViewPart;

/**
 * TODO: abuses contract from super class
 * 
 * @author Mik Kersten
 */
public class FocusTaskListAction extends AbstractFocusViewAction implements IFilteredTreeListener {

	private TaskListInterestFilter taskListInterestFilter = new TaskListInterestFilter();

	private TaskListInterestSorter taskListInterestSorter = new TaskListInterestSorter();

	private Set<AbstractTaskListFilter> previousFilters = new HashSet<AbstractTaskListFilter>();

	private ViewerSorter previousSorter;

	public FocusTaskListAction() {
		super(new InterestFilter(), false, true, false);
	}

	@Override
	public void init(IViewPart view) {
		super.init(view);
		IViewPart part = super.getPartForAction();
		if (part instanceof TaskListView) {
			((TaskListView) part).getFilteredTree().getRefreshPolicy().addListener(this);
		}
		// NOTE: if re-enabling this, ensure that two filters can not get added
		// on startup
		// if
		// (!TasksUiPlugin.getTaskListManager().getActivityThisWeek().getChildren().isEmpty())
		// {
		// update(true);
		// }
	}

	@Override
	public void dispose() {
		super.dispose();
		IViewPart part = super.getPartForAction();
		if (part instanceof TaskListView) {
			((TaskListView) part).getFilteredTree().getRefreshPolicy().removeListener(this);
		}
	}

	@Override
	public void run(IAction action) {
		super.run(action);
		IViewPart part = super.getPartForAction();
		if (part instanceof TaskListView) {
			((TaskListView) part).getFilteredTree().setShowProgress(super.isChecked());
		}
	}

	@Override
	public List<StructuredViewer> getViewers() {
		List<StructuredViewer> viewers = new ArrayList<StructuredViewer>();
		IViewPart part = super.getPartForAction();
		if (part instanceof TaskListView) {
			viewers.add(((TaskListView) part).getViewer());
		}
		return viewers;
	}

	@Override
	protected boolean installInterestFilter(StructuredViewer viewer) {
		IViewPart part = super.getPartForAction();
		if (part instanceof TaskListView) {
			TaskListView taskListView = (TaskListView) part;

			try {
				taskListView.getViewer().getControl().setRedraw(false);
				previousSorter = taskListView.getViewer().getSorter();
				taskListView.getViewer().setSorter(taskListInterestSorter);
				previousFilters = new HashSet<AbstractTaskListFilter>(taskListView.getFilters());
				taskListView.clearFilters(true);
				if (!taskListView.getFilters().contains(taskListInterestFilter)) {
					taskListView.addFilter(taskListInterestFilter);
				}
//				taskListView.getViewer().getTree().setHeaderVisible(false);
				taskListView.setFocusedMode(true);
				taskListView.setManualFiltersEnabled(false);
				taskListView.refreshAndFocus(true);
			} finally {
				taskListView.getViewer().getControl().setRedraw(true);
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void uninstallInterestFilter(StructuredViewer viewer) {
		IViewPart part = super.getPartForAction();
		if (part instanceof TaskListView) {
			TaskListView taskListView = (TaskListView) part;
			try {
				taskListView.getViewer().getControl().setRedraw(false);
				taskListView.getViewer().setSorter(previousSorter);
				taskListView.removeFilter(taskListInterestFilter);
				taskListView.setManualFiltersEnabled(true);
				for (AbstractTaskListFilter filter : previousFilters) {
					TaskListView.getFromActivePerspective().addFilter(filter);
				}
//				taskListView.getViewer().getTree().setHeaderVisible(true);
				taskListView.setFocusedMode(false);
				taskListView.getViewer().collapseAll();
				taskListView.refreshAndFocus(false);
			} finally {
				taskListView.getViewer().getControl().setRedraw(true);
			}
		}
	}

	public void propertyChange(PropertyChangeEvent event) {
		// ignore
	}

	public void filterTextChanged(final String text) {
		if (isChecked() && (text == null || "".equals(text))) {
			IViewPart part = FocusTaskListAction.super.getPartForAction();
			if (part instanceof TaskListView) {
				((TaskListView) part).getViewer().expandAll();
			}
		}
	}

}
