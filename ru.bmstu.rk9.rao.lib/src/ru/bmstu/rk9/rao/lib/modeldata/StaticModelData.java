package ru.bmstu.rk9.rao.lib.modeldata;

import ru.bmstu.rk9.rao.lib.database.Database.DataType;
import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;

// TODO current implementation seems highly inefficient, measure performance and optimize if needed
public class StaticModelData {
	public StaticModelData(JSONObject modelStructure) {
		this.modelStructure = modelStructure;
	}

	private final JSONObject modelStructure;

	public final JSONObject getModelStructure() {
		return modelStructure;
	}

	/* Resource types */
	public final int getNumberOfResourceTypes() {
		return modelStructure.getJSONArray(ModelStructureConstants.RESOURCE_TYPES).length();
	}

	public final JSONObject getResourceType(int index) {
		return modelStructure.getJSONArray(ModelStructureConstants.RESOURCE_TYPES).getJSONObject(index);
	}

	public final int getResourceTypeNumber(String name) {
		JSONArray resourceTypes = modelStructure.getJSONArray(ModelStructureConstants.RESOURCE_TYPES);
		int resourceTypeNumber;
		for (resourceTypeNumber = 0; resourceTypeNumber < resourceTypes.length(); resourceTypeNumber++) {
			JSONObject resourceType = resourceTypes.getJSONObject(resourceTypeNumber);
			if (resourceType.getString(ModelStructureConstants.NAME).equals(name))
				return resourceTypeNumber;
		}

		return -1;
	}

	public final String getResourceTypeName(int index) {
		return modelStructure.getJSONArray(ModelStructureConstants.RESOURCE_TYPES).getJSONObject(index)
				.getString(ModelStructureConstants.NAME);
	}

	public final int getNumberOfResourceTypeParameters(int resourceTypeIndex) {
		return modelStructure.getJSONArray(ModelStructureConstants.RESOURCE_TYPES).getJSONObject(resourceTypeIndex)
				.getJSONArray(ModelStructureConstants.PARAMETERS).length();
	}

	public final JSONObject getResourceTypeParameter(int resourceTypeIndex, int parameterIndex) {
		return modelStructure.getJSONArray(ModelStructureConstants.RESOURCE_TYPES).getJSONObject(resourceTypeIndex)
				.getJSONArray(ModelStructureConstants.PARAMETERS).getJSONObject(parameterIndex);
	}

	public final String getResourceName(int resourceTypeIndex, int resourceIndex) {
		JSONArray namedResources = modelStructure.getJSONArray(ModelStructureConstants.RESOURCE_TYPES)
				.getJSONObject(resourceTypeIndex).getJSONArray(ModelStructureConstants.NAMED_RESOURCES);
		if (namedResources.length() <= resourceIndex)
			return null;

		return namedResources.getJSONObject(resourceIndex).getString(ModelStructureConstants.NAME);
	}

	public final DataType getResourceTypeParameterType(int resourceTypeIndex, int parameterIndex) {
		return (DataType) modelStructure.getJSONArray(ModelStructureConstants.RESOURCE_TYPES)
				.getJSONObject(resourceTypeIndex).getJSONArray(ModelStructureConstants.PARAMETERS)
				.getJSONObject(parameterIndex).get(ModelStructureConstants.TYPE);
	}

	public final int getResourceTypeParameterOffset(int resourceTypeIndex, int parameterIndex) {
		return modelStructure.getJSONArray(ModelStructureConstants.RESOURCE_TYPES).getJSONObject(resourceTypeIndex)
				.getJSONArray(ModelStructureConstants.PARAMETERS).getJSONObject(parameterIndex)
				.getInt(ModelStructureConstants.OFFSET);
	}

	public final int getResourceTypeFinalOffset(int resourceTypeIndex) {
		return modelStructure.getJSONArray(ModelStructureConstants.RESOURCE_TYPES).getJSONObject(resourceTypeIndex)
				.getInt(ModelStructureConstants.FINAL_OFFSET);
	}

