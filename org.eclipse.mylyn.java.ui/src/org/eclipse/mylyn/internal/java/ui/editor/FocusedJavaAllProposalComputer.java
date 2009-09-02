/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tasktop Technologies - changes for bug 219692
 *******************************************************************************/

package org.eclipse.mylyn.internal.java.ui.editor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.JavaHeuristicScanner;
import org.eclipse.jdt.internal.ui.text.Symbols;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.java.ui.JavaUiBridgePlugin;
import org.eclipse.mylyn.internal.java.ui.JavaUiUtil;

/**
 * Based on org.eclipse.jdt.internal.ui.text.java.JavaAllCompletionProposalComputer.
 * 
 * @author Mik Kersten
 * @author Steffen Pingel
 */
// TODO e3.5 extend org.eclipse.jdt.internal.ui.text.java.JavaAllCompletionProposalComputer
public class FocusedJavaAllProposalComputer extends JavaCompletionProposalComputer {

	/**
	 * @see CompletionProposal#METHOD_REF_WITH_CASTED_RECEIVER
	 */
	// TODO e3.4 replace by CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER
	public static final int METHOD_REF_WITH_CASTED_RECEIVER = 24;

	/**
	 * @see CompletionProposal#METHOD_REF_WITH_CASTED_RECEIVER
	 */
	// TODO e3.4 replace by CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER
	public static final int FIELD_REF_WITH_CASTED_RECEIVER = 25;

	/**
	 * @see CompletionProposal#CONSTRUCTOR_INVOCATION
	 */
	// TODO e3.5 replace by CompletionProposal.CONSTRUCTOR_INVOCATION
	public static final int CONSTRUCTOR_INVOCATION = 26;

	/**
	 * @see CompletionProposal#ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION
	 */
	// TODO e3.5 replace by CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION
	public static final int ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION = 27;

	private static Field fieldCoreContext;

	// TODO e3.4 remove invoke CompletionContent.isExtended() instead
	private static Method methodIsExtended;
	
	static {
		try {
			fieldCoreContext = JavaContentAssistInvocationContext.class.getDeclaredField("fCoreContext"); //$NON-NLS-1$
			fieldCoreContext.setAccessible(true);

			methodIsExtended = CompletionContext.class.getDeclaredMethod("isExtended"); //$NON-NLS-1$
		} catch (Exception e) {
			fieldCoreContext = null;
			methodIsExtended = null;
		}
	}

	private boolean coreContextExceptionLogged;

