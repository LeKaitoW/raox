package ru.bmstu.rk9.rao.lib.runtime;

import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public class LoggerExtensions {
	public static void log(Object o) {
		CurrentSimulator.getLogger().log(o);
	}
}