	public final int getVariableWidthParameterIndex(int resourceTypeIndex, int parameterIndex) {
		return modelStructure.getJSONArray(ModelStructureConstants.RESOURCE_TYPES).getJSONObject(resourceTypeIndex)
				.getJSONArray(ModelStructureConstants.PARAMETERS).getJSONObject(parameterIndex)
				.getInt(ModelStructureConstants.VARIABLE_WIDTH_PARAMETER_INDEX);
	}

	/* Events */
	public final String getEventName(int index) {
		return modelStructure.getJSONArray(ModelStructureConstants.EVENTS).getJSONObject(index)
				.getString(ModelStructureConstants.NAME);
	}

	/* Logics */
	public final int getNumberOfLogics() {
		return modelStructure.getJSONArray(ModelStructureConstants.LOGICS).length();
	}

	public final int getNumberOfActivities(int logicIndex) {
		return modelStructure.getJSONArray(ModelStructureConstants.LOGICS).getJSONObject(logicIndex)
				.getJSONArray(ModelStructureConstants.ACTIVITIES).length();
	}

	public final String getActivityName(int logicIndex, int activityIndex) {
		return modelStructure.getJSONArray(ModelStructureConstants.LOGICS).getJSONObject(logicIndex)
				.getJSONArray(ModelStructureConstants.ACTIVITIES).getJSONObject(activityIndex)
				.getString(ModelStructureConstants.NAME);
	}

	/* Patterns */
	public final int getPatternNumber(String name) {
		JSONArray patterns = modelStructure.getJSONArray(ModelStructureConstants.PATTERNS);
		int patternNumber;
		for (patternNumber = 0; patternNumber < patterns.length(); patternNumber++) {
			JSONObject pattern = patterns.getJSONObject(patternNumber);
			if (pattern.getString(ModelStructureConstants.NAME).equals(name))
				return patternNumber;
		}

		return -1;
	}

	public final int getNumberOfRelevantResources(int patternNumber) {
		return modelStructure.getJSONArray(ModelStructureConstants.PATTERNS).getJSONObject(patternNumber)
				.getJSONArray(ModelStructureConstants.RELEVANT_RESOURCES).length();
	}

	public final String getPatternType(int patternNumber) {
		return modelStructure.getJSONArray(ModelStructureConstants.PATTERNS).getJSONObject(patternNumber)
				.getString(ModelStructureConstants.TYPE);
	}

	public final String getRelevantResourceTypeName(int patternNumber, int relevantResourceNumber) {
		return modelStructure.getJSONArray(ModelStructureConstants.PATTERNS).getJSONObject(patternNumber)
				.getJSONArray(ModelStructureConstants.RELEVANT_RESOURCES).getJSONObject(relevantResourceNumber)
				.getString(ModelStructureConstants.TYPE);
	}

	public final int getRelevantResourceTypeNumber(int patternNumber, int relevantResourceNumber) {
		String typeName = modelStructure.getJSONArray(ModelStructureConstants.PATTERNS).getJSONObject(patternNumber)
				.getJSONArray(ModelStructureConstants.RELEVANT_RESOURCES).getJSONObject(relevantResourceNumber)
				.getString(ModelStructureConstants.TYPE);
		return getResourceTypeNumber(typeName);
	}

	/* Searches */
	public final String getSearchName(int index) {
		return modelStructure.getJSONArray(ModelStructureConstants.SEARCHES).getJSONObject(index)
				.getString(ModelStructureConstants.NAME);
	}

	public final String getEdgeName(int searchIndex, int edgeIndex) {
		return modelStructure.getJSONArray(ModelStructureConstants.SEARCHES).getJSONObject(searchIndex)
				.getJSONArray(ModelStructureConstants.EDGES).getJSONObject(edgeIndex)
				.getString(ModelStructureConstants.NAME);
	}

	/* Results */
	public final String getResultName(int index) {
		return modelStructure.getJSONArray(ModelStructureConstants.RESULTS).getJSONObject(index)
				.getString(ModelStructureConstants.NAME);
	}
}
