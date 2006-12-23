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

package org.eclipse.mylar.java.tests;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.mylar.context.tests.UiTestUtil;
import org.eclipse.mylar.context.ui.AbstractContextUiBridge;
import org.eclipse.mylar.context.ui.ContextUiPlugin;
import org.eclipse.mylar.internal.context.ui.actions.FocusOutlineAction;
import org.eclipse.mylar.internal.java.MylarJavaPlugin;
import org.eclipse.mylar.internal.java.ui.actions.FocusPackageExplorerAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * @author Mik Kersten
 */
public class ApplyMylarActionTest extends AbstractJavaContextTest {

	private IViewPart view;

	private FocusOutlineAction action;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		view = openView(FocusOutlineAction.ID_CONTENT_OUTLINE);
		assertNotNull(view);
		assertNotNull(ContextUiPlugin.getDefault());
		assertNotNull(MylarJavaPlugin.getDefault());
		action = new FocusOutlineAction();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testPreservationOfContextPause() {
		FocusPackageExplorerAction action = new FocusPackageExplorerAction();
		ContextCorePlugin.getContextManager().setContextCapturePaused(true);
		action.update(true);
		assertTrue(ContextCorePlugin.getContextManager().isContextCapturePaused());
		
		ContextCorePlugin.getContextManager().setContextCapturePaused(false);
		action.update(false);
		assertFalse(ContextCorePlugin.getContextManager().isContextCapturePaused());
		action.update(true);
		assertFalse(ContextCorePlugin.getContextManager().isContextCapturePaused());
	}
	
	@SuppressWarnings("deprecation")
	public void testContents() throws JavaModelException, PartInitException {
		IMethod m1 = type1.createMethod("void m1() { }", null, true, null);
		openView("org.eclipse.ui.views.ContentOutline");
		JavaUI.openInEditor(m1);

//		FocusOutlineAction.getDefault().update(true);
		List<StructuredViewer> viewers = new ArrayList<StructuredViewer>();
		IEditorPart[] parts = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditors();
		for (int i = 0; i < parts.length; i++) {
			if (parts[i].getTitle().equals("Type1.java")) {
				AbstractContextUiBridge bridge = ContextUiPlugin.getDefault().getUiBridgeForEditor(parts[i]);
				List<TreeViewer> outlineViewers = bridge.getContentOutlineViewers(parts[i]);
				for (TreeViewer viewer : outlineViewers) {
					if (viewer != null && !viewers.contains(viewer))
						viewers.add(viewer);
				}
			}
		}
		assertEquals(1, viewers.size());
		TreeViewer viewer = (TreeViewer) viewers.get(0);
		assertEquals(3, UiTestUtil.countItemsInTree(viewer.getTree()));

		action.updateInterestFilter(true, viewer);
		assertEquals(0, UiTestUtil.countItemsInTree(viewer.getTree()));

		StructuredSelection sm1 = new StructuredSelection(m1);
		monitor.selectionChanged(view, sm1);
		viewer.refresh();
		assertEquals(2, UiTestUtil.countItemsInTree(viewer.getTree()));
	}
}
