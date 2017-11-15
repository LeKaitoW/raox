package ru.bmstu.rk9.rao.lib.result;

import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.naming.RaoNameable;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public class AbstractResult<T> extends RaoNameable {

	public AbstractResult(Statistics<T> statistics) {
		this.statistics = statistics;
	}

	public final JSONObject getData() {
		JSONObject datasetData = new JSONObject();
		datasetData.put("name", getName());
		statistics.updateData(datasetData);
		return datasetData;
	};

	public final void update(T value, double time) {
		statistics.update(value, time);
		CurrentSimulator.getDatabase().addResultEntry(this, value);
	};

	public final void prepareData() {
		statistics.prepareData();
	}

	protected final Statistics<T> statistics;
}
