package ru.bmstu.rk9.rao.lib.simulator;

import java.util.ArrayList;
import java.util.Collection;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ru.bmstu.rk9.rao.lib.modeldata.ModelStructureConstants;
import ru.bmstu.rk9.rao.lib.process.Resource;
import ru.bmstu.rk9.rao.lib.process.Transact;

public class SimulatorPreinitializationInfo {
	public SimulatorPreinitializationInfo() {
		modelStructure = generateModelStructureStrub();
		resourceClasses.add(Resource.class);
		resourceClasses.add(Transact.class);
	}

	public final JsonObject modelStructure;
	public final Collection<Class<? extends ru.bmstu.rk9.rao.lib.resource.Resource>> resourceClasses = new ArrayList<>();

	public static final JsonObject generateModelStructureStrub() {

		JsonObject modelStructureStrub = new JsonObject();
		modelStructureStrub.addProperty(ModelStructureConstants.NAME, "");
		modelStructureStrub.add(ModelStructureConstants.RESOURCE_TYPES, new JsonArray());
		modelStructureStrub.add(ModelStructureConstants.RESULTS, new JsonArray());
		modelStructureStrub.add(ModelStructureConstants.PATTERNS, new JsonArray());
		modelStructureStrub.add(ModelStructureConstants.EVENTS, new JsonArray());
		modelStructureStrub.add(ModelStructureConstants.LOGICS, new JsonArray());
		modelStructureStrub.add(ModelStructureConstants.SEARCHES, new JsonArray());
		return modelStructureStrub;

	}
}
