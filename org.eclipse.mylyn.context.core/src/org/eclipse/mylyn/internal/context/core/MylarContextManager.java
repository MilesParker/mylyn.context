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

package org.eclipse.mylar.internal.context.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.mylar.context.core.AbstractRelationProvider;
import org.eclipse.mylar.context.core.IMylarContext;
import org.eclipse.mylar.context.core.IMylarContextListener;
import org.eclipse.mylar.context.core.IMylarElement;
import org.eclipse.mylar.context.core.IMylarRelation;
import org.eclipse.mylar.context.core.AbstractContextStructureBridge;
import org.eclipse.mylar.context.core.InteractionEvent;
import org.eclipse.mylar.context.core.InterestComparator;
import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.mylar.context.core.MylarStatusHandler;

/**
 * This is the core class resposible for context management.
 * 
 * TODO: fix synchronization instead of using ArrayList copies
 * 
 * @author Mik Kersten
 */
public class MylarContextManager {

	// TODO: move constants

	public static final String CONTEXTS_DIRECTORY = "contexts";

	public static final String CONTEXT_FILENAME_ENCODING = "UTF-8";

	public static final String ACTIVITY_DELTA_DEACTIVATED = "deactivated";

	public static final String ACTIVITY_DELTA_ACTIVATED = "activated";

	public static final String ACTIVITY_ORIGIN_ID = "org.eclipse.mylar.core";

	public static final String ACTIVITY_HANDLE_ATTENTION = "attention";

	public static final String ACTIVITY_HANDLE_LIFECYCLE = "lifecycle";

	public static final String ACTIVITY_DELTA_STARTED = "started";

	public static final String ACTIVITY_DELTA_STOPPED = "stopped";

	public static final String ACTIVITY_STRUCTURE_KIND = "context";

	public static final String CONTEXT_HISTORY_FILE_NAME = "activity";

	public static final String OLD_CONTEXT_HISTORY_FILE_NAME = "context-history";

	public static final String SOURCE_ID_MODEL_PROPAGATION = "org.eclipse.mylar.core.model.interest.propagation";

	public static final String SOURCE_ID_DECAY = "org.eclipse.mylar.core.model.interest.decay";

	public static final String SOURCE_ID_DECAY_CORRECTION = "org.eclipse.mylar.core.model.interest.decay.correction";

	public static final String SOURCE_ID_MODEL_ERROR = "org.eclipse.mylar.core.model.interest.propagation";

	public static final String CONTAINMENT_PROPAGATION_ID = "org.eclipse.mylar.core.model.edges.containment";

	public static final String CONTEXT_FILE_EXTENSION = ".xml.zip";

	public static final String CONTEXT_FILE_EXTENSION_OLD = ".xml";

	private static final int MAX_PROPAGATION = 17; // TODO: parametrize this

	private int numInterestingErrors = 0;

	private List<String> errorElementHandles = new ArrayList<String>();

	private boolean contextCapturePaused = false;

	private CompositeContext currentContext = new CompositeContext();

	private MylarContext activityMetaContext = null;

	private List<IMylarContextListener> activityMetaContextListeners = new ArrayList<IMylarContextListener>();

	private List<IMylarContextListener> listeners = new ArrayList<IMylarContextListener>();

	private List<IMylarContextListener> waitingListeners = new ArrayList<IMylarContextListener>();

	private boolean suppressListenerNotification = false;

	private MylarContextExternalizer externalizer = new MylarContextExternalizer();

	private boolean activationHistorySuppressed = false;

	private static ScalingFactors scalingFactors = new ScalingFactors();

	public MylarContextManager() {

	}

	public MylarContext getActivityHistoryMetaContext() {
		if (activityMetaContext == null) {
			loadActivityMetaContext();
		}
		return activityMetaContext;
	}

