package ru.bmstu.rk9.rao.lib;

public class RangedDouble {
	private double lo;
	private double hi;

	public RangedDouble(double lo, double hi) {
		this.lo = lo;
		this.hi = hi;
	}

	private double value;

	public void set(double value) throws Exception {
		if (value > hi || value < lo)
			throw new Exception("Out of bounds");

		this.value = value;
	}

	public double get() {
		return value;
	}
}
