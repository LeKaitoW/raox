package ru.bmstu.rk9.rao.lib.modelStructure;

import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class ActivityCache {
	ActivityCache(final JSONObject activity) {
		name = ModelStructureCache.getRelativeName(activity.getString("name"));

		final String patternName = activity.getString("pattern");
		JSONArray patterns = Simulator.getDatabase().getModelStructure().getJSONArray("patterns");
		// TODO throw exception if not found
		for (int num = 0; num < patterns.length(); num++)
			if (patternName.equals(patterns.getJSONObject(num).getString("name"))) {
				setPatternNumber(num);
				break;
			}
	}

	public int getPatternNumber() {
		return patternNumber;
	}

	public void setPatternNumber(int patternNumber) {
		this.patternNumber = patternNumber;
	}

	public String getName() {
		return name;
	}

	private final String name;
	private int patternNumber = -1;
}