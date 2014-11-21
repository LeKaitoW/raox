package ru.bmstu.rk9.rdo.lib;

public interface Event extends Pattern
{
	public double getTime();
	public void run();
}
