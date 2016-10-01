package ru.bmstu.rk9.rao.lib.sequence;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.MersenneTwister;
import org.eclipse.xtext.xbase.lib.Pair;

public class DiscreteHistogram<T> implements ArbitraryTypeSequence<T> {
	public class DiscreteHistogramBin {
		DiscreteHistogramBin(T value, double weight) {
			this.weight = weight;
			this.value = value;
		}

		private final T value;
		private final double weight;
	}

	public DiscreteHistogram(long seed, List<Pair<T, ? extends Number>> values) {
		adductionCoefficient = 0;

		for (Pair<T, ? extends Number> value : values) {
			T val = value.getKey();
			Double binHeight = value.getValue().doubleValue();

			bins.add(new DiscreteHistogramBin(val, binHeight));
			adductionCoefficient += binHeight;
		}
	}

	@Override
	public T next() {
		double offset = 0;
		double binRandom = mersenneTwister.nextDouble();
		DiscreteHistogramBin selectedBin = null;

		for (DiscreteHistogramBin bin : bins) {
			if (offset + bin.weight >= binRandom * adductionCoefficient) {
				selectedBin = bin;
				break;
			}
			offset += bin.weight;
		}

		return selectedBin.value;
	}

	private final MersenneTwister mersenneTwister = new MersenneTwister(123456789);
	private final List<DiscreteHistogramBin> bins = new ArrayList<>();
	private double adductionCoefficient;
}
