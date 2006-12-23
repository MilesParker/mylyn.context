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

package org.eclipse.mylar.internal.context.ui;

import org.eclipse.jface.viewers.IColorDecorator;
import org.eclipse.jface.viewers.IFontDecorator;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.mylar.context.core.IMylarElement;
import org.eclipse.mylar.context.core.AbstractContextStructureBridge;
import org.eclipse.mylar.internal.context.core.MylarContextRelation;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * Not currently used.
 * 
 * @author Mik Kersten
 */
public class InterestDecorator implements ILabelDecorator, IFontDecorator, IColorDecorator {

	public InterestDecorator() {
		super();
	}

	private IMylarElement getNode(Object element) {
		IMylarElement node = null;
		if (element instanceof IMylarElement) {
			node = (IMylarElement) element;
		} else {
			AbstractContextStructureBridge adapter = ContextCorePlugin.getDefault().getStructureBridge(element);
			node = ContextCorePlugin.getContextManager().getElement(adapter.getHandleIdentifier(element));
		}
		return node;
	}

	public void addListener(ILabelProviderListener listener) {
		// don't care about listeners
	}

	public void dispose() {
		// don't care when we are disposed
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// don't care about listeners
	}

	public Image decorateImage(Image image, Object element) {
		return null;
	}

	public String decorateText(String text, Object element) {
		return null;
	}

	public Font decorateFont(Object element) {
		IMylarElement node = getNode(element);
		if (node != null) {
			if (node.getInterest().isLandmark() && !node.getInterest().isPropagated()) {
				return ContextUiPrefContstants.BOLD;
			}
		}
		return null;
	}

	public Color decorateForeground(Object element) {
		IMylarElement node = getNode(element);
		if (element instanceof MylarContextRelation) {
			return ColorMap.RELATIONSHIP;
		} else if (node != null) {
			UiUtil.getForegroundForElement(node);
		}
		return null;
	}

	public Color decorateBackground(Object element) {
		IMylarElement node = getNode(element);
		if (node != null) {
			return UiUtil.getBackgroundForElement(node);
		} else {
			return null;
		}
	}
}
