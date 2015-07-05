package ru.bmstu.rk9.rao.ui.runtime;

import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.ui.animation.AnimationView;

public class SimulationModeDispatcher {
	public static void setMode(String currentState) {

		SimulationSynchronizer.setState(currentState);

		AnimationView.disableAnimation(currentState.equals("NA") ? true
				: false);

		if (Simulator.isInitialized()) {
			switch (currentState) {
			case "P":
			case "NA":
				Simulator.getTracer().setPaused(true);
				Simulator.getDatabase().getIndexHelper().setPaused(true);
				break;
			case "FF":
			case "NS":
				Simulator.getTracer().setPaused(false);
				Simulator.getDatabase().getIndexHelper().setPaused(false);
				break;
			}
		}
	}
}
