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
/*
 * Created on Aug 6, 2004
  */
package org.eclipse.mylar.java.ui.editor;


import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.mylar.ui.MylarImages;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Mik Kersten
 */
public class MylarCompilationUnitEditor extends CompilationUnitEditor {
 
    @Override
    protected void initializeEditor() {
        super.initializeEditor();
        setSourceViewerConfiguration(new MylarJavaSourceViewerConfiguration(
                JavaPlugin.getDefault().getJavaTextTools().getColorManager(), 
                getPreferenceStore(), this, IJavaPartitions.JAVA_PARTITIONING));
    }

    public IJavaElement getInputJavaElement() {
    	return super.getInputJavaElement();
    }
    
    @Override
    public void createPartControl(Composite parent) {
    	initializeEditor(); 
    	super.createPartControl(parent);
        
        JavaElementImageProvider prov = new JavaElementImageProvider();
        if (super.getInputJavaElement() != null) {
	        Image image = prov.getJavaImageDescriptor(super.getInputJavaElement(), 0).createImage(); 
	        Point size= JavaElementImageProvider.SMALL_SIZE;
			MylarJavaElementDescriptor desc = new MylarJavaElementDescriptor(image, MylarImages.MYLAR_OVERLAY, size);
			setTitleImage(MylarImages.getImage(desc));
        }
    }

	@Override
	public void updatedTitleImage(Image image) {
		Point size= JavaElementImageProvider.SMALL_SIZE;
		MylarJavaElementDescriptor desc = new MylarJavaElementDescriptor(image, MylarImages.MYLAR_OVERLAY, size);
		setTitleImage(MylarImages.getImage(desc));
	} 
}
