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

package org.eclipse.mylar.internal.resources.ui;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.mylar.context.core.IMylarElement;
import org.eclipse.mylar.context.core.IMylarRelation;
import org.eclipse.mylar.context.core.AbstractContextStructureBridge;
import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.mylar.internal.context.ui.AbstractContextLabelProvider;
import org.eclipse.mylar.internal.context.ui.ContextUiImages;
import org.eclipse.mylar.internal.resources.ResourceStructureBridge;
import org.eclipse.swt.graphics.Image;

/**
 * @author Mik Kersten
 */
public class ResourceContextLabelProvider extends AbstractContextLabelProvider {

	@Override
	public Image getImage(IMylarElement node) {
		AbstractContextStructureBridge bridge = ContextCorePlugin.getDefault()
				.getStructureBridge(ResourceStructureBridge.CONTENT_TYPE);
		Object object = bridge.getObjectForHandle(node.getHandleIdentifier());
		return getImageForObject(object);
	}

	@Override
	protected Image getImageForObject(Object object) {
		if (object instanceof IFile) {
			return ContextUiImages.getImage(ContextUiImages.FILE_GENERIC);
		} else if (object instanceof IContainer) {
			return ContextUiImages.getImage(ContextUiImages.FOLDER_GENERIC);
		}
		return null;
	}

	@Override
	protected String getTextForObject(Object object) {
		AbstractContextStructureBridge bridge = ContextCorePlugin.getDefault().getStructureBridge(object);
		return bridge.getName(object);
	}

	/**
	 * TODO: slow?
	 */
	@Override
	public String getText(IMylarElement node) {
		AbstractContextStructureBridge bridge = ContextCorePlugin.getDefault()
				.getStructureBridge(ResourceStructureBridge.CONTENT_TYPE);
		return bridge.getName(bridge.getObjectForHandle(node.getHandleIdentifier()));
	}

	@Override
	protected Image getImage(IMylarRelation edge) {
		return null;
	}

	@Override
	protected String getText(IMylarRelation edge) {
		return null;
	}
}
