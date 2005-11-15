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
 * Created on Jan 26, 2005
  */
package org.eclipse.mylar.java.search;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.ui.search.JavaSearchQuery;
import org.eclipse.jdt.internal.ui.search.JavaSearchResult;
import org.eclipse.jdt.ui.search.ElementQuerySpecification;
import org.eclipse.jdt.ui.search.QuerySpecification;
import org.eclipse.mylar.core.AbstractRelationshipProvider;
import org.eclipse.mylar.core.IMylarContextNode;
import org.eclipse.mylar.core.IMylarStructureBridge;
import org.eclipse.mylar.core.MylarPlugin;
import org.eclipse.mylar.core.search.IActiveSearchListener;
import org.eclipse.mylar.core.search.IMylarSearchOperation;
import org.eclipse.mylar.java.JavaStructureBridge;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search2.internal.ui.InternalSearchUI;



/**
 * @author Mik Kersten
 */
public abstract class AbstractJavaRelationshipProvider extends AbstractRelationshipProvider {
	
	public static List<Job> runningJobs = new ArrayList<Job>();
    
	public static final String ID_GENERIC = "org.eclipse.mylar.java.relation";
    public static final String NAME = "Java relationships";	
    public static final int DEFAULT_DEGREE = 2; 
    
    public String getGenericId(){
    	return ID_GENERIC;
    }
    
	protected AbstractJavaRelationshipProvider(String structureKind, String id) {
        super(structureKind, id);
    }
    
    @Override
    protected void findRelated(final IMylarContextNode node, int degreeOfSeparation) {
    	if (node == null) return;
        if (!node.getContentKind().equals(JavaStructureBridge.CONTENT_TYPE)) return;
        IJavaElement javaElement = JavaCore.create(node.getElementHandle());
        if (!acceptElement(javaElement) || !javaElement.exists()) {
            return; 
        }
        
        IJavaSearchScope scope = createJavaSearchScope(javaElement, degreeOfSeparation);
        if (scope != null) runJob(node,  degreeOfSeparation, getId());
    }

    private IJavaSearchScope createJavaSearchScope(IJavaElement element, int degreeOfSeparation) {
        List<IMylarContextNode> landmarks = MylarPlugin.getContextManager().getActiveLandmarks();
        List<IMylarContextNode> interestingElements = MylarPlugin.getContextManager().getActiveContext().getInteresting();
        Set<IJavaElement> searchElements = new HashSet<IJavaElement>();
        int includeMask = IJavaSearchScope.SOURCES;
        if (degreeOfSeparation == 1) {
            for (IMylarContextNode landmark : landmarks) {
            	IMylarStructureBridge sbridge = MylarPlugin.getDefault().getStructureBridge(landmark.getContentKind());
            	Object o = sbridge.getObjectForHandle(landmark.getElementHandle());
            	if(o instanceof IJavaElement){
            		IJavaElement landmarkElement = (IJavaElement)o;
            		if(landmarkElement.exists()){
		                if (landmarkElement instanceof IMember && !landmark.getDegreeOfInterest().isPropagated()) {
		                    searchElements.add(((IMember)landmarkElement).getCompilationUnit());
		                } else if (landmarkElement instanceof ICompilationUnit) {
		                    searchElements.add(landmarkElement);
		                }
            		}
            	}
            } 
        } else if (degreeOfSeparation == 2) {
            for (IMylarContextNode interesting : interestingElements) {
            	IMylarStructureBridge sbridge = MylarPlugin.getDefault().getStructureBridge(interesting.getContentKind());
            	Object o = sbridge.getObjectForHandle(interesting.getElementHandle());
            	if(o instanceof IJavaElement){
	                IJavaElement interestingElement = (IJavaElement)o;
	                if(interestingElement.exists()){
		                if (interestingElement instanceof IMember && !interesting.getDegreeOfInterest().isPropagated()) {
		                    searchElements.add(((IMember)interestingElement).getCompilationUnit());
		                } else if (interestingElement instanceof ICompilationUnit) {
		                    searchElements.add(interestingElement);
		                }
	                }
            	}
            }  
        } else if (degreeOfSeparation == 3 || degreeOfSeparation == 4) {
            for (IMylarContextNode interesting : interestingElements) {
            	IMylarStructureBridge sbridge = MylarPlugin.getDefault().getStructureBridge(interesting.getContentKind());
            	Object o = sbridge.getObjectForHandle(interesting.getElementHandle());
            	IProject project = sbridge.getProjectForObject(o);// TODO what to do when the element is not a java element, how determine if a javaProject?
            	
            	if(project != null && JavaProject.hasJavaNature(project) && project.exists()){
            		IJavaProject javaProject = JavaCore.create(project);//((IJavaElement)o).getJavaProject();
            		if (javaProject != null && javaProject.exists()) searchElements.add(javaProject);
            	}
            }   
            if (degreeOfSeparation == 4) {
                includeMask = IJavaSearchScope.SOURCES | IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SYSTEM_LIBRARIES;
            }
        } else if (degreeOfSeparation == 5) {
            return SearchEngine.createWorkspaceScope();
        } 
     
        if (searchElements.size() == 0) {
            return null;
        } else {    
            IJavaElement[] elements = new IJavaElement[searchElements.size()];
            int j = 0;
            for (IJavaElement searchElement : searchElements) {
                elements[j] = searchElement;
                j++;
            } 
            return SearchEngine.createJavaSearchScope(elements, includeMask);
        }
    }
    
