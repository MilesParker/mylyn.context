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
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.filters.ImportDeclarationFilter;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.mylar.internal.java.ui.actions.ApplyMylarToPackageExplorerAction;
import org.eclipse.mylar.internal.ui.actions.AbstractApplyMylarAction;
import org.eclipse.mylar.provisional.ui.InterestFilter;

/**
 * @author Mik Kersten
 */
public class InterestFilterTest extends AbstractJavaContextTest {

	private InterestFilter filter;

	private PackageExplorerPart explorer;

	private AbstractApplyMylarAction applyAction;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		explorer = PackageExplorerPart.openInActivePerspective();
		assertNotNull(explorer);
		applyAction = AbstractApplyMylarAction.getActionForPart(explorer);		
		assertTrue(applyAction instanceof ApplyMylarToPackageExplorerAction);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testPreservedFilterRemovalExclusion() throws JavaModelException {
		List<Class> filterClasses = new ArrayList<Class>();
		for (ViewerFilter filter : Arrays.asList(explorer.getTreeViewer().getFilters())) {
			filterClasses.add(filter.getClass());
		}
		assertTrue(filterClasses.contains(ImportDeclarationFilter.class));

		applyAction.update(true);
		filterClasses = new ArrayList<Class>();
		for (ViewerFilter filter : Arrays.asList(explorer.getTreeViewer().getFilters())) {
			filterClasses.add(filter.getClass());
		} 
		assertTrue(filterClasses.contains(ImportDeclarationFilter.class));
	}
	
	public void testFilterRemovalAndRestore() throws JavaModelException {

		ViewerFilter[] previousFilters = explorer.getTreeViewer().getFilters();
		assertTrue(previousFilters.length > 1);
		applyAction.update(true);
		ViewerFilter[] afterInstall = explorer.getTreeViewer().getFilters();
		// more than 1 since we preserve some filters
		assertEquals(3, afterInstall.length);
		
		applyAction.update(false);
		ViewerFilter[] restoredFilters = explorer.getTreeViewer().getFilters();
		assertEquals(previousFilters.length, restoredFilters.length);
	}
	
	public void testInterestFilter() throws JavaModelException {

		applyAction.update(true);
		filter = applyAction.getInterestFilter();
		assertNotNull(filter);

		IMethod m1 = type1.createMethod("public void m10() { }", null, true, null);

		assertFalse(filter.select(explorer.getTreeViewer(), null, type1));
		monitor.selectionChanged(PackageExplorerPart.getFromActivePerspective(), new StructuredSelection(type1));
		manager.activateContext(context);

		monitor.selectionChanged(PackageExplorerPart.getFromActivePerspective(), new StructuredSelection(type1));
		assertTrue(filter.select(explorer.getTreeViewer(), null, type1));

		assertFalse(filter.select(explorer.getTreeViewer(), null, m1));

		filter.setExcludedMatches("*1*");
		assertTrue(filter.select(explorer.getTreeViewer(), null, m1));
		
		//teardown
		filter.setExcludedMatches(null);
	}

	
}
