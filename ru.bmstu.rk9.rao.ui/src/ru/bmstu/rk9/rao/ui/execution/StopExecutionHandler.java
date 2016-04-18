package ru.bmstu.rk9.rao.ui.execution;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public class StopExecutionHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		CurrentSimulator.stopExecution();
		return null;
	}
}