/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylar.internal.team.ccvs;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylar.internal.team.AbstractCommitWorkflowProvider;
import org.eclipse.mylar.internal.team.ui.wizards.CommitContextWizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.ui.PlatformUI;

/**
 * @author Mik Kersten
 */
public class CvsCommitWorkflowProvider extends AbstractCommitWorkflowProvider {

	private static final String WIZARD_LABEL = "Commit Resources in Task Context";
	
	@Override
	public boolean hasOutgoingChanges(IResource[] resources) {
		try {
			CommitContextWizard wizard = new CommitContextWizard(resources, null);
			return wizard.hasOutgoingChanges();
		} catch (CVSException e) {
			return false;
		}
	}

	@Override
	public void commit(IResource[] resources) {
		try {
			CommitContextWizard wizard = new CommitContextWizard(resources, null);
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			if (shell != null && !shell.isDisposed() && wizard.hasOutgoingChanges()) {
				wizard.loadSize();
				WizardDialog dialog = new WizardDialog(shell, wizard);
				dialog.setMinimumPageSize(wizard.loadSize());
				dialog.create();
				dialog.setTitle(WIZARD_LABEL);
				dialog.setBlockOnOpen(true);
				if (dialog.open() == Dialog.CANCEL) {
					dialog.close();
				}
			}
		} catch (CVSException e) {
		}
	}
	
}
