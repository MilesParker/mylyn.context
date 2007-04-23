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

package org.eclipse.mylar.context.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Mik Kersten
 */
public class AllContextTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.eclipse.mylar.core.tests");
		// $JUnit-BEGIN$
		suite.addTestSuite(MylarContextTest.class);
		suite.addTestSuite(ContextExternalizerTest.class);
		suite.addTestSuite(DegreeOfInterestTest.class);
		suite.addTestSuite(ContextTest.class);
		suite.addTestSuite(InteractionEventTest.class);
		// $JUnit-END$
		return suite;
	}

}
