package ru.bmstu.rk9.rao.lib.sequence;

import org.apache.commons.math3.random.MersenneTwister;

import ru.bmstu.rk9.rao.lib.exception.RaoLibException;

public class UniformSequence implements Sequence {
	private final Double a;
	private final Double b;
	private final MersenneTwister mersenneTwister;
	private final SequenceParametersType parametersType;

	public UniformSequence(long seed, double a, double b) {
		this.a = a;
		this.b = b;
		this.mersenneTwister = new MersenneTwister(seed);
		this.parametersType = SequenceParametersType.DEFINED_PARAMETERS;
	}

	public UniformSequence(long seed) {
		this.a = null;
		this.b = null;
		this.mersenneTwister = new MersenneTwister(seed);
		this.parametersType = SequenceParametersType.UNDEFINED_PARAMETERS;
	}

	@Override
	public Double next() {
		if (parametersType == SequenceParametersType.UNDEFINED_PARAMETERS) {
			throw new RaoLibException("Sequence parameters are undefined");
		}

		return (b - a) * mersenneTwister.nextDouble() + a;
	}

	public Double next(double a, double b) {
		return (b - a) * mersenneTwister.nextDouble() + a;
	}
}
