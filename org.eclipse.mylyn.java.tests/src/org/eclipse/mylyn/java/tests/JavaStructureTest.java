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
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylar.ide.tests.ResourceTestUtil;
import org.eclipse.mylar.internal.core.MylarContext;
import org.eclipse.mylar.internal.core.MylarContextManager;
import org.eclipse.mylar.internal.core.ScalingFactors;
import org.eclipse.mylar.internal.java.JavaEditingMonitor;
import org.eclipse.mylar.provisional.core.IMylarElement;
import org.eclipse.mylar.provisional.core.MylarPlugin;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * @author Mik Kersten
 */
public class JavaStructureTest extends AbstractJavaContextTest {

	private MylarContextManager manager = MylarPlugin.getContextManager();

	private JavaEditingMonitor monitor = new JavaEditingMonitor();

	private IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();

	private TestJavaProject project;

	private IPackageFragment pkg;

	private IType typeFoo;

	private IMethod caller;

	private IMethod callee;

	private MylarContext taskscape;

	private ScalingFactors scaling = new ScalingFactors();

	@Override
	protected void setUp() throws Exception {
		project = new TestJavaProject(this.getClass().getName());
		pkg = project.createPackage("pkg1");
		typeFoo = project.createType(pkg, "Foo.java", "public class Foo { }");
		caller = typeFoo.createMethod("void caller() { callee(); }", null, true, null);
		callee = typeFoo.createMethod("void callee() { }", callee, true, null);

		taskscape = new MylarContext("12312", scaling);
		manager.contextActivated(taskscape);
	}

	@Override
	protected void tearDown() throws Exception {
		manager.removeAllListeners();
		manager.contextDeactivated("12312");
		ResourceTestUtil.deleteProject(project.getProject());
	}

	public void testNavigation() throws JavaModelException, PartInitException {
		CompilationUnitEditor editorPart = (CompilationUnitEditor) JavaUI.openInEditor(caller);

		monitor.selectionChanged(part, new StructuredSelection(caller));

		Document document = new Document(typeFoo.getCompilationUnit().getSource());

		TextSelection callerSelection = new TextSelection(document, typeFoo.getCompilationUnit().getSource().indexOf(
				"callee();"), "callee".length());
		editorPart.setHighlightRange(callerSelection.getOffset(), callerSelection.getLength(), true);
		monitor.selectionChanged(editorPart, callerSelection);

		TextSelection calleeSelection = new TextSelection(document, callee.getSourceRange().getOffset(), callee
				.getSourceRange().getLength());
		editorPart.setHighlightRange(callerSelection.getOffset(), callerSelection.getLength(), true);
		monitor.selectionChanged(editorPart, calleeSelection);

		IMylarElement callerNode = manager.getElement(caller.getHandleIdentifier());
		IMylarElement calleeNode = manager.getElement(callee.getHandleIdentifier());
		assertTrue(callerNode.getInterest().isInteresting());
		assertTrue(calleeNode.getInterest().isInteresting());
		assertEquals(1, callerNode.getRelations().size());

		TextSelection callerAgain = new TextSelection(document, typeFoo.getCompilationUnit().getSource().indexOf(
				"callee();"), "callee".length());
		editorPart.setHighlightRange(callerAgain.getOffset(), callerAgain.getLength(), true);
		monitor.selectionChanged(editorPart, callerSelection);
		assertTrue(calleeNode.getRelations().size() == 1);
	}

}
