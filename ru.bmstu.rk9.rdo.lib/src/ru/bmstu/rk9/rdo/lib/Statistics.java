package ru.bmstu.rk9.rdo.lib;

import java.util.Iterator;

import java.util.PriorityQueue;

import java.util.function.Function;

public class Statistics
{
	public static class WeightedStoreless
	{
		private boolean started = false;

		private double lastValue;

		private double lastWeight;

		private double weightSum;

		private double mean;
		private double variance;
			// mind that this is a weighted variance

		public void next(double nextWeight, double nextValue)
		{
			if(started)
			{
				double x = lastValue;
				double weight = nextWeight - lastWeight;

				if(weight != 0)
				{
					double lastMean = mean;
	
					weightSum += weight;
	
					mean = weight / weightSum * (x - lastMean) + lastMean;
					variance = variance + weight * (x - lastMean) * (x - mean);
				}
			}
			else
			{
				mean = 0;
				weightSum = 0;
				variance = 0;

				started = true;
			}

			lastValue = nextValue;
			lastWeight = nextWeight;
		}

		public double getMean()
		{
			return mean;
		}

		public double getStandartDeviation()
		{
			return Math.sqrt(variance / weightSum);
		}

		public double getCoefficientOfVariation()
		{
			return variance / weightSum / mean * 100d;
		}

		private double median;

		public boolean initFromDatabase(Result result)
		{
			Database database = Simulator.getDatabase();
			Database.Index resultIndex = database.resultIndex.get(result.getName());

			if(resultIndex != null && resultIndex.entries.size() > 0)
			{
				Function<Integer, Double> getValue =
					result.getData().getString("value_type").equals("real")
						? i -> database.allEntries.get(i).data.getDouble(0)
						: i -> (double)database.allEntries.get(i).data.getInt(0);

				PriorityQueue<Integer> queue = new PriorityQueue<Integer>
				(
					resultIndex.entries.size(),
					(a, b) -> getValue.apply(a).compareTo(getValue.apply(b))
				);

				queue.addAll(resultIndex.entries);

				double tempSum = weightSum; 

				int number = -1;
				Iterator<Integer> iterator = queue.iterator();
				while(tempSum > weightSum / 2)
				{
					number = iterator.next();

					double weight =
						database.allEntries.get(number + 1).header
							.getDouble(Database.TypeSize.Internal.TIME_OFFSET) -
						database.allEntries.get(number).header
							.getDouble(Database.TypeSize.Internal.TIME_OFFSET);

					tempSum -= weight;
				}

				median = getValue.apply(number);

				return true;
			}

			return false;
		}

		public double getMedian()
		{
			return median;
		}
	}
}
