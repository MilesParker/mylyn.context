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

package org.eclipse.mylar.provisional.core;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.mylar.internal.core.util.MylarStatusHandler;
import org.eclipse.mylar.provisional.core.InteractionEvent.Kind;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Self-registering on construction. Encapsulates users' interaction with the
 * context model.
 * 
 * @author Mik Kersten
 */
public abstract class AbstractUserInteractionMonitor implements ISelectionListener {

	protected Object lastSelectedElement = null;

	/**
	 * Requires workbench to be active.
	 */
	public AbstractUserInteractionMonitor() {
		try {
			MylarPlugin.getDefault().addWindowPostSelectionListener(this);
		} catch (NullPointerException npe) {
			MylarStatusHandler.log("Monitors can not be instantiated until the workbench is active", this);
		}
	}

	public void dispose() {
		try {
			MylarPlugin.getDefault().removeWindowPostSelectionListener(this);
		} catch (NullPointerException npe) {
			MylarStatusHandler.log(npe, "Could not dispose monitor.");
		}
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection == null || selection.isEmpty())
			return;
		if (!MylarPlugin.getContextManager().isContextActive()) {
			handleWorkbenchPartSelection(part, selection, false);
		} else {
			handleWorkbenchPartSelection(part, selection, true);
		}
	}

	protected abstract void handleWorkbenchPartSelection(IWorkbenchPart part, ISelection selection, boolean contributeToContext);

	/**
	 * Intended to be called back by subclasses.
	 */
	protected InteractionEvent handleElementSelection(IWorkbenchPart part, Object selectedElement, boolean contributeToContext) {
		if (selectedElement == null || selectedElement.equals(lastSelectedElement))
			return null;
		IMylarStructureBridge bridge = MylarPlugin.getDefault().getStructureBridge(selectedElement);
		InteractionEvent selectionEvent;
		if (bridge.getContentType() != null) {
			selectionEvent = new InteractionEvent(InteractionEvent.Kind.SELECTION,
					bridge.getContentType(), bridge.getHandleIdentifier(selectedElement), part.getSite().getId());
		} else {
			selectionEvent = new InteractionEvent(InteractionEvent.Kind.SELECTION,
					null, null, part.getSite().getId());			
		}
		if (contributeToContext) {
			MylarPlugin.getContextManager().handleInteractionEvent(selectionEvent);
		}
		MylarPlugin.getDefault().notifyInteractionObserved(selectionEvent);
		return selectionEvent;
	}

	/**
	 * Intended to be called back by subclasses.
	 */
	protected void handleElementEdit(IWorkbenchPart part, Object selectedElement, boolean contributeToContext) {
		if (selectedElement == null)
			return;
		IMylarStructureBridge bridge = MylarPlugin.getDefault().getStructureBridge(selectedElement);
		InteractionEvent editEvent = new InteractionEvent(InteractionEvent.Kind.EDIT, bridge.getContentType(),
				bridge.getHandleIdentifier(selectedElement), part.getSite().getId());
		if (contributeToContext) {
			MylarPlugin.getContextManager().handleInteractionEvent(editEvent);
		}
		MylarPlugin.getDefault().notifyInteractionObserved(editEvent);
	}

	/**
	 * Intended to be called back by subclasses.
	 */
	protected void handleNavigation(IWorkbenchPart part, Object targetElement, String kind, boolean contributeToContext) {
		IMylarStructureBridge adapter = MylarPlugin.getDefault().getStructureBridge(targetElement);
		if (adapter.getContentType() != null) {
			InteractionEvent navigationEvent = new InteractionEvent(InteractionEvent.Kind.SELECTION, adapter
					.getContentType(), adapter.getHandleIdentifier(targetElement), part.getSite().getId(), kind);
			if (contributeToContext) {
				MylarPlugin.getContextManager().handleInteractionEvent(navigationEvent);
			}
			MylarPlugin.getDefault().notifyInteractionObserved(navigationEvent);
		}
	}

	public Kind getEventKind() {
		return InteractionEvent.Kind.SELECTION;
	}
}