	public void loadActivityMetaContext() {
		if (ContextCorePlugin.getDefault().getContextStore() != null) {
			File contextActivityFile = getFileForContext(CONTEXT_HISTORY_FILE_NAME);
			activityMetaContext = externalizer.readContextFromXML(CONTEXT_HISTORY_FILE_NAME, contextActivityFile);
			if (activityMetaContext == null) {
				resetActivityHistory();
			}
			for (IMylarContextListener listener : activityMetaContextListeners) {
				listener.contextActivated(activityMetaContext);
			}
		} else {
			resetActivityHistory();
			MylarStatusHandler.log("No context store installed, not restoring activity context.", this);
		}
	}

	public void handleActivityMetaContextEvent(InteractionEvent event) {
		IMylarElement element = getActivityHistoryMetaContext().parseEvent(event);
		for (IMylarContextListener listener : activityMetaContextListeners) {
			try {
				List<IMylarElement> changed = new ArrayList<IMylarElement>();
				changed.add(element);
				listener.interestChanged(changed);
			} catch (Throwable t) {
				MylarStatusHandler.fail(t, "context listener failed", false);
			}
		}
	}

	public void resetActivityHistory() {
		activityMetaContext = new MylarContext(CONTEXT_HISTORY_FILE_NAME, MylarContextManager.getScalingFactors());
		saveActivityHistoryContext();
	}

	public IMylarElement getActiveElement() {
		if (currentContext != null) {
			return currentContext.getActiveNode();
		} else {
			return null;
		}
	}

	public void addErrorPredictedInterest(String handle, String kind, boolean notify) {
		if (numInterestingErrors > scalingFactors.getMaxNumInterestingErrors()
				|| currentContext.getContextMap().isEmpty())
			return;
		InteractionEvent errorEvent = new InteractionEvent(InteractionEvent.Kind.PROPAGATION, kind, handle,
				SOURCE_ID_MODEL_ERROR, scalingFactors.getErrorInterest());
		handleInteractionEvent(errorEvent, true);
		errorElementHandles.add(handle);
		numInterestingErrors++;
	}

	/**
	 * TODO: worry about decay-related change if predicted interest dacays
	 */
	public void removeErrorPredictedInterest(String handle, String kind, boolean notify) {
		if (currentContext.getContextMap().isEmpty())
			return;
		if (handle == null)
			return;
		IMylarElement element = currentContext.get(handle);
		if (element != null && element.getInterest().isInteresting() && errorElementHandles.contains(handle)) {
			InteractionEvent errorEvent = new InteractionEvent(InteractionEvent.Kind.MANIPULATION, kind, handle,
					SOURCE_ID_MODEL_ERROR, -scalingFactors.getErrorInterest());
			handleInteractionEvent(errorEvent, true);
			numInterestingErrors--;
			errorElementHandles.remove(handle);
			// TODO: this results in double-notification
			if (notify)
				for (IMylarContextListener listener : new ArrayList<IMylarContextListener>(listeners)) {
					List<IMylarElement> changed = new ArrayList<IMylarElement>();
					changed.add(element);
					listener.interestChanged(changed);
				}
		}
	}

	public IMylarElement getElement(String elementHandle) {
		if (currentContext != null && elementHandle != null) {
			return currentContext.get(elementHandle);
		} else {
			return null;
		}
	}

	public IMylarElement handleInteractionEvent(InteractionEvent event) {
		return handleInteractionEvent(event, true);
	}

	public IMylarElement handleInteractionEvent(InteractionEvent event, boolean propagateToParents) {
		return handleInteractionEvent(event, propagateToParents, true);
	}

	public void handleInteractionEvents(List<InteractionEvent> events, boolean propagateToParents) {
		Set<IMylarElement> compositeDelta = new HashSet<IMylarElement>();
		for (InteractionEvent event : events) {
			compositeDelta.addAll(internalHandleInteractionEvent(event, propagateToParents));
		}
		notifyInterestDelta(new ArrayList<IMylarElement>(compositeDelta));
	}

