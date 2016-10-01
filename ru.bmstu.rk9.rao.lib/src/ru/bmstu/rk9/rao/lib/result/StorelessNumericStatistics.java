package ru.bmstu.rk9.rao.lib.result;

import ru.bmstu.rk9.rao.lib.json.JSONObject;

public class StorelessNumericStatistics<T extends Number> extends Statistics<T> {
	@Override
	public void updateData(JSONObject data) {
		data.put("Mean", getMean());
		data.put("Standard deviation", getStandartDeviation());
		data.put("varcoef", getCoefficientOfVariation());
		data.put("Median", getMedian());
	}
	
	@Override
	public void update(T value, double currentTime) {
		next(value.doubleValue());
	}
	
	private int sum;

	private double mean;
	private double variance;

	private void next(double value) {
		double lastMean = mean;

		mean = 1d / ++sum * (value - lastMean) + lastMean;
		variance = variance + 1d * (value - lastMean) * (value - mean);
	}

	private double getMean() {
		return mean;
	}

	private double getStandartDeviation() {
		return Math.sqrt(variance / sum);
	}

	private double getCoefficientOfVariation() {
		return variance / sum / mean * 100d;
	}

	private double median;

	private double getMedian() {
		return median;
	}
}
