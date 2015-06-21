package ru.bmstu.rk9.rdo.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.bmstu.rk9.rdo.lib.json.JSONArray;
import ru.bmstu.rk9.rdo.lib.json.JSONObject;

public class ModelStructureCache {
	ModelStructureCache() {
		initResourceCache();
		initResourceTypesCache();
		initPatternsCache();
		initDecisionPointsCache();
		initResultsCache();
		initEventNames();
	}

	enum ValueType {
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

	final Map<Integer, HashMap<Integer, String>> resourceNames = new HashMap<Integer, HashMap<Integer, String>>();
	final List<ResourceTypeCache> resourceTypesInfo = new ArrayList<ResourceTypeCache>();
	final List<ResultCache> resultsInfo = new ArrayList<ResultCache>();
	final List<PatternCache> patternsInfo = new ArrayList<PatternCache>();
	final List<String> eventNames = new ArrayList<String>();
	final List<DecisionPointCache> decisionPointsInfo = new ArrayList<DecisionPointCache>();

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

			resourceNames.put(typeNum, resources);
		}
	}

	final void initResourceTypesCache() {
		final JSONArray resourceTypes = Simulator.getDatabase()
				.getModelStructure().getJSONArray("resource_types");

		for (int num = 0; num < resourceTypes.length(); num++)
			resourceTypesInfo.add(new ResourceTypeCache(resourceTypes
					.getJSONObject(num)));
	}

	final void initPatternsCache() {
		final JSONArray patterns = Simulator.getDatabase().getModelStructure()
				.getJSONArray("patterns");

		for (int num = 0; num < patterns.length(); num++)
			patternsInfo.add(new PatternCache(patterns.getJSONObject(num)));
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
			resultsInfo.add(new ResultCache(results.getJSONObject(num)));
	}

	final void initEventNames() {
		final JSONArray events = Simulator.getDatabase().getModelStructure()
				.getJSONArray("events");

		for (int num = 0; num < events.length(); num++)
			eventNames.add(events.getJSONObject(num).getString("name"));
	}

	final static JSONObject getEnumOrigin(String enumOrigin) {
		String typeName = enumOrigin.substring(0, enumOrigin.lastIndexOf("."));

		JSONArray enums = Simulator.getDatabase().getModelStructure()
				.getJSONArray("enums");
		JSONObject originDeclaration = null;
		for (int num = 0; num < enums.length(); num++) {
			JSONObject curType = enums.getJSONObject(num);
			if (typeName.equals(curType.getString("name"))) {
				originDeclaration = curType;
				break;
			}
		}

		return originDeclaration;
	}

	public final static String getRelativeName(final String fullName) {
		return fullName.substring(fullName.lastIndexOf(".") + 1);
	}
}

class ValueCache {
	ValueCache(final JSONObject param) {
		type = ModelStructureCache.ValueType.get(param.getString("type"));
		if (type == ModelStructureCache.ValueType.ENUM) {
			enumNames = new HashMap<Integer, String>();
			JSONObject enumOrigin = null;
			String enumOriginName = param.getString("enum_origin");
			enumOrigin = ModelStructureCache.getEnumOrigin(enumOriginName);

			JSONArray enums = enumOrigin.getJSONObject("structure")
					.getJSONArray("enums");
			for (int num = 0; num < enums.length(); num++)
				enumNames.put(num, enums.getString(num));
		} else
			enumNames = null;
	}

	final ModelStructureCache.ValueType type;
	final HashMap<Integer, String> enumNames;
}

