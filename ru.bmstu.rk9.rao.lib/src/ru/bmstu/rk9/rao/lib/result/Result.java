package ru.bmstu.rk9.rao.lib.result;

import ru.bmstu.rk9.rao.lib.json.JSONObject;

public abstract class Result<T> {

	public abstract T evaluate();

	public abstract boolean condition();

	public abstract JSONObject getData();

}
