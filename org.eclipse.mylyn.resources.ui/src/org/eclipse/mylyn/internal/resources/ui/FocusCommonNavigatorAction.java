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

package org.eclipse.mylar.internal.resources.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.mylar.context.ui.InterestFilter;
import org.eclipse.mylar.internal.context.ui.actions.AbstractAutoFocusViewAction;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.eclipse.ui.internal.navigator.extensions.LinkHelperService;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.ILinkHelper;

/**
 * @author Mik Kersten
 */
public abstract class FocusCommonNavigatorAction extends AbstractAutoFocusViewAction {

	private LinkHelperService linkService;
	
	private CommonNavigator commonNavigator;

	public FocusCommonNavigatorAction(InterestFilter interestFilter, boolean manageViewer, boolean manageFilters, boolean manageLinking) {
		super(interestFilter, manageViewer, manageFilters, manageLinking);
	}

	@Override
	protected ISelection resolveSelection(IEditorPart editor, ITextSelection changedSelection, StructuredViewer viewer) throws CoreException {
		if (commonNavigator == null) {
			commonNavigator = (CommonNavigator) super.getPartForAction();
		}
		if (linkService == null) {
			linkService = new LinkHelperService((NavigatorContentService) commonNavigator.getCommonViewer()
					.getNavigatorContentService());
		}
	
		IEditorInput input = editor.getEditorInput();
		ILinkHelper[] helpers = linkService.getLinkHelpersFor(editor.getEditorInput());
	
		IStructuredSelection selection = StructuredSelection.EMPTY;
		IStructuredSelection newSelection = StructuredSelection.EMPTY;
	
		for (int i = 0; i < helpers.length; i++) {
			selection = helpers[i].findSelection(input);
			if (selection != null && !selection.isEmpty()) {
				newSelection = mergeSelection(newSelection, selection);
			}
		}
		if (!newSelection.isEmpty()) {
			return newSelection;
		}
		return null;
	}

	@Override
	protected void select(StructuredViewer viewer, ISelection toSelect) {
		if (commonNavigator == null) {
			commonNavigator = (CommonNavigator) super.getPartForAction();
		}
		if (commonNavigator != null) {
			commonNavigator.selectReveal(toSelect);
		}
	}

	@Override
	protected void setDefaultLinkingEnabled(boolean on) {
		IViewPart part = super.getPartForAction();
		if (part instanceof CommonNavigator) {
			((CommonNavigator) part).setLinkingEnabled(on);
		}
	}

	@Override
	protected boolean isDefaultLinkingEnabled() {
		IViewPart part = super.getPartForAction();
		if (part instanceof CommonNavigator) {
			return ((CommonNavigator) part).isLinkingEnabled();
		}
		return false;
	}

	/**
	 * Copied from
	 * 
	 * @{link LinkEditorAction}
	 */
	@SuppressWarnings("unchecked")
	private IStructuredSelection mergeSelection(IStructuredSelection aBase, IStructuredSelection aSelectionToAppend) {
		if (aBase == null || aBase.isEmpty()) {
			return (aSelectionToAppend != null) ? aSelectionToAppend : StructuredSelection.EMPTY;
		} else if (aSelectionToAppend == null || aSelectionToAppend.isEmpty()) {
			return aBase;
		} else {
			List newItems = new ArrayList(aBase.toList());
			newItems.addAll(aSelectionToAppend.toList());
			return new StructuredSelection(newItems);
		}
	}

}