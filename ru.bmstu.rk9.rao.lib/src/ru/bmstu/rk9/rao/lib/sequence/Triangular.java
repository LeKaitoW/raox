package ru.bmstu.rk9.rao.lib.sequence;

import org.apache.commons.math3.random.MersenneTwister;

import ru.bmstu.rk9.rao.lib.exception.RaoLibException;

public class Triangular implements NumericSequence {
	private final Double a;
	private final Double b;
	private final Double c;
	private final MersenneTwister mersenneTwister;
	private final SequenceParametersType parametersType;

	public Triangular(long seed, double a, double b, double c) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.mersenneTwister = new MersenneTwister(seed);
		this.parametersType = SequenceParametersType.DEFINED_PARAMETERS;
	}

	public Triangular(long seed) {
		this.a = null;
		this.b = null;
		this.c = null;
		this.mersenneTwister = new MersenneTwister(seed);
		this.parametersType = SequenceParametersType.UNDEFINED_PARAMETERS;
	}

	@Override
	public Double next() {
		if (parametersType == SequenceParametersType.UNDEFINED_PARAMETERS) {
			throw new RaoLibException("Sequence parameters are undefined");
		}

		return next(a, b, c);
	}

	public Double next(double a, double b, double c) {
		double next = mersenneTwister.nextDouble();
		double edge = (double) (c - a) / (double) (b - a);

		if (next < edge)
			return a + Math.sqrt((b - a) * (c - a) * next);
		else
			return b - Math.sqrt((1 - next) * (b - a) * (b - c));
	}
}
