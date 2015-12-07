package ru.bmstu.rk9.rao.lib.runtime;

import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class Model {
	public static final double getCurrentTime() {
		return Simulator.getTime();
	}
}
