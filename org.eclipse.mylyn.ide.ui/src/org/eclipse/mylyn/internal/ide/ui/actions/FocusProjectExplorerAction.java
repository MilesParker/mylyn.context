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

package org.eclipse.mylar.internal.ide.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.mylar.context.ui.InterestFilter;
import org.eclipse.mylar.internal.resources.ui.FocusCommonNavigatorAction;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.navigator.CommonNavigator;

/**
 * @author Mik Kersten
 */
public class FocusProjectExplorerAction extends FocusCommonNavigatorAction {

	public FocusProjectExplorerAction() {
		super(new InterestFilter(), true, true, true);
	}
	
	protected FocusProjectExplorerAction(InterestFilter filter) {
		super(filter, true, true, true);
	}

	@Override
	public List<StructuredViewer> getViewers() {
		List<StructuredViewer> viewers = new ArrayList<StructuredViewer>();

		IViewPart view = super.getPartForAction();
		if (view instanceof CommonNavigator) {
			CommonNavigator navigator = (CommonNavigator) view;
			viewers.add(navigator.getCommonViewer());
		}
		return viewers;
	}

}
