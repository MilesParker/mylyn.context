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
package org.eclipse.mylar.java.tests.xml;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.internal.resources.File;
import org.eclipse.mylar.context.core.AbstractRelationProvider;
import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.mylar.context.core.AbstractContextStructureBridge;
import org.eclipse.mylar.context.tests.support.search.TestActiveSearchListener;
import org.eclipse.mylar.internal.ide.xml.XmlNodeHelper;
import org.eclipse.search.internal.ui.text.FileSearchResult;
import org.eclipse.search.ui.text.Match;

public class XmlTestActiveSearchListener extends TestActiveSearchListener {

	private List<?> results = null;

	public XmlTestActiveSearchListener(AbstractRelationProvider prov) {
		super(prov);
	}

	private boolean gathered = false;

	@Override
	public void searchCompleted(List<?> l) {

		results = l;

		// deal with File
		if (l.isEmpty()) {
			gathered = true;
			return;
		}

		if (l.get(0) instanceof FileSearchResult) {
			FileSearchResult fsr = (FileSearchResult) l.get(0);
			List<Object> nodes = new ArrayList<Object>();
			Object[] far = fsr.getElements();
			for (int i = 0; i < far.length; i++) {
				Match[] mar = fsr.getMatches(far[i]);

				if (far[i] instanceof File) {
					File f = (File) far[i];

					for (int j = 0; j < mar.length; j++) {
						Match m = mar[j];
						try {

							AbstractContextStructureBridge bridge = ContextCorePlugin.getDefault().getStructureBridge(f.getName());

							String handle = bridge.getHandleForOffsetInObject(f, m.getOffset());

							XmlNodeHelper node = new XmlNodeHelper(handle);
							if (node != null) {
								nodes.add(node);
							}
						} catch (Exception e) {
							e.printStackTrace();
							// don't care
						}
					}
				}
			}
			results = nodes;
		}
		gathered = true;
	}

	@Override
	public boolean resultsGathered() {
		return gathered;
	}

	@Override
	public List<?> getResults() {
		return results;
	}

}
