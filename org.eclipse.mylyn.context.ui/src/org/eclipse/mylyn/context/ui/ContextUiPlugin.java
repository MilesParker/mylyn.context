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

package org.eclipse.mylar.context.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.mylar.context.core.AbstractRelationProvider;
import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.mylar.context.core.IMylarElement;
import org.eclipse.mylar.context.core.IMylarRelation;
import org.eclipse.mylar.core.MylarStatusHandler;
import org.eclipse.mylar.internal.context.ui.AbstractContextLabelProvider;
import org.eclipse.mylar.internal.context.ui.ActiveSearchViewTracker;
import org.eclipse.mylar.internal.context.ui.ColorMap;
import org.eclipse.mylar.internal.context.ui.ContentOutlineManager;
import org.eclipse.mylar.internal.context.ui.ContextPerspectiveManager;
import org.eclipse.mylar.internal.context.ui.ContextUiPrefContstants;
import org.eclipse.mylar.internal.context.ui.FocusedViewerManager;
import org.eclipse.mylar.internal.context.ui.Highlighter;
import org.eclipse.mylar.internal.context.ui.HighlighterList;
import org.eclipse.mylar.internal.context.ui.MylarWorkingSetManager;
import org.eclipse.mylar.internal.context.ui.actions.ContextRetrieveAction;
import org.eclipse.mylar.internal.tasks.ui.ITaskHighlighter;
import org.eclipse.mylar.internal.tasks.ui.ITasksUiConstants;
import org.eclipse.mylar.monitor.ui.MylarMonitorUiPlugin;
import org.eclipse.mylar.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylar.tasks.core.AbstractRepositoryTask;
import org.eclipse.mylar.tasks.core.DateRangeContainer;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.core.ITaskActivityListener;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author Mik Kersten
 */
