package ru.bmstu.rk9.rao.lib.simulator;

import java.util.ArrayList;
import java.util.List;

import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.modeldata.ModelStructureConstants;
import ru.bmstu.rk9.rao.lib.process.Transact;

public class SimulatorPreinitializationInfo {
	public SimulatorPreinitializationInfo() {
		modelStructure = generateModelStructureStub();
		getResourceClasses().add(Transact.class);
	}

	private final JSONObject modelStructure;
	private final List<Class<?>> resourceClasses = new ArrayList<>();
	private final List<Runnable> resourcePreinitializers = new ArrayList<>();

	public static final JSONObject generateModelStructureStub() {
		return new JSONObject().put(ModelStructureConstants.NAME, "").put(ModelStructureConstants.NUMBER_OF_MODELS, 1)
				.put(ModelStructureConstants.RESOURCE_TYPES, new JSONArray())
				.put(ModelStructureConstants.RESULTS, new JSONArray())
				.put(ModelStructureConstants.PATTERNS, new JSONArray())
				.put(ModelStructureConstants.EVENTS, new JSONArray())
				.put(ModelStructureConstants.LOGICS, new JSONArray())
				.put(ModelStructureConstants.SEARCHES, new JSONArray());
	}

	public JSONObject getModelStructure() {
		return modelStructure;
	}

	public List<Class<?>> getResourceClasses() {
		return resourceClasses;
	}

	public List<Runnable> getResourcePreinitializers() {
		return resourcePreinitializers;
	}
}
