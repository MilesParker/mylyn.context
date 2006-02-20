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
package org.eclipse.mylar.java.tests.search;

import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.mylar.core.tests.support.search.ISearchPluginTest;
import org.eclipse.mylar.core.tests.support.search.TestActiveSearchListener;
import org.eclipse.mylar.internal.core.search.IMylarSearchOperation;
import org.eclipse.mylar.internal.java.search.JUnitReferencesProvider;
import org.eclipse.mylar.provisional.core.IMylarElement;

public class JUnitReferencesSearchPluginTest extends TestCase implements ISearchPluginTest {

	// TESTCASENEEDED need a project that has a method that starts with test,
	// but doesn't extend junit.framework.TestCase

	public void testJUnitReferencesSearchDOS1() {
		// List<?> results = search(1);
	}

	//	
	// public void testJUnitReferencesSearchDOS2(){
	// List<?> results = search(2);
	// }
	//	
	// public void testJUnitReferencesSearchDOS3(){
	// List<?> results = search(3);
	// }
	//	
	// public void testJUnitReferencesSearchDOS4(){
	// List<?> results = search(4);
	// }
	//	
	// public void testJUnitReferencesSearchDOS5(){
	// List<?> results = search(5);
	// }

	public List<?> search(int dos, IMylarElement node) {

		if (node == null)
			return null;

		// test with each of the sepatations
		JUnitReferencesProvider prov = new JUnitReferencesProvider();

		TestActiveSearchListener l = new TestActiveSearchListener(prov);
		IMylarSearchOperation o = prov.getSearchOperation(node, IJavaSearchConstants.REFERENCES, dos);
		SearchPluginTestHelper.search(o, l);
		return l.getResults();
	}
}
