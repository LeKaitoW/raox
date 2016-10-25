package ru.bmstu.rk9.rao.lib.result;

import ru.bmstu.rk9.rao.lib.json.JSONObject;

public class StorelessNumericStatistics<T extends Number> extends Statistics<T> {

	@Override
	public void updateData(JSONObject data) {
		data.put("Last value", getLastValue());
		data.put("Mean", getMean());
		data.put("Standard deviation", getStandartDeviation());
		data.put("varcoef", getCoefficientOfVariation());
	}

	@Override
	public void update(T value, double currentTime) {
		next(value.doubleValue());
	}

	private int sum = 0;
	private double mean = 0;
	private double variance;
	private double lastValue = Double.NaN;

	private final void next(double value) {
		double lastMean = mean;

		mean = 1d / ++sum * (value - lastMean) + lastMean;
		variance = variance + 1d * (value - lastMean) * (value - mean);
		lastValue = value;
	}

	private final double getMean() {
		return mean;
	}

	private final double getStandartDeviation() {
		return Math.sqrt(variance / sum);
	}

	private final double getCoefficientOfVariation() {
		return variance / sum / mean * 100d;
	}

	private final double getLastValue() {
		return lastValue;
	}
}
