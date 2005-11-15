/*******************************************************************************
 * Copyright (c) 2004 - 2005 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylar.java.ui.editor;

import org.eclipse.core.internal.commands.util.Assert;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.ui.texteditor.ITextEditor;

public abstract class AbstractMylarHyperlinkDetector implements IHyperlinkDetector {

	private ITextEditor editor;
	
	public AbstractMylarHyperlinkDetector() {
	}
	
	public ITextEditor getEditor() {
		return editor; 
	}
	
	public abstract IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks);

	public void setEditor(ITextEditor editor) {
		Assert.isNotNull(editor);
		this.editor = editor;
	}

}
