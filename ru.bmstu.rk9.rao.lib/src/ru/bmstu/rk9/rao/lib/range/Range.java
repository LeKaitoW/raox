package ru.bmstu.rk9.rao.lib.range;

import ru.bmstu.rk9.rao.lib.sequence.NumericSequence;

public class Range implements NumericSequence {
	private final double start;
	private final double stop;
	private final double step;
	private double current;
	
	public Range(double start, double stop, double step) {
		this.start = start;
		this.stop = stop;
		this.step = step;
		this.current = start;
	}
	
	@Override
	public Double next() {
		current += step;
		if (current < stop ) {
			return (current);
		}
		return (-1.);
	}
}
