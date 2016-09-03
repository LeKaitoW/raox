package ru.bmstu.rk9.rao.lib.result;

import ru.bmstu.rk9.rao.lib.json.JSONObject;

public class ValueStatistics<T> extends Statistics<T> {

	@Override
	public void updateData(JSONObject data) {
		data.put("Value", value);
	}

	@Override
	public void update(T value, double currentTime) {
		this.value = value;	
	}
	
	private T value;
}
