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

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.mylar.context.core.IMylarElement;
import org.eclipse.mylar.context.ui.AbstractContextUiBridge;
import org.eclipse.mylar.tasks.ui.editors.TaskEditor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

/**
 * @author Mik Kersten
 */
public class TaskUiBridge extends AbstractContextUiBridge {

	@Override
	public void open(IMylarElement node) {
		// ignore
	}

	@Override
	public void close(IMylarElement node) {
		// ignore
	}

	@Override
	public boolean acceptsEditor(IEditorPart editorPart) {
		return editorPart instanceof TaskEditor;
	}

	@Override
	public List<TreeViewer> getContentOutlineViewers(IEditorPart editorPart) {
		return Collections.emptyList();
	}

	@Override
	public Object getObjectForTextSelection(TextSelection selection, IEditorPart editor) {
		return null;
	}

	@Override
	public IMylarElement getElement(IEditorInput input) {
		return null;
	}

	@Override
	public String getContentType() {
		return TaskStructureBridge.CONTENT_TYPE;
	}
}
