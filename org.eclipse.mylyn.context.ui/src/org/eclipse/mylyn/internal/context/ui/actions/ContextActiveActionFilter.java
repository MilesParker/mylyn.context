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

package org.eclipse.mylar.internal.context.ui.actions;

import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.ui.IActionFilter;

/**
 * @author Mik Kersten
 */
public class ContextActiveActionFilter implements IActionFilter {

	public boolean testAttribute(Object target, String name, String value) {
		return ContextCorePlugin.getContextManager().isContextActive();
	}

}
