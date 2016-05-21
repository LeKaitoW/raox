package ru.bmstu.rk9.rao.lib.result;

import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Function;

import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.Index;
import ru.bmstu.rk9.rao.lib.database.Database;
import ru.bmstu.rk9.rao.lib.database.Database.Entry;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public class Statistics {
	public static class Storeless {
		private int sum;

		private double mean;
		private double variance;

		public void next(double value) {
			double lastMean = mean;

			mean = 1d / ++sum * (value - lastMean) + lastMean;
			variance = variance + 1d * (value - lastMean) * (value - mean);
		}

		public double getMean() {
			return mean;
		}

		public double getStandartDeviation() {
			return Math.sqrt(variance / sum);
		}

		public double getCoefficientOfVariation() {
			return variance / sum / mean * 100d;
		}

		private double median;

		public double getMedian() {
			return median;
		}
	}

	public static class WeightedStoreless {
		private boolean started = false;

		private double lastValue;

		private double lastWeight;

		private double weightSum;

		private double mean;
		private double variance;

		// mind that this is a weighted variance

		public void next(double nextWeight, double nextValue) {
			if (started) {
				double x = lastValue;
				double weight = nextWeight - lastWeight;

				if (weight != 0) {
					double lastMean = mean;

					weightSum += weight;

					mean = weight / weightSum * (x - lastMean) + lastMean;
					variance = variance + weight * (x - lastMean) * (x - mean);
				}
			} else {
				mean = 0;
				weightSum = 0;
				variance = 0;

				started = true;
			}

			lastValue = nextValue;
			lastWeight = nextWeight;
		}

		public double getMean() {
			return mean;
		}

		public double getStandartDeviation() {
			return Math.sqrt(variance / weightSum);
		}

		public double getCoefficientOfVariation() {
			return variance / weightSum / mean * 100d;
		}

		private double median;

		public double getMedian() {
			return median;
		}
	}

	public static class LogicStoreless {
		private double minFalse = Double.MAX_VALUE;
		private double maxFalse = Double.MIN_VALUE;

		private double minTrue = Double.MAX_VALUE;
		private double maxTrue = Double.MIN_VALUE;

		private double timeFalse = 0;
		private double timeTrue = 0;

		public void addState(boolean value, double delta) {
			if (value) {
				if (delta > maxTrue)
					maxTrue = delta;
				if (delta < minTrue)
					minTrue = delta;
				timeTrue += delta;
			} else {
				if (delta > maxFalse)
					maxFalse = delta;
				if (delta < minFalse)
					minFalse = delta;
				timeFalse += delta;
			}
		}

		public double getMinFalse() {
			return minFalse;
		}

		public double getMinTrue() {
			return minTrue;
		}

		public double getMaxFalse() {
			return maxFalse;
		}

		public double getMaxTrue() {
			return maxTrue;
		}

		public double getPercent() {
			return timeTrue / (timeFalse + timeTrue);
		}
	}
}
