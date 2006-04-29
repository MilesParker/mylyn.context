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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.mylar.ide.tests.CommitMessageTest;
import org.eclipse.mylar.internal.ide.MylarIdePlugin;
import org.eclipse.mylar.java.tests.search.JUnitReferencesSearchPluginTest;
import org.eclipse.mylar.java.tests.search.JavaImplementorsSearchPluginTest;
import org.eclipse.mylar.java.tests.search.JavaReadAccessSearchPluginTest;
import org.eclipse.mylar.java.tests.search.JavaReferencesSearchTest;
import org.eclipse.mylar.java.tests.search.JavaWriteAccessSearchPluginTest;

/**
 * @author Mik Kersten
 */
public class AllJavaTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Tests for org.eclipse.mylar.java.tests");

		// $JUnit-BEGIN$
		suite.addTestSuite(CommitMessageTest.class);
		suite.addTestSuite(InterestManipulationTest.class);
//		suite.addTestSuite(ChangeSetManagerTest.class);
		suite.addTestSuite(EditorManagerTest.class);
		suite.addTestSuite(RefactoringTest.class);
		suite.addTestSuite(ContentOutlineRefreshTest.class);
		suite.addTestSuite(TypeHistoryManagerTest.class);
		suite.addTestSuite(PackageExplorerRefreshTest.class);
		suite.addTestSuite(ActiveHierarchyTest.class);
		suite.addTestSuite(ActiveSearchTest.class);
		suite.addTestSuite(ProblemsListTest.class);
		suite.addTestSuite(InterestFilterTest.class);
		suite.addTestSuite(ContextManagerTest.class);
		suite.addTestSuite(JavaStructureTest.class);
		suite.addTestSuite(JavaImplementorsSearchPluginTest.class);
		suite.addTestSuite(JavaReadAccessSearchPluginTest.class);
		suite.addTestSuite(JavaReferencesSearchTest.class);
		suite.addTestSuite(JavaWriteAccessSearchPluginTest.class);
		suite.addTestSuite(JUnitReferencesSearchPluginTest.class);
		// $JUnit-END$

		MylarIdePlugin.getDefault().setResourceMonitoringEnabled(true);
		return suite;
	}
}
