package ru.bmstu.rk9.rao.lib.sequence;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.random.MersenneTwister;

import ru.bmstu.rk9.rao.lib.exception.RaoLibException;

public class Poisson implements NumericSequence {
	private final Double rate;
	private final MersenneTwister mersenneTwister;
	private final SequenceParametersType parametersType;
	private Map<Double, PoissonDistribution> distributions = new HashMap<Double, PoissonDistribution>();

	public Poisson(long seed, double rate) {
		this.rate = rate;
		this.mersenneTwister = new MersenneTwister(seed);
		this.distributions.put(rate, createDistribution(rate));
		this.parametersType = SequenceParametersType.DEFINED_PARAMETERS;
	}

	public Poisson(long seed) {
		this.rate = null;
		this.mersenneTwister = new MersenneTwister(seed);
		this.parametersType = SequenceParametersType.UNDEFINED_PARAMETERS;
	}

	private final PoissonDistribution createDistribution(double rate) {
		return new PoissonDistribution(mersenneTwister, rate, PoissonDistribution.DEFAULT_EPSILON,
				PoissonDistribution.DEFAULT_MAX_ITERATIONS);
	}

	@Override
	public Double next() {
		if (parametersType == SequenceParametersType.UNDEFINED_PARAMETERS)
			throw new RaoLibException("Sequence parameters are undefined");

		return (double) distributions.get(rate).sample();
	}

	public Double next(double rate) {
		PoissonDistribution distribution = distributions.get(rate);

		if (distribution == null) {
			distribution = createDistribution(rate);
			distributions.put(rate, distribution);
		}

		return (double) distribution.sample();
	}
}
