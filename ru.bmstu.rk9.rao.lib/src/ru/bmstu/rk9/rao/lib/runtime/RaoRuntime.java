package ru.bmstu.rk9.rao.lib.runtime;

import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public class RaoRuntime {
	public static final double getCurrentTime() {
		return CurrentSimulator.getTime();
	}

	public static final void start() {
		Experiments.start();
	}
}
