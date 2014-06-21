package ru.bmstu.rk9.rdo.lib;

public interface Converter<R, P>
{
	public void run(R resources, P parameters);
}
