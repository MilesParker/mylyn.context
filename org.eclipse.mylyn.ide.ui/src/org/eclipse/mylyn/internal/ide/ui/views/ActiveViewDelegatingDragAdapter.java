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

package org.eclipse.mylar.internal.ide.ui.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;

/**
 * Copied from JDT
 */
public class ActiveViewDelegatingDragAdapter implements DragSourceListener {

	private TransferDragSourceListener[] fPossibleListeners;

	private List fActiveListeners;

	private TransferDragSourceListener fFinishListener;

	public ActiveViewDelegatingDragAdapter(TransferDragSourceListener[] listeners) {
		setPossibleListeners(listeners);
	}

	protected void setPossibleListeners(TransferDragSourceListener[] listeners) {
		Assert.isNotNull(listeners);
		Assert.isTrue(fActiveListeners == null, "Can only set possible listeners before drag operation has started"); //$NON-NLS-1$
		fPossibleListeners = listeners;
	}

	@SuppressWarnings("unchecked")
	public void dragStart(DragSourceEvent event) {
		fFinishListener = null;
		boolean saveDoit = event.doit;
		Object saveData = event.data;
		boolean doIt = false;
		List transfers = new ArrayList(fPossibleListeners.length);
		fActiveListeners = new ArrayList(fPossibleListeners.length);

		for (int i = 0; i < fPossibleListeners.length; i++) {
			TransferDragSourceListener listener = fPossibleListeners[i];
			event.doit = saveDoit;
			listener.dragStart(event);
			if (event.doit) {
				transfers.add(listener.getTransfer());
				fActiveListeners.add(listener);
			}
			doIt = doIt || event.doit;
		}
		if (doIt) {
			((DragSource) event.widget).setTransfer((Transfer[]) transfers.toArray(new Transfer[transfers.size()]));
		}
		event.data = saveData;
		event.doit = doIt;
	}

	public void dragSetData(DragSourceEvent event) {
		fFinishListener = getListener(event.dataType);
		if (fFinishListener != null)
			fFinishListener.dragSetData(event);
	}

	public void dragFinished(DragSourceEvent event) {
		try {
			if (fFinishListener != null) {
				fFinishListener.dragFinished(event);
			} else {
				// If the user presses Escape then we get a dragFinished without
				// getting a dragSetData before.
				fFinishListener = getListener(event.dataType);
				if (fFinishListener != null)
					fFinishListener.dragFinished(event);
			}
		} finally {
			fFinishListener = null;
			fActiveListeners = null;
		}
	}

	private TransferDragSourceListener getListener(TransferData type) {
		if (type == null)
			return null;

		for (Iterator iter = fActiveListeners.iterator(); iter.hasNext();) {
			TransferDragSourceListener listener = (TransferDragSourceListener) iter.next();
			if (listener.getTransfer().isSupportedType(type)) {
				return listener;
			}
		}
		return null;
	}
}
