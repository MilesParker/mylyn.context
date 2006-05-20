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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylar.core.tests.UiTestUtil;
import org.eclipse.mylar.core.tests.support.search.TestActiveSearchListener;
import org.eclipse.mylar.internal.core.search.IMylarSearchOperation;
import org.eclipse.mylar.internal.ide.ui.views.ActiveSearchView;
import org.eclipse.mylar.internal.java.search.JavaReferencesProvider;
import org.eclipse.mylar.java.tests.search.SearchPluginTestHelper;
import org.eclipse.mylar.provisional.core.AbstractRelationProvider;
import org.eclipse.mylar.provisional.core.IMylarElement;
import org.eclipse.mylar.provisional.core.MylarPlugin;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.Perspective;
import org.eclipse.ui.internal.WorkbenchPage;

/**
 * @author Mik Kersten
 */
public class ActiveSearchTest extends AbstractJavaContextTest {

	private ActiveSearchView view;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testViewRecursion() throws JavaModelException, PartInitException {
		view = (ActiveSearchView) JavaPlugin.getActivePage().showView(ActiveSearchView.ID);
		ActiveSearchView.getFromActivePerspective().setSyncExecForTesting(false);

		for (AbstractRelationProvider provider : MylarPlugin.getContextManager().getActiveRelationProviders()) {
			assertTrue(provider.isEnabled());
		}
		assertEquals(0, view.getViewer().getTree().getItemCount());

		IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
		IMethod m1 = type1.createMethod("void m1() {\n m1(); \n}", null, true, null);
		StructuredSelection sm1 = new StructuredSelection(m1);
		monitor.selectionChanged(part, sm1);
		IMylarElement node = manager.handleInteractionEvent(mockInterestContribution(m1.getHandleIdentifier(), scaling
				.getLandmark()));

		// force an edge on so that it shows up in the view
		// ((MylarContextElement)((CompositeContextElement)node).getNodes().iterator().next()).addEdge(new
		// MylarContextRelation("kind", "edgeKind", node, node, context));

		assertEquals(1, MylarPlugin.getContextManager().getActiveLandmarks().size());

		assertEquals(1, search(2, node).size());

		List<TreeItem> collectedItems = new ArrayList<TreeItem>();
		UiTestUtil.collectTreeItemsInView(view.getViewer().getTree().getItems(), collectedItems);

		// just make sure that the view didn't blow up.
		assertEquals(1, collectedItems.size());
		monitor.selectionChanged(part, sm1);
		manager.handleInteractionEvent(mockInterestContribution(m1.getHandleIdentifier(), -scaling.getLandmark()));
	}

	public void testSearchNotRunIfViewDeactivated() throws PartInitException, JavaModelException {
		view = (ActiveSearchView) JavaPlugin.getActivePage().showView(ActiveSearchView.ID);
		for (AbstractRelationProvider provider : MylarPlugin.getContextManager().getActiveRelationProviders()) {
			assertTrue(provider.getCurrentDegreeOfSeparation() > 0);
		}
		JavaPlugin.getActivePage().showView("org.eclipse.ui.views.ProblemView"); 

		Perspective perspective = ((WorkbenchPage) JavaPlugin.getActivePage()).getActivePerspective();
		IViewReference reference = JavaPlugin.getActivePage().findViewReference(ActiveSearchView.ID);
		assertNotNull(reference);
//		assertTrue(perspective.canCloseView(view));
		assertTrue(perspective.hideView(reference));

		for (AbstractRelationProvider provider : MylarPlugin.getContextManager().getActiveRelationProviders()) {
			assertFalse(provider.isEnabled());
		}

		JavaPlugin.getActivePage().showView(ActiveSearchView.ID);
		for (AbstractRelationProvider provider : MylarPlugin.getContextManager().getActiveRelationProviders()) {
			assertTrue(provider.isEnabled());
		}
	}

	public void testSearchAfterDeletion() throws JavaModelException, PartInitException, IOException, CoreException {
		view = (ActiveSearchView) JavaPlugin.getActivePage().showView(ActiveSearchView.ID);
		if (view != null) {
			assertEquals(0, view.getViewer().getTree().getItemCount());

			IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
			IMethod m1 = type1.createMethod("void m1() {\n m2() \n}", null, true, null);
			IMethod m2 = type1.createMethod("void m2() { }", null, true, null);
			StructuredSelection sm2 = new StructuredSelection(m2);
			monitor.selectionChanged(part, sm2);
			IMylarElement node = manager.handleInteractionEvent(mockInterestContribution(m2.getHandleIdentifier(),
					scaling.getLandmark()));
			assertEquals(1, MylarPlugin.getContextManager().getActiveLandmarks().size());

			assertEquals(1, search(2, node).size());

			m1.delete(true, null);
			assertFalse(m1.exists());

			assertEquals(0, search(2, node).size());
		}
	}

	public List<?> search(int dos, IMylarElement node) {
		if (node == null) {
			fail("null element");
		}

		JavaReferencesProvider prov = new JavaReferencesProvider();

		TestActiveSearchListener l = new TestActiveSearchListener(prov);
		IMylarSearchOperation o = prov.getSearchOperation(node, IJavaSearchConstants.REFERENCES, dos);
		if (o == null)
			return null;

		SearchPluginTestHelper.search(o, l);
		return l.getResults();
	}
}
