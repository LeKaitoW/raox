package ru.bmstu.rk9.rao.lib.simulator;

import java.util.ArrayList;
import java.util.List;

import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.process.Resource;
import ru.bmstu.rk9.rao.lib.process.Transact;

public class SimulatorPreinitializationInfo {
	public SimulatorPreinitializationInfo() {
		modelStructure.put("name", "").put("resource_types", new JSONArray()).put("results", new JSONArray())
				.put("patterns", new JSONArray()).put("events", new JSONArray())
				.put("decision_points", new JSONArray());
		resourceClasses.add(Resource.class);
		resourceClasses.add(Transact.class);
	}

	public final JSONObject modelStructure = new JSONObject();
	public final List<Class<?>> resourceClasses = new ArrayList<>();
}
