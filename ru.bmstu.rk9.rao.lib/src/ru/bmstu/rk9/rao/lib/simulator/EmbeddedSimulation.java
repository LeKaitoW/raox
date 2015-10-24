package ru.bmstu.rk9.rao.lib.simulator;

import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;

public abstract class EmbeddedSimulation {
	protected void init() {
	}

	protected TerminateCondition getTerminateCondition() {
		return () -> false;
	}

	public static final double getCurrentTime() {
		return Simulator.getTime();
	}

	public final int run() {
		Simulator.initSimulation(modelStructure);
		Simulator.addTerminateCondition(getTerminateCondition());
		init();
		return Simulator.run();
	}

	protected final JSONObject modelStructure = new JSONObject()
			.put("name", "").put("resource_types", new JSONArray())
			.put("results", new JSONArray()).put("patterns", new JSONArray())
			.put("events", new JSONArray())
			.put("decision_points", new JSONArray());
}
