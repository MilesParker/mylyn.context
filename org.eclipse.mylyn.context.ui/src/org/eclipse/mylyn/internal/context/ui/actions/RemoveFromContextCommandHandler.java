package org.eclipse.mylyn.internal.context.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

public class RemoveFromContextCommandHandler extends AbstractHandler {

	private final InterestDecrementAction action = new InterestDecrementAction();

	public Object execute(ExecutionEvent event) throws ExecutionException {

		action.selectionChanged(null, HandlerUtil.getCurrentSelection(event));
		action.run(null);
		return null;
	}

}