package ru.bmstu.rk9.rao.lib.modelStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class ModelStructureCache {
	public ModelStructureCache() {
		initResourceCache();
		initResourceTypesCache();
		initPatternsCache();
		initDecisionPointsCache();
		initResultsCache();
		initEventNames();
	}

	public enum ValueType {
		INTEGER("int"), REAL("double"), BOOLEAN("boolean"), ENUM("enum"), STRING(
				"String");

		private final String type;

		ValueType(String type) {
			this.type = type;
		}

		static final ValueType get(final String type) {
			for (ValueType t : values()) {
				if (t.type.equals(type))
					return t;
			}
			return null;
		}
	}

	private final Map<Integer, HashMap<Integer, String>> resourceNames = new HashMap<Integer, HashMap<Integer, String>>();
	private final List<ResourceTypeCache> resourceTypesInfo = new ArrayList<ResourceTypeCache>();
	private final List<ResultCache> resultsInfo = new ArrayList<ResultCache>();
	private final List<PatternCache> patternsInfo = new ArrayList<PatternCache>();
	private final List<String> eventNames = new ArrayList<String>();
	public final List<DecisionPointCache> decisionPointsInfo = new ArrayList<DecisionPointCache>();

	public final List<DecisionPointCache> getDecisionPointsInfo() {
		return decisionPointsInfo;
	}

	public final String getDecisionPointName(int dptNumber) {
		return getDecisionPointsInfo().get(dptNumber).getName();
	}

	final void initResourceCache() {
		final JSONArray resourceTypes = Simulator.getDatabase()
				.getModelStructure().getJSONArray("resource_types");

		for (int typeNum = 0; typeNum < resourceTypes.length(); typeNum++) {
			JSONObject type = resourceTypes.getJSONObject(typeNum);
			HashMap<Integer, String> resources = new HashMap<Integer, String>();

			JSONArray jResources = type.getJSONArray("resources");
			for (int resNum = 0; resNum < jResources.length(); resNum++)
				resources.put(resNum,
						getRelativeName(jResources.getString(resNum)));

			getResourceNames().put(typeNum, resources);
		}
	}

	final void initResourceTypesCache() {
		final JSONArray resourceTypes = Simulator.getDatabase()
				.getModelStructure().getJSONArray("resource_types");

		for (int num = 0; num < resourceTypes.length(); num++)
			getResourceTypesInfo().add(
					new ResourceTypeCache(resourceTypes.getJSONObject(num)));
	}

	final void initPatternsCache() {
		final JSONArray patterns = Simulator.getDatabase().getModelStructure()
				.getJSONArray("patterns");

		for (int num = 0; num < patterns.length(); num++)
			getPatternsInfo().add(new PatternCache(patterns.getJSONObject(num)));
	}

	final void initDecisionPointsCache() {
		final JSONArray decisionPoints = Simulator.getDatabase()
				.getModelStructure().getJSONArray("decision_points");

		for (int num = 0; num < decisionPoints.length(); num++)
			decisionPointsInfo.add(new DecisionPointCache(decisionPoints
					.getJSONObject(num)));
	}

	final void initResultsCache() {
		final JSONArray results = Simulator.getDatabase().getModelStructure()
				.getJSONArray("results");

		for (int num = 0; num < results.length(); num++)
			getResultsInfo().add(new ResultCache(results.getJSONObject(num)));
	}

	final void initEventNames() {
		final JSONArray events = Simulator.getDatabase().getModelStructure()
				.getJSONArray("events");

		for (int num = 0; num < events.length(); num++)
			getEventNames().add(events.getJSONObject(num).getString("name"));
	}

	final static JSONObject getEnumOrigin(String enumOrigin) {
		String typeName = enumOrigin.substring(0, enumOrigin.lastIndexOf("."));

		JSONArray enums = Simulator.getDatabase().getModelStructure()
				.getJSONArray("enums");
		for (int num = 0; num < enums.length(); num++) {
			JSONObject enumItem = enums.getJSONObject(num);
			if (typeName.equals(enumItem.getString("name"))) {
				return enumItem;
			}
		}

		// TODO throw exception
		return null;
	}

	public final static String getRelativeName(final String fullName) {
		return fullName.substring(fullName.lastIndexOf(".") + 1);
	}

	public List<ResourceTypeCache> getResourceTypesInfo() {
		return resourceTypesInfo;
	}

	public List<ResultCache> getResultsInfo() {
		return resultsInfo;
	}

	public Map<Integer, HashMap<Integer, String>> getResourceNames() {
		return resourceNames;
	}

	public List<PatternCache> getPatternsInfo() {
		return patternsInfo;
	}

	public List<String> getEventNames() {
		return eventNames;
	}
}
