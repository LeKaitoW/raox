package ru.bmstu.rk9.rao.lib.simulator;

import java.util.ArrayList;
import java.util.List;

import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.modelData.ModelStructureConstants;
import ru.bmstu.rk9.rao.lib.process.Transact;

public class SimulatorPreinitializationInfo {
	public SimulatorPreinitializationInfo() {
		modelStructure.put(ModelStructureConstants.NAME, "")
				.put(ModelStructureConstants.RESOURCE_TYPES, new JSONArray())
				.put(ModelStructureConstants.RESULTS, new JSONArray())
				.put(ModelStructureConstants.PATTERNS, new JSONArray())
				.put(ModelStructureConstants.EVENTS, new JSONArray())
				.put(ModelStructureConstants.LOGICS, new JSONArray())
				.put(ModelStructureConstants.SEARCHES, new JSONArray());
		resourceClasses.add(Transact.class);
	}

	public final JSONObject modelStructure = new JSONObject();
	public final List<Class<?>> resourceClasses = new ArrayList<>();
}
