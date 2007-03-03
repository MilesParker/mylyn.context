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

package org.eclipse.mylar.internal.java.ui.wizards;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylar.internal.tasks.ui.TaskListColorsAndFonts;
import org.eclipse.mylar.internal.tasks.ui.TaskUiUtil;
import org.eclipse.mylar.internal.tasks.ui.views.TaskListView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;

/**
 * @author Shawn Minto
 * @author Mik Kersten
 */
public class MylarPreferenceWizardPage extends WizardPage {

	private static final String DESCRIPTION = 
		"Configures Mylar preferences to the recommended defaults. To alter these\n"
      + "re-invoke this wizard via the File -> New menu.";

	private static final String AUTO_FOLDING = "Turn automatic Java editor folding on";

	private static final String AUTO_CLOSE = "Automatically manage open editors to match task context";

//	private static final String WORKING_SET = "Add the \"active task context\" working set";

	private static final String CONTENT_ASSIST = "Enable task-context ranked content assist, requires Eclipse restart.";

	private static final String CONTENT_ASSIST_WARNING = "Toggle via Preferences->Java->Editor->Content Assist->Advanced ";
	
	private static final String OPEN_TASK_LIST = "Open the " + TaskListView.LABEL_VIEW + " view";

	private Button contentAssistButton;

	private Button turnOnAutoFoldingButton;

	private boolean autoFolding = true;

//	private Button addMylarActiveWorkingSetButton;

	// TODO: remove
	private boolean createWorkingSet = false;

	private Button closeEditorsOnDeactivationButton;

	private boolean closeEditors = true;

	private Button openTaskListButton;

	private boolean openTaskList = true;

	protected MylarPreferenceWizardPage(String pageName) {
		super(pageName);
		setTitle(pageName);
		setDescription(DESCRIPTION);
	}

	public void createControl(Composite parent) {

		Composite containerComposite = new Composite(parent, SWT.NULL);
		containerComposite.setLayout(new GridLayout());

		Composite buttonComposite = new Composite(containerComposite, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		buttonComposite.setLayout(layout);

		contentAssistButton = new Button(buttonComposite, SWT.CHECK);
		GridData gd = new GridData();
		contentAssistButton.setLayoutData(gd);
		contentAssistButton.setSelection(true);

		Label label = new Label(buttonComposite, SWT.NONE);
		label.setText(CONTENT_ASSIST);
		label = new Label(buttonComposite, SWT.NONE);
		label = new Label(buttonComposite, SWT.NONE);
		label.setFont(JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT));
		label.setText(CONTENT_ASSIST_WARNING);
		
//		label = new Label(buttonComposite, SWT.NONE);
//		label = new Label(buttonComposite, SWT.NONE);
//		label.setText("NOTE: if Mylar is uninstalled you must Restore Defaults on above page ");
//		label.setForeground(TaskListColorsAndFonts.COLOR_LABEL_CAUTION);
		
		gd = new GridData();
		label.setLayoutData(gd);

		turnOnAutoFoldingButton = new Button(buttonComposite, SWT.CHECK);
		gd = new GridData();
		turnOnAutoFoldingButton.setLayoutData(gd);
		turnOnAutoFoldingButton.setSelection(true);
		turnOnAutoFoldingButton.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				autoFolding = turnOnAutoFoldingButton.getSelection();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// don't care about this event
			}
		});

		label = new Label(buttonComposite, SWT.NONE);
		label.setText(AUTO_FOLDING);
		gd = new GridData();
		label.setLayoutData(gd);
		label = new Label(buttonComposite, SWT.NONE);
		label = new Label(buttonComposite, SWT.NONE);
		label.setFont(JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT));
		label.setText("Toggle via toolbar button ");
		
		
		closeEditorsOnDeactivationButton = new Button(buttonComposite, SWT.CHECK);
		gd = new GridData();
		closeEditorsOnDeactivationButton.setLayoutData(gd);
		closeEditorsOnDeactivationButton.setSelection(true);
		closeEditorsOnDeactivationButton.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				closeEditors = closeEditorsOnDeactivationButton.getSelection();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// don't care about this event
			}
		});
		
		label = new Label(buttonComposite, SWT.NONE);
		label.setText(AUTO_CLOSE);
		gd = new GridData();
		label.setLayoutData(gd);

		label = new Label(buttonComposite, SWT.NONE);
		label = new Label(buttonComposite, SWT.NONE);
		label.setFont(JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT));
		label.setText("Toggle via Mylar preferences page ");
		
