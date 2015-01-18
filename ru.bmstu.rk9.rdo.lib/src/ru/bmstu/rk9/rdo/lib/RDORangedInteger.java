package ru.bmstu.rk9.rdo.lib;

public class RDORangedInteger
{
	private int lo;
	private int hi;

	public RDORangedInteger(int lo, int hi)
	{
		this.lo = lo;
		this.hi = hi;
	}

	private int value;

	public void set(int value) throws Exception
	{
		if(value > hi || value < lo)
			throw new Exception("Out of bounds");

		this.value = value;
	}

	public int get()
	{
		return value;
	}
}
