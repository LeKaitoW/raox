package ru.bmstu.rk9.rao.lib.modelStructure;

import java.util.ArrayList;
import java.util.List;

import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;

public class DecisionPointCache {
	DecisionPointCache(final JSONObject dpt) {
		name = ModelStructureCache.getRelativeName(dpt.getString("name"));

		activitiesInfo = new ArrayList<ActivityCache>();
		JSONArray activities = dpt.getJSONArray("activities");
		for (int num = 0; num < activities.length(); num++)
			getActivitiesInfo()
					.add(new ActivityCache(activities.getJSONObject(num)));
	}

	final String name;
	private final List<ActivityCache> activitiesInfo;

	public String getName() {
		return name;
	}

	public List<ActivityCache> getActivitiesInfo() {
		return activitiesInfo;
	}
}