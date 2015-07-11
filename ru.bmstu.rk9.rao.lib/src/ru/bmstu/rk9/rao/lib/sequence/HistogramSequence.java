package ru.bmstu.rk9.rao.lib.sequence;

public class HistogramSequence {
	public HistogramSequence(double[] values, double[] weights) {
		this.values = values;
		this.weights = weights;

		this.range = new double[weights.length];

		calculateSum();
		calculateRange();
	}

	private double[] values;
	private double[] weights;

	private double sum = 0;
	private double[] range;

	private void calculateSum() {
		for (int i = 0; i < weights.length; i++)
			sum += weights[i] * (values[i + 1] - values[i]);
	}

	private void calculateRange() {
		double crange = 0;
		for (int i = 0; i < weights.length; i++) {
			crange += (weights[i] * (values[i + 1] - values[i])) / sum;
			range[i] = crange;
		}
	}

	public double calculateValue(double rand) {
		double x = values[0];

		for (int i = 0; i < range.length; i++)
			if (range[i] <= rand)
				x = values[i + 1];
			else {
				x += (sum / weights[i]) * (rand - (i > 0 ? range[i - 1] : 0));
				break;
			}

		return x;
	}
}
