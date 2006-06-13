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

package org.eclipse.mylar.ide.tests;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.mylar.internal.ide.MylarIdePlugin;

/**
 * @author Mik Kersten
 */
public class IdePreferencesTest extends TestCase {

	public void testExclusionPatterns() {
		MylarIdePlugin.getDefault().setExcludedResourcePatterns(new HashSet<String>());
		assertEquals(0, MylarIdePlugin.getDefault().getExcludedResourcePatterns().size());
		
		Set<String> ignored = new HashSet<String>();
		ignored.add("one*");
		ignored.add(".two");
		
		MylarIdePlugin.getDefault().setExcludedResourcePatterns(ignored);
		Set<String> read = MylarIdePlugin.getDefault().getExcludedResourcePatterns();
		assertEquals(2, read.size());
		assertTrue(read.contains("one*"));
		assertTrue(read.contains(".two"));
	}
	
}
