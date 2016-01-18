package ru.bmstu.rk9.rao.lib.sequence;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.special.Erf;

import ru.bmstu.rk9.rao.lib.exception.RaoLibException;

public class NormalSequence implements Sequence {
	private final Double mean;
	private final Double variance;
	private final MersenneTwister mersenneTwister;
	private final SequenceParametersType parametersType;

	public NormalSequence(long seed, double mean, double variance) {
		this.mean = mean;
		this.variance = variance;
		this.mersenneTwister = new MersenneTwister(seed);
		this.parametersType = SequenceParametersType.DEFINED_PARAMETERS;
	}

	public NormalSequence(long seed) {
		this.mean = null;
		this.variance = null;
		this.mersenneTwister = new MersenneTwister(seed);
		this.parametersType = SequenceParametersType.UNDEFINED_PARAMETERS;
	}

	@Override
	public Double next() {
		if (parametersType == SequenceParametersType.UNDEFINED_PARAMETERS) {
			throw new RaoLibException("Sequence parameters are undefined");
		}

		return next(mean, variance);
	}

	public Double next(double mean, double variance) {
		return (mean + variance * Math.sqrt(2) * Erf.erfInv(2 * mersenneTwister.nextDouble() - 1));
	}
}
