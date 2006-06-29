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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylar.internal.core.MylarContextManager;
import org.eclipse.mylar.internal.ide.MylarIdePlugin;
import org.eclipse.mylar.internal.java.ActiveFoldingEditorTracker;
import org.eclipse.mylar.internal.java.JavaStructureBridge;
import org.eclipse.mylar.internal.java.MylarJavaPlugin;
import org.eclipse.mylar.internal.tasklist.ui.TaskUiUtil;
import org.eclipse.mylar.internal.ui.MylarUiPrefContstants;
import org.eclipse.mylar.provisional.core.IMylarElement;
import org.eclipse.mylar.provisional.core.IMylarStructureBridge;
import org.eclipse.mylar.provisional.core.InteractionEvent;
import org.eclipse.mylar.provisional.core.MylarPlugin;
import org.eclipse.mylar.provisional.tasklist.ITask;
import org.eclipse.mylar.provisional.tasklist.Task;
import org.eclipse.mylar.provisional.ui.IMylarUiBridge;
import org.eclipse.mylar.provisional.ui.MylarUiPlugin;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * @author Mik Kersten
 */
public class EditorManagerTest extends AbstractJavaContextTest {

	private IWorkbenchPage page;

	private IViewPart view;

	@SuppressWarnings("deprecation")
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		assertNotNull(page);
		view = PackageExplorerPart.openInActivePerspective();
		assertNotNull(view);
		assertTrue(MylarUiPlugin.getDefault().getPreferenceStore().getBoolean(MylarUiPrefContstants.AUTO_MANAGE_EDITORS));
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		MylarIdePlugin.getDefault().getEditorManager().closeAllEditors();
	}

	public void testInterestCapturedForResourceOnFocus() throws CoreException, InvocationTargetException,
			InterruptedException {
		// TODO: shouldn't need to do this
//		float decayFactor = MylarContextManager.getScalingFactors().getDecay().getValue();
//		MylarContextManager.getScalingFactors().getDecay().setValue(0f);
//		manager.deactivateContext(context.getHandleIdentifier());
//		manager.activateContext(context);
		
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
		MylarPlugin.getContextManager().setContextCapturePaused(true);

		IType typeA = project.createType(p1, "TypeAa.java", "public class TypeD{ }");
		IType typeB = project.createType(p1, "TypeBb.java", "public class TypeC{ }");
				
		IFile fileA = (IFile)typeA.getAdapter(IResource.class);
		IFile fileB = (IFile)typeB.getAdapter(IResource.class);
		
		IMylarStructureBridge structureBridge = MylarPlugin.getDefault().getStructureBridge(fileA);
		
		IMylarElement elementA = MylarPlugin.getContextManager().getElement(structureBridge.getHandleIdentifier(fileA));
		IMylarElement elementB = MylarPlugin.getContextManager().getElement(structureBridge.getHandleIdentifier(fileB));
		
		assertFalse(elementA.getInterest().isInteresting());
		assertFalse(elementB.getInterest().isInteresting());
		MylarPlugin.getContextManager().setContextCapturePaused(false);
		
		elementA = MylarPlugin.getContextManager().getElement(structureBridge.getHandleIdentifier(fileA));
		assertFalse(elementA.getInterest().isInteresting());
		
		IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), fileA, true);
		elementA = MylarPlugin.getContextManager().getElement(structureBridge.getHandleIdentifier(fileA));
		float selectionFactor = MylarContextManager.getScalingFactors().get(InteractionEvent.Kind.SELECTION).getValue();	
		// TODO: should use selectionFactor test instead
		assertTrue(elementA.getInterest().getValue() <= selectionFactor && elementA.getInterest().isInteresting());
//		assertEquals(selectionFactor, elementA.getInterest().getValue());
		IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), fileB, true);
		IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), fileA, true);
		elementA = MylarPlugin.getContextManager().getElement(structureBridge.getHandleIdentifier(fileA));
		// TODO: punting on decay
//		assertEquals(selectionFactor-decayFactor*2, elementA.getInterest().getValue());
		assertTrue(elementA.getInterest().getValue() > 1 && elementA.getInterest().getValue() < 2);