class ResourceTypeCache {
	ResourceTypeCache(final JSONObject resourceType) {
		name = ModelStructureCache.getRelativeName(resourceType
				.getString("name"));
		temporary = resourceType.getBoolean("temporary");

		JSONObject structure = resourceType.getJSONObject("structure");
		JSONArray parameters = structure.getJSONArray("parameters");
		numberOfParameters = parameters.length();

		paramTypes = new HashMap<Integer, ValueCache>();
		indexList = new HashMap<Integer, Integer>();
		for (int num = 0; num < numberOfParameters; num++) {
			final JSONObject currentParameter = parameters.getJSONObject(num);
			ModelStructureCache.ValueType type = ModelStructureCache.ValueType
					.get(currentParameter.getString("type"));
			paramTypes.put(num, new ValueCache(currentParameter));
			if (type == ModelStructureCache.ValueType.STRING) {
				indexList.put(num, currentParameter.getInt("index"));
			}
		}
		finalOffset = structure.getInt("last_offset");
	}

	final String name;
	final boolean temporary;
	final int numberOfParameters;
	final HashMap<Integer, ValueCache> paramTypes;
	final HashMap<Integer, Integer> indexList;
	final int finalOffset;
}

class PatternCache {
	PatternCache(final JSONObject pattern) {
		name = ModelStructureCache.getRelativeName(pattern.getString("name"));
		String type = pattern.getString("type");
		if (type.equals("event")) {
			relResTypes = null;
			return;
		}

		relResTypes = new HashMap<Integer, Integer>();

		JSONArray relevantResources = pattern
				.getJSONArray("relevant_resources");
		for (int num = 0; num < relevantResources.length(); num++) {
			String typeName = relevantResources.getJSONObject(num).getString(
					"type");

			JSONArray resTypes = Simulator.getDatabase().getModelStructure()
					.getJSONArray("resource_types");
			int typeNum = -1;
			// TODO throw exception if not found
			for (int i = 0; i < resTypes.length(); i++)
				if (typeName
						.equals(resTypes.getJSONObject(i).getString("name"))) {
					typeNum = i;
					break;
				}
			relResTypes.put(num, typeNum);
		}
	}

	final String name;
	final HashMap<Integer, Integer> relResTypes;
}

class DecisionPointCache {
	DecisionPointCache(final JSONObject dpt) {
		name = ModelStructureCache.getRelativeName(dpt.getString("name"));

		activitiesInfo = new HashMap<Integer, ActivityCache>();
		JSONArray activities = dpt.getJSONArray("activities");
		for (int num = 0; num < activities.length(); num++)
			activitiesInfo.put(num,
					new ActivityCache(activities.getJSONObject(num)));
	}

	final String name;
	final HashMap<Integer, ActivityCache> activitiesInfo;
}

class ActivityCache {
	ActivityCache(final JSONObject activity) {
		name = ModelStructureCache.getRelativeName(activity.getString("name"));

		final String patternName = activity.getString("pattern");
		JSONArray patterns = Simulator.getDatabase().getModelStructure()
				.getJSONArray("patterns");
		// TODO throw exception if not found
		for (int num = 0; num < patterns.length(); num++)
			if (patternName.equals(patterns.getJSONObject(num)
					.getString("name"))) {
				patternNumber = num;
				break;
			}
	}

	final String name;
	int patternNumber = -1;
}

class ResultCache {
	ResultCache(final JSONObject result) {
		name = ModelStructureCache.getRelativeName(result.getString("name"));
		String valueTypeRaw = result.getString("value_type");
		if (valueTypeRaw.contains("_enum")) {
			valueType = ModelStructureCache.ValueType.ENUM;
			enumNames = new HashMap<Integer, String>();
			String enumOriginName = valueTypeRaw.substring(0,
					valueTypeRaw.lastIndexOf("_"));
			JSONObject originParam = ModelStructureCache
					.getEnumOrigin(enumOriginName);

			JSONArray enums = originParam.getJSONArray("enums");
			for (int num = 0; num < enums.length(); num++)
				enumNames.put(num, enums.getString(num));
		} else {
			valueType = ModelStructureCache.ValueType.get(result
					.getString("value_type"));
			enumNames = null;
		}
	}

	final String name;
	final ModelStructureCache.ValueType valueType;
	final HashMap<Integer, String> enumNames;
}