public class ContextUiPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.eclipse.mylar.ui";

	private Map<String, AbstractContextUiBridge> bridges = new HashMap<String, AbstractContextUiBridge>();

	private Map<String, ILabelProvider> contextLabelProviders = new HashMap<String, ILabelProvider>();

	private static ContextUiPlugin INSTANCE;

	private ResourceBundle resourceBundle;

	private boolean decorateInterestMode = false;

	private HighlighterList highlighters = null;

	private Highlighter intersectionHighlighter;

	private ColorMap colorMap = new ColorMap();

	private FocusedViewerManager viewerManager;

	private ContextPerspectiveManager perspectiveManager = new ContextPerspectiveManager();

	private ContentOutlineManager contentOutlineManager = new ContentOutlineManager();

	private List<MylarWorkingSetManager> workingSetUpdaters = null;

	private ActiveSearchViewTracker activeSearchViewTracker = new ActiveSearchViewTracker();

	private Map<AbstractContextUiBridge, ImageDescriptor> activeSearchIcons = new HashMap<AbstractContextUiBridge, ImageDescriptor>();

	private Map<AbstractContextUiBridge, String> activeSearchLabels = new HashMap<AbstractContextUiBridge, String>();

	private Map<String, Set<Class<?>>> preservedFilterClasses = new HashMap<String, Set<Class<?>>>();

	private Map<String, Set<String>> preservedFilterIds = new HashMap<String, Set<String>>();

	private final ITaskHighlighter DEFAULT_HIGHLIGHTER = new ITaskHighlighter() {
		public Color getHighlightColor(ITask task) {
			Highlighter highlighter = getHighlighterForContextId("" + task.getHandleIdentifier());
			if (highlighter != null) {
				return highlighter.getHighlightColor();
			} else {
				return null;
			}
		}
	};

	private static final AbstractContextLabelProvider DEFAULT_LABEL_PROVIDER = new AbstractContextLabelProvider() {

		@Override
		protected Image getImage(IMylarElement node) {
			return null;
		}

		@Override
		protected Image getImage(IMylarRelation edge) {
			return null;
		}

		@Override
		protected String getText(IMylarElement node) {
			return "? " + node;
		}

		@Override
		protected String getText(IMylarRelation edge) {
			return "? " + edge;
		}

		@Override
		protected Image getImageForObject(Object object) {
			return null;
		}

		@Override
		protected String getTextForObject(Object node) {
			return "? " + node;
		}

	};

	private static final AbstractContextUiBridge DEFAULT_UI_BRIDGE = new AbstractContextUiBridge() {

		@Override
		public void open(IMylarElement node) {
			// ignore
		}

		@Override
		public void close(IMylarElement node) {
			// ignore
		}

		@Override
		public boolean acceptsEditor(IEditorPart editorPart) {
			return false;
		}

		@Override
		public List<TreeViewer> getContentOutlineViewers(IEditorPart editor) {
			return Collections.emptyList();
		}

		@Override
		public Object getObjectForTextSelection(TextSelection selection, IEditorPart editor) {
			return null;
		}

		@Override
		public IMylarElement getElement(IEditorInput input) {
			return null;
		}

		@Override
		public String getContentType() {
			return null;
		}
	};

	private static final ITaskActivityListener TASK_ACTIVATION_LISTENER = new ITaskActivityListener() {

		public void activityChanged(DateRangeContainer week) {
			// ignore

		}

		public void calendarChanged() {
			// ignore

		}

		public void taskActivated(ITask task) {
			boolean hasLocalContext = ContextCorePlugin.getContextManager().hasContext(task.getHandleIdentifier());
			if (!hasLocalContext && task instanceof AbstractRepositoryTask) {
				AbstractRepositoryTask repositoryTask = (AbstractRepositoryTask) task;
				AbstractRepositoryConnector connector = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(
						repositoryTask);
				TaskRepository repository = TasksUiPlugin.getRepositoryManager().getRepository(
						repositoryTask.getRepositoryUrl());

				if (connector != null && connector.hasRepositoryContext(repository, repositoryTask)) {
					boolean getRemote = MessageDialog.openQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getShell(), ITasksUiConstants.TITLE_DIALOG,
							"No local task context exists.  Retrieve from repository?");
					if (getRemote) {
						new ContextRetrieveAction().run(repositoryTask);
					}
				}
			}
		}

		public void taskDeactivated(ITask task) {
			// ignore

		}

		public void taskListRead() {
			// ignore

		}

		public void tasksActivated(List<ITask> tasks) {
			// ignore

		}

	};

	public ContextUiPlugin() {
		super();
		INSTANCE = this;
		try {
			resourceBundle = ResourceBundle.getBundle("org.eclipse.mylar.MylarUiPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		} catch (Throwable t) {
			MylarStatusHandler.log(t, "plug-in intialization failed");
		}
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		initializeDefaultPreferences(getPreferenceStore());
		initializeHighlighters();
		initializeActions();

		viewerManager = new FocusedViewerManager();

		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					ContextCorePlugin.getContextManager().addListener(viewerManager);
					MylarMonitorUiPlugin.getDefault().addWindowPartListener(contentOutlineManager);

					// NOTE: task list must have finished initializing
					TasksUiPlugin.getDefault().setHighlighter(DEFAULT_HIGHLIGHTER);
					TasksUiPlugin.getTaskListManager().addActivityListener(perspectiveManager);
					TasksUiPlugin.getTaskListManager().addActivityListener(TASK_ACTIVATION_LISTENER);

					workbench.addWindowListener(activeSearchViewTracker);
					IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
					for (int i = 0; i < windows.length; i++) {
						windows[i].addPageListener(activeSearchViewTracker);
						IWorkbenchPage[] pages = windows[i].getPages();
						for (int j = 0; j < pages.length; j++) {
							pages[j].addPartListener(activeSearchViewTracker);
						}
					}
				} catch (Exception e) {
					MylarStatusHandler.fail(e, "Context UI initialization failed", true);
				}
			}
		});
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			super.stop(context);
			ContextCorePlugin.getContextManager().removeListener(viewerManager);
			MylarMonitorUiPlugin.getDefault().removeWindowPartListener(contentOutlineManager);

			TasksUiPlugin.getTaskListManager().removeActivityListener(perspectiveManager);
			TasksUiPlugin.getTaskListManager().removeActivityListener(TASK_ACTIVATION_LISTENER);

			IWorkbench workbench = PlatformUI.getWorkbench();
			if (workbench != null) {
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

			viewerManager.dispose();
			colorMap.dispose();
			highlighters.dispose();
		} catch (Exception e) {
			MylarStatusHandler.fail(e, "Mylar UI stop failed", false);
		}
	}

	private void initializeActions() {
		// don't have any actions to initialize
	}

	private void initializeHighlighters() {
		String hlist = getPreferenceStore().getString(ContextUiPrefContstants.HIGHLIGHTER_PREFIX);
		if (hlist != null && hlist.length() != 0) {
			highlighters = new HighlighterList(hlist);
		} else {
			highlighters = new HighlighterList();
			highlighters.setToDefaultList();
			getPreferenceStore().setValue(ContextUiPrefContstants.HIGHLIGHTER_PREFIX,
					this.highlighters.externalizeToString());
		}
	}

	@Override
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		store.setDefault(ContextUiPrefContstants.NAVIGATORS_AUTO_FILTER_ENABLE, true);

		store.setDefault(ContextUiPrefContstants.AUTO_MANAGE_PERSPECTIVES, false);
		store.setDefault(ContextUiPrefContstants.AUTO_MANAGE_EDITORS, true);
		store.setDefault(ContextUiPrefContstants.AUTO_MANAGE_EDITORS_OPEN_NUM, 4);
		store.setDefault(ContextUiPrefContstants.AUTO_MANAGE_EXPANSION, true);

		store.setDefault(ContextUiPrefContstants.GAMMA_SETTING_LIGHTENED, false);
		store.setDefault(ContextUiPrefContstants.GAMMA_SETTING_STANDARD, true);
		store.setDefault(ContextUiPrefContstants.GAMMA_SETTING_DARKENED, false);
	}

	public void setHighlighterMapping(String id, String name) {
		String prefId = ContextUiPrefContstants.TASK_HIGHLIGHTER_PREFIX + id;
		getPreferenceStore().putValue(prefId, name);
	}

	/**
	 * Returns the shared instance.
	 */
	public static ContextUiPlugin getDefault() {
		return INSTANCE;
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = ContextUiPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 */
	public static String getMessage(String key) {
		ResourceBundle bundle = getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public boolean isDecorateInterestMode() {
		return decorateInterestMode;
	}

	public void setDecorateInterestMode(boolean decorateInterestLevel) {
		this.decorateInterestMode = decorateInterestLevel;
	}

	public List<AbstractContextUiBridge> getUiBridges() {
		UiExtensionPointReader.initExtensions();
		return new ArrayList<AbstractContextUiBridge>(bridges.values());
	}

	/**
	 * @return the corresponding adapter if found, or an adapter with no
	 *         behavior otherwise (so null is never returned)
	 */
	public AbstractContextUiBridge getUiBridge(String contentType) {
		UiExtensionPointReader.initExtensions();
		AbstractContextUiBridge bridge = bridges.get(contentType);
		if (bridge != null) {
			return bridge;
		} else {
			return DEFAULT_UI_BRIDGE;
		}
	}

	/**
	 * TODO: cache this to improve performance?
	 */
	public AbstractContextUiBridge getUiBridgeForEditor(IEditorPart editorPart) {
		UiExtensionPointReader.initExtensions();
		AbstractContextUiBridge foundBridge = null;
		for (AbstractContextUiBridge bridge : bridges.values()) {
			if (bridge.acceptsEditor(editorPart)) {
				foundBridge = bridge;
				break;
			}
		}
		if (foundBridge != null) {
			return foundBridge;
		} else {
			return DEFAULT_UI_BRIDGE;
		}
	}

	private void internalAddBridge(String extension, AbstractContextUiBridge bridge) {
		this.bridges.put(extension, bridge);
	}

	public ILabelProvider getContextLabelProvider(String extension) {
		// if (!UiExtensionPointReader.extensionsRead)
		// UiExtensionPointReader.initExtensions();
		ILabelProvider provider = contextLabelProviders.get(extension);
		if (provider != null) {
			return provider;
		} else {
			return DEFAULT_LABEL_PROVIDER;
		}
	}

	private void internalAddContextLabelProvider(String extension, ILabelProvider provider) {
		this.contextLabelProviders.put(extension, provider);
	}

	public void updateGammaSetting(ColorMap.GammaSetting setting) {
		if (colorMap.getGammaSetting() != setting) {
			highlighters.updateHighlighterWithGamma(colorMap.getGammaSetting(), setting);
			colorMap.setGammaSetting(setting);
		}
	}

	public ColorMap getColorMap() {
		return colorMap;
	}

	public Highlighter getDefaultHighlighter() {
		return HighlighterList.DEFAULT_HIGHLIGHTER;
	}

	/**
	 * @return null if not found
	 */
	public Highlighter getHighlighter(String name) {
		if (highlighters == null) {
			this.initializeHighlighters();
		}
		return highlighters.getHighlighter(name);
	}

	public Highlighter getHighlighterForContextId(String id) {
		String prefId = ContextUiPrefContstants.TASK_HIGHLIGHTER_PREFIX + id;
		String highlighterName = getPreferenceStore().getString(prefId);
		return getHighlighter(highlighterName);
	}

	public HighlighterList getHighlighterList() {
		if (this.highlighters == null) {
			this.initializeHighlighters();
		}
		return this.highlighters;
	}

	public List<Highlighter> getHighlighters() {
		if (highlighters == null) {
			this.initializeHighlighters();
		}
		return highlighters.getHighlighters();
	}

	public Highlighter getIntersectionHighlighter() {
		return intersectionHighlighter;
	}

	public void setColorMap(ColorMap colorMap) {
		this.colorMap = colorMap;
	}

	public void setIntersectionHighlighter(Highlighter intersectionHighlighter) {
		this.intersectionHighlighter = intersectionHighlighter;
	}

	public boolean isIntersectionMode() {
		return getPreferenceStore().getBoolean(ContextUiPrefContstants.INTERSECTION_MODE);
	}

	public void setIntersectionMode(boolean isIntersectionMode) {
		getPreferenceStore().setValue(ContextUiPrefContstants.INTERSECTION_MODE, isIntersectionMode);
	}

	public FocusedViewerManager getViewerManager() {
		return viewerManager;
	}

	static class UiExtensionPointReader {

		private static boolean extensionsRead = false;

		private static UiExtensionPointReader thisReader = new UiExtensionPointReader();

		public static final String EXTENSION_ID_CONTEXT = "org.eclipse.mylar.context.ui.bridges";

		public static final String ELEMENT_UI_BRIDGE = "uiBridge";

		public static final String ELEMENT_PRESERVED_FILTERS = "preservedFilters";

		public static final String ELEMENT_VIEW_ID = "viewId";

		public static final String ELEMENT_ID = "id";

		public static final String ELEMENT_FILTER = "filter";

		public static final String ELEMENT_CLASS = "class";

		public static final String ELEMENT_UI_CONTEXT_LABEL_PROVIDER = "labelProvider";

		public static final String ELEMENT_UI_BRIDGE_CONTENT_TYPE = "contentType";

		public static final String ELEMENT_STRUCTURE_BRIDGE_SEARCH_ICON = "activeSearchIcon";

		public static final String ELEMENT_STRUCTURE_BRIDGE_SEARCH_LABEL = "activeSearchLabel";

		public static void initExtensions() {
			if (!extensionsRead) {
				IExtensionRegistry registry = Platform.getExtensionRegistry();
				IExtensionPoint extensionPoint = registry
						.getExtensionPoint(UiExtensionPointReader.EXTENSION_ID_CONTEXT);
				IExtension[] extensions = extensionPoint.getExtensions();
				for (int i = 0; i < extensions.length; i++) {
					IConfigurationElement[] elements = extensions[i].getConfigurationElements();
					for (int j = 0; j < elements.length; j++) {
						if (elements[j].getName().equals(UiExtensionPointReader.ELEMENT_UI_BRIDGE)) {
							readBridge(elements[j]);
						} else if (elements[j].getName().equals(
								UiExtensionPointReader.ELEMENT_UI_CONTEXT_LABEL_PROVIDER)) {
							readLabelProvider(elements[j]);
						} else if (elements[j].getName().equals(UiExtensionPointReader.ELEMENT_PRESERVED_FILTERS)) {
							readPreservedFilters(elements[j]);
						}
					}
				}
				extensionsRead = true;
			}
		}

		private static void readLabelProvider(IConfigurationElement element) {
			try {
				Object provider = element.createExecutableExtension(UiExtensionPointReader.ELEMENT_CLASS);
				Object contentType = element.getAttribute(UiExtensionPointReader.ELEMENT_UI_BRIDGE_CONTENT_TYPE);
				if (provider instanceof ILabelProvider && contentType != null) {
					ContextUiPlugin.getDefault().internalAddContextLabelProvider((String) contentType,
							(ILabelProvider) provider);
				} else {
					MylarStatusHandler.log("Could not load label provider: " + provider.getClass().getCanonicalName()
							+ " must implement " + ILabelProvider.class.getCanonicalName(), thisReader);
				}
			} catch (CoreException e) {
				MylarStatusHandler.log(e, "Could not load label provider extension");
			}
		}

		private static void readPreservedFilters(IConfigurationElement element) {
			String viewId = element.getAttribute(UiExtensionPointReader.ELEMENT_VIEW_ID);
			IConfigurationElement[] children = element.getChildren();
			for (IConfigurationElement child : children) {
				if (child.getName().equals(UiExtensionPointReader.ELEMENT_FILTER)) {
					try {
						Object filterClass = child.createExecutableExtension(UiExtensionPointReader.ELEMENT_CLASS);
						ContextUiPlugin.getDefault().addPreservedFilterClass(viewId, (ViewerFilter) filterClass);
					} catch (Exception e) {
						String filterId = child.getAttribute(ELEMENT_ID);
						ContextUiPlugin.getDefault().addPreservedFilterId(viewId, filterId);
					}
				}
			}
		}

		@SuppressWarnings("deprecation")
		private static void readBridge(IConfigurationElement element) {
			try {
				Object bridge = element.createExecutableExtension(UiExtensionPointReader.ELEMENT_CLASS);
				Object contentType = element.getAttribute(UiExtensionPointReader.ELEMENT_UI_BRIDGE_CONTENT_TYPE);
				if (bridge instanceof AbstractContextUiBridge && contentType != null) {
					ContextUiPlugin.getDefault().internalAddBridge((String) contentType,
							(AbstractContextUiBridge) bridge);

					String iconPath = element.getAttribute(ELEMENT_STRUCTURE_BRIDGE_SEARCH_ICON);
					if (iconPath != null) {
						ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(element
								.getDeclaringExtension().getNamespace(), iconPath);
						if (descriptor != null) {
							ContextUiPlugin.getDefault().setActiveSearchIcon((AbstractContextUiBridge) bridge,
									descriptor);
						}
					}
					String label = element.getAttribute(ELEMENT_STRUCTURE_BRIDGE_SEARCH_LABEL);
					if (label != null) {
						ContextUiPlugin.getDefault().setActiveSearchLabel((AbstractContextUiBridge) bridge, label);
					}

				} else {
					MylarStatusHandler.log("Could not load bridge: " + bridge.getClass().getCanonicalName()
							+ " must implement " + AbstractContextUiBridge.class.getCanonicalName(), thisReader);
				}
			} catch (CoreException e) {
				MylarStatusHandler.log(e, "Could not load bridge extension");
			}
		}
	}

	/**
	 * @param task
	 *            can be null to indicate no task
	 */
	public String getPerspectiveIdFor(ITask task) {
		if (task != null) {
			return getPreferenceStore().getString(
					ContextUiPrefContstants.PREFIX_TASK_TO_PERSPECTIVE + task.getHandleIdentifier());
		} else {
			return getPreferenceStore().getString(ContextUiPrefContstants.PERSPECTIVE_NO_ACTIVE_TASK);
		}
	}

	/**
	 * @param task
	 *            can be null to indicate no task
	 */
	public void setPerspectiveIdFor(ITask task, String perspectiveId) {
		if (task != null) {
			getPreferenceStore().setValue(
					ContextUiPrefContstants.PREFIX_TASK_TO_PERSPECTIVE + task.getHandleIdentifier(), perspectiveId);
		} else {
			getPreferenceStore().setValue(ContextUiPrefContstants.PERSPECTIVE_NO_ACTIVE_TASK, perspectiveId);
		}
	}

	public void addWorkingSetManager(MylarWorkingSetManager updater) {
		if (workingSetUpdaters == null) {
			workingSetUpdaters = new ArrayList<MylarWorkingSetManager>();
		}
		workingSetUpdaters.add(updater);
		ContextCorePlugin.getContextManager().addListener(updater);
	}

	public MylarWorkingSetManager getWorkingSetUpdater() {
		if (workingSetUpdaters == null)
			return null;
		else
			return workingSetUpdaters.get(0);
	}

	private void setActiveSearchIcon(AbstractContextUiBridge bridge, ImageDescriptor descriptor) {
		activeSearchIcons.put(bridge, descriptor);
	}

	public ImageDescriptor getActiveSearchIcon(AbstractContextUiBridge bridge) {
		UiExtensionPointReader.initExtensions();
		return activeSearchIcons.get(bridge);
	}

	private void setActiveSearchLabel(AbstractContextUiBridge bridge, String label) {
		activeSearchLabels.put(bridge, label);
	}

	public String getActiveSearchLabel(AbstractContextUiBridge bridge) {
		UiExtensionPointReader.initExtensions();
		return activeSearchLabels.get(bridge);
	}

	public void updateDegreesOfSeparation(Collection<AbstractRelationProvider> providers, int degreeOfSeparation) {
		for (AbstractRelationProvider provider : providers) {
			updateDegreeOfSeparation(provider, degreeOfSeparation);
		}
	}

	public void updateDegreeOfSeparation(AbstractRelationProvider provider, int degreeOfSeparation) {
		ContextCorePlugin.getContextManager().resetLandmarkRelationshipsOfKind(provider.getId());
		ContextUiPlugin.getDefault().getPreferenceStore().setValue(provider.getGenericId(), degreeOfSeparation);
		provider.setDegreeOfSeparation(degreeOfSeparation);
		for (IMylarElement element : ContextCorePlugin.getContextManager().getActiveContext().getInteresting()) {
			if (element.getInterest().isLandmark()) {
				provider.landmarkAdded(element);
			}
		}
	}

	public void refreshRelatedElements() {
		try {
			for (AbstractRelationProvider provider : ContextCorePlugin.getDefault().getRelationProviders()) {
				List<AbstractRelationProvider> providerList = new ArrayList<AbstractRelationProvider>();
				providerList.add(provider);
				updateDegreesOfSeparation(providerList, provider.getCurrentDegreeOfSeparation());
			}
		} catch (Throwable t) {
			MylarStatusHandler.fail(t, "Could not refresn related elements", false);
		}
	}

	public void addPreservedFilterClass(String viewId, ViewerFilter filter) {
		Set<Class<?>> preservedList = preservedFilterClasses.get(viewId);
		if (preservedList == null) {
			preservedList = new HashSet<Class<?>>();
			preservedFilterClasses.put(viewId, preservedList);
		}
		preservedList.add(filter.getClass());
	}

	public Set<Class<?>> getPreservedFilterClasses(String viewId) {
		UiExtensionPointReader.initExtensions();
		if (preservedFilterClasses.containsKey(viewId)) {
			return preservedFilterClasses.get(viewId);
		} else {
			return Collections.emptySet();
		}
	}

	public void addPreservedFilterId(String viewId, String filterId) {
		Set<String> preservedList = preservedFilterIds.get(viewId);
		if (preservedList == null) {
			preservedList = new HashSet<String>();
			preservedFilterIds.put(viewId, preservedList);
		}
		preservedList.add(filterId);
	}

	public Set<String> getPreservedFilterIds(String viewId) {
		UiExtensionPointReader.initExtensions();
		if (preservedFilterIds.containsKey(viewId)) {
			return preservedFilterIds.get(viewId);
		} else {
			return Collections.emptySet();
		}
	}
}
