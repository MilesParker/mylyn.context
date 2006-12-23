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

package org.eclipse.mylar.internal.context.ui.views;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.mylar.context.core.IMylarElement;
import org.eclipse.mylar.context.core.AbstractContextStructureBridge;
import org.eclipse.mylar.internal.context.ui.UiUtil;
import org.eclipse.swt.dnd.TransferData;

/**
 * @author Mik Kersten
 */
public class ActiveViewDropAdapter extends ViewerDropAdapter {

	public static final String ID_MANIPULATION = "org.eclipse.mylar.ui.views.active.drop.landmark";

	public ActiveViewDropAdapter(Viewer viewer) {
		super(viewer);
		setFeedbackEnabled(false);
	}

	@Override
	public boolean performDrop(Object data) {
		if (data instanceof StructuredSelection) {
			Object firstElement = ((StructuredSelection) data).getFirstElement();
			AbstractContextStructureBridge bridge = ContextCorePlugin.getDefault().getStructureBridge(firstElement);
			String handle = bridge.getHandleIdentifier(firstElement);
			IMylarElement node = ContextCorePlugin.getContextManager().getElement(handle);
			boolean manipulated = ContextCorePlugin.getContextManager().manipulateInterestForElement(node, true, true,
					ID_MANIPULATION);
			if (!manipulated) {
				UiUtil.displayInterestManipulationFailure();
			}
		}
		return false; // to ensure that the sender doesn't treat this as a
		// move
	}

	@Override
	public boolean validateDrop(Object targetObject, int operation, TransferData transferType) {
		return LocalSelectionTransfer.getTransfer().isSupportedType(transferType);
	}
}