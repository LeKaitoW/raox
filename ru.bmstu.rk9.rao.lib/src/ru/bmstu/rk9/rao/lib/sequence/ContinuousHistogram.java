package ru.bmstu.rk9.rao.lib.sequence;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.MersenneTwister;
import org.eclipse.xtext.xbase.lib.Pair;

public class ContinuousHistogram implements NumericSequence {
	public class NumericHistogramBin {
		NumericHistogramBin(double start, double width, double weight, double offset) {
			this.start = start;
			this.width = width;
			this.weight = weight;
			this.offset = offset;
		}

		private final double start;
		private final double width;
		private final double weight;
		private final double offset;
	}

	// TODO add values validation
	public ContinuousHistogram(long seed, List<Pair<? extends Number, ? extends Number>> values) {
		this(seed, 0, values);
	}

	public ContinuousHistogram(long seed, double offset, List<Pair<? extends Number, ? extends Number>> values) {
		double currentOffset = offset;
		double binOffset = 0;
		adductionCoefficient = 0;

		for (Pair<? extends Number, ? extends Number> value : values) {
			double binEnd = value.getKey().doubleValue();
			double binWidth = binEnd - currentOffset;
			double binHeight = value.getValue().doubleValue();

			double weight = binWidth * binHeight;
			bins.add(new NumericHistogramBin(currentOffset, binWidth, weight, binOffset));
			currentOffset += binWidth;
			adductionCoefficient += binWidth * binHeight;
			binOffset += weight;
		}
	}

	@Override
	public Double next() {
		double binRandom = mersenneTwister.nextDouble();
		NumericHistogramBin selectedBin = null;

		for (NumericHistogramBin bin : bins) {
			if (bin.offset + bin.weight >= binRandom * adductionCoefficient) {
				selectedBin = bin;
				break;
			}
		}

		double valueRandom = mersenneTwister.nextDouble();
		double value = selectedBin.start + valueRandom * selectedBin.width;

		return value;
	}

	private final MersenneTwister mersenneTwister = new MersenneTwister(123456789);
	private final List<NumericHistogramBin> bins = new ArrayList<>();
	private double adductionCoefficient;
}