	public IMylarElement handleInteractionEvent(InteractionEvent event, boolean propagateToParents,
			boolean notifyListeners) {
		List<IMylarElement> interestDelta = internalHandleInteractionEvent(event, propagateToParents);
		if (notifyListeners) {
			notifyInterestDelta(interestDelta);
		}
		return currentContext.get(event.getStructureHandle());
	}

	private List<IMylarElement> internalHandleInteractionEvent(InteractionEvent event, boolean propagateToParents) {
		if (contextCapturePaused || InteractionEvent.Kind.COMMAND.equals(event.getKind()) || !isContextActive()
				|| suppressListenerNotification) {
			return Collections.emptyList();
		}

		IMylarElement previous = currentContext.get(event.getStructureHandle());
		float previousInterest = 0;
		boolean previouslyPredicted = false;
		boolean previouslyPropagated = false;
		float decayOffset = 0;
		if (previous != null) {
			previousInterest = previous.getInterest().getValue();
			previouslyPredicted = previous.getInterest().isPredicted();
			previouslyPropagated = previous.getInterest().isPropagated();
		}
		if (event.getKind().isUserEvent()) {
			decayOffset = ensureIsInteresting(event.getStructureKind(), event.getStructureHandle(), previous,
					previousInterest);
		}
		IMylarElement element = currentContext.addEvent(event);
		List<IMylarElement> interestDelta = new ArrayList<IMylarElement>();
		if (propagateToParents && !event.getKind().equals(InteractionEvent.Kind.MANIPULATION)) {
			propegateInterestToParents(event.getKind(), element, previousInterest, decayOffset, 1, interestDelta);
		}
		if (event.getKind().isUserEvent())
			currentContext.setActiveElement(element);

		if (isInterestDelta(previousInterest, previouslyPredicted, previouslyPropagated, element)) {
			interestDelta.add(element);
		}

		checkForLandmarkDeltaAndNotify(previousInterest, element);
		return interestDelta;
	}

	private float ensureIsInteresting(String contentType, String handle, IMylarElement previous, float previousInterest) {
		float decayOffset = 0;
		if (previousInterest < 0) { // reset interest if not interesting
			decayOffset = (-1) * (previous.getInterest().getValue());
			currentContext.addEvent(new InteractionEvent(InteractionEvent.Kind.MANIPULATION, contentType, handle,
					SOURCE_ID_DECAY_CORRECTION, decayOffset));
		}
		return decayOffset;
	}

	private void notifyInterestDelta(List<IMylarElement> interestDelta) {
		if (!interestDelta.isEmpty()) {
			for (IMylarContextListener listener : new ArrayList<IMylarContextListener>(listeners)) {
				listener.interestChanged(interestDelta);
			}
		}
	}

	protected boolean isInterestDelta(float previousInterest, boolean previouslyPredicted,
			boolean previouslyPropagated, IMylarElement node) {
		float currentInterest = node.getInterest().getValue();
		if (previousInterest <= 0 && currentInterest > 0) {
			return true;
		} else if (previousInterest > 0 && currentInterest <= 0) {
			return true;
		} else if (currentInterest > 0 && previouslyPredicted && !node.getInterest().isPredicted()) {
			return true;
		} else if (currentInterest > 0 && previouslyPropagated && !node.getInterest().isPropagated()) {
			return true;
		} else {
			return false;
		}
	}

	protected void checkForLandmarkDeltaAndNotify(float previousInterest, IMylarElement node) {
		// TODO: don't call interestChanged if it's a landmark?
		AbstractContextStructureBridge bridge = ContextCorePlugin.getDefault().getStructureBridge(node.getContentType());
		if (bridge.canBeLandmark(node.getHandleIdentifier())) {
			if (previousInterest >= scalingFactors.getLandmark() && !node.getInterest().isLandmark()) {
				for (IMylarContextListener listener : new ArrayList<IMylarContextListener>(listeners))
					listener.landmarkRemoved(node);
			} else if (previousInterest < scalingFactors.getLandmark() && node.getInterest().isLandmark()) {
				for (IMylarContextListener listener : new ArrayList<IMylarContextListener>(listeners))
					listener.landmarkAdded(node);
			}
		}
	}

