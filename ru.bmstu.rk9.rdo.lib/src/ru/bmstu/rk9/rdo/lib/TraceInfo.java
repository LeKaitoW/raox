package ru.bmstu.rk9.rdo.lib;

import java.util.HashMap;

public class TraceInfo
{
	private HashMap<Serializable, Boolean> traceStateList =
		new HashMap<Serializable, Boolean>();

	public void setTraceState(Serializable object, boolean state)
	{
		traceStateList.put(object, state);
	}

	public boolean isBeingTraced(Serializable object)
	{
		Boolean state = traceStateList.get(object);
		return state == null ? false : state;
	}
}
