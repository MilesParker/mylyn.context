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
package org.eclipse.mylar.ide;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylar.core.IMylarElement;
import org.eclipse.mylar.core.IMylarStructureBridge;
import org.eclipse.mylar.core.MylarPlugin;
import org.eclipse.mylar.ide.internal.ActiveSearchViewTracker;
import org.eclipse.mylar.ide.ui.NavigatorRefreshListener;
import org.eclipse.mylar.ide.ui.actions.ApplyMylarToNavigatorAction;
import org.eclipse.mylar.ide.ui.actions.ApplyMylarToProblemsListAction;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author Mik Kersten
 */
public class MylarIdePlugin extends AbstractUIPlugin {

	private NavigatorRefreshListener navigatorRefreshListener = new NavigatorRefreshListener();

	private MylarEditorManager editorManager = new MylarEditorManager();
    	
	private ResourceSelectionMonitor resourceSelectionMonitor;

	private static MylarIdePlugin plugin;

	private ActiveSearchViewTracker activeSearchViewTracker = new ActiveSearchViewTracker();

	public MylarIdePlugin() {
		plugin = this;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		MylarPlugin.getContextManager().addListener(navigatorRefreshListener);

		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(new Runnable() {
			public void run() {
				resourceSelectionMonitor = new ResourceSelectionMonitor();
				MylarPlugin.getContextManager().addListener(editorManager);
            	
				MylarPlugin.getDefault().getSelectionMonitors().add(resourceSelectionMonitor);

				if (ApplyMylarToNavigatorAction.getDefault() != null)
					ApplyMylarToNavigatorAction.getDefault().update();
				if (ApplyMylarToProblemsListAction.getDefault() != null)
					ApplyMylarToProblemsListAction.getDefault().update();

				workbench.addWindowListener(activeSearchViewTracker);
				IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
				for (int i = 0; i < windows.length; i++) {
					windows[i].addPageListener(activeSearchViewTracker);
					IWorkbenchPage[] pages = windows[i].getPages();
					for (int j = 0; j < pages.length; j++) {
						pages[j].addPartListener(activeSearchViewTracker);
					}
				}
			}
		});
	}

	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
		MylarPlugin.getContextManager().removeListener(editorManager);
		MylarPlugin.getDefault().getSelectionMonitors().remove(resourceSelectionMonitor);
		MylarPlugin.getContextManager().removeListener(navigatorRefreshListener);

		IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.removeWindowListener(activeSearchViewTracker);
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			IWorkbenchPage[] pages = windows[i].getPages();
			windows[i].removePageListener(activeSearchViewTracker);
			for (int j = 0; j < pages.length; j++) {
				pages[j].removePartListener(activeSearchViewTracker);
			}
		}
	}

	public static MylarIdePlugin getDefault() {
		return plugin;
	}

	public List<IResource> getInterestingResources() {
		List<IResource> interestingResources = new ArrayList<IResource>();
		Set<IMylarElement> resourceElements = MylarPlugin.getContextManager().getInterestingDocuments();
		for (IMylarElement element : resourceElements) {
			IResource resource = getResourceForElement(element);
			if (resource != null) interestingResources.add(resource); 
		}
		
		return interestingResources;
	}
	
	public IResource getResourceForElement(IMylarElement element) {
		IMylarStructureBridge bridge = MylarPlugin.getDefault().getStructureBridge(element.getContentType());
		Object object = bridge.getObjectForHandle(element.getHandleIdentifier());
		if (object instanceof IResource) {
			return (IResource)object;
		} else if (object instanceof IAdaptable) {
			Object adapted = ((IAdaptable)object).getAdapter(IResource.class);
			if (adapted instanceof IResource) {
				return (IResource)adapted;
			}
		}
		return null;
	}
	
	public MylarEditorManager getEditorManager() {
		return editorManager;
	}
	
	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.mylar.ide", path);
	}
}
