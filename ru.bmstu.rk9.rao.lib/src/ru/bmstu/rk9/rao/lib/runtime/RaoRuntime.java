package ru.bmstu.rk9.rao.lib.runtime;

import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class RaoRuntime {
	public static final double getCurrentTime() {
		return Simulator.getTime();
	}
}
