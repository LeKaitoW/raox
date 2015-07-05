package ru.bmstu.rk9.rao.lib.event;

public interface Event {

	public String getName();

	public double getTime();

	public void run();
}