	public FocusedJavaAllProposalComputer() {
		FocusedJavaProposalProcessor.getDefault().addMonitoredComputer(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
		if (shouldReturnResults()) {
			// TODO e3.6 remove code below (work-around for bug 271252)
			if (fieldCoreContext != null && methodIsExtended != null) {
				try {
					CompletionContext coreContext = (CompletionContext) fieldCoreContext.get(context);
					if (coreContext != null) {
						Object isExtended = methodIsExtended.invoke(coreContext);
						if (isExtended != null && !(Boolean) isExtended) {
							// trigger re-computation of coreContext to ensure that coreContext is extended
							fieldCoreContext.set(context, null);
						}
					}
				} catch (Exception e) {
					if (!coreContextExceptionLogged) {
						coreContextExceptionLogged = true;
						StatusHandler.log(new Status(
								IStatus.WARNING,
								JavaUiBridgePlugin.ID_PLUGIN,
								"An error was encountered while computing Task-Focused content assist. To recover use Restore Defaults in Preferences > Java > Editor > Content Assist > Advanced.", e)); //$NON-NLS-1$
					}
				}
			}
			List proposals = super.computeCompletionProposals(context, monitor);
			return FocusedJavaProposalProcessor.getDefault().projectInterestModel(this, proposals);
		} else {
			return Collections.emptyList();
		}
	}

	private boolean shouldReturnResults() {
		if (JavaUiUtil.isDefaultAssistActive(JavaUiUtil.ASSIST_JDT_ALL)) {
			// do not return duplicates if the default JDT processor is already enabled on Eclipse 3.5
			return false;
		}
		Set<String> disabledIds = JavaUiUtil.getDisabledIds(JavaPlugin.getDefault().getPreferenceStore());
		if (!disabledIds.contains(JavaUiUtil.ASSIST_JDT_NOTYPE) && !disabledIds.contains(JavaUiUtil.ASSIST_JDT_TYPE)) {
			// do not return duplicates if the default JDT processors are already enabled on on Eclipse 3.3 and 3.4
			return false;
		}
		return true;
	}

	/**
	 * @see org.eclipse.jdt.internal.ui.text.java.JavaAllCompletionProposalComputer#createCollector(JavaContentAssistInvocationContext)
	 */
	@Override
	protected CompletionProposalCollector createCollector(JavaContentAssistInvocationContext context) {
		CompletionProposalCollector collector = super.createCollector(context);
		collector.setIgnored(CompletionProposal.ANNOTATION_ATTRIBUTE_REF, false);
		collector.setIgnored(CompletionProposal.ANONYMOUS_CLASS_DECLARATION, false);
		try {
			collector.setIgnored(ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION, false);
		} catch (IllegalArgumentException e) {
			// ignore
		}
		collector.setIgnored(CompletionProposal.FIELD_REF, false);
		try {
			collector.setIgnored(FIELD_REF_WITH_CASTED_RECEIVER, false);
		} catch (IllegalArgumentException e) {
			// ignore
		}
		collector.setIgnored(CompletionProposal.KEYWORD, false);
		collector.setIgnored(CompletionProposal.LABEL_REF, false);
		collector.setIgnored(CompletionProposal.LOCAL_VARIABLE_REF, false);
		collector.setIgnored(CompletionProposal.METHOD_DECLARATION, false);
		collector.setIgnored(CompletionProposal.METHOD_NAME_REFERENCE, false);
		collector.setIgnored(CompletionProposal.METHOD_REF, false);
		try {
			collector.setIgnored(CONSTRUCTOR_INVOCATION, false);
		} catch (IllegalArgumentException e) {
			// ignore
		}
		try {
			collector.setIgnored(METHOD_REF_WITH_CASTED_RECEIVER, false);
		} catch (IllegalArgumentException e) {
			// ignore
		}
		collector.setIgnored(CompletionProposal.PACKAGE_REF, false);
		collector.setIgnored(CompletionProposal.POTENTIAL_METHOD_DECLARATION, false);
		collector.setIgnored(CompletionProposal.VARIABLE_DECLARATION, false);
		collector.setIgnored(CompletionProposal.TYPE_REF, false);
		return collector;
	}

	/**
	 * @see org.eclipse.jdt.internal.ui.text.java.JavaAllCompletionProposalComputer#guessContextInformationPosition(ContentAssistInvocationContext)
	 */
	@Override
	protected int guessContextInformationPosition(ContentAssistInvocationContext context) {
		int invocationOffset = context.getInvocationOffset();
		int typeContext = super.guessContextInformationPosition(context);
		int methodContext = guessMethodContextInformationPosition2(context);
		if (typeContext != invocationOffset && typeContext > methodContext) {
			return typeContext;
		} else if (methodContext != invocationOffset) {
			return methodContext;
		} else {
			return invocationOffset;
		}
	}

	// renamed, since guessMethodContextInformationPosition(ContentAssistInvocationContext) is final
	protected final int guessMethodContextInformationPosition2(ContentAssistInvocationContext context) {
		final int contextPosition = context.getInvocationOffset();

		IDocument document = context.getDocument();
		JavaHeuristicScanner scanner = new JavaHeuristicScanner(document);
		int bound = Math.max(-1, contextPosition - 200);

		// try the innermost scope of parentheses that looks like a method call
		int pos = contextPosition - 1;
		do {
			int paren = scanner.findOpeningPeer(pos, bound, '(', ')');
			if (paren == JavaHeuristicScanner.NOT_FOUND) {
				break;
			}
			int token = scanner.previousToken(paren - 1, bound);
			// next token must be a method name (identifier) or the closing angle of a
			// constructor call of a parameterized type.
			if (token == Symbols.TokenIDENT || token == Symbols.TokenGREATERTHAN) {
				return paren + 1;
			}
			pos = paren - 1;
		} while (true);

		return contextPosition;
	}

}