    protected boolean acceptResultElement(IJavaElement element) {
        return !(element instanceof IImportDeclaration);
    }
    
    protected boolean acceptElement(IJavaElement javaElement) {
        return javaElement != null 
            && (javaElement instanceof IMember || javaElement instanceof IType);
    }

    private void runJob(
            final IMylarContextNode node, 
            final int degreeOfSeparation, 
            final String kind) {
    	
        int limitTo = 0;
        if (kind.equals(JavaReferencesProvider.ID)) {
            limitTo = IJavaSearchConstants.REFERENCES;
        } else if (kind.equals(JavaImplementorsProvider.ID)) {
            limitTo = IJavaSearchConstants.IMPLEMENTORS;
        } else if (kind.equals(JUnitReferencesProvider.ID)) {
            limitTo = IJavaSearchConstants.REFERENCES; 
        } else if (kind.equals(JavaReadAccessProvider.ID)) {
            limitTo = IJavaSearchConstants.REFERENCES; 
        } else if (kind.equals(JavaWriteAccessProvider.ID)) {
            limitTo = IJavaSearchConstants.REFERENCES; 
        }
        
        final JavaSearchOperation query = (JavaSearchOperation)getSearchOperation(node, limitTo, degreeOfSeparation);
        if(query == null) return;
        
        JavaSearchJob job = new JavaSearchJob(query.getLabel(), query);
        query.addListener(new IActiveSearchListener() {
        
        	private boolean gathered = false;
        
        	public boolean resultsGathered() {
    			return gathered;
    		} 
        	
           public void searchCompleted(List l) {               
               if (l == null) return;
                List<IJavaElement> relatedHandles = new ArrayList<IJavaElement>();
                Object[] elements = l.toArray();
                for (int i = 0; i < elements.length; i++) {
                    if (elements[i] instanceof IJavaElement) relatedHandles.add((IJavaElement)elements[i]);
                } 

                for(IJavaElement element : relatedHandles) {
                    if (!acceptResultElement(element)) continue;
                        incrementInterest(node, JavaStructureBridge.CONTENT_TYPE, element.getHandleIdentifier(), degreeOfSeparation);
                } 
                gathered = true;
                AbstractJavaRelationshipProvider.this.searchCompleted(node);
            }

		
        });
    	InternalSearchUI.getInstance();
        
        runningJobs.add(job);
        job.setPriority(Job.DECORATE);
        job.schedule();
    }

