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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylar.internal.core.MylarContext;
import org.eclipse.mylar.internal.core.MylarContextManager;
import org.eclipse.mylar.internal.java.JavaProblemListener;
import org.eclipse.mylar.internal.java.JavaStructureBridge;
import org.eclipse.mylar.provisional.core.AbstractRelationProvider;
import org.eclipse.mylar.provisional.core.IMylarContext;
import org.eclipse.mylar.provisional.core.IMylarContextListener;
import org.eclipse.mylar.provisional.core.IMylarElement;
import org.eclipse.mylar.provisional.core.IMylarStructureBridge;
import org.eclipse.mylar.provisional.core.InteractionEvent;
import org.eclipse.mylar.provisional.core.MylarPlugin;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/**
 * @author Mik Kersten
 */
public class ContextManagerTest extends AbstractJavaContextTest {

	protected PackageExplorerPart explorer;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		explorer = PackageExplorerPart.openInActivePerspective();
		assertNotNull(explorer);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	};

	class LandmarksModelListener implements IMylarContextListener {
		public int numAdditions = 0;

		public int numDeletions = 0;

		public void interestChanged(IMylarElement info) {
			// don't care about this event
		}

		public void landmarkAdded(IMylarElement element) {
			numAdditions++;
		}

		public void landmarkRemoved(IMylarElement element) {
			numDeletions++;
		}

		public void modelUpdated() {
			// don't care about this event
		}

		public void edgesChanged(IMylarElement node) {
			// don't care about this event
		}

		public void presentationSettingsChanging(UpdateKind kind) {
			// don't care about this event
		}

		public void presentationSettingsChanged(UpdateKind kind) {
			// don't care about this event
		}

		public void nodeDeleted(IMylarElement node) {
			// don't care about this event
		}

		public void contextActivated(IMylarContext taskscapeActivated) {
			// don't care about this event
		}

		public void contextDeactivated(IMylarContext taskscapeDeactivated) {
			// don't care about this event
		}

		public void interestChanged(List<IMylarElement> elements) {
			// ignore
			
		}
	}

	public void testHandleToPathConversion() throws IOException {
		String handle = "https://bugs.eclipse.org/bugs" + MylarContextManager.CONTEXT_HANDLE_DELIM + "123";
		File file = manager.getFileForContext(handle);
		assertFalse(file.exists());
		file.createNewFile();
		assertTrue(file.exists());
	}

	public void testPauseAndResume() throws JavaModelException {
		MylarPlugin.getContextManager().setContextCapturePaused(true);
		MylarPlugin.getContextManager().handleInteractionEvent(mockInterestContribution("paused", 3));
		IMylarElement paused = MylarPlugin.getContextManager().getElement("paused");
		assertFalse(paused.getInterest().isInteresting());

		MylarPlugin.getContextManager().setContextCapturePaused(false);
		MylarPlugin.getContextManager().handleInteractionEvent(mockInterestContribution("paused", 3));
		IMylarElement resumed = MylarPlugin.getContextManager().getElement("paused");
		assertTrue(resumed.getInterest().isInteresting());
	}

	public void testShellLifecycleActivityStart() {
		List<InteractionEvent> events = manager.getActivityHistoryMetaContext().getInteractionHistory();
		assertEquals(MylarContextManager.ACTIVITY_DELTA_STARTED, events.get(0).getDelta());
		assertEquals(MylarContextManager.ACTIVITY_DELTA_ACTIVATED, events.get(1).getDelta());
	}
	
	public void testActivityHistory() {
		manager.resetActivityHistory();
		MylarContext history = manager.getActivityHistoryMetaContext();
		assertNotNull(history);
		assertEquals(0, manager.getActivityHistoryMetaContext().getInteractionHistory().size());

		manager.activateContext(manager.loadContext("1"));
		assertEquals(1, manager.getActivityHistoryMetaContext().getInteractionHistory().size());

		manager.deactivateContext("2");
		assertEquals(2, manager.getActivityHistoryMetaContext().getInteractionHistory().size());
	}

	public void testChangeHandle() {
		MylarPlugin.getContextManager().handleInteractionEvent(mockInterestContribution("old", 3));
		IMylarElement old = MylarPlugin.getContextManager().getElement("old");
		assertTrue(old.getInterest().isInteresting());

		MylarPlugin.getContextManager().getActiveContext().updateElementHandle(old, "new");
		IMylarElement changed = MylarPlugin.getContextManager().getElement("new");
		assertTrue(changed.getInterest().isInteresting());
	}

	public void testHasContext() {
		manager.getFileForContext("1").delete();
		assertFalse(manager.getFileForContext("1").exists());
		assertFalse(manager.hasContext("1"));
		manager.activateContext(manager.loadContext("1"));
		assertTrue(manager.isContextActive());

		manager.deactivateContext("1");
		assertFalse(manager.hasContext("1"));

		manager.activateContext(manager.loadContext("1"));
		manager.handleInteractionEvent(mockSelection());
		manager.deactivateContext("1");
		assertTrue(manager.hasContext("1"));
		manager.getFileForContext("1").delete();
	}

	public void testEdgeReset() throws CoreException, InterruptedException, InvocationTargetException {
		IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
		IMethod m1 = type1.createMethod("public void m1() { }", null, true, null);
		IPackageFragment p2 = project.createPackage("p2");

		IType type2 = project.createType(p2, "Type2.java", "public class Type2 { }");
		IMethod m2 = type2.createMethod("void m2() { }", null, true, null);

		assertTrue(m1.exists());
		assertEquals(1, type1.getMethods().length);

		monitor.selectionChanged(part, new StructuredSelection(m1));
		IMylarElement m1Node = MylarPlugin.getContextManager().getElement(m1.getHandleIdentifier());
		assertTrue(m1Node.getInterest().isInteresting());
		monitor.selectionChanged(part, new StructuredSelection(m2));
		IMylarElement m2Node = MylarPlugin.getContextManager().getElement(m2.getHandleIdentifier());
		manager.handleInteractionEvent(mockInterestContribution(m2.getHandleIdentifier(), scaling.getLandmark()));
		assertTrue(m2Node.getInterest().isLandmark());

		AbstractRelationProvider provider = new JavaStructureBridge().getRelationshipProviders().get(0);
		provider.createEdge(m2Node, m1Node.getContentType(), m2.getHandleIdentifier());

		assertEquals(1, m2Node.getRelations().size());

		manager.resetLandmarkRelationshipsOfKind(provider.getId());

		assertEquals(0, m2Node.getRelations().size());
	}

	public void testPredictedInterest() {
		IMylarElement node = MylarPlugin.getContextManager().getElement("doesn't exist");
		assertFalse(node.getInterest().isInteresting());
		assertFalse(node.getInterest().isPropagated());
	}

	public void testErrorInterest() throws CoreException, InterruptedException, InvocationTargetException {
		JavaPlugin.getDefault().getProblemMarkerManager().addListener(new JavaProblemListener());

		IViewPart problemsPart = JavaPlugin.getActivePage().showView("org.eclipse.ui.views.ProblemView");
		assertNotNull(problemsPart);

		IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
		IMethod m1 = type1.createMethod("public void m1() { }", null, true, null);
		IPackageFragment p2 = project.createPackage("p2");

		IType type2 = project.createType(p2, "Type2.java", "public class Type2 { }");
		IMethod m2 = type2.createMethod("void m2() { new p1.Type1().m1(); }", null, true, null);

		assertTrue(m1.exists());
		assertEquals(1, type1.getMethods().length);

		monitor.selectionChanged(part, new StructuredSelection(m1));
		IMylarElement m1Node = MylarPlugin.getContextManager().getElement(m1.getHandleIdentifier());
		assertTrue(m1Node.getInterest().isInteresting());

		// delete method to cause error
		m1.delete(true, null);
		assertEquals(0, type1.getMethods().length);
		project.build();

		IMarker[] markers = type2.getResource().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false,
				IResource.DEPTH_INFINITE);
		assertEquals(1, markers.length);

		String resourceHandle = new JavaStructureBridge().getHandleIdentifier(m2.getCompilationUnit());
		assertTrue(MylarPlugin.getContextManager().getElement(resourceHandle).getInterest().isInteresting());

		// put it back
		type1.createMethod("public void m1() { }", null, true, null);

		// XXX: put this back, but it needs to wait on the resource marker
		// update somehow
		// project.build();
		// project.build(); // HACK
		// project.build(); // HACK
		// assertFalse(MylarPlugin.getContextManager().getElement(resourceHandle).getInterest().isInteresting());
	}

	public void testParentInterestAfterDecay() throws JavaModelException {
		IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
		IMethod m1 = type1.createMethod("void m1() { }", null, true, null);
		StructuredSelection sm1 = new StructuredSelection(m1);
		monitor.selectionChanged(part, sm1);

		IMylarElement node = MylarPlugin.getContextManager().getElement(m1.getHandleIdentifier());
		assertTrue(node.getInterest().isInteresting());
		IMylarStructureBridge bridge = MylarPlugin.getDefault().getStructureBridge(node.getContentType());
		IMylarElement parent = MylarPlugin.getContextManager().getElement(
				bridge.getParentHandle(node.getHandleIdentifier()));
		assertTrue(parent.getInterest().isInteresting());
		assertTrue(parent.getInterest().isPropagated());

		for (int i = 0; i < 1 / (scaling.getDecay().getValue()) * 3; i++) {
			MylarPlugin.getContextManager().handleInteractionEvent(mockSelection());
		}

		assertFalse(MylarPlugin.getContextManager().getElement(m1.getHandleIdentifier()).getInterest().isInteresting());
		MylarPlugin.getContextManager().handleInteractionEvent(mockSelection(m1.getHandleIdentifier()));
		assertTrue(MylarPlugin.getContextManager().getElement(m1.getHandleIdentifier()).getInterest().isInteresting());
	}

	public void testPropagation() throws JavaModelException, Exception {
		IMethod m1 = type1.createMethod("void m1() { }", null, true, null);
		IMylarElement node = MylarPlugin.getContextManager().getElement(m1.getHandleIdentifier());
		assertFalse(node.getInterest().isInteresting());

		InteractionEvent event = new InteractionEvent(InteractionEvent.Kind.MANIPULATION, new JavaStructureBridge().getContentType(), m1.getHandleIdentifier(), "source");
		MylarPlugin.getContextManager().handleInteractionEvent(event, true);
		
		node = MylarPlugin.getContextManager().getElement(m1.getHandleIdentifier());
		assertTrue(node.getInterest().isInteresting());

		project.build();
		IJavaElement parent = m1.getParent();
		IMylarElement parentNode = MylarPlugin.getContextManager().getElement(parent.getHandleIdentifier());
		assertFalse(parentNode.getInterest().isInteresting());
		
		InteractionEvent selectionEvent = new InteractionEvent(InteractionEvent.Kind.SELECTION, new JavaStructureBridge().getContentType(), m1.getHandleIdentifier(), "source");
		MylarPlugin.getContextManager().handleInteractionEvent(selectionEvent, true);
		parentNode = MylarPlugin.getContextManager().getElement(parent.getHandleIdentifier());
		assertTrue(parentNode.getInterest().isInteresting()); 
	}
	
	public void testIncremenOfParentDoi() throws JavaModelException, Exception {
		IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
		IMethod m1 = type1.createMethod("void m1() { }", null, true, null);
		IMylarElement node = MylarPlugin.getContextManager().getElement(m1.getHandleIdentifier());
		assertFalse(node.getInterest().isInteresting());

		StructuredSelection sm1 = new StructuredSelection(m1);
		monitor.selectionChanged(part, sm1);
		node = MylarPlugin.getContextManager().getElement(m1.getHandleIdentifier());
		assertTrue(node.getInterest().isInteresting());

		project.build();
		IJavaElement parent = m1.getParent();
		int level = 1;
		do {
			level++;
			IMylarElement parentNode = MylarPlugin.getContextManager().getElement(parent.getHandleIdentifier());
			if (!(parent instanceof JavaModel)) {
				assertEquals("failed on: " + parent.getClass(), node.getInterest().getValue(), parentNode.getInterest()
						.getValue());
			}
			parent = parent.getParent();
		} while (parent != null);
	}

	public void testIncremenOfParentDoiAfterForcedDecay() throws JavaModelException, Exception {
		IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
		IMethod m1 = type1.createMethod("void m1() { }", null, true, null);
		IMethod m2 = type1.createMethod("void m2() { }", null, true, null);
		IMylarElement node = MylarPlugin.getContextManager().getElement(m1.getHandleIdentifier());
		assertFalse(node.getInterest().isInteresting());

		monitor.selectionChanged(part, new StructuredSelection(m1));
		node = MylarPlugin.getContextManager().getElement(m1.getHandleIdentifier());
		assertTrue(node.getInterest().isInteresting());

		// make all the parents interest propated to have negative interest
		IJavaElement parent = m1.getParent();
		int level = 1;
		do {
			level++;
			IMylarElement parentNode = MylarPlugin.getContextManager().getElement(parent.getHandleIdentifier());
			if (!(parent instanceof JavaModel)) {
				assertTrue(parentNode.getInterest().isInteresting());
				MylarPlugin.getContextManager().handleInteractionEvent(mockInterestContribution(parentNode.getHandleIdentifier(), 
						-2*parentNode.getInterest().getValue()));
				IMylarElement updatedParent = MylarPlugin.getContextManager().getElement(parent.getHandleIdentifier());
				assertFalse(updatedParent.getInterest().isInteresting());
			}
			parent = parent.getParent();
		} while (parent != null);
		
//		assertFalse(node.getInterest().isInteresting());
		
		// select the element, should propagate up
		monitor.selectionChanged(part, new StructuredSelection(m2));
		monitor.selectionChanged(part, new StructuredSelection(m1));
		node = MylarPlugin.getContextManager().getElement(m1.getHandleIdentifier());
		assertTrue(node.getInterest().isInteresting());
		
		project.build();
		parent = m1.getParent();
		level = 1;
		do {
			level++;
			IMylarElement parentNode = MylarPlugin.getContextManager().getElement(parent.getHandleIdentifier());
			if (!(parent instanceof JavaModel)) {
				assertTrue(parentNode.getInterest().isInteresting());
//				assertEquals("failed on: " + parent.getClass(), node.getInterest().getValue(), parentNode.getInterest()
//						.getValue());
			}
			parent = parent.getParent();
		} while (parent != null);
	}
	
	public void testLandmarks() throws CoreException, IOException {
		LandmarksModelListener listener = new LandmarksModelListener();
		manager.addListener(listener);

		IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
		IMethod m1 = type1.createMethod("void m1() { }", null, true, null);

		StructuredSelection sm1 = new StructuredSelection(m1);
		monitor.selectionChanged(part, sm1);
		manager.handleInteractionEvent(mockInterestContribution(m1.getHandleIdentifier(), scaling.getLandmark()));
		// packages can't be landmarks
		manager.handleInteractionEvent(mockInterestContribution(m1.getCompilationUnit().getParent()
				.getHandleIdentifier(), scaling.getLandmark()));
		// source folders can't be landmarks
		manager.handleInteractionEvent(mockInterestContribution(m1.getCompilationUnit().getParent().getParent()
				.getHandleIdentifier(), scaling.getLandmark()));
		// projects can't be landmarks
		manager.handleInteractionEvent(mockInterestContribution(m1.getCompilationUnit().getParent().getParent()
				.getParent().getHandleIdentifier(), scaling.getLandmark()));

		assertEquals(1, MylarPlugin.getContextManager().getActiveLandmarks().size());
		assertEquals(1, listener.numAdditions);

		manager.handleInteractionEvent(mockInterestContribution(m1.getHandleIdentifier(), -scaling.getLandmark()));
		assertEquals(1, listener.numDeletions);
	}

}
