package ru.bmstu.rk9.rao.ui.simulation;

import ru.bmstu.rk9.rao.ui.notification.RealTimeUpdater;
import ru.bmstu.rk9.rao.ui.player.StateObserver;

public class RuntimeComponents {
	public static RealTimeUpdater realTimeUpdater = null;
	public static SimulationSynchronizer simulationSynchronizer = null;
	public static StateObserver stateObserver = null;

	private static boolean isInitialized = false;

	public static final boolean isInitialized() {
		return isInitialized;
	}

	public static final void initialize() {
		realTimeUpdater = new RealTimeUpdater();
		simulationSynchronizer = new SimulationSynchronizer();
		stateObserver = new StateObserver();
		isInitialized = true;
	}

	public static final void deinitialize() {
		isInitialized = false;
		realTimeUpdater.deinitialize();
		simulationSynchronizer.deinitializeSubscribers();
		stateObserver.deinitializeSubscribers();

	}
}
