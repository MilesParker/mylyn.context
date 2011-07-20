/*******************************************************************************
 * Copyright (c) 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Tasktop Technologies - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.context.core;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Assert;

/**
 * @author Shawn Minto
 * @since 3.2
 */
public class ContextChangeEvent {

	/**
	 * @author Shawn Minto
	 * @since 3.2
	 */
	public enum ContextChangeKind {
		PRE_ACTIVATED, ACTIVATED, DEACTIVATED, CLEARED, INTEREST_CHANGED, LANDMARKS_ADDED, LANDMARKS_REMOVED, ELEMENTS_DELETED;
	}

	private final String contextHandle;

	private final IInteractionContext context;

	private final ContextChangeKind eventKind;

	private final List<IInteractionElement> elements;

	private final boolean isExplicitManipulation;

	/**
	 * @deprecated Use factory methods instead
	 */
	@Deprecated
	public ContextChangeEvent(ContextChangeKind eventKind, String contextHandle, IInteractionContext context,
			List<IInteractionElement> elements) {
		this(eventKind, contextHandle, context, elements, false);
	}

	/**
	 * @since 3.3
	 * @deprecated Use factory methods instead
	 */
	@Deprecated
	public ContextChangeEvent(ContextChangeKind eventKind, String contextHandle, IInteractionContext context,
			List<IInteractionElement> elements, boolean isExplicitManipulation) {
		Assert.isNotNull(eventKind);
		this.contextHandle = contextHandle;
		this.context = context;
		this.eventKind = eventKind;
		if (elements == null) {
			this.elements = Collections.emptyList();
		} else {
			this.elements = elements;
		}
		this.isExplicitManipulation = isExplicitManipulation;
	}

	/**
	 * The Type of context event that occurred
	 * 
	 * @since 3.2
	 */
	public ContextChangeKind getEventKind() {
		return eventKind;
	}

	/**
	 * The elements that were manipulated for the event (may be empty)
	 * 
	 * @since 3.2
	 */
	public List<IInteractionElement> getElements() {
		return elements;
	}

	/**
	 * The handle of the context that was changed (Can be null if a composite context with multiple
	 * IInteractionContext's is changed)
	 * 
	 * @since 3.2
	 */
	public String getContextHandle() {
		return contextHandle;
	}

	/**
	 * The context that was changed (Can be null e.g. context deleted)
	 * 
	 * @since 3.2
	 */
	public IInteractionContext getContext() {
		return context;
	}

	/**
	 * Utility for whether the manipulated context is the active one
	 * 
	 * @since 3.2
	 */
	public boolean isActiveContext() {
		IInteractionContext activeContext = ContextCore.getContextManager().getActiveContext();
		return activeContext != null && activeContext.getHandleIdentifier() != null
				&& activeContext.getHandleIdentifier().equals(contextHandle);
	}

	/**
	 * Whether the event was a result of a users explicit manipulation of the context (i.e. mark as landmark) This can
	 * only be true for {@link ContextChangeKind#LANDMARKS_ADDED},{@link ContextChangeKind#LANDMARKS_REMOVED} and
	 * {@link ContextChangeKind#ELEMENTS_DELETED}
	 * 
	 * @since 3.3
	 */
	public boolean isExplicitManipulation() {
		return isExplicitManipulation;
	}

	/**
	 * @since 3.7
	 */
	public static ContextChangeEvent createLandmarksAddedEvent(IInteractionElement element,
			boolean isExplicitManipulation) {
		//TODO can we assume that interaciton element context is always the right one?
		return new ContextChangeEvent(ContextChangeKind.LANDMARKS_ADDED, element.getContext().getHandleIdentifier(),
				element.getContext(), Collections.singletonList(element), isExplicitManipulation);
	}

	/**
	 * @since 3.7
	 */
	public static ContextChangeEvent createLandmarksAddedEvent(List<IInteractionElement> elements,
			boolean isExplicitManipulation) {
		//TODO can we assume that interaciton element context is always the right one?
		IInteractionContext context = elements.get(0).getContext();
		return new ContextChangeEvent(ContextChangeKind.LANDMARKS_ADDED, context.getHandleIdentifier(), context,
				elements, isExplicitManipulation);
	}

	/**
	 * @since 3.7
	 */
	public static ContextChangeEvent createLandmarkRemovedEvent(IInteractionElement element,
			boolean isExplicitManipulation) {
		//TODO can we assume that interaciton element context is always the right one?
		return new ContextChangeEvent(ContextChangeKind.LANDMARKS_REMOVED, element.getContext().getHandleIdentifier(),
				element.getContext(), Collections.singletonList(element), isExplicitManipulation);
	}

	/**
	 * @since 3.7
	 */
	public static ContextChangeEvent createLandmarksRemovedEvent(List<IInteractionElement> elements,
			boolean isExplicitManipulation) {
		//TODO can we assume that interaciton element context is always the right one?
		IInteractionContext context = elements.get(0).getContext();
		return new ContextChangeEvent(ContextChangeKind.LANDMARKS_REMOVED, context.getHandleIdentifier(), context,
				elements, isExplicitManipulation);
	}

	/**
	 * @since 3.7
	 */
	public static ContextChangeEvent createElementDeletedEvent(IInteractionElement element,
			boolean isExplicitManipulation) {
		return new ContextChangeEvent(ContextChangeKind.ELEMENTS_DELETED, element.getContext().getHandleIdentifier(),
				element.getContext(), Collections.singletonList(element), isExplicitManipulation);
	}

	/**
	 * @since 3.7
	 */
	public static ContextChangeEvent createElementsDeletedEvent(List<IInteractionElement> elements,
			boolean isExplicitManipulation) {
		//TODO can we assume that interaction element context is always the right one?
		//TODO we don't handle degenerate case where empty elements is passed
		IInteractionContext context = elements.get(0).getContext();
		return new ContextChangeEvent(ContextChangeKind.ELEMENTS_DELETED, context.getHandleIdentifier(), context,
				elements, isExplicitManipulation);
	}

	/**
	 * @since 3.7
	 */
	public static ContextChangeEvent createActivationEvent(IInteractionContext context) {
		return new ContextChangeEvent(ContextChangeKind.ACTIVATED, context.getHandleIdentifier(), context, null);
	}

	/**
	 * @since 3.7
	 */
	public static ContextChangeEvent createPreactivationEvent(IInteractionContext context) {
		return new ContextChangeEvent(ContextChangeKind.PRE_ACTIVATED, context.getHandleIdentifier(), context, null);
	}

	/**
	 * @since 3.7
	 */
	public static ContextChangeEvent createPreactivationEvent(String handleIdentifier) {
		return new ContextChangeEvent(ContextChangeKind.PRE_ACTIVATED, handleIdentifier, null, null);
	}

	/**
	 * @since 3.7
	 */
	public static ContextChangeEvent createDeactivationEvent(IInteractionContext context) {
		return new ContextChangeEvent(ContextChangeKind.DEACTIVATED, context.getHandleIdentifier(), context, null);
	}

	/**
	 * @since 3.7
	 */
	public static ContextChangeEvent createClearedEvent(IInteractionContext context, String handle) {
		return new ContextChangeEvent(ContextChangeKind.CLEARED, handle, context, null);
	}

	/**
	 * @since 3.7
	 */
	public static ContextChangeEvent createInterestChangeEvent(IInteractionContext context,
			List<IInteractionElement> elements) {
		return new ContextChangeEvent(ContextChangeKind.INTEREST_CHANGED, context.getHandleIdentifier(), context,
				elements);
	}
}