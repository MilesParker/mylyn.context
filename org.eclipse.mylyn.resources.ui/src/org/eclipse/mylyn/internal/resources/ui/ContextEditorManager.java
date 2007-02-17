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

package org.eclipse.mylar.internal.resources.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.mylar.context.core.AbstractContextStructureBridge;
import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.mylar.context.core.IMylarContext;
import org.eclipse.mylar.context.core.IMylarContextListener;
import org.eclipse.mylar.context.core.IMylarElement;
import org.eclipse.mylar.context.ui.AbstractContextUiBridge;
import org.eclipse.mylar.context.ui.ContextUiPlugin;
import org.eclipse.mylar.core.MylarStatusHandler;
import org.eclipse.mylar.internal.context.ui.ContextUiPrefContstants;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.eclipse.mylar.tasks.ui.editors.NewTaskEditorInput;
import org.eclipse.mylar.tasks.ui.editors.TaskEditor;
import org.eclipse.mylar.tasks.ui.editors.TaskEditorInput;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.Workbench;

/**
 * @author Mik Kersten
 */
public class ContextEditorManager implements IMylarContextListener {

	private boolean previousCloseEditorsSetting = Workbench.getInstance().getPreferenceStore().getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN);
	
	public void contextActivated(IMylarContext context) {
		if (ContextUiPlugin.getDefault().getPreferenceStore().getBoolean(ContextUiPrefContstants.AUTO_MANAGE_EDITORS)) {
			Workbench workbench = (Workbench) PlatformUI.getWorkbench();
			previousCloseEditorsSetting = workbench.getPreferenceStore().getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN);
			workbench.getPreferenceStore().setValue(IPreferenceConstants.REUSE_EDITORS_BOOLEAN, false);
			boolean wasPaused = ContextCorePlugin.getContextManager().isContextCapturePaused();
			try {
//				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setRedraw(false);
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setEnabled(false);
				workbench.largeUpdateStart();
				
				if (!wasPaused) {
					ContextCorePlugin.getContextManager().setContextCapturePaused(true);
				}

				List<IMylarElement> documents = ContextCorePlugin.getContextManager().getInterestingDocuments();
				int opened = 0;
				int threshold = ContextUiPlugin.getDefault().getPreferenceStore().getInt(ContextUiPrefContstants.AUTO_MANAGE_EDITORS_OPEN_NUM);
				for (Iterator<IMylarElement> iter = documents.iterator(); iter.hasNext() && opened < threshold - 1; opened++) {
					IMylarElement document = iter.next();
					AbstractContextUiBridge bridge = ContextUiPlugin.getDefault().getUiBridge(document.getContentType());
					bridge.restoreEditor(document);
					opened++;
				}
				IMylarElement activeNode = context.getActiveNode();
				if (activeNode != null) {
					ContextUiPlugin.getDefault().getUiBridge(activeNode.getContentType()).open(activeNode);
				}
			} catch (Exception e) {
				MylarStatusHandler.fail(e, "failed to open editors on activation", false);
			} finally {
				ContextCorePlugin.getContextManager().setContextCapturePaused(false);
				workbench.largeUpdateEnd();
//				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setRedraw(true);
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setEnabled(true);
			}
		}
	}

	public void contextDeactivated(IMylarContext context) {
		if (ContextUiPlugin.getDefault().getPreferenceStore().getBoolean(ContextUiPrefContstants.AUTO_MANAGE_EDITORS)) {
			Workbench.getInstance().getPreferenceStore().setValue(IPreferenceConstants.REUSE_EDITORS_BOOLEAN, previousCloseEditorsSetting);
			closeAllEditors();
		}
	}

	public void closeAllEditors() {
		try {     
			if (PlatformUI.getWorkbench().isClosing()) {
				return;  
			}
			for(IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
				IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					IEditorReference[] references = page.getEditorReferences();
					List<IEditorReference> toClose = new ArrayList<IEditorReference>();
					for (int i = 0; i < references.length; i++) {
						if (!isActiveTaskEditor(references[i]) && !isUnsubmittedTaskEditor(references[i])) {
							toClose.add(references[i]);
						}
					}
					page.closeEditors(toClose.toArray(new IEditorReference[toClose.size()]), true);
				}
			}
		} catch (Throwable t) {
			MylarStatusHandler.fail(t, "Could not auto close editor.", false);
		}
	}

	private boolean isUnsubmittedTaskEditor(IEditorReference editorReference) {
		IEditorPart part = editorReference.getEditor(false);
		if (part instanceof TaskEditor) {
			try {
				IEditorInput input = editorReference.getEditorInput();
				if (input instanceof NewTaskEditorInput) {
					return true;
				}
			} catch (PartInitException e) {
				// ignore
			}
		}
		return false;
	}

	private boolean isActiveTaskEditor(IEditorReference editorReference) {
		ITask activeTask = TasksUiPlugin.getTaskListManager().getTaskList().getActiveTask();
		try {
			IEditorInput input = editorReference.getEditorInput();
			if (input instanceof TaskEditorInput) {
				TaskEditorInput taskEditorInput = (TaskEditorInput)input;
				if (activeTask != null && taskEditorInput.getTask() != null
					&& taskEditorInput.getTask().getHandleIdentifier().equals(activeTask.getHandleIdentifier())) {
					return true;
				}
			}
		} catch (PartInitException e) {
			// ignore
		}
		return false;
	}

	public void presentationSettingsChanging(UpdateKind kind) {
		// ignore
	}

	public void presentationSettingsChanged(UpdateKind kind) {
		// ignore
	}

	public void interestChanged(List<IMylarElement> elements) {
		for (IMylarElement element : elements) {
			if (ContextUiPlugin.getDefault().getPreferenceStore().getBoolean(ContextUiPrefContstants.AUTO_MANAGE_EDITORS)) {
				if (!element.getInterest().isInteresting()) {
					AbstractContextStructureBridge bridge = ContextCorePlugin.getDefault().getStructureBridge(element.getContentType());
					if (bridge.isDocument(element.getHandleIdentifier())) {
						AbstractContextUiBridge uiBridge = ContextUiPlugin.getDefault().getUiBridge(element.getContentType());
						uiBridge.close(element);
					}
				}
			}
		}
	}

	public void elementDeleted(IMylarElement node) {
		// ignore
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