	private void propegateInterestToParents(InteractionEvent.Kind kind, IMylarElement node, float previousInterest,
			float decayOffset, int level, List<IMylarElement> interestDelta) {

		if (level > MAX_PROPAGATION || node == null || node.getInterest().getValue() <= 0) {
			return;
		}

		checkForLandmarkDeltaAndNotify(previousInterest, node);

		level++; // original is 1st level
		float propagatedIncrement = node.getInterest().getValue() - previousInterest + decayOffset;

		AbstractContextStructureBridge bridge = ContextCorePlugin.getDefault().getStructureBridge(node.getContentType());
		String parentHandle = bridge.getParentHandle(node.getHandleIdentifier());
		
		// check if should use child bridge
		for (String contentType : ContextCorePlugin.getDefault().getChildContentTypes(bridge.getContentType())) {
			AbstractContextStructureBridge childBridge = ContextCorePlugin.getDefault().getStructureBridge(contentType);
			Object resolved = childBridge.getObjectForHandle(parentHandle);
			if (resolved != null) {
				AbstractContextStructureBridge canonicalBridge = ContextCorePlugin.getDefault().getStructureBridge(resolved);
				// HACK: hard-coded resource content type
				if (!canonicalBridge.getContentType().equals(ContextCorePlugin.CONTENT_TYPE_ANY)) {
					// NOTE: resetting bridge
					bridge = canonicalBridge;
				}
			}
		} 
	
		if (parentHandle != null) {
			InteractionEvent propagationEvent = new InteractionEvent(InteractionEvent.Kind.PROPAGATION, bridge
					.getContentType(node.getHandleIdentifier()), parentHandle,
					SOURCE_ID_MODEL_PROPAGATION, CONTAINMENT_PROPAGATION_ID, propagatedIncrement);
//			InteractionEvent propagationEvent = new InteractionEvent(InteractionEvent.Kind.PROPAGATION, bridge
//					.getContentType(node.getHandleIdentifier()), bridge.getParentHandle(node.getHandleIdentifier()),
//					SOURCE_ID_MODEL_PROPAGATION, CONTAINMENT_PROPAGATION_ID, propagatedIncrement);
			IMylarElement previous = currentContext.get(propagationEvent.getStructureHandle());
			if (previous != null && previous.getInterest() != null) {
				previousInterest = previous.getInterest().getValue();
			}
			CompositeContextElement parentNode = (CompositeContextElement) currentContext.addEvent(propagationEvent);
			if (kind.isUserEvent() && parentNode.getInterest().getEncodedValue() < scalingFactors.getInteresting()) {
				float parentOffset = ((-1) * parentNode.getInterest().getEncodedValue()) + 1;
				currentContext.addEvent(new InteractionEvent(InteractionEvent.Kind.MANIPULATION, parentNode
						.getContentType(), parentNode.getHandleIdentifier(), SOURCE_ID_DECAY_CORRECTION, parentOffset));
			}
			if (isInterestDelta(previousInterest, previous.getInterest().isPredicted(), previous.getInterest()
					.isPropagated(), parentNode)) {
				interestDelta.add(0, parentNode);
			}
			propegateInterestToParents(kind, parentNode, previousInterest, decayOffset, level, interestDelta);// adapter.getResourceExtension(),
		}
	}

	public List<IMylarElement> findCompositesForNodes(List<MylarContextElement> nodes) {
		List<IMylarElement> composites = new ArrayList<IMylarElement>();
		for (MylarContextElement node : nodes) {
			composites.add(currentContext.get(node.getHandleIdentifier()));
		}
		return composites;
	}

	public void addListener(IMylarContextListener listener) {
		if (listener != null) {
			if (suppressListenerNotification && !waitingListeners.contains(listener)) {
				waitingListeners.add(listener);
			} else {
				if (!listeners.contains(listener))
					listeners.add(listener);
			}
		} else {
			MylarStatusHandler.log("attempted to add null lisetener", this);
		}
	}

