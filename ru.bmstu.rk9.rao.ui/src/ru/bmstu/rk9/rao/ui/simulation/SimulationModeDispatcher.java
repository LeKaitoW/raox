package ru.bmstu.rk9.rao.ui.simulation;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import ru.bmstu.rk9.rao.ui.animation.AnimationView;
import ru.bmstu.rk9.rao.ui.notification.RealTimeUpdater;
import ru.bmstu.rk9.rao.ui.simulation.SimulationSynchronizer.ExecutionMode;

public class SimulationModeDispatcher {
	public static void setMode(ExecutionMode currentMode) {
		if (!RuntimeComponents.isInitialized())
			throw new SimulationComponentsException(
					"Runtime components are not initialized");

		RuntimeComponents.simulationSynchronizer.setExecutionMode(currentMode);

		AnimationView
				.setAnimationEnabled(currentMode != ExecutionMode.NO_ANIMATION);

		RealTimeUpdater realTimeUpdater = RuntimeComponents.realTimeUpdater;
		switch (currentMode) {
		case PAUSE:
		case NO_ANIMATION:
			realTimeUpdater.setPaused(true);
			break;
		case FAST_FORWARD:
		case NORMAL_SPEED:
			realTimeUpdater.setPaused(false);
			break;
		}
	};

	public static ExecutionMode getMode() {
		ICommandService service = (ICommandService) PlatformUI.getWorkbench()
				.getService(ICommandService.class);
		Command command = service
				.getCommand("ru.bmstu.rk9.rao.ui.runtime.setExecutionMode");
		State state = command.getState("org.eclipse.ui.commands.radioState");

		return ExecutionMode.getByString((String) state.getValue());
	}
}
