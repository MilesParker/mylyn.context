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
package org.eclipse.mylar.internal.xml;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylar.internal.core.util.MylarStatusHandler;
import org.eclipse.search.internal.ui.text.FileSearchResult;
import org.eclipse.search.ui.IQueryListener;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.Match;

/**
 * COPIED FROM: org.eclipse.search.internal.ui.text.SearchResultUpdater
 * 
 * @author Shawn Minto
 * 
 */
public class XmlActiveSearchUpdater implements IResourceChangeListener, IQueryListener {
	private FileSearchResult fResult;

	public XmlActiveSearchUpdater(FileSearchResult result) {
		fResult = result;
		NewSearchUI.addQueryListener(this);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		if (delta != null)
			handleDelta(delta);
	}

	private void handleDelta(IResourceDelta d) {
		try {
			d.accept(new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) throws CoreException {
					switch (delta.getKind()) {
					case IResourceDelta.ADDED:
						return false;
					case IResourceDelta.REMOVED:
						IResource res = delta.getResource();
						if (res instanceof IFile) {
							Match[] matches = fResult.getMatches(res);
							fResult.removeMatches(matches);

							for (int j = 0; j < matches.length; j++) {
								// Match m = matches[j];
								// XmlNodeHelper xnode =
								// XmlReferencesProvider.nodeMap.remove(m);
								// System.out.println("REMOVED RES: " +
								// xnode.getHandle());
								// System.out.println(XmlReferencesProvider.nodeMap);
							}
						}
						break;
					case IResourceDelta.CHANGED:
						// TODO want to do something on chages to invalidate
						// handle changed resource
						break;
					}
					return true;
				}
			});
		} catch (CoreException e) {
			MylarStatusHandler.log(e.getStatus());
		}
	}

	public void queryAdded(ISearchQuery query) {
		// don't care
	}

	public void queryRemoved(ISearchQuery query) {
		if (fResult.equals(query.getSearchResult())) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
			NewSearchUI.removeQueryListener(this);
		}
	}

	public void queryStarting(ISearchQuery query) {
		// don't care
	}

	public void queryFinished(ISearchQuery query) {
		// don't care
	}
}
