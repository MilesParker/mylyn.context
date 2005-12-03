/*******************************************************************************
 * Copyright (c) 2004 - 2005 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.java.ui.views;

import org.eclipse.jdt.internal.ui.JavaPerspectiveFactory;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;


public class MylarPerspectiveFactory extends JavaPerspectiveFactory {

    public static final String ID_RELATED_ELEMENTS_VIEW = "org.eclipse.mylar.ui.views.RelatedElementsView"; 
    public static final String ID_TASK_LIST_VIEW = "org.eclipse.mylar.tasklist.ui.views.TaskListView"; 
	  
	/**
	 * Constructs a new Default layout engine.
	 */
	public MylarPerspectiveFactory() {
		super();
	}

	public void createInitialLayout(IPageLayout layout) {
		super.createInitialLayout(layout);
	
		IFolderLayout rightFolder = layout.createFolder("right", IPageLayout.TOP, (float)0.4, IPageLayout.ID_OUTLINE);
		rightFolder.addView(ID_TASK_LIST_VIEW);
	}
}
