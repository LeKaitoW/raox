package ru.bmstu.rk9.rdo.lib;

import java.util.Iterator;

import java.util.PriorityQueue;

import java.util.function.Function;

public class Statistics
{
	public static class Storeless
	{
		private int sum;

		private double mean;
		private double variance;

		public void next(double value)
		{
			double lastMean = mean;
	
			mean = 1d / ++sum * (value - lastMean) + lastMean;
			variance = variance + 1d * (value - lastMean) * (value - mean);
		}

		public double getMean()
		{
			return mean;
		}

		public double getStandartDeviation()
		{
			return Math.sqrt(variance / sum);
		}

		public double getCoefficientOfVariation()
		{
			return variance / sum / mean * 100d;
		}

		private double median;

		public boolean initFromDatabase(Result result)
		{
			Database database = Simulator.getDatabase();
			Database.Index resultIndex = database.resultIndex.get(result.getName());

			if(resultIndex != null && !resultIndex.entries.isEmpty())
			{
				Function<Integer, Double> getValue =
					result.getData().getString("valueType").equals("real")
						? i -> database.getAllEntries().get(i).data.getDouble(0)
						: i -> (double)database.getAllEntries().get(i).data.getInt(0);

				PriorityQueue<Integer> queue = new PriorityQueue<Integer>
				(
					resultIndex.entries.size(),
					(a, b) -> getValue.apply(a).compareTo(getValue.apply(b))
				);

				queue.addAll(resultIndex.entries);

				int size = queue.size(), value = -1;

				Iterator<Integer> iter = queue.iterator();

				for(int i = 0; i < size / 2; i++, value = iter.next());

				if(size % 2 == 0)
					median = (getValue.apply(value) +
						getValue.apply(iter.next())) / 2; 
				else
					median = getValue.apply(iter.next());
					
				return true;
			}

			return false;
		}

		public double getMedian()
		{
			return median;
		}
	}

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

			if(resultIndex != null && !resultIndex.entries.isEmpty())
			{
				Function<Integer, Double> getValue =
					result.getData().getString("valueType").equals("real")
						? i -> database.getAllEntries().get(i).data.getDouble(0)
						: i -> (double)database.getAllEntries().get(i).data.getInt(0);

				PriorityQueue<Integer> queue = new PriorityQueue<Integer>
				(
					resultIndex.entries.size(),
					(a, b) -> getValue.apply(resultIndex.entries.get(a))
						.compareTo(getValue.apply(resultIndex.entries.get(b)))
				);

				for(int i = 0; i < resultIndex.entries.size(); i++)
					queue.add(i);

				double tempSum = weightSum; 

				int number = -1, previousN = -1;
				double weight = 0, previousW = 0;
				Iterator<Integer> iterator = queue.iterator();
				while(tempSum >= weightSum / 2)
				{
					previousN = number;
					number = iterator.next();

					previousW = weight;
					weight =
						database.getAllEntries().get(resultIndex.entries.get(number + 1))
							.header.getDouble(Database.TypeSize.Internal.TIME_OFFSET) -
						database.getAllEntries().get(resultIndex.entries.get(number))
							.header.getDouble(Database.TypeSize.Internal.TIME_OFFSET);

					tempSum -= weight;
				}

				double value = getValue.apply(resultIndex.entries.get(number));
				double previous = getValue.apply(resultIndex.entries.get(previousN));

				median = 2d * (value - previous) / (weight + previousW) *
					((previousW / 2 + weight) - (weightSum / 2 - tempSum)) + previous;

				return true;
			}

			return false;
		}

		public double getMedian()
		{
			return median;
		}
	}

	public static class LogicStoreless
	{
		private double minFalse = Double.MAX_VALUE;
		private double maxFalse = Double.MIN_VALUE;

		private double minTrue = Double.MAX_VALUE;
		private double maxTrue = Double.MIN_VALUE;
		
		private double timeFalse = 0;
		private double timeTrue = 0;

		public void addState(boolean value, double delta)
		{
			if(value)
			{
				if(delta > maxTrue)
					maxTrue = delta;
				if(delta < minTrue)
					minTrue = delta;
				timeTrue += delta;
			}
			else
			{
				if(delta > maxFalse)
					maxFalse = delta;
				if(delta < minFalse)
					minFalse = delta;
				timeFalse += delta;
			}
		}

		public double getMinFalse()
		{
			return minFalse;
		}

		public double getMinTrue()
		{
			return minTrue;
		}

		public double getMaxFalse()
		{
			return maxFalse;
		}

		public double getMaxTrue()
		{
			return maxTrue;
		}

		public double getPercent()
		{
			return timeTrue / (timeFalse + timeTrue);
		}
	}
}
