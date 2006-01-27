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
 * Created on Apr 20, 2005
 */
package org.eclipse.mylar.internal.ide;

import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylar.core.AbstractUserInteractionMonitor;
import org.eclipse.mylar.core.MylarPlugin;
import org.eclipse.mylar.internal.core.util.MylarStatusHandler;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.EditorPart;

/**
 * @author Mik Kersten
 */
public class ResourceSelectionMonitor extends AbstractUserInteractionMonitor {

	@Override
	protected void handleWorkbenchPartSelection(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof StructuredSelection) {
			StructuredSelection structuredSelection = (StructuredSelection) selection;

			Object selectedObject = structuredSelection.getFirstElement();
			if (selectedObject instanceof File) {
				File file = (File) selectedObject;
				super.handleElementSelection(part, file);
			}
		} else if (selection instanceof TextSelection) {
			if (part instanceof EditorPart) {
				try {
					Object object = ((EditorPart) part).getEditorInput().getAdapter(IResource.class);
					if (object instanceof IFile) {
						IFile file = (IFile) object;
						if (!MylarPlugin.getDefault().getKnownContentTypes().contains(file.getFileExtension())) {
							super.handleElementEdit(part, object);
						}
					}
				} catch (Throwable t) {
					MylarStatusHandler.fail(t, "failed to resolve resource edit", false);
				}
			}
		}
	}
}
