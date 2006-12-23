/***************************************************************************
 * Copyright (c) 2004, 2005, 2006 Eike Stepper, Germany.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 **************************************************************************/
package org.eclipse.mylar.team;

import org.eclipse.mylar.tasks.core.ITask;

/**
 * @author Eike Stepper
 * @author Mik Kersten
 */
public abstract class AbstractCommitTemplateVariable {
	
	protected String description;

	protected String recognizedKeyword;

	public String getDescription() {
		return description != null ? description : "Handler for '" + recognizedKeyword + "'";
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRecognizedKeyword() {
		return recognizedKeyword;
	}

	public abstract String getValue(ITask task);
	
	public void setRecognizedKeyword(String recognizedKeyword) {
		if (recognizedKeyword == null) {
			throw new IllegalArgumentException("Keyword to recognize must not be null"); //$NON-NLS-1$
		}

		this.recognizedKeyword = recognizedKeyword;
	}
}