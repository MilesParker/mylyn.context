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
package org.eclipse.mylar.java;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.mylar.core.IMylarContext;
import org.eclipse.mylar.core.IMylarContextListener;
import org.eclipse.mylar.core.IMylarContextNode;
import org.eclipse.mylar.core.MylarPlugin;
import org.eclipse.mylar.java.ui.MembersFilter;
import org.eclipse.mylar.java.ui.actions.ApplyMylarToPackageExplorerAction;

/**
 * @author Mik Kersten
 */
public class PackageExplorerManager implements IMylarContextListener {

    public void contextActivated(IMylarContext taskscape) {
    	try {
	    	if (MylarPlugin.getContextManager().hasActiveContext()
	    		&& ApplyMylarToPackageExplorerAction.getDefault() != null
	        	&& ApplyMylarToPackageExplorerAction.getDefault().isChecked()) {
	    		
				PackageExplorerPart packageExplorer = PackageExplorerPart.getFromActivePerspective();
				if (packageExplorer != null) { 
					packageExplorer.getTreeViewer().expandAll();
				}
	    	}	
    	} catch (Throwable t) {
    		MylarPlugin.log(t, "Could not update package explorer");
    	}
    }

    public void contextDeactivated(IMylarContext taskscape) {
		PackageExplorerPart packageExplorer = PackageExplorerPart.getFromActivePerspective();
		if (packageExplorer != null) { 
			packageExplorer.getTreeViewer().collapseAll();
		}
    }
    
   public void interestChanged(List<IMylarContextNode> nodes) {
    	if (nodes.size() == 0) return;
    	IMylarContextNode lastNode = nodes.get(nodes.size()-1);
    	interestChanged(lastNode);
    }
    
    public void interestChanged(IMylarContextNode node) {
	    try {
    		if (MylarPlugin.getContextManager().hasActiveContext()
	    		&& ApplyMylarToPackageExplorerAction.getDefault() != null
	    		&& ApplyMylarToPackageExplorerAction.getDefault().isChecked()) {
    			
    			IJavaElement lastElement = JavaCore.create(node.getElementHandle()); 
				PackageExplorerPart packageExplorer = PackageExplorerPart.getFromActivePerspective();
				if (packageExplorer != null && lastElement != null) { 
					ISelection selection = packageExplorer.getTreeViewer().getSelection();
					boolean suppressSelection = false;
					boolean membersFilteredMode = false;
					if (selection instanceof StructuredSelection) {
						if (((StructuredSelection)selection).size() > 1) suppressSelection = true;
					}
					if (!isInLinkToEditorMode(packageExplorer)) suppressSelection = true;
					for (ViewerFilter filter : Arrays.asList(packageExplorer.getTreeViewer().getFilters())) {
						if (filter instanceof MembersFilter) membersFilteredMode = true;
					}
					if (!suppressSelection) { 
						if (membersFilteredMode) {
							if (lastElement instanceof IMember) {
								packageExplorer.getTreeViewer().setSelection(new StructuredSelection(
									((IMember)lastElement).getCompilationUnit()), true);
							}
						} else {
							packageExplorer.getTreeViewer().setSelection(new StructuredSelection(lastElement), true);
						}
					}
				}
    		}
	    } catch (Throwable t) {
			MylarPlugin.log(t, "Could not update package explorer");
		}
    }
    
    private boolean isInLinkToEditorMode(PackageExplorerPart packageExplorer) {
		return JavaPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.LINK_PACKAGES_TO_EDITOR);
	}

	public void revealInteresting() {
    	// ignore     
    }
    
    public void presentationSettingsChanging(UpdateKind kind) {
    	// ignore
    }

    public void presentationSettingsChanged(UpdateKind kind) {
    	// ignore
    }

    public void landmarkAdded(IMylarContextNode node) {
    	// ignore
    } 

    public void landmarkRemoved(IMylarContextNode node) {
    	// ignore
    }
    
    public void nodeDeleted(IMylarContextNode node) {
    	// ignore
    }
      
    public void edgesChanged(IMylarContextNode node) {
    	// ignore
    }
}