    @Override
    public IMylarSearchOperation getSearchOperation(IMylarContextNode node, int limitTo, int degreeOfSeparation){
    	IJavaElement javaElement = JavaCore.create(node.getElementHandle());
    	if(javaElement == null || !javaElement.exists())
    		return null;
    	
        IJavaSearchScope scope = createJavaSearchScope(javaElement, degreeOfSeparation);

        if(scope == null) return null;
        
        QuerySpecification specs = new ElementQuerySpecification(
                javaElement, 
                limitTo,  
                scope, 
                "Mylar degree of separation: " + degreeOfSeparation);
      
    	return new JavaSearchOperation(specs);
    }
        
    public class JavaSearchJob extends Job{

        private JavaSearchOperation op;

        public JavaSearchJob(String name, JavaSearchOperation op) {
            super(name);
            this.op = op;
        }

        /**
         * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        protected IStatus run(IProgressMonitor monitor) {
        		return op.run(monitor);
        }
        
    }
    
    public class JavaSearchOperation extends JavaSearchQuery implements IMylarSearchOperation{
    	private ISearchResult result = null;
    	@Override
    	public ISearchResult getSearchResult() {
    		if(result == null)
    			result = new JavaSearchResult(this);
    		new JavaActiveSearchResultUpdater((JavaSearchResult) result);
    		return result;
    	}
    	
        @Override
        public IStatus run(IProgressMonitor monitor) {
            try {
                IStatus s = super.run(monitor);
                ISearchResult result = getSearchResult();
                if(result instanceof JavaSearchResult){
                    //TODO make better
                    Object[] objs = ((JavaSearchResult)result).getElements();
                    if(objs == null)
                    	notifySearchCompleted(null);	
                    List<Object> l = new ArrayList<Object>();
                    for(int i = 0; i < objs.length; i++){
                        l.add(objs[i]);
                    }
                    notifySearchCompleted(l);
                }
                return s;
            } catch (ConcurrentModificationException cme) {
            	MylarPlugin.log(cme, "java search failed");
            } catch (Throwable t) {
            	MylarPlugin.log(t, "java search failed");
            } 
//            	 search manager not initalized?
        	IStatus status =new Status(IStatus.WARNING,
                    MylarPlugin.IDENTIFIER,
                    IStatus.OK,
                    "could not run Java search",
                    null); 
        	notifySearchCompleted(null);
            return status;
        }
        /**
         * Constructor
         * @param data
         */
        public JavaSearchOperation(QuerySpecification data) {
            super(data);

        }
        
        /** List of listeners wanting to know about the searches */
        private List<IActiveSearchListener> listeners = new ArrayList<IActiveSearchListener>();
        
        
        /**
         * Add a listener for when the bugzilla search is completed
         * 
         * @param l
         *            The listener to add
         */
        public void addListener(IActiveSearchListener l) {
            // add the listener to the list
            listeners.add(l);
        }

        /**
         * Remove a listener for when the bugzilla search is completed
         * 
         * @param l
         *            The listener to remove
         */
        public void removeListener(IActiveSearchListener l) {
            // remove the listener from the list
            listeners.remove(l);
        }

        /**
         * Notify all of the listeners that the bugzilla search is completed
         * 
         * @param doiList
         *            A list of BugzillaSearchHitDoiInfo
         * @param member
         *            The IMember that the search was performed on
         */
        public void notifySearchCompleted(List<Object> l) {
            // go through all of the listeners and call searchCompleted(colelctor,
            // member)
            for (IActiveSearchListener listener : listeners) {
                listener.searchCompleted(l);
            }
        }
        
    }

    @Override
	public void stopAllRunningJobs() {
		for(Job j: runningJobs){
			j.cancel();
		}
		runningJobs.clear();
	}
    
	@Override
	protected int getDefaultDegreeOfSeparation() {
		return DEFAULT_DEGREE;
	}
}
 