/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.context.ui.editors;

import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.context.ui.InterestFilter;

/**
 * @author Mik Kersten
 */
public class ScalableInterestFilter extends InterestFilter {

	private double threshold = 0;

	public ScalableInterestFilter() {
	}

	public ScalableInterestFilter(IInteractionContext context) {
		super(context);
	}

	@Override
	protected boolean isInteresting(IInteractionElement element) {
		if (element.getInterest().getEvents().isEmpty()) {
			return false;
		} else {
			return element.getInterest().getValue() >= threshold;
		}
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

}
