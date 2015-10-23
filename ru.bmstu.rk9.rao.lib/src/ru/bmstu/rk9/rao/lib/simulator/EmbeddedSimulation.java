package ru.bmstu.rk9.rao.lib.simulator;

import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;

public abstract class EmbeddedSimulation {
	protected static EmbeddedSimulation INSTANCE = null;

	protected abstract void init();

	public static double getCurrentTime() {
		return Simulator.getTime();
	}

	public int initSimulation(TerminateCondition terminateCondition) {
		Simulator.initSimulation(modelStructure);
		Simulator.addTerminateCondition(terminateCondition);
		init();
		return Simulator.run();
	}

	protected final JSONObject modelStructure = new JSONObject()
			.put("name", "").put("resource_types", new JSONArray())
			.put("results", new JSONArray()).put("patterns", new JSONArray())
			.put("events", new JSONArray())
			.put("decision_points", new JSONArray());
}
