/*******************************************************************************
 * Copyright (c) 2004, 2010 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.context.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * @author Mik Kersten
 */
public class ContextUiImages {

	private static ImageRegistry imageRegistry;

	private static final String T_ELCL = "elcl16"; //$NON-NLS-1$

	private static final String T_TOOL = "etool16"; //$NON-NLS-1$

	private static final URL baseURL = ContextUiPlugin.getDefault().getBundle().getEntry("/icons/"); //$NON-NLS-1$

	// TODO 3.5 move images below to sandbox

	@Deprecated
	public static final ImageDescriptor COLOR_PALETTE = create(T_ELCL, "color-palette.gif"); //$NON-NLS-1$

	@Deprecated
	public static final ImageDescriptor STOP_SEARCH = create(T_ELCL, "stop_all.gif"); //$NON-NLS-1$

	@Deprecated
	public static final ImageDescriptor QUALIFY_NAMES = create(T_TOOL, "qualify-names.gif"); //$NON-NLS-1$

	@Deprecated
	public static final ImageDescriptor EDGE_INHERITANCE = create(T_ELCL, "edge-inheritance.gif"); //$NON-NLS-1$

	@Deprecated
	public static final ImageDescriptor EDGE_REFERENCE = create(T_ELCL, "edge-reference.gif"); //$NON-NLS-1$

	@Deprecated
	public static final ImageDescriptor EDGE_ACCESS_READ = create(T_ELCL, "edge-read.gif"); //$NON-NLS-1$

	@Deprecated
	public static final ImageDescriptor EDGE_ACCESS_WRITE = create(T_ELCL, "edge-write.gif"); //$NON-NLS-1$

	public static final ImageDescriptor FILE_XML = create(T_ELCL, "file-xml.gif"); //$NON-NLS-1$

	public static final ImageDescriptor FILE_GENERIC = create(T_ELCL, "file_obj.gif"); //$NON-NLS-1$

	public static final ImageDescriptor FOLDER_GENERIC = create(T_ELCL, "fldr_obj.gif"); //$NON-NLS-1$

	public static ImageDescriptor create(String prefix, String name) {
		return create(prefix, name, baseURL);
	}

	public static ImageDescriptor create(String prefix, String name, URL baseURL) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name, baseURL));
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static URL makeIconFileURL(String prefix, String name, URL baseURL) throws MalformedURLException {
		if (baseURL == null) {
			throw new MalformedURLException();
		}

		StringBuffer buffer = new StringBuffer(prefix);
		buffer.append('/');
		buffer.append(name);
		return new URL(baseURL, buffer.toString());
	}

	private static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
		}

		return imageRegistry;
	}

	/**
	 * Lazily initializes image map.
	 */
	public static Image getImage(ImageDescriptor imageDescriptor) {
		if (imageDescriptor == null) {
			return null;
		}

		ImageRegistry imageRegistry = getImageRegistry();
		Image image = imageRegistry.get("" + imageDescriptor.hashCode()); //$NON-NLS-1$
		if (image == null) {
			image = imageDescriptor.createImage();
			imageRegistry.put("" + imageDescriptor.hashCode(), image); //$NON-NLS-1$
		}
		return image;
	}
}
