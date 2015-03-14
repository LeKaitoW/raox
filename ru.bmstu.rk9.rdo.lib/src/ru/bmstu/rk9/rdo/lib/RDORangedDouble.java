package ru.bmstu.rk9.rdo.lib;

public class RDORangedDouble
{
	private double lo;
	private double hi;

	public RDORangedDouble(double lo, double hi)
	{
		this.lo = lo;
		this.hi = hi;
	}

	private double value;

	public void set(double value) throws Exception
	{
		if(value > hi || value < lo)
			throw new Exception("Out of bounds");

		this.value = value;
	}

	public double get()
	{
		return value;
	}
}
