package ru.bmstu.rk9.rdo.lib;

import ru.bmstu.rk9.rdo.lib.json.JSONObject;

public interface Result extends Serializable
{
	public String getName();

	public void calculate();

	public JSONObject getData();
}
