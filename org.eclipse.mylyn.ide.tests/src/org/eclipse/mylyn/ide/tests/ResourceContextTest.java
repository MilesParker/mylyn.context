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

package org.eclipse.mylar.ide.tests;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylar.internal.ide.MylarIdePlugin;
import org.eclipse.mylar.provisional.core.IMylarElement;
import org.eclipse.mylar.provisional.core.MylarPlugin;

/**
 * @author Mik Kersten
 */
public class ResourceContextTest extends AbstractResourceContextTest {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		MylarIdePlugin.getDefault().setResourceMonitoringEnabled(true);
		MylarIdePlugin.getDefault().getInterestUpdater().setSyncExec(true);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		MylarIdePlugin.getDefault().getInterestUpdater().setSyncExec(false);
	}

	public void testResourceSelect() throws CoreException {
		MylarPlugin.getContextManager().setContextCapturePaused(true);
		IFile file = project.getProject().getFile("file");
		file.create(null, true, null);
		assertTrue(file.exists());

		IMylarElement element = MylarPlugin.getContextManager().getElement(structureBridge.getHandleIdentifier(file));
		assertFalse(element.getInterest().isInteresting());
		MylarPlugin.getContextManager().setContextCapturePaused(false);

		monitor.selectionChanged(navigator, new StructuredSelection(file));
		element = MylarPlugin.getContextManager().getElement(structureBridge.getHandleIdentifier(file));
		assertTrue(element.getInterest().isInteresting());
	}

	public void testFileNotAddedIfExcluded() throws CoreException {
		Set<String> previousExcludions = MylarIdePlugin.getDefault().getExcludedResourcePatterns();
		Set<String> exclude = new HashSet<String>();
		exclude.add("boring");
		MylarIdePlugin.getDefault().setExcludedResourcePatterns(exclude);
		
		IFile file = project.getProject().getFile("boring");
		file.create(null, true, null);
		assertTrue(file.exists());

		IMylarElement element = MylarPlugin.getContextManager().getElement(structureBridge.getHandleIdentifier(file));
		assertFalse(element.getInterest().isInteresting());
		MylarIdePlugin.getDefault().setExcludedResourcePatterns(previousExcludions);
	}
	
	public void testPatternNotAddedIfExcluded() throws CoreException {
		Set<String> previousExcludions = MylarIdePlugin.getDefault().getExcludedResourcePatterns();
		Set<String> exclude = new HashSet<String>();
		exclude.add("b*.txt");
		MylarIdePlugin.getDefault().setExcludedResourcePatterns(exclude);
		
		IFile file = project.getProject().getFile("boring.txt");
		file.create(null, true, null);
		assertTrue(file.exists());

		IMylarElement element = MylarPlugin.getContextManager().getElement(structureBridge.getHandleIdentifier(file));
		assertFalse(element.getInterest().isInteresting());
		MylarIdePlugin.getDefault().setExcludedResourcePatterns(previousExcludions);
	}

	public void testPatternNotAddedMatching() throws CoreException {
		Set<String> previousExcludions = MylarIdePlugin.getDefault().getExcludedResourcePatterns();
		Set<String> exclude = new HashSet<String>();
		exclude.add(".*");
		MylarIdePlugin.getDefault().setExcludedResourcePatterns(exclude);
		
		String pattern = ".*";
		String segment = "boring";
		
		String s = pattern.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*");
		assertFalse(segment.matches(s));
		assertTrue(".boring".matches(s));
		
		IFile file = project.getProject().getFile(".boring");
		file.create(null, true, null);
		assertTrue(file.exists());
		IMylarElement element = MylarPlugin.getContextManager().getElement(structureBridge.getHandleIdentifier(file));
		assertFalse(element.getInterest().isInteresting());
		
		file = project.getProject().getFile("boring");
		file.create(null, true, null);
		assertTrue(file.exists());
		element = MylarPlugin.getContextManager().getElement(structureBridge.getHandleIdentifier(file));
		assertTrue(element.getInterest().isInteresting());
		
		MylarIdePlugin.getDefault().setExcludedResourcePatterns(previousExcludions);
	}
	
	public void testFileAdded() throws CoreException {
		IFile file = project.getProject().getFile("new-file.txt");
		file.create(null, true, null);
		assertTrue(file.exists());

		IMylarElement element = MylarPlugin.getContextManager().getElement(structureBridge.getHandleIdentifier(file));
		assertTrue(element.getInterest().isInteresting());
	}

	public void testFolderAddedOnCreation() throws CoreException {
		IFolder folder = project.getProject().getFolder("folder");
		folder.create(true, true, null);
		assertTrue(folder.exists());

		IMylarElement element = MylarPlugin.getContextManager().getElement(structureBridge.getHandleIdentifier(folder));
		assertTrue(element.getInterest().isInteresting());
	}

	public void testDecrementOfFile() throws CoreException, InvocationTargetException, InterruptedException {
		IFolder folder = project.getProject().getFolder("folder");
		folder.create(true, true, null);
		IFile file = project.getProject().getFile(new Path("folder/foo.txt"));
		file.create(null, true, null);

		monitor.selectionChanged(navigator, new StructuredSelection(file));
		monitor.selectionChanged(navigator, new StructuredSelection(folder));

		IMylarElement fileElement = MylarPlugin.getContextManager().getElement(
				structureBridge.getHandleIdentifier(file));
		IMylarElement folderElement = MylarPlugin.getContextManager().getElement(
				structureBridge.getHandleIdentifier(folder));

		assertTrue(fileElement.getInterest().isInteresting());
		assertTrue(folderElement.getInterest().isInteresting());

		assertTrue(MylarPlugin.getContextManager().manipulateInterestForElement(folderElement, false, false, "test"));

		assertFalse(folderElement.getInterest().isInteresting());
		assertFalse(fileElement.getInterest().isInteresting());
	}
}
