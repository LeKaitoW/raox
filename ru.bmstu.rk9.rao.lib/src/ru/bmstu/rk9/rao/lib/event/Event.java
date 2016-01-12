package ru.bmstu.rk9.rao.lib.event;

import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public abstract class Event {
	protected double time;

	public final double getTime() {
		return time;
	}

	protected static void pushEvent(Event event) {
		Simulator.pushEvent(event);
	}

	public abstract String getName();

	public abstract void run();
}
