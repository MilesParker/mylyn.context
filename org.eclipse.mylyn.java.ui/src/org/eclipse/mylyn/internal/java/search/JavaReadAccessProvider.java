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

package org.eclipse.mylar.internal.java.search;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.mylar.internal.java.JavaStructureBridge;

/**
 * @author Mik Kersten
 */
public class JavaReadAccessProvider extends AbstractJavaRelationProvider {

	public static final String ID = ID_GENERIC + ".readaccess";

	public static final String NAME = "read by";

	public JavaReadAccessProvider() {
		super(JavaStructureBridge.CONTENT_TYPE, ID);
	}

	@Override
	protected boolean acceptElement(IJavaElement javaElement) {
		return javaElement instanceof IField;
	}

	@Override
	protected String getSourceId() {
		return ID;
	}

	@Override
	public String getName() {
		return NAME;
	}
}