	public void removeListener(IMylarContextListener listener) {
		listeners.remove(listener);
	}

	public void addActivityMetaContextListener(IMylarContextListener listener) {
		activityMetaContextListeners.add(listener);
	}

	public void removeActivityMetaContextListener(IMylarContextListener listener) {
		activityMetaContextListeners.remove(listener);
	}

	public void removeAllListeners() {
		listeners.clear();
	}

	public void notifyPostPresentationSettingsChange(IMylarContextListener.UpdateKind kind) {
		for (IMylarContextListener listener : listeners)
			listener.presentationSettingsChanged(kind);
	}

	public void notifyActivePresentationSettingsChange(IMylarContextListener.UpdateKind kind) {
		for (IMylarContextListener listener : listeners)
			listener.presentationSettingsChanging(kind);
	}

	/**
	 * Public for testing, activiate via handle
	 */
	public void activateContext(MylarContext context) {
		currentContext.getContextMap().put(context.getHandleIdentifier(), context);
		if (!activationHistorySuppressed) {
			handleActivityMetaContextEvent(new InteractionEvent(InteractionEvent.Kind.COMMAND, ACTIVITY_STRUCTURE_KIND,
					context.getHandleIdentifier(), ACTIVITY_ORIGIN_ID, null, ACTIVITY_DELTA_ACTIVATED, 1f));
		}
	}

	public List<MylarContext> getActiveContexts() {
		return new ArrayList<MylarContext>(currentContext.getContextMap().values());
	}

	public void activateContext(String handleIdentifier) {
		try {
			suppressListenerNotification = true;
			MylarContext context = currentContext.getContextMap().get(handleIdentifier);
			if (context == null)
				context = loadContext(handleIdentifier);
			if (context != null) {
				activateContext(context);
				for (IMylarContextListener listener : new ArrayList<IMylarContextListener>(listeners)) {
					try {
						listener.contextActivated(context);
					} catch (Exception e) {
						MylarStatusHandler.fail(e, "context listener failed", false);
					}
				}
				// refreshRelatedElements();
			} else {
				MylarStatusHandler.log("Could not load context", this);
			}
			suppressListenerNotification = false;
			listeners.addAll(waitingListeners);
			waitingListeners.clear();
		} catch (Throwable t) {
			MylarStatusHandler.log(t, "Could not activate context");
		}
	}

	/**
	 * Could load in the context and inspect it, but this is cheaper.
	 */
	public boolean hasContext(String path) {
		File contextFile = getFileForContext(path);
		return contextFile.exists() && contextFile.length() > 0;
	}

	public void deactivateAllContexts() {
		for (String handleIdentifier : currentContext.getContextMap().keySet()) {
			deactivateContext(handleIdentifier);
		}
	}

	public void deactivateContext(String handleIdentifier) {
		try {
			IMylarContext context = currentContext.getContextMap().get(handleIdentifier);
			if (context != null) {
				saveContext(handleIdentifier);
				currentContext.getContextMap().remove(handleIdentifier);

				setContextCapturePaused(true);
				for (IMylarContextListener listener : new ArrayList<IMylarContextListener>(listeners)) {
					try {
						listener.contextDeactivated(context);
					} catch (Exception e) {
						MylarStatusHandler.fail(e, "context listener failed", false);
					}
				}
				setContextCapturePaused(false);
			}
			if (!activationHistorySuppressed) {
				handleActivityMetaContextEvent(new InteractionEvent(InteractionEvent.Kind.COMMAND,
						ACTIVITY_STRUCTURE_KIND, handleIdentifier, ACTIVITY_ORIGIN_ID, null,
						ACTIVITY_DELTA_DEACTIVATED, 1f));
			}
			saveActivityHistoryContext();
		} catch (Throwable t) {
			MylarStatusHandler.log(t, "Could not deactivate context");
		}
	}

