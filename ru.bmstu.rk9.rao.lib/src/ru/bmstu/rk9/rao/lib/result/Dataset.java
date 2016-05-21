package ru.bmstu.rk9.rao.lib.result;

import ru.bmstu.rk9.rao.lib.json.JSONObject;

public abstract class Dataset<T> extends Result<T> {

	public abstract T evaluate();

	public abstract boolean condition();

	public JSONObject getData() {
		JSONObject datasetData = new JSONObject();
		datasetData.put("dataset", evaluate());
		return datasetData;
	};
}
