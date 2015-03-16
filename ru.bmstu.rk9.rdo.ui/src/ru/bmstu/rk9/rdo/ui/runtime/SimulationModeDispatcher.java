package ru.bmstu.rk9.rdo.ui.runtime;

import ru.bmstu.rk9.rdo.lib.Simulator;
import ru.bmstu.rk9.rdo.ui.animation.RDOAnimationView;

public class SimulationModeDispatcher {
	public static void setMode(String currentState) {

		SimulationSynchronizer.setState(currentState);

		RDOAnimationView.disableAnimation(currentState.equals("NA") ? true
				: false);

		if (Simulator.isInitialized()) {
			switch (currentState) {
			case "P":
			case "NA":
				Simulator.getTracer().setPaused(true);
				break;
			case "FF":
			case "NS":
				Simulator.getTracer().setPaused(false);
				break;
			}
		}
	}
}
