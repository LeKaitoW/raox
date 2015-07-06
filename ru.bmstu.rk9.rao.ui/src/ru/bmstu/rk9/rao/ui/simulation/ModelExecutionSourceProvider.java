package ru.bmstu.rk9.rao.ui.simulation;

import java.util.HashMap;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

import ru.bmstu.rk9.rao.ui.run.ExecutionHandler;

public class ModelExecutionSourceProvider extends AbstractSourceProvider {
	public final static String ModelExecutionKey = "ru.bmstu.rk9.rao.ui.handlers.simulationRunning";
	public final static String running = "RUNNING";
	public final static String stopped = "STOPPED";

	@Override
	public void dispose() {
	}

	@Override
	public HashMap<String, String> getCurrentState() {
		HashMap<String, String> currentState = new HashMap<String, String>(1);
		currentState.put(ModelExecutionKey,
				ExecutionHandler.getRunningState() ? running : stopped);
		return currentState;
	}

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] { ModelExecutionKey };
	}

	public void updateRunningState() {
		fireSourceChanged(ISources.WORKBENCH, ModelExecutionKey,
				ExecutionHandler.getRunningState() ? running : stopped);
	}
}