//		addMylarActiveWorkingSetButton = new Button(buttonComposite, SWT.CHECK);
//		gd = new GridData();
//		addMylarActiveWorkingSetButton.setSelection(true);
//		addMylarActiveWorkingSetButton.addSelectionListener(new SelectionListener() {
//
//			public void widgetSelected(SelectionEvent e) {
//				workingSet = addMylarActiveWorkingSetButton.getSelection();
//			}
//
//			public void widgetDefaultSelected(SelectionEvent e) {
//				// don't care about this event
//			}
//		});

//		label = new Label(buttonComposite, SWT.NONE);
//		label.setText(WORKING_SET);
//		gd = new GridData();
//		label.setLayoutData(gd);
//		setControl(buttonComposite);

//		label = new Label(buttonComposite, SWT.NONE);
//		label = new Label(buttonComposite, SWT.NONE);
//		label.setFont(JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT));
//		label.setText("Remove via Window->Working Sets ");
		
		openTaskListButton = new Button(buttonComposite, SWT.CHECK);
		gd = new GridData();
		openTaskListButton.setLayoutData(gd);
		openTaskListButton.setSelection(true);
		openTaskListButton.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				openTaskList = openTaskListButton.getSelection();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// don't care about this event
			}
		});

		label = new Label(buttonComposite, SWT.NONE);
		label.setText(OPEN_TASK_LIST);
		gd = new GridData();
		label.setLayoutData(gd);

		Label spacer = new Label(buttonComposite, SWT.NONE);
		spacer.setText(" ");
		spacer = new Label(buttonComposite, SWT.NONE);
		spacer.setText(" ");

		Hyperlink hyperlink = new Hyperlink(containerComposite, SWT.NULL);
		hyperlink.setUnderlined(true);
		hyperlink.setForeground(TaskListColorsAndFonts.COLOR_HYPERLINK);
		hyperlink.setText("If this is your first time using Mylar please watch the short Getting Started video");
		hyperlink.addHyperlinkListener(new IHyperlinkListener() {

			public void linkActivated(HyperlinkEvent e) {
				TaskUiUtil.openUrl("http://eclipse.org/mylar/start.php");
			}

			public void linkEntered(HyperlinkEvent e) {
				// ignore
			}

			public void linkExited(HyperlinkEvent e) {
				// ignore
			}
		});
		
//		Composite browserComposite = new Composite(containerComposite, SWT.NULL);
//		browserComposite.setLayout(new GridLayout());
//		try {
//			Browser browser = new Browser(browserComposite, SWT.NONE);
//			browser.setText(htmlDocs);
//			GridData browserLayout = new GridData(GridData.FILL_HORIZONTAL);
//			browserLayout.heightHint = 100;
//			browserLayout.widthHint = 600;
//			browser.setLayoutData(browserLayout);
//		} catch (Throwable t) {
//			// fail silently if there is no browser
//		}

		setControl(containerComposite);
	}

	public boolean isAutoFolding() {
		return autoFolding;
	}

	public boolean closeEditors() {
		return closeEditors;
	}

	public boolean isMylarContentAssistDefault() {
		return contentAssistButton.getSelection();
	}

	public boolean isCreateWorkingSet() {
		return createWorkingSet;
	}

	public boolean isOpenTaskList() {
		return openTaskList;
	}
}
