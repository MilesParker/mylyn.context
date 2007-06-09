/***************************************************************************
 * Copyright (c) 2004, 2005, 2006 Eike Stepper, Germany.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 **************************************************************************/
package org.eclipse.mylyn.internal.team.ui.templates;

import java.util.List;
import java.util.Locale;

import org.eclipse.mylyn.internal.tasks.ui.editors.TaskPlanningEditor;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryTask;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.RepositoryTaskData;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.team.ui.AbstractCommitTemplateVariable;

/**
 * @author Eike Stepper
 * @author Mik Kersten
 * 
 * TODO refactor into extension point
 */
public class CommitTemplateVariables {

	public static String implode(List<String> list, String separator) {
		StringBuilder builder = new StringBuilder();
		for (String cc : list) {
			if (builder.length() != 0) {
				builder.append(separator);
			}

			builder.append(cc);
		}

		return builder.toString();
	}

	public static class ConnectorTaskPrefix extends AbstractCommitTemplateVariable {

		@Override
		public String getValue(ITask task) {
			if (task instanceof AbstractRepositoryTask) {
				AbstractRepositoryConnector connector = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(
						(AbstractRepositoryTask) task);
				if (connector != null) {
					return connector.getTaskIdPrefix();
				}
			}
			return null;
		}

	}

	public static class RepositoryKind extends AbstractCommitTemplateVariable {
		@Override
		public String getValue(ITask task) {
			if (task instanceof AbstractRepositoryTask) {
				return ((AbstractRepositoryTask) task).getRepositoryKind();
			}
			return null;
		}
	}

	public static class RepositoryUrl extends AbstractCommitTemplateVariable {
		@Override
		public String getValue(ITask task) {
			if (task instanceof AbstractRepositoryTask) {
				return ((AbstractRepositoryTask) task).getRepositoryUrl();
			}

			return null;
		}
	}

	public static class TaskProduct extends AbstractCommitTemplateVariable {
		@Override
		public String getValue(ITask task) {
			if (task instanceof AbstractRepositoryTask) {
				return getTaskData(task).getProduct();
			}

			return null;
		}
	}

	public static class TaskAssignee extends AbstractCommitTemplateVariable {
		@Override
		public String getValue(ITask task) {
			if (task instanceof AbstractRepositoryTask) {
				return getTaskData(task).getAssignedTo();
			}

			return null;
		}
	}

	public static class TaskReporter extends AbstractCommitTemplateVariable {
		@Override
		public String getValue(ITask task) {
			if (task instanceof AbstractRepositoryTask) {
				return getTaskData(task).getReporter();
			}

			return null;
		}
	}

	public static class TaskResolution extends AbstractCommitTemplateVariable {
		@Override
		public String getValue(ITask task) {
			if (task instanceof AbstractRepositoryTask) {
				return getTaskData(task).getResolution();
			}

			return null;
		}
	}

	public static class TaskStatus extends AbstractCommitTemplateVariable {
		@Override
		public String getValue(ITask task) {
			if (task instanceof AbstractRepositoryTask && getTaskData(task) != null) {
				return getTaskData(task).getStatus().toUpperCase(Locale.ENGLISH);
			} else {
				// TODO: refactor completion labels
				if (task.isCompleted()) {
					return TaskPlanningEditor.LABEL_COMPLETE;
				} else {
					return TaskPlanningEditor.LABEL_INCOMPLETE;
				}
			}
		}
	}

	public static class TaskCc extends AbstractCommitTemplateVariable {
		@Override
		public String getValue(ITask task) {
			if (task instanceof AbstractRepositoryTask) {
				List<String> list = getTaskData(task).getCC();
				return implode(list, ", ");
			}

			return null;
		}
	}

	public static class TaskKeywords extends AbstractCommitTemplateVariable {
		@Override
		public String getValue(ITask task) {
			if (task instanceof AbstractRepositoryTask) {
				List<String> list = getTaskData(task).getKeywords();
				return implode(list, ", ");
			}

			return null;
		}
	}

	public static class TaskLastModified extends AbstractCommitTemplateVariable {
		@Override
		public String getValue(ITask task) {
			if (task instanceof AbstractRepositoryTask) {
				return getTaskData(task).getLastModified();
			}

			return null;
		}
	}

	public static class TaskSummary extends AbstractCommitTemplateVariable {
		@Override
		public String getValue(ITask task) {
			if (task instanceof AbstractRepositoryTask) {
				return getTaskData(task).getSummary();
			}

			return task.getSummary();
		}
	}

	public static class TaskDescription extends AbstractCommitTemplateVariable {
		@Override
		public String getValue(ITask task) {
			return task.getSummary();
		}
	}

	public static class TaskId extends AbstractCommitTemplateVariable {
		@Override
		public String getValue(ITask task) {
			if (task instanceof AbstractRepositoryTask) {
				return ((AbstractRepositoryTask) task).getTaskId();
			} else {
				return null;
			}
		}
	}

	public static class TaskKey extends AbstractCommitTemplateVariable {
		@Override
		public String getValue(ITask task) {
			if (task instanceof AbstractRepositoryTask) {
				String value = ((AbstractRepositoryTask) task).getTaskKey();
				if (value == null) {
					value = ((AbstractRepositoryTask) task).getTaskId();
				}
				return value;
			} else {
				return null;
			}
		}
	}

	public static class TaskNotes extends AbstractCommitTemplateVariable {
		@Override
		public String getValue(ITask task) {
			return task.getNotes();
		}
	}

	public static class TaskPriority extends AbstractCommitTemplateVariable {
		@Override
		public String getValue(ITask task) {
			return task.getPriority();
		}
	}

	public static class TaskType extends AbstractCommitTemplateVariable {
		@Override
		public String getValue(ITask task) {
			return task.getTaskKind();
		}
	}

	public static class TaskURL extends AbstractCommitTemplateVariable {
		@Override
		public String getValue(ITask task) {
			return task.getTaskUrl();
		}
	}
	
	public static RepositoryTaskData getTaskData(ITask task) {
		return TasksUiPlugin.getDefault().getTaskDataManager().getNewTaskData(task.getHandleIdentifier());
	}

	/**
	 * @author Eike Stepper
	 */
	protected static abstract class CommitTemplateDate extends AbstractCommitTemplateVariable {
		@Override
		public String getValue(ITask task) {
			java.util.Date date = getDate(task);
			return formatDate(date);
		}

		protected String formatDate(java.util.Date date) {
			return date.toString();
		}

		protected abstract java.util.Date getDate(ITask task);

		/**
		 * @author Eike Stepper
		 */
		public static class TaskCompletion extends CommitTemplateDate {
			@Override
			protected java.util.Date getDate(ITask task) {
				return task.getCompletionDate();
			}
		}

		/**
		 * @author Eike Stepper
		 */
		public static class TaskCreation extends CommitTemplateDate {
			@Override
			protected java.util.Date getDate(ITask task) {
				return task.getCreationDate();
			}
		}

		/**
		 * @author Eike Stepper
		 */
		public static class TaskReminder extends CommitTemplateDate {
			@Override
			protected java.util.Date getDate(ITask task) {
				return task.getScheduledForDate();
			}
		}
	}
}