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

package org.eclipse.mylar.internal.team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.mylar.context.core.AbstractContextStructureBridge;
import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.mylar.context.core.IMylarContext;
import org.eclipse.mylar.context.core.IMylarElement;
import org.eclipse.mylar.core.MylarStatusHandler;
import org.eclipse.mylar.internal.context.core.MylarContextManager;
import org.eclipse.mylar.resources.MylarResourcesPlugin;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.eclipse.mylar.team.AbstractActiveChangeSetProvider;
import org.eclipse.mylar.team.AbstractContextChangeSetManager;
import org.eclipse.mylar.team.MylarTeamPlugin;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSetManager;
import org.eclipse.team.internal.core.subscribers.ChangeSet;
import org.eclipse.team.internal.core.subscribers.IChangeSetChangeListener;

/**
 * @author Mik Kersten
 */
public class ContextActiveChangeSetManager extends AbstractContextChangeSetManager {
	
	private final IChangeSetChangeListener CHANGE_SET_LISTENER = new IChangeSetChangeListener() {
		public void setRemoved(ChangeSet set) {
			if (set instanceof ContextChangeSet) {
				ContextChangeSet contextChangeSet = (ContextChangeSet) set;
				if (contextChangeSet.getTask().isActive()) {
					for (ActiveChangeSetManager collector : changeSetManagers) {
						collector.add(contextChangeSet); // put it back
					}
				}
			}
		}

		public void setAdded(ChangeSet set) {
			// ignore
		}

		public void defaultSetChanged(ChangeSet previousDefault, ChangeSet set) {
			// ignore
		}

		public void nameChanged(ChangeSet set) {
			// ignore
		}

		public void resourcesChanged(ChangeSet set, IPath[] paths) {
			// ignore
		}
	};

	private List<ActiveChangeSetManager> changeSetManagers = new ArrayList<ActiveChangeSetManager>();

	private Map<String, ContextChangeSet> activeChangeSets = new HashMap<String, ContextChangeSet>();

	public ContextActiveChangeSetManager() {
		Set<AbstractActiveChangeSetProvider> providerList = MylarTeamPlugin.getDefault().getActiveChangeSetProviders();
		for (AbstractActiveChangeSetProvider provider : providerList) {
			ActiveChangeSetManager changeSetManager = provider.getActiveChangeSetManager();
			if (null != changeSetManager) {
				changeSetManagers.add(changeSetManager);
			}
		}
	}

	@Override
	protected void updateChangeSetLabel(ITask task) {
		for (ActiveChangeSetManager collector : changeSetManagers) {
			ChangeSet[] sets = collector.getSets();
			for (int i = 0; i < sets.length; i++) {
				ChangeSet set = sets[i];
				if (set instanceof ContextChangeSet) {
					ContextChangeSet contextChangeSet = (ContextChangeSet) set;
					if (contextChangeSet.getTask().equals(task)) {
						contextChangeSet.initTitle();
					}
				}
			}
		}
	}
	
	@Override
	public void enable() {
		super.enable();
		if (!isEnabled) {
			for (ActiveChangeSetManager collector : changeSetManagers) {
				collector.addListener(CHANGE_SET_LISTENER);
			}
		}
	}

	@Override
	public void disable() {
		super.disable();
		for (ActiveChangeSetManager collector : changeSetManagers) {
			collector.removeListener(CHANGE_SET_LISTENER);
		}
	}

	@Override
	protected void initContextChangeSets() {
		for (ActiveChangeSetManager collector : changeSetManagers) {
			ChangeSet[] sets = collector.getSets();
			for (int i = 0; i < sets.length; i++) {
				ChangeSet restoredSet = sets[i];
				if (!(restoredSet instanceof ContextChangeSet)) {
					String encodedTitle = restoredSet.getName();
					String taskHandle = ContextChangeSet.getHandleFromPersistedTitle(encodedTitle);
					ITask task = TasksUiPlugin.getTaskListManager().getTaskList().getTask(taskHandle);
					if (task != null) {
						try {
							ContextChangeSet contextChangeSet = new ContextChangeSet(task, collector);
							contextChangeSet.restoreResources(restoredSet.getResources());
							collector.remove(restoredSet);
							collector.add(contextChangeSet);
						} catch (Exception e) {
							MylarStatusHandler.fail(e, "could not restore change set", false);
						}
					}
				}
			}
		}
	}

