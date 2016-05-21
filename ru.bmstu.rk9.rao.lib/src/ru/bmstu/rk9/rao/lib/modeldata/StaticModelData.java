package ru.bmstu.rk9.rao.lib.modeldata;

import com.google.gson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;


import ru.bmstu.rk9.rao.lib.database.Database.DataType;

// TODO current implementation seems highly inefficient, measure performance and optimize if needed
public class StaticModelData {
	public StaticModelData(JsonObject modelStructure2) {
		this.modelStructure = modelStructure2;
	}

	private final JsonObject modelStructure;

	public final JsonObject getModelStructure() {
		return modelStructure;
	}

	/* Resource types */
	public final int getNumberOfResourceTypes() {
		return modelStructure.getAsJsonArray(ModelStructureConstants.RESOURCE_TYPES).size();
	}

	public final JsonObject getResourceType(int index) {
		return modelStructure.getAsJsonArray(ModelStructureConstants.RESOURCE_TYPES).get(index).getAsJsonObject();
	}

	public final int getResourceTypeNumber(String name) {
		JsonArray resourceTypes = modelStructure.getAsJsonArray(ModelStructureConstants.RESOURCE_TYPES);
		int resourceTypeNumber;
		for (resourceTypeNumber = 0; resourceTypeNumber < resourceTypes.size(); resourceTypeNumber++) {
			JsonObject resourceType = resourceTypes.get(resourceTypeNumber).getAsJsonObject();
			if (resourceType.get(ModelStructureConstants.NAME).equals(name))
				return resourceTypeNumber;
		}

		return -1;
	}

	public final String getResourceTypeName(int index) {
		return modelStructure.getAsJsonArray(ModelStructureConstants.RESOURCE_TYPES).get(index).getAsJsonObject()
				.get(ModelStructureConstants.NAME).getAsString();
	}

	public final int getNumberOfResourceTypeParameters(int resourceTypeIndex) {
		return modelStructure.getAsJsonArray(ModelStructureConstants.RESOURCE_TYPES).get(resourceTypeIndex).getAsJsonObject()
				.get(ModelStructureConstants.PARAMETERS).getAsJsonArray().size();
	}

	public final JsonObject getResourceTypeParameter(int resourceTypeIndex, int parameterIndex) {
		return modelStructure.getAsJsonArray(ModelStructureConstants.RESOURCE_TYPES).get(resourceTypeIndex).getAsJsonObject()
				.get(ModelStructureConstants.PARAMETERS).getAsJsonArray().get(parameterIndex).getAsJsonObject();
	}

	public final String getResourceName(int resourceTypeIndex, int resourceIndex) {
		JsonArray namedResources = modelStructure.getAsJsonArray(ModelStructureConstants.RESOURCE_TYPES)
				.get(resourceTypeIndex).getAsJsonObject().get(ModelStructureConstants.NAMED_RESOURCES).getAsJsonArray();
		if (namedResources.size() <= resourceIndex)
			return null;

		return namedResources.get(resourceIndex).getAsJsonObject().get(ModelStructureConstants.NAME).getAsString();
	}

	public final DataType getResourceTypeParameterType(int resourceTypeIndex, int parameterIndex) {
		Gson gson = new Gson();
		String string = modelStructure.getAsJsonArray(ModelStructureConstants.RESOURCE_TYPES)
				.get(resourceTypeIndex).getAsJsonObject().get(ModelStructureConstants.PARAMETERS).getAsJsonArray()
				.get(parameterIndex).getAsJsonObject().get(ModelStructureConstants.TYPE).getAsString();
		DataType dataType = gson.fromJson(string, DataType.class);
		return dataType;
	}

	public final int getResourceTypeParameterOffset(int resourceTypeIndex, int parameterIndex) {
		return modelStructure.getAsJsonArray(ModelStructureConstants.RESOURCE_TYPES).get(resourceTypeIndex).getAsJsonObject()
				.get(ModelStructureConstants.PARAMETERS).getAsJsonArray().get(parameterIndex).getAsJsonObject()
				.get(ModelStructureConstants.OFFSET).getAsInt();
	}

	public final int getResourceTypeFinalOffset(int resourceTypeIndex) {
		return modelStructure.getAsJsonArray(ModelStructureConstants.RESOURCE_TYPES).get(resourceTypeIndex).getAsJsonObject()
				.get(ModelStructureConstants.FINAL_OFFSET).getAsInt();
	}

