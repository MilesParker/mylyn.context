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

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.mylar.core.tests.UiTestUtil;
import org.eclipse.mylar.internal.java.ui.actions.ApplyMylarToPackageExplorerAction;
import org.eclipse.mylar.provisional.ui.MylarUiPlugin;

/**
 * @author Mik Kersten
 */
public class PackageExplorerRefreshTest extends AbstractJavaContextTest {

	private PackageExplorerPart view;

	private TreeViewer viewer;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		view = PackageExplorerPart.openInActivePerspective();
		viewer = view.getTreeViewer();
		MylarUiPlugin.getDefault().getViewerManager().setSyncRefreshMode(true);
		ApplyMylarToPackageExplorerAction.getActionForPart(view).update(true);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testIsEmptyAfterDeactivation() throws JavaModelException, InterruptedException {
		IMethod m1 = type1.createMethod("void m111() { }", null, true, null);
		StructuredSelection sm1 = new StructuredSelection(m1);
		monitor.selectionChanged(view, sm1);
		viewer.expandAll();

		assertTrue(UiTestUtil.countItemsInTree(viewer.getTree()) > 0);
		assertNotNull(viewer.testFindItem(m1));
		assertNotNull(viewer.testFindItem(m1.getParent()));

		manager.deactivateContext(contextId);
		ApplyMylarToPackageExplorerAction.getActionForPart(view).update(true);
		assertTrue("num items: " + UiTestUtil.countItemsInTree(viewer.getTree()), UiTestUtil.countItemsInTree(viewer.getTree()) == 0);
		ApplyMylarToPackageExplorerAction.getActionForPart(view).update();
	}

	public void testPropagation() throws JavaModelException {
		IMethod m1 = type1.createMethod("void m111() { }", null, true, null);
		StructuredSelection sm1 = new StructuredSelection(m1);
		monitor.selectionChanged(view, sm1);
		viewer.expandAll();

		assertNotNull(viewer.testFindItem(m1));
		assertNotNull(viewer.testFindItem(m1.getParent()));
	}
}
