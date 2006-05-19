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

package org.eclipse.mylar.provisional.core;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.mylar.internal.core.MylarContext;

/**
 * @author Mik Kersten
 */
public interface IContextWriter {

	public abstract void setOutputStream(OutputStream outputStream);

	public abstract void writeContextToStream(MylarContext context) throws IOException;

}