	public final int getVariableWidthParameterIndex(int resourceTypeIndex, int parameterIndex) {
		return modelStructure.getAsJsonArray(ModelStructureConstants.RESOURCE_TYPES).get(resourceTypeIndex).getAsJsonObject()
				.get(ModelStructureConstants.PARAMETERS).getAsJsonArray().get(parameterIndex).getAsJsonObject()
				.get(ModelStructureConstants.VARIABLE_WIDTH_PARAMETER_INDEX).getAsInt();
	}

	/* Events */
	public final String getEventName(int index) {
		return modelStructure.getAsJsonArray(ModelStructureConstants.EVENTS).get(index).getAsJsonObject()
				.get(ModelStructureConstants.NAME).getAsString();
	}

	/* Logics */
	public final int getNumberOfLogics() {
		return modelStructure.getAsJsonArray(ModelStructureConstants.LOGICS).size();
	}

	public final int getNumberOfActivities(int logicIndex) {
		return modelStructure.getAsJsonArray(ModelStructureConstants.LOGICS).get(logicIndex).getAsJsonObject()
				.get(ModelStructureConstants.ACTIVITIES).getAsJsonArray().size();
	}

	public final String getActivityName(int logicIndex, int activityIndex) {
		return modelStructure.getAsJsonArray(ModelStructureConstants.LOGICS).get(logicIndex).getAsJsonObject()
				.get(ModelStructureConstants.ACTIVITIES).getAsJsonArray().get(activityIndex).getAsJsonObject()
				.get(ModelStructureConstants.NAME).getAsString();
	}

	/* Patterns */
	public final int getPatternNumber(String name) {
		JsonArray patterns = modelStructure.getAsJsonArray(ModelStructureConstants.PATTERNS);
		int patternNumber;
		for (patternNumber = 0; patternNumber < patterns.size(); patternNumber++) {
			JsonObject pattern = patterns.get(patternNumber).getAsJsonObject();
			if (pattern.get(ModelStructureConstants.NAME).getAsString().equals(name))
				return patternNumber;
		}

		return -1;
	}

	public final int getNumberOfRelevantResources(int patternNumber) {
		return modelStructure.getAsJsonArray(ModelStructureConstants.PATTERNS).get(patternNumber).getAsJsonObject()
				.get(ModelStructureConstants.RELEVANT_RESOURCES).getAsJsonArray().size();
	}

	public final String getPatternType(int patternNumber) {
		return modelStructure.getAsJsonArray(ModelStructureConstants.PATTERNS).get(patternNumber).getAsJsonObject()
				.get(ModelStructureConstants.TYPE).getAsString();
	}

	public final String getRelevantResourceTypeName(int patternNumber, int relevantResourceNumber) {
		return modelStructure.getAsJsonArray(ModelStructureConstants.PATTERNS).get(patternNumber).getAsJsonObject()
				.get(ModelStructureConstants.RELEVANT_RESOURCES).getAsJsonArray().get(relevantResourceNumber).getAsJsonObject()
				.get(ModelStructureConstants.TYPE).getAsString();
	}

	public final int getRelevantResourceTypeNumber(int patternNumber, int relevantResourceNumber) {
		String typeName = modelStructure.getAsJsonArray(ModelStructureConstants.PATTERNS).get(patternNumber).getAsJsonObject()
				.get(ModelStructureConstants.RELEVANT_RESOURCES).getAsJsonArray().get(relevantResourceNumber).getAsJsonObject()
				.get(ModelStructureConstants.TYPE).getAsString();
		return getResourceTypeNumber(typeName);
	}

	/* Searches */
	public final String getSearchName(int index) {
		return modelStructure.getAsJsonArray(ModelStructureConstants.SEARCHES).get(index).getAsJsonObject()
				.get(ModelStructureConstants.NAME).getAsString();
	}

	public final String getEdgeName(int searchIndex, int edgeIndex) {
		return modelStructure.getAsJsonArray(ModelStructureConstants.SEARCHES).get(searchIndex).getAsJsonObject()
				.get(ModelStructureConstants.EDGES).getAsJsonArray().get(edgeIndex).getAsJsonObject()
				.get(ModelStructureConstants.NAME).getAsString();
	}

	/* Results */
	public final String getResultName(int index) {
		return modelStructure.getAsJsonArray(ModelStructureConstants.RESULTS).get(index).getAsJsonObject()
				.get(ModelStructureConstants.NAME).getAsString();
	}
}
