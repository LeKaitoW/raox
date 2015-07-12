package ru.bmstu.rk9.rao.ui.simulation;

import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.ui.animation.AnimationView;
import ru.bmstu.rk9.rao.ui.notification.RealTimeUpdater;
import ru.bmstu.rk9.rao.ui.simulation.SimulationSynchronizer.ExecutionMode;

public class SimulationModeDispatcher {
	public static void setMode(ExecutionMode currentMode) {

		SimulationSynchronizer.setState(currentMode);

		AnimationView
				.disableAnimation(currentMode == ExecutionMode.NO_ANIMATION ? true
						: false);

		if (Simulator.isInitialized()) {
			switch (currentMode) {
			case PAUSE:
			case NO_ANIMATION:
				RealTimeUpdater.setPaused(true);
				break;
			case FAST_FORWARD:
			case NORMAL_SPEED:
				RealTimeUpdater.setPaused(false);
				break;
			}
		}
	}
}
