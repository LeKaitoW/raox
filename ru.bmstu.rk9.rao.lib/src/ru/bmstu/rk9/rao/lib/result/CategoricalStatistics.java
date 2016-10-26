package ru.bmstu.rk9.rao.lib.result;

import java.util.HashMap;
import java.util.Map;

import ru.bmstu.rk9.rao.lib.json.JSONObject;

public class CategoricalStatistics<T> extends Statistics<T> {

	private class StatisticsData {
		double valueMinTime;
		double valueMaxTime;
		double valueTime;
	}

	@Override
	public void updateData(JSONObject data) {
		if (lastValue != null)
			addState(lastValue, lastCurrentTime - lastFlipTime);
		for (Map.Entry<T, StatisticsData> e : statisticsDataset.entrySet()) {
			StatisticsData statisticsData = e.getValue();
			data.put("Longest \"" + e.getKey().toString() + "\"", statisticsData.valueMaxTime);
			data.put("Shortest \"" + e.getKey().toString() + "\"", statisticsData.valueMinTime);
			data.put("Time of \"" + e.getKey().toString() + "\"", statisticsData.valueTime);
			data.put("Persent of \"" + e.getKey().toString() + "\"", statisticsData.valueTime / fullTime);
		}
	}

	@Override
	public void update(T value, double currentTime) {
		if (value == null)
			return;

		lastCurrentTime = currentTime;

		if (value.equals(lastValue))
			return;

		if (!statisticsDataset.containsKey(value)) {
			StatisticsData data = new StatisticsData();
			data.valueMaxTime = Double.MIN_VALUE;
			data.valueMinTime = Double.MAX_VALUE;
			data.valueTime = 0;
			statisticsDataset.put(value, data);
		}

		if (lastValue != null) {
			addState(lastValue, currentTime - lastFlipTime);
		}

		lastValue = value;
		lastFlipTime = currentTime;
	}

	private T lastValue = null;
	private double lastFlipTime = 0;
	private double lastCurrentTime = 0;
	private double fullTime = 0;

	private final Map<T, StatisticsData> statisticsDataset = new HashMap<T, StatisticsData>();

	private final void addState(T value, double delta) {
		fullTime += delta;
		StatisticsData data = statisticsDataset.get(value);
		if (data.valueMaxTime < delta) {
			data.valueMaxTime = delta;
		}
		if (delta < data.valueMinTime) {
			data.valueMinTime = delta;
		}
		data.valueTime += delta;
		statisticsDataset.put(value, data);
	}
}
