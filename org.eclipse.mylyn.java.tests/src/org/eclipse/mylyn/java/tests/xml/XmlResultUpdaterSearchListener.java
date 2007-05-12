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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.internal.resources.File;
import org.eclipse.mylar.context.core.AbstractRelationProvider;
import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.mylar.context.core.IMylarElement;
import org.eclipse.mylar.context.core.AbstractContextStructureBridge;
import org.eclipse.mylar.context.tests.support.search.TestActiveSearchListener;
import org.eclipse.mylar.internal.ide.xml.XmlNodeHelper;
import org.eclipse.mylar.internal.ide.xml.pde.PdeStructureBridge;
import org.eclipse.mylar.internal.java.search.XmlJavaRelationProvider;
import org.eclipse.mylar.monitor.core.InteractionEvent;
import org.eclipse.search.internal.ui.text.FileSearchResult;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.part.FileEditorInput;

public class XmlResultUpdaterSearchListener extends TestActiveSearchListener {

	private List<IMylarElement> results = null;

	private IMylarElement node;

	private int degreeOfSeparation;

	public XmlResultUpdaterSearchListener(AbstractRelationProvider prov, IMylarElement searchNode,
			int degreeOfSeparation) {
		super(prov);
		this.node = searchNode;
		this.degreeOfSeparation = degreeOfSeparation;
	}

	private boolean gathered = false;

	@Override
	public void searchCompleted(List<?> l) {
		results = new ArrayList<IMylarElement>();

		if (l.isEmpty())
			return;

		Map<String, String> nodes = new HashMap<String, String>();

		if (l.get(0) instanceof FileSearchResult) {
			FileSearchResult fsr = (FileSearchResult) l.get(0);

			Object[] far = fsr.getElements();
			for (int i = 0; i < far.length; i++) {
				Match[] mar = fsr.getMatches(far[i]);

				if (far[i] instanceof File) {
					File f = (File) far[i];

					// change the file into a document
					FileEditorInput fei = new FileEditorInput(f);

					for (int j = 0; j < mar.length; j++) {
						Match m = mar[j];
						try {
							XmlNodeHelper xnode = new XmlNodeHelper(fei.getFile().getFullPath().toString(), m
									.getOffset());
							AbstractContextStructureBridge bridge = ContextCorePlugin.getDefault().getStructureBridge(f.getName());
							String handle = xnode.getHandle();
							Object o = bridge.getObjectForHandle(handle);
							String name = bridge.getName(o);
							if (o != null) {
								nodes.put(handle, name);
								results.add(node);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		for (String handle : nodes.keySet()) {
			incrementInterest(node, PdeStructureBridge.CONTENT_TYPE, handle, degreeOfSeparation);
		}
		gathered = true;
	}

	protected void incrementInterest(IMylarElement node, String elementKind, String elementHandle,
			int degreeOfSeparation) {
		int predictedInterest = 1;// (7-degreeOfSeparation) *
									// TaskscapeManager.getScalingFactors().getDegreeOfSeparationScale();
		InteractionEvent event = new InteractionEvent(InteractionEvent.Kind.PREDICTION, elementKind, elementHandle,
				XmlJavaRelationProvider.SOURCE_ID, XmlJavaRelationProvider.SOURCE_ID, null, predictedInterest);
		ContextCorePlugin.getContextManager().handleInteractionEvent(event);

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
