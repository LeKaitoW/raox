package ru.bmstu.rk9.rao.ui.simulation;

import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.ui.animation.AnimationView;
import ru.bmstu.rk9.rao.ui.notification.RealTimeUpdater;

public class SimulationModeDispatcher {
	public static void setMode(String currentState) {

		SimulationSynchronizer.setState(currentState);

		AnimationView.disableAnimation(currentState.equals("NA") ? true
				: false);

		if (Simulator.isInitialized()) {
			switch (currentState) {
			case "P":
			case "NA":
				RealTimeUpdater.setPaused(true);
				break;
			case "FF":
			case "NS":
				RealTimeUpdater.setPaused(false);
				break;
			}
		}
	}
}
