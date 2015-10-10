package ru.bmstu.rk9.rao.ui.simulation;

import java.util.HashMap;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.ISourceProviderService;

public class ModelExecutionSourceProvider extends AbstractSourceProvider {
	public final static String ModelExecutionKey = "ru.bmstu.rk9.rao.ui.handlers.simulationRunning";
	public final static String running = "RUNNING";
	public final static String stopped = "STOPPED";

	private static boolean isRunning = false;

	public static boolean getRunningState() {
		return isRunning;
	}

	public static void setRunningState(IWorkbenchWindow workbenchWindow,
			boolean newstate) {
		ISourceProviderService sourceProviderService = (ISourceProviderService) workbenchWindow
				.getService(ISourceProviderService.class);
		ModelExecutionSourceProvider sourceProvider = (ModelExecutionSourceProvider) sourceProviderService
				.getSourceProvider(ModelExecutionKey);

		isRunning = newstate;
		PlatformUI.getWorkbench().getDisplay()
				.syncExec(() -> sourceProvider.updateRunningState());
	}

	@Override
	public void dispose() {
	}

	@Override
	public HashMap<String, String> getCurrentState() {
		HashMap<String, String> currentState = new HashMap<String, String>(1);
		currentState.put(ModelExecutionKey, isRunning ? running : stopped);
		return currentState;
	}

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] { ModelExecutionKey };
	}

	public void updateRunningState() {
		fireSourceChanged(ISources.WORKBENCH, ModelExecutionKey,
				isRunning ? running : stopped);
	}
}
