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
 * Created on Apr 21, 2005
  */
package org.eclipse.mylar.ide;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.mylar.core.AbstractRelationProvider;
import org.eclipse.mylar.core.IDegreeOfSeparation;
import org.eclipse.mylar.core.IMylarElement;
import org.eclipse.mylar.core.IMylarStructureBridge;
import org.eclipse.mylar.core.MylarPlugin;
import org.eclipse.ui.views.markers.internal.ProblemMarker;

/**
 * @author Mik Kersten
 */
public class ResourceStructureBridge implements IMylarStructureBridge {

    public final static String CONTENT_TYPE = MylarPlugin.CONTENT_TYPE_ANY;

    public ResourceStructureBridge() {
    	if (false) { // XXX enable
    		IWorkspace workspace = ResourcesPlugin.getWorkspace();
   	   		workspace.addResourceChangeListener(new ResourceMarkerListener());
    	}
    }
    
    public String getContentType() {
        return CONTENT_TYPE;
    }
    
    public String getParentHandle(String handle) {
        IResource resource = (IResource)getObjectForHandle(handle);
        if (resource != null) {
            return getHandleIdentifier(resource.getParent());
        } else {
            return null;
        }
    }

    public String getHandleIdentifier(Object object) {
        if (object instanceof IProject) {
            String path = ((IResource)object).getFullPath().toPortableString();
            String javaCoreStylePath = "=" + path.substring(1);
            return javaCoreStylePath;
        }
        if (object instanceof IResource) {
            return ((IResource)object).getFullPath().toPortableString();
        } else {
            return null;
        }
    }
    
    public Object getObjectForHandle(String handle) {
    	if (handle == null) return null;
    	IPath path = new Path(handle);
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        if (path.segmentCount() == 1) {
            String projectName = handle.substring(1);
            return workspace.getRoot().getProject(projectName);
        } else if (path.segmentCount() > 1) {
        	return workspace.getRoot().findMember(path);
        } else {
            return null;
        }
    }

    public String getName(Object object) {
        if (object instanceof IResource) {
            return ((IResource)object).getName();
        } else {
            return "";
        }
    }

    public boolean canBeLandmark(String handle) {
    	Object element = getObjectForHandle(handle);
    	return element instanceof IFile;
    }

    public boolean acceptsObject(Object object) {
        return object instanceof IResource;
    }

    public boolean canFilter(Object element) {
        return true;
    }

    public boolean isDocument(String handle) {
        return getObjectForHandle(handle) instanceof IFile;
    }

	public String getHandleForOffsetInObject(Object resource, int offset) {
        if (resource == null || !(resource instanceof ProblemMarker)) return null;
    	ProblemMarker marker = (ProblemMarker)resource;
		// we can only get a handle for a marker with the resource plugin.xml
        if (marker == null) return null;
        try {
            IResource res= marker.getResource();

            if (res instanceof IFile) {
                IFile file = (IFile)res; 
                return getHandleIdentifier(file);
            }
            return null;
        }
        catch (Throwable t) {
            MylarPlugin.log(t, "Could not find element for: " + marker);
            return null;
        }
    }

	public IProject getProjectForObject(Object object) {
		if(object instanceof IResource){
			return ((IResource)object).getProject();
		}
		return null;
	}

    public String getContentType(String elementHandle) {
        return getContentType();
    }

    /**
     * These methods aren't needed since there is no generic active search
     */
	public List<AbstractRelationProvider> getRelationshipProviders() {
		return Collections.emptyList();
	}

	public List<IDegreeOfSeparation> getDegreesOfSeparation() {
		return Collections.emptyList();
	}

	public void setParentBridge(IMylarStructureBridge bridge) {
		// TODO Auto-generated method stub
		
	}

	public boolean containsProblem(IMylarElement node) {
		// TODO Auto-generated method stub
		return false;
	}
}
