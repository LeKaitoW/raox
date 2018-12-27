package ru.bmstu.rk9.rao.lib.runtime;

public class Experiments {
	private static Runnable start;

	public static final void start() {
		start.run();
	}

	public static final void setStart(Runnable start) {
		Experiments.start = start;
	}

}
