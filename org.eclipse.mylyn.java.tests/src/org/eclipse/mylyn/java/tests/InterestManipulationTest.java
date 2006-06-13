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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylar.internal.ide.ResourceSelectionMonitor;
import org.eclipse.mylar.internal.ide.ResourceStructureBridge;
import org.eclipse.mylar.internal.ui.actions.AbstractInterestManipulationAction;
import org.eclipse.mylar.provisional.core.IMylarElement;
import org.eclipse.mylar.provisional.core.InteractionEvent;
import org.eclipse.mylar.provisional.core.MylarPlugin;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/**
 * @author Mik Kersten
 */
public class InterestManipulationTest extends AbstractJavaContextTest {

	private IMylarElement method;

	private IMylarElement clazz;

	private IMylarElement cu;

	private IMethod javaMethod;

	private IType javaType;

	private ICompilationUnit javaCu;

	private IPackageFragment javaPackage;

	private IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		javaMethod = type1.createMethod("void testDecrement() { }", null, true, null);
		javaType = (IType) javaMethod.getParent();
		javaCu = (ICompilationUnit) javaType.getParent();
		javaPackage = (IPackageFragment) javaCu.getParent();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testDecrementAcrossBridges() throws CoreException, InvocationTargetException, InterruptedException {
		monitor.selectionChanged(part, new StructuredSelection(javaMethod));
		method = MylarPlugin.getContextManager().getElement(javaMethod.getHandleIdentifier());

		IFile file = project.getProject().getFile("foo.txt");
		file.create(null, true, null);
		// IFile file = (IFile)javaCu.getAdapter(IResource.class);
		ResourceStructureBridge bridge = new ResourceStructureBridge();
		new ResourceSelectionMonitor().selectionChanged(part, new StructuredSelection(file));

		IMylarElement fileElement = MylarPlugin.getContextManager().getElement(bridge.getHandleIdentifier(file));
		IMylarElement projectElement = MylarPlugin.getContextManager().getElement(
				javaCu.getJavaProject().getHandleIdentifier());

		assertTrue(fileElement.getInterest().isInteresting());
		assertTrue(method.getInterest().isInteresting());

		MylarPlugin.getContextManager().manipulateInterestForNode(projectElement, false, false, "test");

		assertFalse(fileElement.getInterest().isInteresting());
		// TODO: re-enable, fails in AllTests
		// assertFalse(method.getInterest().isInteresting());
	}

	public void testDecrementInterestOfCompilationUnit() throws JavaModelException {
		monitor.selectionChanged(part, new StructuredSelection(javaMethod));
		monitor.selectionChanged(part, new StructuredSelection(javaCu));
		method = MylarPlugin.getContextManager().getElement(javaMethod.getHandleIdentifier());
		clazz = MylarPlugin.getContextManager().getElement(javaType.getHandleIdentifier());
		cu = MylarPlugin.getContextManager().getElement(javaCu.getHandleIdentifier());

		IMylarElement packageNode = MylarPlugin.getContextManager().getElement(javaPackage.getHandleIdentifier());

		assertTrue(method.getInterest().isInteresting());
		assertTrue(clazz.getInterest().isInteresting());
		assertTrue(cu.getInterest().isInteresting());

		MylarPlugin.getContextManager().manipulateInterestForNode(packageNode, false, false, "test");
		assertFalse(packageNode.getInterest().isInteresting());
		assertFalse(cu.getInterest().isInteresting());
		assertFalse(clazz.getInterest().isInteresting());
		assertFalse(method.getInterest().isInteresting());
	}

	public void testManipulation() throws JavaModelException {
		InterestManipulationAction action = new InterestManipulationAction();

		IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
		IMethod m1 = type1.createMethod("void m22() { }", null, true, null);
		StructuredSelection sm1 = new StructuredSelection(m1);
		monitor.selectionChanged(part, sm1);
		IMylarElement node = MylarPlugin.getContextManager().getElement(m1.getHandleIdentifier());
		assertFalse(node.getInterest().isLandmark());
		assertNotNull(MylarPlugin.getContextManager().getActiveElement());
		action.changeInterestForSelected(true);
		assertTrue(node.getInterest().isLandmark());
		action.changeInterestForSelected(true);

		assertEquals(scaling.getLandmark() + scaling.get(InteractionEvent.Kind.SELECTION).getValue(), node
				.getInterest().getValue());

		action.changeInterestForSelected(false);
		assertFalse(node.getInterest().isLandmark());
		assertTrue(node.getInterest().isInteresting());
		action.changeInterestForSelected(false);
		assertFalse(node.getInterest().isInteresting());
		assertEquals(node.getInterest().getValue(), -scaling.get(InteractionEvent.Kind.SELECTION).getValue());
		action.changeInterestForSelected(false);
		assertEquals(node.getInterest().getValue(), -scaling.get(InteractionEvent.Kind.SELECTION).getValue());
	}

	class InterestManipulationAction extends AbstractInterestManipulationAction {

		@Override
		protected boolean isIncrement() {
			return true;
		}

		public void changeInterestForSelected(boolean increment) {
			MylarPlugin.getContextManager().manipulateInterestForNode(
					MylarPlugin.getContextManager().getActiveElement(), increment, false, "");
		}
	}
}
