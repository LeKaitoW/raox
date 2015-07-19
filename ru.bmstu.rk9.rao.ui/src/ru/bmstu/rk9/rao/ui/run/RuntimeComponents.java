package ru.bmstu.rk9.rao.ui.run;

import ru.bmstu.rk9.rao.ui.notification.RealTimeUpdater;
import ru.bmstu.rk9.rao.ui.simulation.SimulationSynchronizer;

public class RuntimeComponents {
	public static RealTimeUpdater realTimeUpdater = null;
	public static SimulationSynchronizer simulationSynchronizer = null;

	private static boolean isInitialized = false;

	public static final boolean isInitialized() {
		return isInitialized;
	}

	public static final void initialize() {
		isInitialized = true;
		realTimeUpdater = new RealTimeUpdater();
		simulationSynchronizer = new SimulationSynchronizer();
	}

	public static final void deinitialize() {
		isInitialized = false;
	}
}
