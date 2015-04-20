package ru.bmstu.rk9.rdo.ui.runtime;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import ru.bmstu.rk9.rdo.lib.Simulator;

public class StoppingHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Simulator.stopExecution();
		return null;
	}
}