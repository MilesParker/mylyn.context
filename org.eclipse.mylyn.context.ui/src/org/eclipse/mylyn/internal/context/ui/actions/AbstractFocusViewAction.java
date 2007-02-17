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

package org.eclipse.mylar.internal.context.ui.actions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.mylar.context.ui.ContextUiPlugin;
import org.eclipse.mylar.context.ui.InterestFilter;
import org.eclipse.mylar.core.MylarStatusHandler;
import org.eclipse.mylar.internal.context.ui.ContextUiImages;
import org.eclipse.mylar.monitor.ui.MylarMonitorUiPlugin;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

/**
 * Extending this class makes it possible to apply Mylar management to a
 * structured view (e.g. to provide interest-based filtering).
 * 
 * @author Mik Kersten
 */
public abstract class AbstractFocusViewAction extends Action implements IViewActionDelegate, IActionDelegate2,
		ISelectionListener {

	private static final String ACTION_LABEL = "Apply Mylar";

	public static final String PREF_ID_PREFIX = "org.eclipse.mylar.ui.interest.filter.";

	protected String globalPrefId;

	protected IAction initAction = null;

	protected final InterestFilter interestFilter;

	protected IViewPart viewPart;

	protected Map<StructuredViewer, List<ViewerFilter>> previousFilters = new WeakHashMap<StructuredViewer, List<ViewerFilter>>();

	private boolean manageViewer = true;

	private boolean manageFilters = true;

	private boolean manageLinking = false;

	private boolean wasLinkingEnabled = false;
	
	private static Map<IViewPart, AbstractFocusViewAction> partMap = new WeakHashMap<IViewPart, AbstractFocusViewAction>();

	public static AbstractFocusViewAction getActionForPart(IViewPart part) {
		return partMap.get(part);
	}

	public IViewPart getPartForAction() {
		if (viewPart == null) {
			if (this instanceof IWorkbenchWindowActionDelegate) {
				if (Platform.isRunning()) {
					throw new RuntimeException("not supported on IWorkbenchWindowActionDelegate");
				}
			} else {
				throw new RuntimeException("error: viewPart is null");
			}
		}
		return viewPart;
	}

	public AbstractFocusViewAction(InterestFilter interestFilter, boolean manageViewer, boolean manageFilters,
			boolean manageLinking) {
		super();
		this.interestFilter = interestFilter;
		this.manageViewer = manageViewer;
		this.manageFilters = manageFilters;
		this.manageLinking = manageLinking;
		setText(ACTION_LABEL);
		setToolTipText(ACTION_LABEL);
		setImageDescriptor(ContextUiImages.INTEREST_FILTERING);
	}

	public void init(IAction action) {
		initAction = action;
		setChecked(action.isChecked());
	}

	public void init(IViewPart view) {
		String id = view.getSite().getId();
		globalPrefId = PREF_ID_PREFIX + id;
		viewPart = view;
		partMap.put(view, this);
	}

	public void run(IAction action) {
		setChecked(action.isChecked());
		valueChanged(action, action.isChecked(), true);
	}

	/**
	 * Don't update if the preference has not been initialized.
	 */
	public void update() {
		if (globalPrefId != null) {
			update(ContextUiPlugin.getDefault().getPreferenceStore().getBoolean(globalPrefId));
		}
	}

	/**
	 * This operation is expensive.
	 */
	public void update(boolean on) {
		valueChanged(initAction, on, false);
	}

	protected void valueChanged(IAction action, final boolean on, boolean store) {
		if (PlatformUI.getWorkbench().isClosing()) {
			return;
		}
		boolean wasPaused = ContextCorePlugin.getContextManager().isContextCapturePaused();
		try {
			if (!wasPaused) {
				ContextCorePlugin.getContextManager().setContextCapturePaused(true);
			}
			setChecked(on);
			action.setChecked(on);
			if (store && ContextCorePlugin.getDefault() != null) {
				ContextUiPlugin.getDefault().getPreferenceStore().setValue(globalPrefId, on);
			}

			List<StructuredViewer> viewers = getViewers();
			for (StructuredViewer viewer : viewers) {
				if (viewPart != null && !viewer.getControl().isDisposed() && manageViewer) {
					ContextUiPlugin.getDefault().getViewerManager().addManagedViewer(viewer, viewPart);
				}
				updateInterestFilter(on, viewer);
			}

			if (manageLinking) {
				updateLinking(on);
			}
		} catch (Throwable t) {
			MylarStatusHandler.fail(t, "Could not install viewer manager on: " + globalPrefId, false);
		} finally {
			if (!wasPaused) {
				ContextCorePlugin.getContextManager().setContextCapturePaused(false);
			}
		}
	}

	private void updateLinking(boolean on) {
		if (on) {
			wasLinkingEnabled = isDefaultLinkingEnabled();
			setDefaultLinkingEnabled(false);
			MylarMonitorUiPlugin.getDefault().addWindowPostSelectionListener(this);
		} else {
			MylarMonitorUiPlugin.getDefault().removeWindowPostSelectionListener(this);
			setDefaultLinkingEnabled(wasLinkingEnabled);
		}
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (manageLinking && selection instanceof ITextSelection && part instanceof IEditorPart) {
			try {
				List<StructuredViewer> viewers = getViewers();
				if (viewers.size() == 1) {
					StructuredViewer viewer = getViewers().get(0);
					ITextSelection textSelection = (ITextSelection) selection;
					ISelection toSelect = resolveSelection((IEditorPart)part, textSelection, viewer);
					if (toSelect != null) {
						ISelection currentSelection = viewer.getSelection();
						if (!selection.equals(currentSelection)) {
							select(viewer, toSelect);
						}
					}
				}
			} catch (Throwable t) {
				// ignore, linking failure is not fatal
			}
		}
	}

	protected void select(StructuredViewer viewer, ISelection selection) {
		viewer.setSelection(selection, true);
	}

	/**
	 * Override to provide managed linking
	 */
	protected ISelection resolveSelection(IEditorPart part, ITextSelection selection, StructuredViewer viewer)
			throws CoreException {
		return null;
	}

	/**
	 * Override to provide managed linking
	 */
	protected void setDefaultLinkingEnabled(boolean on) {
		// ignore
	}
	
	/**
	 * Override to provide managed linking
	 */	
	protected boolean isDefaultLinkingEnabled() {
		return false;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// ignore
	}

	/**
	 * Public for testing
	 */
	public void updateInterestFilter(final boolean on, StructuredViewer viewer) {
		if (viewer != null) {
			if (on) {
				installInterestFilter(viewer);
				ContextUiPlugin.getDefault().getViewerManager().addFilteredViewer(viewer);
			} else {
				ContextUiPlugin.getDefault().getViewerManager().removeFilteredViewer(viewer);
				uninstallInterestFilter(viewer);
			}
		}
	}

	/**
	 * Public for testing
	 */
	public abstract List<StructuredViewer> getViewers();

	/**
	 * @return filters that should not be removed when the interest filter is
	 *         installed
	 */
	private Set<Class<?>> getPreservedFilters() {
		return ContextUiPlugin.getDefault().getPreservedFilterClasses(viewPart.getSite().getId());
	}

	protected boolean installInterestFilter(StructuredViewer viewer) {
		if (viewer == null) {
			MylarStatusHandler.log("The viewer to install InterestFilter is null", this);
			return false;
		} else if (viewer.getControl().isDisposed() && manageViewer) {
			// TODO: do this with part listener, not lazily?
			return false;
		}

		try {
			viewer.getControl().setRedraw(false);
			previousFilters.put(viewer, Arrays.asList(viewer.getFilters()));
			if (viewPart != null && manageFilters) {
				Set<Class<?>> excludedFilters = getPreservedFilters();
				for (ViewerFilter filter : previousFilters.get(viewer)) {
					if (!excludedFilters.contains(filter.getClass())) {
						try {
							viewer.removeFilter(filter);
						} catch (Throwable t) {
							MylarStatusHandler.fail(t, "Failed to remove filter: " + filter, false);
						}
					}
				}
			}
			viewer.addFilter(interestFilter);
			if (viewer instanceof TreeViewer) {
				((TreeViewer) viewer).expandAll();
			}
			viewer.getControl().setRedraw(true);
			return true;
		} catch (Throwable t) {
			t.printStackTrace();
			MylarStatusHandler.fail(t, "Could not install viewer filter on: " + globalPrefId, false);
		}
		return false;
	}

	protected void uninstallInterestFilter(StructuredViewer viewer) {
		if (viewer == null) {
			MylarStatusHandler.log("Could not uninstall interest filter", this);
			return;
		} else if (viewer.getControl().isDisposed()) {
			// TODO: do this with part listener, not lazily?
			ContextUiPlugin.getDefault().getViewerManager().removeManagedViewer(viewer, viewPart);
			return;
		}

		viewer.getControl().setRedraw(false);
		if (viewPart != null && manageFilters) {
			Set<Class<?>> excludedFilters = getPreservedFilters();
			if (previousFilters.containsKey(viewer)) {
				for (ViewerFilter filter : previousFilters.get(viewer)) {
					if (!excludedFilters.contains(filter.getClass())) {
						try {
							viewer.addFilter(filter);
						} catch (Throwable t) {
							MylarStatusHandler.fail(t, "Failed to remove filter: " + filter, false);
						}
					}
				}
				previousFilters.remove(viewer);
			}
		}
		for (ViewerFilter filter : Arrays.asList(viewer.getFilters())) {
			if (filter instanceof InterestFilter) {
				viewer.removeFilter(interestFilter);
			}
		}
		viewer.getControl().setRedraw(true);
	}

	public void dispose() {
		partMap.remove(getPartForAction());
		if (viewPart != null && !PlatformUI.getWorkbench().isClosing()) {
			for (StructuredViewer viewer : getViewers()) {
				ContextUiPlugin.getDefault().getViewerManager().removeManagedViewer(viewer, viewPart);
			}
		}
		MylarMonitorUiPlugin.getDefault().removeWindowPostSelectionListener(this);
	}

	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	public String getGlobalPrefId() {
		return globalPrefId;
	}

	/**
	 * For testing.
	 */
	public InterestFilter getInterestFilter() {
		return interestFilter;
	}

}
