package ru.bmstu.rk9.rao.lib.event;

import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public abstract class Event {
	protected double time;

	public final double getTime() {
		return time;
	}

	public abstract String getName();

	public final void run() {
		execute();
		Simulator.getDatabase().addEventEntry(this);
	}

	protected abstract void execute();
}
