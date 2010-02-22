/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.ide.tests;

import java.util.List;

import junit.framework.TestCase;

import org.eclipse.mylyn.context.core.AbstractContextListener;
import org.eclipse.mylyn.context.tests.support.ContextTestUtil;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
import org.eclipse.mylyn.internal.team.ui.ContextActiveChangeSetManager;

/**
 * @author Mik Kersten
 */
public class IdeStartupTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		ContextTestUtil.triggerContextUiLazyStart();
	}

	public void testChangeSetsStartup() {
		List<AbstractContextListener> listeners = ContextCorePlugin.getContextManager().getListeners();
		boolean containsManager = false;
		for (AbstractContextListener listener : listeners) {
			if (listener instanceof ContextActiveChangeSetManager) {
				containsManager = true;
			}
		}
		assertTrue(containsManager);
	}

}
