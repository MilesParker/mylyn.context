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

package org.eclipse.mylar.internal.java.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.mylar.context.core.IMylarElement;
import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.mylar.core.MylarStatusHandler;
import org.eclipse.mylar.internal.context.core.MylarContextManager;
import org.eclipse.mylar.internal.context.ui.ContextUiImages;

/**
 * TODO: parametrize relevance levels (requires JDT changes, bug 119063)
 * 
 * @author Mik Kersten
 */
public class MylarJavaProposalProcessor {

	static final int THRESHOLD_INTEREST = 10000;

	private static final int THRESHOLD_IMPLICIT_INTEREST = THRESHOLD_INTEREST * 2;

	private static final int RELEVANCE_IMPLICIT_INTEREST = 300;

	private static final String IDENTIFIER_THIS = "this";

	public static final String LABEL_SEPARATOR = " -------------------------------------------- ";

	public static final MylarProposalSeparator PROPOSAL_SEPARATOR = new MylarProposalSeparator();

	private List<IJavaCompletionProposalComputer> monitoredProposalComputers = new ArrayList<IJavaCompletionProposalComputer>();

	private List<IJavaCompletionProposalComputer> alreadyComputedProposals = new ArrayList<IJavaCompletionProposalComputer>();

	private List<IJavaCompletionProposalComputer> alreadyContainSeparator = new ArrayList<IJavaCompletionProposalComputer>();

	private List<IJavaCompletionProposalComputer> containsSingleInterestingProposal = new ArrayList<IJavaCompletionProposalComputer>();

	private static MylarJavaProposalProcessor INSTANCE = new MylarJavaProposalProcessor();

	private MylarJavaProposalProcessor() {
	}

	public static MylarJavaProposalProcessor getDefault() {
		return INSTANCE;
	}

	public void addMonitoredComputer(IJavaCompletionProposalComputer proposalComputer) {
		monitoredProposalComputers.add(proposalComputer);
	}

	@SuppressWarnings("unchecked")
	public List projectInterestModel(IJavaCompletionProposalComputer proposalComputer, List proposals) {
		try {
			if (!ContextCorePlugin.getContextManager().isContextActive()) {
				return proposals;
			} else {
				boolean hasInterestingProposals = false;
				for (Object object : proposals) {
					if (object instanceof AbstractJavaCompletionProposal) {
						boolean foundInteresting = boostRelevanceWithInterest((AbstractJavaCompletionProposal) object);
						if (!hasInterestingProposals && foundInteresting) {
							hasInterestingProposals = true;
						}
					}
				}

				// NOTE: this annoying state needs to be maintainted to ensure
				// the
				// separator is added only once, and not added for single
				// proposals
				if (containsSingleInterestingProposal.size() > 0 && proposals.size() > 0) {
					proposals.add(MylarJavaProposalProcessor.PROPOSAL_SEPARATOR);
				} else if (hasInterestingProposals && alreadyContainSeparator.isEmpty()) {
					if (proposals.size() == 1) {
						containsSingleInterestingProposal.add(proposalComputer);
					} else {
						proposals.add(MylarJavaProposalProcessor.PROPOSAL_SEPARATOR);
						alreadyContainSeparator.add(proposalComputer);
					}
				}

				alreadyComputedProposals.add(proposalComputer);
				if (alreadyComputedProposals.size() == monitoredProposalComputers.size()) {
					alreadyComputedProposals.clear();
					alreadyContainSeparator.clear();
					containsSingleInterestingProposal.clear();
				}

				return proposals;
			}
		} catch (Throwable t) {
			MylarStatusHandler.fail(t, "Failed to project interest onto propsals", false);
			return proposals;
		}
	}

	private boolean boostRelevanceWithInterest(AbstractJavaCompletionProposal proposal) {
		boolean hasInteresting = false;
		IJavaElement javaElement = proposal.getJavaElement();
		if (javaElement != null) {
			IMylarElement mylarElement = ContextCorePlugin.getContextManager().getElement(
					javaElement.getHandleIdentifier());
			float interest = mylarElement.getInterest().getValue();
			if (interest > MylarContextManager.getScalingFactors().getInteresting()) {
				// TODO: losing precision here, only going to one decimal place
				proposal.setRelevance(THRESHOLD_INTEREST + (int) (interest * 10));
				hasInteresting = true;
			}
		} else if (isImplicitlyInteresting(proposal)) {
			proposal.setRelevance(THRESHOLD_IMPLICIT_INTEREST + proposal.getRelevance());
			hasInteresting = true;
		}
		return hasInteresting;
	}

	public boolean isImplicitlyInteresting(AbstractJavaCompletionProposal proposal) {
		return proposal.getRelevance() > RELEVANCE_IMPLICIT_INTEREST
				&& !IDENTIFIER_THIS.equals(proposal.getDisplayString());
	}

	static class MylarProposalSeparator extends JavaCompletionProposal {
		public MylarProposalSeparator() {
			super("", 0, 0, ContextUiImages.getImage(ContextUiImages.CONTENT_ASSIST_SEPARATOR), LABEL_SEPARATOR,
					MylarJavaProposalProcessor.THRESHOLD_INTEREST);
		}
	}
}