//		MylarContextManager.getScalingFactors().getDecay().setValue(decayFactor);
	}

	public void testWaitingListenersDoNotLeakOnEditorActivation() throws JavaModelException {
		manager.deleteContext(contextId);
		MylarIdePlugin.getDefault().getEditorManager().closeAllEditors();

		int initialNumListeners = manager.getListeners().size();
		manager.activateContext(contextId);
		assertEquals(initialNumListeners, manager.getListeners().size());

		IType typeA = project.createType(p1, "TypeA.java", "public class TypeA{ }");
		monitor.selectionChanged(view, new StructuredSelection(typeA));
		manager.deactivateContext(contextId);
		assertEquals(initialNumListeners, manager.getListeners().size());

		manager.activateContext(contextId);
		assertEquals(initialNumListeners + 1, manager.getListeners().size());
		manager.deactivateContext(contextId);
		assertEquals(initialNumListeners, manager.getListeners().size());

		manager.activateContext(contextId);
		manager.deactivateContext(contextId);
		assertEquals(initialNumListeners, manager.getListeners().size());

		manager.activateContext(contextId);
		manager.deactivateContext(contextId);
		assertEquals(initialNumListeners, manager.getListeners().size());
	}

	public void testEditorTrackerListenerRegistration() throws JavaModelException {
		MylarIdePlugin.getDefault().getEditorManager().closeAllEditors();

		ActiveFoldingEditorTracker tracker = MylarJavaPlugin.getDefault().getEditorTracker();
		assertTrue(tracker.getEditorListenerMap().isEmpty());

		IMylarUiBridge bridge = MylarUiPlugin.getDefault().getUiBridge(JavaStructureBridge.CONTENT_TYPE);
		IMethod m1 = type1.createMethod("void m111() { }", null, true, null);
		monitor.selectionChanged(view, new StructuredSelection(m1));

		int numListeners = MylarPlugin.getContextManager().getListeners().size();
		IMylarElement element = MylarPlugin.getContextManager().getElement(type1.getHandleIdentifier());
		bridge.open(element);

		assertEquals(numListeners + 1, MylarPlugin.getContextManager().getListeners().size());
		assertEquals(1, page.getEditorReferences().length);
		assertEquals(1, tracker.getEditorListenerMap().size());
		MylarIdePlugin.getDefault().getEditorManager().closeAllEditors();

		assertEquals(numListeners, MylarPlugin.getContextManager().getListeners().size());
		assertEquals(0, page.getEditorReferences().length);
		assertEquals(0, tracker.getEditorListenerMap().size());
	}

	@SuppressWarnings("deprecation")
	public void testActivationPreservesActiveTaskEditor() throws JavaModelException, InvocationTargetException,
			InterruptedException {
		assertEquals(0, page.getEditorReferences().length);
		ITask task = new Task(contextId, contextId, true);
		TaskUiUtil.openEditor(task, false, false);
		assertEquals(1, page.getEditorReferences().length);
		manager.activateContext(contextId);
		assertEquals(1, page.getEditorReferences().length);
	}

	@SuppressWarnings("deprecation")
	public void testAutoCloseWithDecay() throws JavaModelException, InvocationTargetException, InterruptedException {
		MylarIdePlugin.getDefault().getEditorManager().closeAllEditors();
		assertEquals(0, page.getEditors().length);
		IMylarUiBridge bridge = MylarUiPlugin.getDefault().getUiBridge(JavaStructureBridge.CONTENT_TYPE);
		IMethod m1 = type1.createMethod("void m111() { }", null, true, null);
		monitor.selectionChanged(view, new StructuredSelection(m1));
		IMylarElement element = MylarPlugin.getContextManager().getElement(type1.getHandleIdentifier());
		bridge.open(element);

		IType typeA = project.createType(p1, "TypeA.java", "public class TypeA{ }");
		monitor.selectionChanged(view, new StructuredSelection(typeA));
		IMylarElement elementA = MylarPlugin.getContextManager().getElement(typeA.getHandleIdentifier());
		bridge.open(elementA);

		assertEquals(2, page.getEditors().length);
		for (int i = 0; i < 1 / (scaling.getDecay().getValue()) * 3; i++) {
			MylarPlugin.getContextManager().handleInteractionEvent(mockSelection());
		}
		assertFalse(element.getInterest().isInteresting());
		assertFalse(elementA.getInterest().isInteresting());
		IType typeB = project.createType(p1, "TypeB.java", "public class TypeB{ }");
		monitor.selectionChanged(view, new StructuredSelection(typeB));
		IMylarElement elementB = MylarPlugin.getContextManager().getElement(typeB.getHandleIdentifier());
		bridge.open(elementB);
		monitor.selectionChanged(view, new StructuredSelection(typeB));
		assertEquals(1, page.getEditors().length);
	}

	@SuppressWarnings("deprecation")
	public void testAutoClose() throws JavaModelException, InvocationTargetException, InterruptedException {
		MylarIdePlugin.getDefault().getEditorManager().closeAllEditors();
		assertEquals(0, page.getEditors().length);
		IMylarUiBridge bridge = MylarUiPlugin.getDefault().getUiBridge(JavaStructureBridge.CONTENT_TYPE);
		IMethod m1 = type1.createMethod("void m111() { }", null, true, null);
		monitor.selectionChanged(view, new StructuredSelection(m1));
		IMylarElement element = MylarPlugin.getContextManager().getElement(type1.getHandleIdentifier());
		bridge.open(element);

		assertEquals(1, page.getEditors().length);
		manager.deactivateContext(contextId);
		assertEquals(0, page.getEditors().length);
	}

	@SuppressWarnings("deprecation")
	public void testAutoOpen() throws JavaModelException, InvocationTargetException, InterruptedException {
		manager.deleteContext(contextId);
		MylarIdePlugin.getDefault().getEditorManager().closeAllEditors();
		assertEquals(0, page.getEditors().length);

		manager.activateContext(contextId);
		// assertEquals(0, page.getEditors().length);

		IType typeA = project.createType(p1, "TypeA.java", "public class TypeA{ }");
		IType typeB = project.createType(p1, "TypeB.java", "public class TypeB{ }");
		monitor.selectionChanged(view, new StructuredSelection(typeA));
		monitor.selectionChanged(view, new StructuredSelection(typeB));
		manager.deactivateContext(contextId);
		assertEquals(0, page.getEditors().length);

		manager.activateContext(contextId);
		assertTrue("num editors: " + page.getEditors().length, page.getEditors().length == 2
				|| page.getEditors().length == 3);
	}

	public void testCloseOnUninteresting() {
		// fail();
	}

	// private int getNumActiveEditors() {
	// return ;
	// for (int i = 0; i < page.getEditors().length; i++) {
	// IEditorPart editor = page.getEditors()[i];

	// if (editor instanceof AbstractDecoratedTextEditor) {
	// manager.contextDeactivated(contextId, contextId);
	// assertEquals(0, page.getEditors().length);
	// }
	// }
	// }

	// assertEquals(1, page.getEditors().length);
	// WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
	// protected void execute(IProgressMonitor monitor) throws CoreException {

	// }
	// };
	// IProgressService service =
	// PlatformUI.getWorkbench().getProgressService();
	// service.run(true, true, op);
}
