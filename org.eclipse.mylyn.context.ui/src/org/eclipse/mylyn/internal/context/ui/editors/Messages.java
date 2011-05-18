/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.context.ui.editors;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.mylyn.internal.context.ui.editors.messages"; //$NON-NLS-1$

	static {
		// load message values from bundle file
		reloadMessages();
	}

	public static void reloadMessages() {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String ContextEditorFormPage_No_context_active;

	public static String ContextEditorFormPage_No_context_attachments_Error;

	public static String ContextEditorFormPage_Actions;

	public static String ContextEditorFormPage_Activate_task_to_edit_context;

	public static String ContextEditorFormPage_Activate_task_to_remove_invisible;

	public static String ContextEditorFormPage_Attach_context_;

	public static String ContextEditorFormPage_Collapse_All;

	public static String ContextEditorFormPage_Context_is_empty_Error;

	public static String ContextEditorFormPage_Copy_Context_to_;

	public static String ContextEditorFormPage_Elements;

	public static String ContextEditorFormPage_Expand_All;

	public static String ContextEditorFormPage_Remove_every_element_not_visible;

	public static String ContextEditorFormPage_Remove_Invisible;

	public static String ContextEditorFormPage_Remove_Invisible_;

	public static String ContextEditorFormPage_RemoveAll;

	public static String ContextEditorFormPage_Retrieve_Context_;

	public static String ContextEditorFormPage_Show_All_Elements;

	public static String ContextPageFactory_Context;

	public static String InvisibleContextElementsPart_Collecting_all_invisible;

	public static String InvisibleContextElementsPart_Computing_invisible_elements;

	public static String InvisibleContextElementsPart_Invisible_elements;

	public static String InvisibleContextElementsPart_Structure_handle;

	public static String InvisibleContextElementsPart_Structure_kind;

	public static String InvisibleContextElementsPart_Updating_invisible_element_list;
}
