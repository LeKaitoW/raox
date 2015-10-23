package ru.bmstu.rk9.rao.ui.simulation;

import java.util.HashMap;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.ISourceProviderService;

public class ModelExecutionSourceProvider extends AbstractSourceProvider {
	public final static String ModelExecutionKey = "ru.bmstu.rk9.rao.ui.handlers.simulationState";

	public enum SimulationState {
		RUNNING("RUNNING"), STOPPED("STOPPED"), DISABLED("DISABLED");

		SimulationState(String state) {
			this.state = state;
		}

		private final String state;

		@Override
		public final String toString() {
			return state;
		}
	}

	private static String simulationState = SimulationState.STOPPED.toString();

	public static String getSimulationState() {
		return simulationState;
	}

	public static void setSimulationState(IWorkbenchWindow workbenchWindow,
			String newState) {
		ISourceProviderService sourceProviderService = (ISourceProviderService) workbenchWindow
				.getService(ISourceProviderService.class);
		ModelExecutionSourceProvider sourceProvider = (ModelExecutionSourceProvider) sourceProviderService
				.getSourceProvider(ModelExecutionKey);

		simulationState = newState;
		PlatformUI.getWorkbench().getDisplay()
				.syncExec(() -> sourceProvider.updateSimulationState());
	}

	@Override
	public void dispose() {
	}

	@Override
	public HashMap<String, String> getCurrentState() {
		HashMap<String, String> currentState = new HashMap<String, String>(1);
		currentState.put(ModelExecutionKey, simulationState.toString());
		return currentState;
	}

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] { ModelExecutionKey };
	}

	public void updateSimulationState() {
		fireSourceChanged(ISources.WORKBENCH, ModelExecutionKey,
				simulationState.toString());
	}
}
