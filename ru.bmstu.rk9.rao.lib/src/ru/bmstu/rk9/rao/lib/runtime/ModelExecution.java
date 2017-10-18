package ru.bmstu.rk9.rao.lib.runtime;

public class ModelExecution {

	private static Execution init;

	private static Execution start;

	public static final void init() {
		init.run();
	}

	public static final void start() {
		start.run();
	}

	public static final void setInit(Execution init) {
		ModelExecution.init = init;
	}

	public static final void setStart(Execution start) {
		ModelExecution.start = start;
	}
}