	public void deleteContext(String handleIdentifier) {
		IMylarContext context = currentContext.getContextMap().get(handleIdentifier);
		eraseContext(handleIdentifier, false);
		if (context != null) {
			// TODO: this notification is redundant with eraseContext's
			setContextCapturePaused(true);
			for (IMylarContextListener listener : new ArrayList<IMylarContextListener>(listeners)) {
				listener.contextDeactivated(context);
			}
			setContextCapturePaused(false);
		}
		try {
			File file = getFileForContext(handleIdentifier);
			if (file.exists()) {
				file.delete();
			}
		} catch (SecurityException e) {
			MylarStatusHandler.fail(e, "Could not delete context file", false);
		}
	}

	private void eraseContext(String id, boolean notify) {
		MylarContext context = currentContext.getContextMap().get(id);
		if (context == null)
			return;
		currentContext.getContextMap().remove(context);
		context.reset();
		if (notify) {
			for (IMylarContextListener listener : listeners)
				listener.presentationSettingsChanging(IMylarContextListener.UpdateKind.UPDATE);
		}
	}

	/**
	 * @return false if the map could not be read for any reason
	 */
	public MylarContext loadContext(String handleIdentifier) {
		MylarContext loadedContext = externalizer.readContextFromXML(handleIdentifier,
				getFileForContext(handleIdentifier));
		if (loadedContext == null) {
			return new MylarContext(handleIdentifier, MylarContextManager.getScalingFactors());
		} else {
			return loadedContext;
		}
	}

	public synchronized void saveContext(String handleIdentifier) {
		boolean wasPaused = contextCapturePaused;
		try {
			if (!wasPaused) {
				setContextCapturePaused(true);
			}
			MylarContext context = currentContext.getContextMap().get(handleIdentifier);
			if (context == null)
				return;
			context.collapse();
			externalizer.writeContextToXml(context, getFileForContext(handleIdentifier));
		} catch (Throwable t) {
			MylarStatusHandler.fail(t, "could not save context", false);
		} finally {
			if (!wasPaused) {
				setContextCapturePaused(false);
			}
		}
	}

	public void saveActivityHistoryContext() {
		if (ContextCorePlugin.getDefault().getContextStore() == null) {
			return;
		}
		boolean wasPaused = contextCapturePaused;
		try {
			if (!wasPaused) {
				setContextCapturePaused(true);
			}
			externalizer.writeContextToXml(getActivityHistoryMetaContext(),
					getFileForContext(CONTEXT_HISTORY_FILE_NAME));
		} catch (Throwable t) {
			MylarStatusHandler.fail(t, "could not save activity history", false);
		} finally {
			if (!wasPaused) {
				setContextCapturePaused(false);
			}
		}
	}

	public File getFileForContext(String handleIdentifier) {
		String encoded;
		try {
			encoded = URLEncoder.encode(handleIdentifier, CONTEXT_FILENAME_ENCODING);
			File dataDirectory = ContextCorePlugin.getDefault().getContextStore().getRootDirectory();
			File contextDirectory = new File(dataDirectory, MylarContextManager.CONTEXTS_DIRECTORY);
			if (!contextDirectory.exists()) {
				contextDirectory.mkdir();
			}
			File contextFile = new File(contextDirectory, encoded + CONTEXT_FILE_EXTENSION);
			return contextFile;
		} catch (UnsupportedEncodingException e) {
			MylarStatusHandler.fail(e, "Could not determine path for context", false);
		}
		return null;
	}

	public IMylarContext getActiveContext() {
		return currentContext;
	}

	/**
	 * @param kind
	 */
	public void resetLandmarkRelationshipsOfKind(String reltationKind) {
		for (IMylarElement landmark : currentContext.getLandmarks()) {
			for (IMylarRelation edge : landmark.getRelations()) {
				if (edge.getRelationshipHandle().equals(reltationKind)) {
					landmark.clearRelations();
				}
			}
		}
		for (IMylarContextListener listener : listeners)
			listener.relationsChanged(null);
	}

