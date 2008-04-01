/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Chris Aniszczyk <zx@us.ibm.com> - bug 208819
 *******************************************************************************/

package org.eclipse.mylyn.internal.bugzilla.ide.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.internal.bugzilla.ide.BugzillaIdePlugin;
import org.eclipse.mylyn.internal.bugzilla.ide.wizards.ErrorLogStatus;
import org.eclipse.mylyn.tasks.core.TaskSelection;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.views.log.LogEntry;

/**
 * Creates a new task from the selected error log entry.
 * 
 * @author Jeff Pound
 * @author Steffen Pingel
 */
public class NewTaskFromErrorAction implements IObjectActionDelegate {

	public static final String ID = "org.eclipse.mylyn.tasklist.ui.repositories.actions.create";

	private LogEntry entry;

	/**
	 * Fills a {@link StringBuilder} with {@link LogEntry} information, optionally including subentries too
	 * 
	 * @param entry
	 *            The {@link LogEntry} who provides the information
	 * @param sb
	 *            An {@link StringBuilder} to be filled with
	 * @param includeChildren
	 *            Indicates if it should include subentries, if the {@link LogEntry} have any
	 */
	private void buildDescriptionFromLogEntry(LogEntry entry, StringBuilder sb, boolean includeChildren) {
		sb.append("\n\n-- Error Log --\nDate: ");
		sb.append(entry.getDate());
		sb.append("\nMessage: ");
		sb.append(entry.getMessage());
		sb.append("\nSeverity: " + entry.getSeverityText());
		sb.append("\nPlugin ID: ");
		sb.append(entry.getPluginId());
		sb.append("\nStack Trace:\n");
		if (entry.getStack() == null) {
			sb.append("no stack trace available");
		} else {
			sb.append(entry.getStack());
		}

		if (includeChildren && entry.hasChildren()) {
			Object[] children = entry.getChildren(null);
			for (Object child : children) {
				if (child instanceof LogEntry) {
					buildDescriptionFromLogEntry((LogEntry) child, sb, includeChildren);
				}
			}
		}
	}

	private void createTask(LogEntry entry) {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		boolean includeChildren = false;

		if (entry.hasChildren()
				&& MessageDialog.openQuestion(shell, "Report Bug", "Include children of this entry in the report?")) {
			includeChildren = true;
		}

		StringBuilder sb = new StringBuilder();
		buildDescriptionFromLogEntry(entry, sb, includeChildren);

		if (BugzillaIdePlugin.getTaskErrorReporter().isEnabled()) {
			ErrorLogStatus status = new ErrorLogStatus(entry.getSeverity(), entry.getPluginId(), entry.getCode(), entry.getMessage());
			status.setDate(entry.getDate());
			status.setStack(entry.getStack());
			if (entry.getSession() != null) {
				status.setLogSessionData(entry.getSession().getSessionData());
			}
			BugzillaIdePlugin.getTaskErrorReporter().handle(status);
		} else {
			TaskSelection taskSelection = new TaskSelection("", sb.toString());
			TasksUiUtil.openNewTaskEditor(shell, taskSelection, null);			
		}
	}

	public void run() {
		createTask(entry);
	}

	public void run(IAction action) {
		run();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		Object object = ((IStructuredSelection) selection).getFirstElement();
		if (object instanceof LogEntry) {
			entry = (LogEntry) object;
		}
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

}
