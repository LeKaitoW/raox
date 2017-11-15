package ru.bmstu.rk9.rao.lib.result;

import ru.bmstu.rk9.rao.lib.json.JSONObject;

public abstract class Statistics<T> {
	public abstract void updateData(JSONObject data);

	public abstract void update(T value, double currentTime);

	public void prepareData() {
	};
}