	/**
	 * Copy the listener list in case it is modified during the notificiation.
	 * 
	 * @param node
	 */
	public void notifyRelationshipsChanged(IMylarElement node) {
		if (suppressListenerNotification)
			return;
		for (IMylarContextListener listener : new ArrayList<IMylarContextListener>(listeners)) {
			listener.relationsChanged(node);
		}
	}

	public List<AbstractRelationProvider> getActiveRelationProviders() {
		List<AbstractRelationProvider> providers = new ArrayList<AbstractRelationProvider>();
		Map<String, AbstractContextStructureBridge> bridges = ContextCorePlugin.getDefault().getStructureBridges();
		for (Entry<String, AbstractContextStructureBridge> entry : bridges.entrySet()) {
			AbstractContextStructureBridge bridge = entry.getValue();// bridges.get(extension);
			if (bridge.getRelationshipProviders() != null) {
				providers.addAll(bridge.getRelationshipProviders());
			}
		}
		return providers;
	}

	public static ScalingFactors getScalingFactors() {
		return MylarContextManager.scalingFactors;
	}

	public boolean isContextActive() {
		return !contextCapturePaused && currentContext.getContextMap().values().size() > 0;
	}

	public List<IMylarElement> getActiveLandmarks() {
		List<IMylarElement> allLandmarks = currentContext.getLandmarks();
		List<IMylarElement> acceptedLandmarks = new ArrayList<IMylarElement>();
		for (IMylarElement node : allLandmarks) {
			AbstractContextStructureBridge bridge = ContextCorePlugin.getDefault().getStructureBridge(node.getContentType());

			if (bridge.canBeLandmark(node.getHandleIdentifier())) {
				acceptedLandmarks.add(node);
			}
		}
		return acceptedLandmarks;
	}

	/**
	 * Sorted in descending interest order.
	 */
	public List<IMylarElement> getInterestingDocuments(IMylarContext context) {
		Set<IMylarElement> set = new HashSet<IMylarElement>();
		List<IMylarElement> allIntersting = context.getInteresting();
		for (IMylarElement node : allIntersting) {
			if (ContextCorePlugin.getDefault().getStructureBridge(node.getContentType()).isDocument(
					node.getHandleIdentifier())) {
				set.add(node);
			}
		}
		List<IMylarElement> list = new ArrayList<IMylarElement>(set);
		Collections.sort(list, new InterestComparator<IMylarElement>());
		return list;
	}

	/**
	 * Get the interesting resources for the active context.
	 * 
	 * Sorted in descending interest order.
	 */
	public List<IMylarElement> getInterestingDocuments() {
		return getInterestingDocuments(currentContext);
	}

	public boolean isActivationHistorySuppressed() {
		return activationHistorySuppressed;
	}

	public void setActivationHistorySuppressed(boolean activationHistorySuppressed) {
		this.activationHistorySuppressed = activationHistorySuppressed;
	}

	/**
	 * @return true if interest was manipulated successfully
	 */
	public boolean manipulateInterestForElement(IMylarElement element, boolean increment, boolean forceLandmark,
			String sourceId) {
		if (element == null) {
			return false;
		}
		float originalValue = element.getInterest().getValue();
		float changeValue = 0;
		AbstractContextStructureBridge bridge = ContextCorePlugin.getDefault().getStructureBridge(element.getContentType());
		if (!increment) {
			if (element.getInterest().isLandmark() && bridge.canBeLandmark(element.getHandleIdentifier())) {
				// keep it interesting
				changeValue = (-1 * originalValue) + 1;
			} else {
				// make uninteresting
				if (originalValue >= 0) {
					changeValue = (-1 * originalValue) - 1;
				}

				// reduce interest of children
				for (String childHandle : bridge.getChildHandles(element.getHandleIdentifier())) {
					IMylarElement childElement = getElement(childHandle);
					if (childElement.getInterest().isInteresting() && !childElement.equals(element)) {
						manipulateInterestForElement(childElement, increment, forceLandmark, sourceId);
					}
				}
			}
		} else {
			if (!forceLandmark && (originalValue > MylarContextManager.getScalingFactors().getLandmark())) {
				changeValue = 0;
			} else {
				// make it a landmark by setting interest to 2 x landmark
				// interest
				if (element != null && bridge.canBeLandmark(element.getHandleIdentifier())) {
					changeValue = (2 * MylarContextManager.getScalingFactors().getLandmark()) - originalValue + 1;
				} else {
					return false;
				}
			}
		}
		if (changeValue != 0) {
			InteractionEvent interactionEvent = new InteractionEvent(InteractionEvent.Kind.MANIPULATION, element
					.getContentType(), element.getHandleIdentifier(), sourceId, changeValue);
			ContextCorePlugin.getContextManager().handleInteractionEvent(interactionEvent);
		}
		return true;
	}

