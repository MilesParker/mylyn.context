/*******************************************************************************
 * Copyright (c) 2011 Tasktop Technologies.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.internal.context.ui;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylyn.context.core.AbstractContextStructureBridge;
import org.eclipse.mylyn.context.core.ContextChangeEvent;
import org.eclipse.mylyn.context.core.ContextComputationStrategy;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.context.ui.AbstractFocusViewAction;
import org.eclipse.mylyn.internal.context.core.StrategiesExtensionPointReader;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.progress.UIJob;

/**
 * Populate a context from a task data
 * 
 * @author David Green
 * @see ContextComputationStrategy
 */
public class ContextPopulationStrategy {

	private static final String PART_ID = ContextPopulationStrategy.class.getName();

	private ContextComputationStrategy contextComputationStrategy;

	private boolean disabled;

	public void activated(ContextChangeEvent event) {
		if (disabled) {
			return;
		}

		// detect empty context
		if (event.getContext().getAllElements().isEmpty()) {
			// get corresponding task
			ITask task = TasksUi.getRepositoryModel().getTask(event.getContextHandle());
			if (task != null) {
				try {
					TaskData taskData = null;
					if (TasksUiPlugin.getTaskDataManager().hasTaskData(task)) {
						taskData = TasksUiPlugin.getTaskDataManager().getWorkingCopy(task, false).getLocalData();
					}
					IInteractionContext context = event.getContext();
					populateContext(context, task, taskData);
				} catch (CoreException e) {
					ContextUiPlugin.getDefault().getLog().log(e.getStatus());
				}
			}
		}
	}

	public void populateContext(final IInteractionContext context, final ITask task, final TaskData taskData) {
		Job job = new Job(Messages.ContextPopulationStrategy_Populate_Context_Job_Label) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				// compute data model elements
				ContextComputationStrategy strategy = getContextComputationStrategy();
				if (strategy == null) {
					return Status.CANCEL_STATUS;
				}
				final List<Object> contextItems = strategy.computeContext(context, new IAdaptable() {
					public Object getAdapter(@SuppressWarnings("rawtypes")
					Class adapter) {
						if (adapter == ITask.class) {
							return task;
						} else if (adapter == TaskData.class) {
							return taskData;
						}
						return null;
					}
				}, monitor);

				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}

				// add elements to context through simulating selection events  
				if (!contextItems.isEmpty()) {
					UIJob uiJob = new UIJob(Messages.ContextPopulationStrategy_Populate_Context_Job_Label) {
						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {
							IInteractionContext activeContext = ContextCore.getContextManager().getActiveContext();
							if (activeContext != null && activeContext.getHandleIdentifier() != null
									&& activeContext.getHandleIdentifier().equals(context.getHandleIdentifier())) {
								monitor.beginTask(Messages.ContextPopulationStrategy_Populate_Context_Job_Label,
										contextItems.size());
								try {
									for (Object element : contextItems) {

										monitor.worked(1);
										AbstractContextStructureBridge structureBridge = ContextCore.getStructureBridge(element);
										if (structureBridge != null) {
											String handleIdentifier = structureBridge.getHandleIdentifier(element);
											if (handleIdentifier != null) {
												try {
													ContextCore.getContextManager().processInteractionEvent(
															new InteractionEvent(InteractionEvent.Kind.SELECTION,
																	structureBridge.getContentType(), handleIdentifier,
																	PART_ID));
												} catch (Exception e) {
													IStatus status = new Status(IStatus.ERROR,
															ContextUiPlugin.ID_PLUGIN,
															"Unexpected error manipulating context", e); //$NON-NLS-1$
													ContextUiPlugin.getDefault().getLog().log(status);
												}
											}
										}
									}
								} finally {
									monitor.done();
								}
								if (!activeContext.getAllElements().isEmpty()) {
									// activate structured view filters if needed
									if (ContextUiPlugin.getDefault()
											.getPreferenceStore()
											.getBoolean(IContextUiPreferenceContstants.AUTO_FOCUS_NAVIGATORS)) {
										for (IWorkbenchWindow window : Workbench.getInstance().getWorkbenchWindows()) {
											for (IWorkbenchPage page : window.getPages()) {
												for (IViewReference viewReference : page.getViewReferences()) {
													IViewPart viewPart = (IViewPart) viewReference.getPart(false);
													if (viewPart != null) {
														AbstractFocusViewAction applyAction = AbstractFocusViewAction.getActionForPart(viewPart);
														if (applyAction != null) {
															applyAction.update(true);
														}
													}
												}
											}
										}
									}
								}
							}

							return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
						}
					};
					uiJob.schedule();

				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	public synchronized ContextComputationStrategy getContextComputationStrategy() {
		if (contextComputationStrategy == null) {
			List<ContextComputationStrategy> strategies = StrategiesExtensionPointReader.readContextComputationStrategies();
			if (strategies.isEmpty()) {
				disabled = true;
			} else if (strategies.size() > 1) {
				CompoundContextComputationStrategy compoundStrategy = new CompoundContextComputationStrategy();
				compoundStrategy.setDelegates(strategies);
				contextComputationStrategy = compoundStrategy;
			} else {
				contextComputationStrategy = strategies.get(0);
			}
		}
		return contextComputationStrategy;
	}

	public void setContextComputationStrategy(ContextComputationStrategy contextComputationStrategy) {
		this.contextComputationStrategy = contextComputationStrategy;
	}

}
