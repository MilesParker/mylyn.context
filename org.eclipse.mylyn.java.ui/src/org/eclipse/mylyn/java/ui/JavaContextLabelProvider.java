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
package org.eclipse.mylar.java.ui;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.jdt.internal.ui.viewsupport.DecoratingJavaLabelProvider;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.internal.ui.viewsupport.TreeHierarchyLayoutProblemsDecorator;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylar.core.IMylarContextEdge;
import org.eclipse.mylar.core.IMylarContextNode;
import org.eclipse.mylar.core.internal.MylarContextManager;
import org.eclipse.mylar.java.JavaStructureBridge;
import org.eclipse.mylar.java.MylarJavaPlugin;
import org.eclipse.mylar.java.search.AbstractJavaRelationshipProvider;
import org.eclipse.mylar.java.search.JUnitReferencesProvider;
import org.eclipse.mylar.java.search.JavaImplementorsProvider;
import org.eclipse.mylar.java.search.JavaReadAccessProvider;
import org.eclipse.mylar.java.search.JavaReferencesProvider;
import org.eclipse.mylar.java.search.JavaWriteAccessProvider;
import org.eclipse.mylar.ui.MylarImages;
import org.eclipse.swt.graphics.Image;

/**
 * @author Mik Kersten
 */
public class JavaContextLabelProvider extends DecoratingJavaLabelProvider {

	public JavaContextLabelProvider() {
		super(createJavaUiLabelProvider());
	}
	
	@Override
	public String getText(Object object) {
        if (object instanceof IMylarContextNode) { 
            IMylarContextNode node = (IMylarContextNode)object;
            if (node == null) return "<missing info>";
            if (JavaStructureBridge.CONTENT_TYPE.equals(node.getContentKind())) {
                IJavaElement element = JavaCore.create(node.getElementHandle());
                if (element == null) {
                    return "<missing element>";                     
                } else {
                    return super.getText(element);
                }
            } 
        } else if (object instanceof IMylarContextEdge) {
        	return getNameForRelationship(((IMylarContextEdge)object).getRelationshipHandle());
        }
        return super.getText(object);
	}

	@Override
	public Image getImage(Object object) { 
        if (object instanceof IMylarContextNode) {
            IMylarContextNode node = (IMylarContextNode)object;
            if (node == null) return null;
            if (node.getContentKind().equals(JavaStructureBridge.CONTENT_TYPE)) {
                return super.getImage(JavaCore.create(node.getElementHandle()));
            } 
        } else if (object instanceof IMylarContextEdge) {
        	return MylarImages.getImage(getIconForRelationship(((IMylarContextEdge)object).getRelationshipHandle()));
        }
        return super.getImage(object);
	}
	
    private ImageDescriptor getIconForRelationship(String relationshipHandle) {
    	if (relationshipHandle.equals(AbstractJavaRelationshipProvider.ID_GENERIC)) {
            return MylarImages.EDGE_REFERENCE; 
        } else if (relationshipHandle.equals(JavaReferencesProvider.ID)) {
            return MylarImages.EDGE_REFERENCE; 
        } else if (relationshipHandle.equals(JavaImplementorsProvider.ID)) {
            return MylarImages.EDGE_INHERITANCE; 
        } else if (relationshipHandle.equals(JUnitReferencesProvider.ID)) {
            return MylarJavaPlugin.EDGE_REF_JUNIT; 
        } else if (relationshipHandle.equals(JavaWriteAccessProvider.ID)) {
            return MylarImages.EDGE_ACCESS_WRITE; 
        } else if (relationshipHandle.equals(JavaReadAccessProvider.ID)) {
            return MylarImages.EDGE_ACCESS_READ; 
        } else {
            return null;
        }
    }
    
    private String getNameForRelationship(String relationshipHandle) {
    	if (relationshipHandle.equals(AbstractJavaRelationshipProvider.ID_GENERIC)) {
            return AbstractJavaRelationshipProvider.NAME; 
        } else if (relationshipHandle.equals(JavaReferencesProvider.ID)) {
            return JavaReferencesProvider.NAME; 
        } else if (relationshipHandle.equals(JavaImplementorsProvider.ID)) {
            return JavaImplementorsProvider.NAME; 
        } else if (relationshipHandle.equals(JUnitReferencesProvider.ID)) {
            return JUnitReferencesProvider.NAME; 
        } else if (relationshipHandle.equals(JavaWriteAccessProvider.ID)) {
            return JavaWriteAccessProvider.NAME; 
        } else if (relationshipHandle.equals(JavaReadAccessProvider.ID)) {
            return JavaReadAccessProvider.NAME; 
        } else if (relationshipHandle.equals(MylarContextManager.CONTAINMENT_PROPAGATION_ID)) {
            return "Containment"; // TODO: make this generic? 
        } else {
            return null;
        }
    }

	public static AppearanceAwareLabelProvider createJavaUiLabelProvider() {
		AppearanceAwareLabelProvider javaUiLabelProvider = new AppearanceAwareLabelProvider(
				AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS | JavaElementLabels.P_COMPRESSED,
                AppearanceAwareLabelProvider.DEFAULT_IMAGEFLAGS | JavaElementImageProvider.SMALL_ICONS); 
		javaUiLabelProvider.addLabelDecorator(new TreeHierarchyLayoutProblemsDecorator());
		return javaUiLabelProvider;
	}
}