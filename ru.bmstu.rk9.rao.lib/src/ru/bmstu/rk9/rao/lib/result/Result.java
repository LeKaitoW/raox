package ru.bmstu.rk9.rao.lib.result;

import ru.bmstu.rk9.rao.lib.database.Serializable;
import ru.bmstu.rk9.rao.lib.json.JSONObject;

public interface Result extends Serializable {
	public String getName();

	public void calculate();

	public JSONObject getData();
}
