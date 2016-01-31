package ru.bmstu.rk9.rao.lib.sequence;

import org.apache.commons.math3.random.MersenneTwister;

import ru.bmstu.rk9.rao.lib.exception.RaoLibException;

public class Exponential implements NumericSequence {
	private final Double rate;
	private final MersenneTwister mersenneTwister;
	private final SequenceParametersType parametersType;

	public Exponential(long seed, double rate) {
		this.rate = rate;
		this.mersenneTwister = new MersenneTwister(seed);
		this.parametersType = SequenceParametersType.DEFINED_PARAMETERS;
	}

	public Exponential(long seed) {
		this.rate = null;
		this.mersenneTwister = new MersenneTwister(seed);
		this.parametersType = SequenceParametersType.UNDEFINED_PARAMETERS;
	}

	@Override
	public Double next() {
		if (parametersType == SequenceParametersType.UNDEFINED_PARAMETERS) {
			throw new RaoLibException("Sequence parameters are undefined");
		}

		return next(rate);
	}

	public Double next(double rate) {
		return -1.0 / rate * Math.log(1 - mersenneTwister.nextDouble());
	}
}
