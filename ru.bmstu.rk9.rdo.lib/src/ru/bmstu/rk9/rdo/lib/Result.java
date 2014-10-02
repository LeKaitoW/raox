package ru.bmstu.rk9.rdo.lib;

public interface Result extends Serializable
{
	public static enum Type
	{
		WATCH_PAR, WATCH_STATE, WATCH_QUANT, WATCH_VALUE, GET_VALUE
	}

	public String getName();
	public Type getType();
	public String get();
}
