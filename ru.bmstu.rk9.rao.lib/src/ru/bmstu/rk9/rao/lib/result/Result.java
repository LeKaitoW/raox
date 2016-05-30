package ru.bmstu.rk9.rao.lib.result;

import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.naming.RaoNameable;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public abstract class Result<T> extends RaoNameable {

	public abstract T evaluate();

	public abstract boolean condition();

	public JSONObject getData() {
		JSONObject datasetData = new JSONObject();
		datasetData.put("name", getName());
		datasetData.put("type", this.getClass().getSimpleName());
		statistics.updateData(datasetData);
		return datasetData;
	};

	public void setStatistics(Statistics<T> statistics) {
		this.statistics = statistics;
	}

	public Statistics<T> getStatistics() {
		return statistics;
	};

	public void update() {
		if (condition()) {
			statistics.update(evaluate(), CurrentSimulator.getTime());
		}
	};

	protected ResultMode resultMode;

	protected Statistics<T> statistics;

}
