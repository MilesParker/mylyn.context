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
package org.eclipse.mylar.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylar.context.core.AbstractContextStructureBridge;
import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.mylar.context.core.IMylarContext;
import org.eclipse.mylar.context.core.IMylarElement;
import org.eclipse.mylar.core.MylarStatusHandler;
import org.eclipse.mylar.internal.resources.ResourceChangeMonitor;
import org.eclipse.mylar.internal.resources.ResourceInteractionMonitor;
import org.eclipse.mylar.internal.resources.ResourceInterestUpdater;
import org.eclipse.mylar.internal.resources.ui.ContextEditorManager;
import org.eclipse.mylar.internal.resources.ui.EditorInteractionMonitor;
import org.eclipse.mylar.monitor.ui.MylarMonitorUiPlugin;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author Mik Kersten
 */
public class MylarResourcesPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.eclipse.mylar.resources";
	
	private static MylarResourcesPlugin plugin;

	private ResourceChangeMonitor resourceChangeMonitor = new ResourceChangeMonitor();
	
	private ContextEditorManager editorManager = new ContextEditorManager();

	private ResourceInteractionMonitor resourceInteractionMonitor;

	private EditorInteractionMonitor interestEditorTracker = new EditorInteractionMonitor();

	private ResourceInterestUpdater interestUpdater = new ResourceInterestUpdater();
	
	private ResourceBundle resourceBundle;
	
	private static final String PREF_STORE_DELIM = ", ";

	public static final String PREF_RESOURCES_IGNORED = "org.eclipse.mylar.ide.resources.ignored.pattern";

	public static final String PREF_VAL_DEFAULT_RESOURCES_IGNORED = ".*" + PREF_STORE_DELIM;
	
	public MylarResourcesPlugin() {
		super();
		plugin = this;
		resourceInteractionMonitor = new ResourceInteractionMonitor();
	}

	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		initPreferenceDefaults();

		ContextCorePlugin.getContextManager().addListener(editorManager);
		MylarMonitorUiPlugin.getDefault().getSelectionMonitors().add(resourceInteractionMonitor);

		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeMonitor,
				IResourceChangeEvent.POST_CHANGE);
		
		interestEditorTracker.install(PlatformUI.getWorkbench());

//		final IWorkbench workbench = PlatformUI.getWorkbench();
//		workbench.getDisplay().asyncExec(new Runnable() {
//			public void run() {
//				try {
//					initPreferenceDefaults();
//					ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeMonitor,
//							IResourceChangeEvent.POST_CHANGE);
//					
//					MylarMonitorPlugin.getDefault().getSelectionMonitors().add(resourceInteractionMonitor);
//					ContextCorePlugin.getContextManager().addListener(editorManager);
//
//					interestEditorTracker.install(workbench);
//					
//				} catch (Exception e) {
//					MylarStatusHandler.fail(e, "Mylar Resources stop failed", true);
//				}
//			}
//		});
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			super.stop(context);
			plugin = null;
			resourceBundle = null;
			
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeMonitor);
			ContextCorePlugin.getContextManager().removeListener(editorManager);
			MylarMonitorUiPlugin.getDefault().getSelectionMonitors().remove(resourceInteractionMonitor);
		} catch (Exception e) {
			MylarStatusHandler.fail(e, "Mylar XML stop failed", false);
		}
	}
	
	private void initPreferenceDefaults() {
		getPreferenceStore().setDefault(PREF_RESOURCES_IGNORED, PREF_VAL_DEFAULT_RESOURCES_IGNORED);
	}

	public List<IResource> getInterestingResources(IMylarContext context) {
		List<IResource> interestingResources = new ArrayList<IResource>();
		Collection<IMylarElement> resourceElements = ContextCorePlugin.getContextManager().getInterestingDocuments(context);
		for (IMylarElement element : resourceElements) {
			IResource resource = getResourceForElement(element, false);
			if (resource != null) {
				interestingResources.add(resource);
			}
		}
		return interestingResources;
	}

	public void setExcludedResourcePatterns(Set<String> patterns) {
		StringBuilder store = new StringBuilder();
		for (String string : patterns) {
			store.append(string);
			store.append(PREF_STORE_DELIM);
		}
		getPreferenceStore().setValue(PREF_RESOURCES_IGNORED, store.toString());
	}

	public Set<String> getExcludedResourcePatterns() {
		Set<String> ignored = new HashSet<String>();
		String read = getPreferenceStore().getString(PREF_RESOURCES_IGNORED);
		if (read != null) {
 			StringTokenizer st = new StringTokenizer(read, PREF_STORE_DELIM);
			while (st.hasMoreTokens()) {
				ignored.add(st.nextToken());
			}
		}
		return ignored;
	}

	public ResourceInterestUpdater getInterestUpdater() {
		return interestUpdater;
	}
	
	public IResource getResourceForElement(IMylarElement element, boolean findContainingResource) {
		if (element == null)
			return null;
		AbstractContextStructureBridge bridge = ContextCorePlugin.getDefault().getStructureBridge(element.getContentType());
		Object object = bridge.getObjectForHandle(element.getHandleIdentifier());
		if (object instanceof IResource) {
			return (IResource) object;
		} else if (object instanceof IAdaptable) {
			Object adapted = ((IAdaptable) object).getAdapter(IResource.class);
			if (adapted instanceof IResource) {
				return (IResource) adapted;
			}
		}
		if (findContainingResource) { // recurse if not found
			String parentHandle = bridge.getParentHandle(element.getHandleIdentifier());
			if (element.getHandleIdentifier().equals(parentHandle)) {
				return null;
			} else {
				return getResourceForElement(ContextCorePlugin.getContextManager().getElement(parentHandle), true);
			}
		} else {
			return null;
		}
	}
	
	public void setResourceMonitoringEnabled(boolean enabled) {
		resourceChangeMonitor.setEnabled(enabled);
	}
	
	public ContextEditorManager getEditorManager() {
		return editorManager;
	}
	
	public static MylarResourcesPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = MylarResourcesPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	public ResourceBundle getResourceBundle() {
		try {
			if (resourceBundle == null)
				resourceBundle = ResourceBundle.getBundle("org.eclipse.mylar.xml.XmlPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
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
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.mylar.xml", path);
	} 
}
