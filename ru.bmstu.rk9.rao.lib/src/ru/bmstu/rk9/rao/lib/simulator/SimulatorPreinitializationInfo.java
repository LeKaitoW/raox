package ru.bmstu.rk9.rao.lib.simulator;

import java.util.ArrayList;
import java.util.Collection;

import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.modeldata.ModelStructureConstants;
import ru.bmstu.rk9.rao.lib.process.Resource;
import ru.bmstu.rk9.rao.lib.process.Transact;

public class SimulatorPreinitializationInfo {
	public SimulatorPreinitializationInfo() {
		modelStructure = generateModelStructureStrub();
		resourceClasses.add(Resource.class);
		resourceClasses.add(Transact.class);
	}

	public final JSONObject modelStructure;
	public final Collection<Class<? extends ru.bmstu.rk9.rao.lib.resource.Resource>> resourceClasses = new ArrayList<>();

	public static final JSONObject generateModelStructureStrub() {
		return new JSONObject().put(ModelStructureConstants.NAME, "")
				.put(ModelStructureConstants.RESOURCE_TYPES, new JSONArray())
				.put(ModelStructureConstants.RESULTS, new JSONArray())
				.put(ModelStructureConstants.PATTERNS, new JSONArray())
				.put(ModelStructureConstants.EVENTS, new JSONArray())
				.put(ModelStructureConstants.LOGICS, new JSONArray())
				.put(ModelStructureConstants.SEARCHES, new JSONArray());
	}
}
