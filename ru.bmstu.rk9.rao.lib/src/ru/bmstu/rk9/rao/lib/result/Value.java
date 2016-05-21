package ru.bmstu.rk9.rao.lib.result;

import ru.bmstu.rk9.rao.lib.json.JSONObject;

public abstract class Value<T> extends Result<T> {

	public abstract T evaluate();

	public abstract boolean condition();

	public JSONObject getData() {
		JSONObject valueData = new JSONObject();
		valueData.put("value", evaluate());
		return valueData;
	};
}
