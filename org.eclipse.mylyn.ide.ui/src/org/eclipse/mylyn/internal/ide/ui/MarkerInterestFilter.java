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
 * Created on May 6, 2005
 */
package org.eclipse.mylar.internal.ide.ui;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylar.provisional.core.MylarPlugin;
import org.eclipse.mylar.provisional.ui.InterestFilter;
import org.eclipse.ui.views.markers.internal.ConcreteMarker;
import org.eclipse.ui.views.markers.internal.ProblemMarker;

/**
 * @author Mik Kersten
 */
public class MarkerInterestFilter extends InterestFilter {

	public boolean select(Viewer viewer, Object parent, Object element) {
			return isInteresting((ConcreteMarker)element, viewer, parent);
	}

	private boolean isImplicitlyInteresting(ConcreteMarker marker) {
		return (marker instanceof ProblemMarker) && ((ProblemMarker) marker).getSeverity() == IMarker.SEVERITY_ERROR;
	}

	private boolean isInteresting(ConcreteMarker marker, Viewer viewer, Object parent) {
		if (isImplicitlyInteresting(marker)) {
			return true;
		} else {
			String handle = MylarPlugin.getDefault().getStructureBridge(marker.getResource().getFileExtension())
					.getHandleForOffsetInObject(marker, 0);
			if (handle == null) {
				return false;
			} else {
				return super.select(viewer, parent, MylarPlugin.getContextManager().getElement(handle));
			}
		}
	}
}
