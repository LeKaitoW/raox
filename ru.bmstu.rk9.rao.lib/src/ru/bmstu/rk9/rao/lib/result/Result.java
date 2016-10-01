package ru.bmstu.rk9.rao.lib.result;

import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.naming.RaoNameable;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public abstract class Result<T> extends RaoNameable {

	public abstract T evaluate();

	public boolean condition() {
		return true;
	};

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
		if (!condition()) 
			return;
		final T value = evaluate();
		statistics.update(value, CurrentSimulator.getTime());
		CurrentSimulator.getDatabase().addResultEntry(this, value);
	};

	protected ResultMode resultMode;

	protected Statistics<T> statistics;

}
