package ru.bmstu.rk9.rao.lib.modelStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;

public class ResourceTypeCache {
	ResourceTypeCache(final JSONObject resourceType) {
		name = ModelStructureCache.getRelativeName(resourceType
				.getString("name"));
		temporary = resourceType.getBoolean("temporary");

		JSONObject structure = resourceType.getJSONObject("structure");
		JSONArray parameters = structure.getJSONArray("parameters");
		numberOfParameters = parameters.length();

		paramTypes = new ArrayList<ValueCache>();
		indexList = new HashMap<Integer, Integer>();
		for (int num = 0; num < getNumberOfParameters(); num++) {
			final JSONObject currentParameter = parameters.getJSONObject(num);
			ModelStructureCache.ValueType type = ModelStructureCache.ValueType
					.get(currentParameter.getString("type"));
			getParamTypes().add(new ValueCache(currentParameter));
			if (type == ModelStructureCache.ValueType.STRING) {
				getIndexList().put(num, currentParameter.getInt("index"));
			}
		}
		finalOffset = structure.getInt("last_offset");
	}

	public int getNumberOfParameters() {
		return numberOfParameters;
	}

	public List<ValueCache> getParamTypes() {
		return paramTypes;
	}

	public HashMap<Integer, Integer> getIndexList() {
		return indexList;
	}

	public int getFinalOffset() {
		return finalOffset;
	}

	public String getName() {
		return name;
	}

	private final String name;
	final boolean temporary;
	private final int numberOfParameters;
	private final List<ValueCache> paramTypes;
	private final HashMap<Integer, Integer> indexList;
	private final int finalOffset;
}