package ru.bmstu.rk9.rao.lib.simulator;

public class EmbeddedSimulation {
	private static EmbeddedSimulation INSTANCE;

	protected int run() {
		return 0;
	}

	public static int runSimulation() {
		INSTANCE = new EmbeddedSimulation();
		return INSTANCE.run();
	}
}
