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
 * Created on Apr 7, 2005
 */
package org.eclipse.mylar.context.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.mylar.context.core.IAlwaysIntersting;
import org.eclipse.mylar.context.core.IMylarElement;
import org.eclipse.mylar.context.core.AbstractContextStructureBridge;
import org.eclipse.mylar.core.MylarStatusHandler;
import org.eclipse.mylar.internal.context.core.ContextManager;
import org.eclipse.swt.widgets.Tree;

/**
 * This is a generic interest filter that can be applied to any
 * StructuredViewer. It figures out whether an object is interesting by getting
 * it's handle from the corresponding structure bridge.
 * 
 * @author Mik Kersten
 */
public class InterestFilter extends ViewerFilter {

	private Object temporarilyUnfiltered = null;

	public InterestFilter() {
	}

	@Override
	public boolean select(Viewer viewer, Object parent, Object object) {
		try {
			if (!(viewer instanceof StructuredViewer) || !containsMylarInterestFilter((StructuredViewer) viewer)) {
				return true;
			}
			if (isTemporarilyUnfiltered(parent)) {
				return true;
			} else if (temporarilyUnfiltered instanceof Tree) {
				// HACK: should also work for trees without project as root
				if (object instanceof IProjectNature || object instanceof IProject) {
					return true;
				}
			}

			IMylarElement element = null;
			if (object instanceof IAlwaysIntersting) {
				return true;
			} else if (object instanceof IMylarElement) {
				element = (IMylarElement) object;
			} else {
				AbstractContextStructureBridge bridge = ContextCorePlugin.getDefault().getStructureBridge(object);
				if (bridge.getContentType() == null) {
					// try to resolve the resource
					if (object instanceof IAdaptable) {
						Object adapted = ((IAdaptable) object).getAdapter(IResource.class);
						if (adapted instanceof IResource) {
							object = adapted;
						}
						bridge = ContextCorePlugin.getDefault().getStructureBridge(object);
					} else {
						return false;
					}
				}
				if (!bridge.canFilter(object)) {
					return true;
				}

				if (!object.getClass().getName().equals(Object.class.getCanonicalName())) {
					String handle = bridge.getHandleIdentifier(object);
					element = ContextCorePlugin.getContextManager().getElement(handle);
				} else {
					return true;
				}
			}
			if (element != null) {
				return isInteresting(element);
			}
		} catch (Throwable t) {
			MylarStatusHandler.fail(t, "interest filter failed on viewer: " + viewer.getClass(), false);
		}
		return false;
	}

	protected boolean isInteresting(IMylarElement element) {
		if (element.getInterest().isPredicted()) {
			return false;
		} else {
			return element.getInterest().getValue() > ContextManager.getScalingFactors().getInteresting();
		}
	}

	private boolean isTemporarilyUnfiltered(Object parent) {
		if (parent instanceof TreePath) {
			TreePath treePath = (TreePath) parent;
			parent = treePath.getLastSegment();
		}
		return temporarilyUnfiltered != null && temporarilyUnfiltered.equals(parent);
	}

	protected boolean containsMylarInterestFilter(StructuredViewer viewer) {
		for (ViewerFilter filter : viewer.getFilters()) {
			if (filter instanceof InterestFilter) {
				return true;
			}
		}
		return false;
	}

	public void setTemporarilyUnfiltered(Object temprarilyUnfiltered) {
		this.temporarilyUnfiltered = temprarilyUnfiltered;
	}

	/**
	 * @return true if there was an unfiltered node
	 */
	public boolean resetTemporarilyUnfiltered() {
		if (temporarilyUnfiltered != null) {
			this.temporarilyUnfiltered = null;
			return true;
		} else {
			return false;
		}
	}

	public Object getTemporarilyUnfiltered() {
		return temporarilyUnfiltered;
	}

}
