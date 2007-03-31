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
/*
 * Created on Feb 17, 2005
 */
package org.eclipse.mylar.internal.context.ui.views;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylar.context.core.AbstractContextStructureBridge;
import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.mylar.context.core.IMylarElement;
import org.eclipse.mylar.context.core.IMylarRelation;
import org.eclipse.mylar.context.ui.ContextUiPlugin;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Tree;

/**
 * @author Mik Kersten
 */
public class ContextNodeOpenListener implements IOpenListener, IDoubleClickListener, MouseListener {

	private final Viewer viewer;

	public ContextNodeOpenListener(Viewer viewer) {
		this.viewer = viewer;
	}

	public void open(OpenEvent event) {
		StructuredSelection selection = (StructuredSelection) viewer.getSelection();
		Object object = selection.getFirstElement();
		IMylarElement node = null;
		if (object instanceof IMylarElement) {
			node = (IMylarElement) object;
		} else if (!(object instanceof IMylarRelation)) {
			AbstractContextStructureBridge bridge = ContextCorePlugin.getDefault().getStructureBridge(object);
			String handle = bridge.getHandleIdentifier(object);
			node = ContextCorePlugin.getContextManager().getElement(handle);
		}
		if (node != null)
			ContextUiPlugin.getDefault().getUiBridge(node.getContentType()).open(node);
	}

	public void doubleClick(DoubleClickEvent event) {
		open(null);
	}

	public void mouseDoubleClick(MouseEvent event) {
		setSelection(event);
	}

	public void mouseDown(MouseEvent event) {
		setSelection(event);
	}

	private void setSelection(MouseEvent event) {
		try {
			Object selection = ((Tree) event.getSource()).getSelection()[0].getData();
			viewer.setSelection(new StructuredSelection(selection));
			open(null);
		} catch (Exception e) {
			// ignore
		}
	}

	public void mouseUp(MouseEvent e) {
		// ignore
	}
}
