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

package org.eclipse.mylar.internal.ide.ui;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.mylar.context.core.IMylarElement;
import org.eclipse.mylar.context.core.AbstractContextStructureBridge;
import org.eclipse.mylar.context.core.InterestComparator;
import org.eclipse.ui.views.markers.internal.ProblemMarker;

/**
 * @author Mik Kersten
 */
public class MarkerViewerInterestSorter extends ViewerSorter {

	protected InterestComparator<IMylarElement> interestComparator = new InterestComparator<IMylarElement>();

	@Override
	public int compare(Viewer viewer, Object obj1, Object obj2) {
		if (obj1 instanceof ProblemMarker && obj1 instanceof ProblemMarker) {
			ProblemMarker marker1 = (ProblemMarker) obj1;
			ProblemMarker marker2 = (ProblemMarker) obj2;
			if (marker1.getSeverity() == IMarker.SEVERITY_ERROR && marker2.getSeverity() < IMarker.SEVERITY_ERROR) {
				return -1;
			} else if (marker2.getSeverity() == IMarker.SEVERITY_ERROR
					&& marker1.getSeverity() < IMarker.SEVERITY_ERROR) {
				return 1;
			} else {
				if (ContextCorePlugin.getContextManager().isContextActive()) {
					AbstractContextStructureBridge bridge = ContextCorePlugin.getDefault().getStructureBridge(
							marker1.getResource().getFileExtension());
					IMylarElement node1 = ContextCorePlugin.getContextManager().getElement(
							bridge.getHandleForOffsetInObject(marker1, 0));
					IMylarElement node2 = ContextCorePlugin.getContextManager().getElement(
							bridge.getHandleForOffsetInObject(marker2, 0));
					return interestComparator.compare(node1, node2);
				}
			}
		}
		return super.compare(viewer, obj1, obj2);
	}
}
