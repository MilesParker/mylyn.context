/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.context.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.context.core.AbstractContextStructureBridge;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

/**
 * @author Mik Kersten
 */
public abstract class AbstractInterestManipulationAction implements IViewActionDelegate, IWorkbenchWindowActionDelegate {

	public static final String SOURCE_ID = "org.eclipse.mylyn.ui.interest.user"; //$NON-NLS-1$

	protected IViewPart view;

	protected IWorkbenchWindow window;

	protected boolean preserveUninteresting = false;

	private ISelection selection;

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void init(IViewPart view) {
		this.view = view;
	}

	@Deprecated
	protected boolean isRemove() {
		return !isIncrement();
	}

	protected abstract boolean isIncrement();

	/**
	 * Override to return a different context.
	 */
	protected IInteractionContext getContext() {
		return ContextCore.getContextManager().getActiveContext();
	}

	public void run(IAction action) {
		if (!ContextCore.getContextManager().isContextActive()) {
			MessageDialog.openInformation(WorkbenchUtil.getShell(),
					Messages.AbstractInterestManipulationAction_Interest_Manipulation,
					Messages.AbstractInterestManipulationAction_No_task_context_is_active);
			return;
		}

		boolean increment = !isRemove();

		if (selection instanceof StructuredSelection) {
			StructuredSelection structuredSelection = (StructuredSelection) selection;
			List<IInteractionElement> nodes = new ArrayList<IInteractionElement>();
			for (Object object : structuredSelection.toList()) {
				IInteractionElement node = convertSelectionToInteractionElement(object);
				nodes.add(node);
			}

			if (nodes != null && nodes.size() > 0) {
				if (!increment) {
					try {
						// NOTE: need to set the selection null so the
						// automatic reselection does not induce interest
						PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow()
								.getActivePage()
								.getActivePart()
								.getSite()
								.getSelectionProvider()
								.setSelection(null);
					} catch (Exception e) {
						// ignore
					}
				}
				boolean manipulated = ContextCorePlugin.getContextManager().manipulateInterestForElements(nodes,
						increment, false, preserveUninteresting, SOURCE_ID, getContext(), true);
				if (!manipulated) {
					AbstractInterestManipulationAction.displayInterestManipulationFailure();
				}
			}
		} else {
			IInteractionElement node = ContextCore.getContextManager().getActiveElement();
			if (node != null) {
				boolean manipulated = ContextCorePlugin.getContextManager().manipulateInterestForElement(node,
						increment, false, false, SOURCE_ID, getContext(), true);
				if (!manipulated) {
					AbstractInterestManipulationAction.displayInterestManipulationFailure();
				}
			} else {
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
						Messages.AbstractInterestManipulationAction_Interest_Manipulation,
						Messages.AbstractInterestManipulationAction_No_task_context_is_active);
			}
		}
	}

	/**
	 * TODO: consider moving this extensibility to the UI Bridge
	 */
	protected IInteractionElement convertSelectionToInteractionElement(Object object) {
		IInteractionElement node = null;
		if (object instanceof IInteractionElement) {
			node = (IInteractionElement) object;
		} else {
			AbstractContextStructureBridge bridge = ContextCore.getStructureBridge(object);
			String handle = bridge.getHandleIdentifier(object);
			node = ContextCore.getContextManager().getElement(handle);
		}
		return node;
	}

	public void dispose() {
		// ignore
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	public static void displayInterestManipulationFailure() {
		MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
				Messages.AbstractInterestManipulationAction_Interest_Manipulation,
				Messages.AbstractInterestManipulationAction_Not_a_valid_landmark);
	}

}
