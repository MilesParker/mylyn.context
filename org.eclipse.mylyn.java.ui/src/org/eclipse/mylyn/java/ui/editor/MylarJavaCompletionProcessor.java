/*******************************************************************************
 * Copyright (c) 2004 - 2005 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/
/*
 * Created on Mar 14, 2005
  */
package org.eclipse.mylar.java.ui.editor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProcessor;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.MemberProposalInfo;
import org.eclipse.jdt.internal.ui.text.java.ProposalInfo;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.mylar.core.IMylarElement;
import org.eclipse.mylar.core.MylarPlugin;
import org.eclipse.mylar.core.internal.MylarContextManager;
import org.eclipse.mylar.core.internal.dt.MylarWebRef;
import org.eclipse.mylar.core.util.MylarStatusHandler;
import org.eclipse.mylar.ui.MylarImages;
import org.eclipse.ui.IEditorPart;

/**
 * @author Mik Kersten
 * 
 * HACK: uses reflection to get around accessibility restriction.
 */
public class MylarJavaCompletionProcessor extends JavaCompletionProcessor {

    private static final String LABEL_SEPARATOR = " -----------------------------------";

    private Method resolveMemberMethod;
    
    public MylarJavaCompletionProcessor(IEditorPart editor) {
        super(editor); 
        try {
			resolveMemberMethod = MemberProposalInfo.class.getDeclaredMethod("resolveMember", new Class[] { } );
	        resolveMemberMethod.setAccessible(true);
        } catch (SecurityException e) {
        	MylarStatusHandler.fail(e, "could not install content assist, reflection denied", false);
		} catch (NoSuchMethodException e) {
			MylarStatusHandler.fail(e, "could not install content assist, wrong Eclipse version", false);
		}
    }
    
    @MylarWebRef(name="Reflection documentation", url="http://www.onjava.com/pub/a/onjava/2003/11/12/reflection.html?page=last")
    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {    
        ICompletionProposal[] proposals = super.computeCompletionProposals(viewer, offset);
        if (resolveMemberMethod == null || !MylarPlugin.getContextManager().hasActiveContext()) return proposals;
        try {            
        	TreeMap<Float, ICompletionProposal> interesting = new TreeMap<Float, ICompletionProposal>();
            List<ICompletionProposal> rest = new ArrayList<ICompletionProposal>();
            int unresolvedProposals = 0;
            for (Object proposalObject : proposals) {
            	ICompletionProposal proposal = (ICompletionProposal)proposalObject;
                ProposalInfo info = null;
                if (proposal instanceof JavaCompletionProposal) {
                    info = ((JavaCompletionProposal)proposal).getProposalInfo();
                } else if (proposal instanceof LazyJavaCompletionProposal) {
                    info = ((LazyJavaCompletionProposal)proposal).getProposalInfo();
                } 
                boolean added = false;
                try {
                    if (info != null) {
                        IMember member = null; // HACK: using reflection to get member
                        if (info instanceof MemberProposalInfo) member = (IMember)resolveMemberMethod.invoke(info, new Object[] { });
                        if (member == null || MylarPlugin.getContextManager().getActiveContext() == null) {
                        	rest.add(proposal);
                        } else {
                        	IMylarElement node = MylarPlugin.getContextManager().getElement(member.getHandleIdentifier()); 
                            if (node != null) {
                            	float interest = node.getInterest().getValue();
	                            if (interest > MylarContextManager.getScalingFactors().getInteresting()) {
	                                interesting.put(-interest, proposal);  // negative to invert sorting order
	                            } else {
	                            	rest.add(proposal);
	                            }
                            } else {
                            	rest.add(proposal);
                            }
                        } 
                        added = true;
                    }
                } catch (Exception e) {
                	MylarStatusHandler.log(e, "proposals problem");
                } 
                if (!added) {
                	if (!isUninteresting(proposal) && proposal instanceof JavaCompletionProposal) {
                		unresolvedProposals++;
                		interesting.put((float)unresolvedProposals - 100000, proposal); // HACK: should be parametrized
                	} else {
                		rest.add(proposal);
                	}
                }
            }
            if (interesting.keySet().size() == 0) {
                return proposals;
            } else {
                ICompletionProposal[] sorted = new ICompletionProposal[interesting.keySet().size() + rest.size() + 1];
                int i = 0;
                for (Entry<Float, ICompletionProposal> entry : interesting.entrySet()) {
                    sorted[i] = entry.getValue();
                    i++; 
                } 
                if (interesting.keySet().size() > 0) {
                    int replacementOffset = -1;
                    if (sorted[i-1] instanceof JavaCompletionProposal) {
                        replacementOffset = ((JavaCompletionProposal)sorted[i-1]).getReplacementOffset();
                    } else if (sorted[i-1] instanceof LazyJavaCompletionProposal) {
                        replacementOffset = ((LazyJavaCompletionProposal)sorted[i-1]).getReplacementOffset();
                    } else {
                    	MylarStatusHandler.log("Could not create proposal separator for class: " + sorted[i-1].getClass(), this);
                    }
                    sorted[i] = new JavaCompletionProposal(
                    		"", replacementOffset, 0, 
                    		MylarImages.getImage(MylarImages.CONTENT_ASSIST_SEPARATOR), 
                    		LABEL_SEPARATOR, 0);
                    i++;
                } 
                for (ICompletionProposal proposal : rest) {
                    sorted[i] = proposal;
                    i++;
                }
                return sorted;
            }
        } catch (Throwable t) {
        	MylarStatusHandler.log(t, "completion proposal filter and sort failed");
        }
        return null;
    }

	private boolean isUninteresting(ICompletionProposal proposal) {
		return "this".equals(proposal.getDisplayString());
	} 
}

