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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.mylar.internal.tasks.ui.ITasksUiConstants;
import org.eclipse.mylar.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylar.internal.tasks.ui.wizards.ContextAttachWizard;
import org.eclipse.mylar.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylar.tasks.core.AbstractRepositoryTask;
import org.eclipse.mylar.tasks.core.IAttachmentHandler;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.core.AbstractRepositoryTask.RepositoryTaskSyncState;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

/**
 * @author Mik Kersten
 * @author Rob Elves
 * @author Steffen Pingel
 */
public class ContextAttachAction implements IViewActionDelegate {

	private AbstractRepositoryTask task;

	private TaskRepository repository;

	private AbstractRepositoryConnector connector;

	public void init(IViewPart view) {
		// ignore
	}

	public void run(IAction action) {
		if (task == null) {
			return;
		} else {
			run(task);
		}
	}

	public void run(AbstractRepositoryTask task) {
		if (task.getSyncState() != RepositoryTaskSyncState.SYNCHRONIZED) {
			MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					ITasksUiConstants.TITLE_DIALOG, "Task must be synchronized before attaching context");
			return;
		}

		ContextAttachWizard wizard = new ContextAttachWizard(task);
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		if (wizard != null && shell != null && !shell.isDisposed()) {
			WizardDialog dialog = new WizardDialog(shell, wizard);
			dialog.create();
			dialog.setTitle(ContextAttachWizard.WIZARD_TITLE);
			dialog.setBlockOnOpen(true);
			if (dialog.open() == Dialog.CANCEL) {
				dialog.close();
				return;
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		ITask selectedTask = TaskListView.getSelectedTask(selection);
		if (selectedTask instanceof AbstractRepositoryTask) {
			task = (AbstractRepositoryTask) selectedTask;
			repository = TasksUiPlugin.getRepositoryManager().getRepository(task.getRepositoryKind(),
					task.getRepositoryUrl());
			connector = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(task.getRepositoryKind());
			IAttachmentHandler handler = connector.getAttachmentHandler();
			action
					.setEnabled(handler != null
							&& handler.canUploadAttachment(repository, task)
							&& (task.isActive() || ContextCorePlugin.getContextManager().hasContext(
									task.getHandleIdentifier())));
		} else {
			task = null;
			action.setEnabled(false);
		}
	}

}