	/**
	 * For testing.
	 */
	public void clearActiveChangeSets() {
		activeChangeSets.clear();
	}

	public IResource[] getResources(ITask task) {
		for (ActiveChangeSetManager collector : changeSetManagers) {
			ChangeSet[] sets = collector.getSets();
			for (int i = 0; i < sets.length; i++) {
				ChangeSet set = sets[i];
				if (set instanceof ContextChangeSet) {
					ContextChangeSet contextChangeSet = (ContextChangeSet) set;
					if (contextChangeSet.getTask().equals(task)) {
						return contextChangeSet.getResources();
					}
				}
			}
		}
		return null;
	}

	public void contextActivated(IMylarContext context) {
		try {
			ITask task = getTask(context);
			if (task != null && !activeChangeSets.containsKey(task.getHandleIdentifier())) {
				for (ActiveChangeSetManager collector : changeSetManagers) {
					ContextChangeSet contextChangeSet = new ContextChangeSet(task, collector);
					List<IResource> interestingResources = MylarResourcesPlugin.getDefault().getInterestingResources();
					contextChangeSet.add(interestingResources.toArray(new IResource[interestingResources.size()]));

					activeChangeSets.put(task.getHandleIdentifier(), contextChangeSet);

					if (!collector.contains(contextChangeSet)) {
						collector.add(contextChangeSet);
					}
				}
			}
		} catch (Exception e) {
			MylarStatusHandler.fail(e, "could not update change set", false);
		}
	}

	public void contextDeactivated(IMylarContext context) {
		for (ActiveChangeSetManager collector : changeSetManagers) {
			ChangeSet[] sets = collector.getSets();
			for (int i = 0; i < sets.length; i++) {
				ChangeSet set = sets[i];
				if (set instanceof ContextChangeSet) {
					IResource[] resources = set.getResources();
					if (resources == null || resources.length == 0) {
						collector.remove(set);
					}
				}
			}
		}
		activeChangeSets.clear();
	}

	public void interestChanged(List<IMylarElement> elements) {
		for (IMylarElement element : elements) {
			AbstractContextStructureBridge bridge = ContextCorePlugin.getDefault().getStructureBridge(element.getContentType());
			try {
				if (bridge.isDocument(element.getHandleIdentifier())) {
					IResource resource = MylarResourcesPlugin.getDefault().getResourceForElement(element, false);
					if (resource != null && resource.exists()) {
						for (ContextChangeSet activeContextChangeSet : getActiveChangeSets()) {
							if (!activeContextChangeSet.contains(resource)) {
								if (element.getInterest().isInteresting()) {
									activeContextChangeSet.add(new IResource[] { resource });
								}
							}
						}
						if (shouldRemove(element)) {
							for (ActiveChangeSetManager collector : changeSetManagers) {
								ChangeSet[] sets = collector.getSets();
								for (int i = 0; i < sets.length; i++) {
									if (sets[i] instanceof ContextChangeSet) {
										sets[i].remove(resource);
									}
								}
							}
						}
					}
				}
			} catch (Exception e) {
				MylarStatusHandler.fail(e, "could not manipulate change set resources", false);
			}
		}
	}
	
	public List<ContextChangeSet> getActiveChangeSets() {
		return new ArrayList<ContextChangeSet>(activeChangeSets.values());
	}

	private ITask getTask(IMylarContext context) {
		List<ITask> activeTasks = TasksUiPlugin.getTaskListManager().getTaskList().getActiveTasks();

		// TODO: support multiple tasks
		if (activeTasks.size() > 0) {
			return activeTasks.get(0);
		} else {
			return null;
		}
	}

	/**
	 * Ignores decay.
	 */
	private boolean shouldRemove(IMylarElement element) {
		// TODO: generalize this logic?
		return (element.getInterest().getValue() + element.getInterest().getDecayValue()) < MylarContextManager
				.getScalingFactors().getInteresting();
	}
}
