package ru.bmstu.rk9.rao.lib.event;

public abstract class Event {
	protected double time;

	public final double getTime() {
		return time;
	}

	public abstract String getName();

	public abstract void run();
}
