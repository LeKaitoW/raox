package ru.bmstu.rk9.rao.lib.modelStructure;

import java.util.HashMap;

import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class PatternCache {
	PatternCache(final JSONObject pattern) {
		name = ModelStructureCache.getRelativeName(pattern.getString("name"));
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
			getRelResTypes().put(num, typeNum);
		}
	}

	public HashMap<Integer, Integer> getRelResTypes() {
		return relResTypes;
	}

	final String name;
	private final HashMap<Integer, Integer> relResTypes;
}