	public void setActiveSearchEnabled(boolean enabled) {
		for (AbstractRelationProvider provider : getActiveRelationProviders()) {
			provider.setEnabled(enabled);
		}
	}

	/**
	 * Retruns the highest interet context.
	 * 
	 * TODO: refactor this into better multiple context support
	 */
	public String getDominantContextHandleForElement(IMylarElement node) {
		IMylarElement dominantNode = null;
		if (node instanceof CompositeContextElement) {
			CompositeContextElement compositeNode = (CompositeContextElement) node;
			if (compositeNode.getNodes().isEmpty())
				return null;
			dominantNode = (IMylarElement) compositeNode.getNodes().toArray()[0];

			for (IMylarElement concreteNode : compositeNode.getNodes()) {
				if (dominantNode != null
						&& dominantNode.getInterest().getValue() < concreteNode.getInterest().getValue()) {
					dominantNode = concreteNode;
				}
			}
		} else if (node instanceof MylarContextElement) {
			dominantNode = node;
		}
		if (node != null) {
			return ((MylarContextElement) dominantNode).getContext().getHandleIdentifier();
		} else {
			return null;
		}
	}

	public void updateHandle(IMylarElement element, String newHandle) {
		if (element == null)
			return;
		getActiveContext().updateElementHandle(element, newHandle);
		for (IMylarContextListener listener : new ArrayList<IMylarContextListener>(listeners)) {
			List<IMylarElement> changed = new ArrayList<IMylarElement>();
			changed.add(element);
			listener.interestChanged(changed);
		}
		if (element.getInterest().isLandmark()) {
			for (IMylarContextListener listener : new ArrayList<IMylarContextListener>(listeners)) {
				listener.landmarkAdded(element);
			}
		}
	}

	public void delete(IMylarElement element) {
		if (element == null)
			return;
		getActiveContext().delete(element);
		for (IMylarContextListener listener : new ArrayList<IMylarContextListener>(listeners)) {
			listener.elementDeleted(element);
		}
	}

	/**
	 * NOTE: If pausing ensure to restore to original state.
	 */
	public void setContextCapturePaused(boolean paused) {
		this.contextCapturePaused = paused;
	}

	public boolean isContextCapturePaused() {
		return contextCapturePaused;
	}

	/**
	 * For testing.
	 */
	public List<IMylarContextListener> getListeners() {
		return Collections.unmodifiableList(listeners);
	}

	public boolean isValidContextFile(File file) {
		if (file.exists() && file.getName().endsWith(MylarContextManager.CONTEXT_FILE_EXTENSION)) {
			MylarContext context = externalizer.readContextFromXML("temp", file);
			return context != null;
		}
		return false;
	}

	public void transferContextAndActivate(String handleIdentifier, File file) {
		File contextFile = getFileForContext(handleIdentifier);
		contextFile.delete();
		try {
			copy(file, contextFile);
		} catch (IOException e) {
			MylarStatusHandler.fail(e, "Cold not transfer context", false);
		}
	}

	private void copy(File src, File dest) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dest);
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}
}
