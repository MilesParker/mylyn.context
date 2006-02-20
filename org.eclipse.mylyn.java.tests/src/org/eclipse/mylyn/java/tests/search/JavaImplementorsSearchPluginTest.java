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
import org.eclipse.mylar.internal.java.search.JavaImplementorsProvider;
import org.eclipse.mylar.provisional.core.IMylarElement;

/*
 * TEST CASES TO HANDLE 1. all different degree of separations - with result,
 * with multiple, with none
 * 
 * DEGREE OF SEPARATIONS 1 Files of Landmarks 2 Files of Interesting 3 projects
 * of interesting elements 4 projects of interesting elements 5 workspace
 */

public class JavaImplementorsSearchPluginTest extends TestCase implements ISearchPluginTest {

	@Override
	protected void setUp() throws Exception {
		// nothing to do here yet
	}

	@Override
	protected void tearDown() throws Exception {
		// nothing to do here yet
	}

	public void testJavaImplementorsSearchDOS1() throws InterruptedException {
		// List<?> results = search(1);
	}

	//	
	// public void testJavaImplementorsSearchDOS2(){
	// List<?> results = search(2);
	// }
	//	
	// public void testJavaImplementorsSearchDOS3(){
	// List<?> results = search(3);
	// }
	//	
	// public void testJavaImplementorsSearchDOS4(){
	// List<?> results = search(4);
	// }
	//	
	// public void testJavaImplementorsSearchDOS5(){
	// List<?> results = search(5);
	// }

	public List<?> search(int dos, IMylarElement node) {
		if (node == null)
			return null;

		// test with each of the sepatations
		JavaImplementorsProvider prov = new JavaImplementorsProvider();

		IMylarSearchOperation o = prov.getSearchOperation(node, IJavaSearchConstants.IMPLEMENTORS, dos);
		TestActiveSearchListener l = new TestActiveSearchListener(prov);
		SearchPluginTestHelper.search(o, l);
		return l.getResults();
	}
}
