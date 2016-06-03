package ru.bmstu.rk9.rao.ui.simulation;

import ru.bmstu.rk9.rao.ui.dump.Writer;
import ru.bmstu.rk9.rao.ui.notification.RealTimeUpdater;

public class RuntimeComponents {
	public static RealTimeUpdater realTimeUpdater = null;
	public static SimulationSynchronizer simulationSynchronizer = null;
	public static Writer modelPlayer = null;

	private static boolean isInitialized = false;

	public static final boolean isInitialized() {
		return isInitialized;
	}

	public static final void initialize() {
		realTimeUpdater = new RealTimeUpdater();
		simulationSynchronizer = new SimulationSynchronizer();
		modelPlayer = new Writer();
		isInitialized = true;
	}

	public static final void deinitialize() {
		isInitialized = false;
		realTimeUpdater.deinitialize();
		simulationSynchronizer.deinitializeSubscribers();
		modelPlayer.deinitializeSubscribers();

	}
}
