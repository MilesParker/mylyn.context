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
package org.eclipse.mylar.core.tests.support.search;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylar.provisional.core.IMylarElement;

public interface ISearchPluginTest {
	public List<?> search(int dos, IMylarElement node) throws IOException, CoreException;
}
