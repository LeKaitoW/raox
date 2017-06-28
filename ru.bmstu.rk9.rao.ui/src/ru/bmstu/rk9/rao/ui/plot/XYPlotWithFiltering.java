package ru.bmstu.rk9.rao.ui.plot;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.RendererUtilities;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYDataset;

public class XYPlotWithFiltering extends XYPlot {
	private static final long serialVersionUID = 7074889297801956906L;
	private ProxyDataSet proxyDataSet = new ProxyDataSet();

	public XYPlotWithFiltering() {
		super();
	}

	public XYPlotWithFiltering(XYDataset dataset, ValueAxis domainAxis, ValueAxis rangeAxis, XYItemRenderer renderer) {
		super(dataset, domainAxis, rangeAxis, renderer);
	}

	public ProxyDataSet getProxyDataSet() {
		return proxyDataSet;
	}

	public class ProxyDataSet {
		final LinkedHashMap<Integer, Integer> filteredMap = new LinkedHashMap<>();

		public Map<Integer, Integer> getFilteredMap() {
			return filteredMap;
		}

		double increment = 1.0;
		int previousFirst;
		int previousLast;
		int previousPartCount = 0;
		int currentPartCount = 1;

		void setPlotWidth(int width) {
			int newPartCount = width / 2;
			if (((double) Math.max(currentPartCount, newPartCount))
					/ ((double) Math.min(currentPartCount, newPartCount)) > 2)
				this.currentPartCount = newPartCount;
		}

		public Map<Integer, Integer> workAsProxy(XYDataset items, int seriesIndex, int firstItemIndex,
				int lastItemIndex) {
			if (previousFirst == firstItemIndex && previousLast == lastItemIndex
					&& previousPartCount == currentPartCount)
				return filteredMap;

			filteredMap.clear();
			if (lastItemIndex - firstItemIndex < currentPartCount)
				return filteredMap;

			int previousFiltered = firstItemIndex;
			double groupSize = (lastItemIndex - firstItemIndex) / (double) currentPartCount;

			previousFirst = firstItemIndex;
			previousLast = lastItemIndex;
			previousPartCount = currentPartCount;

			for (int groupIndex = 0; groupIndex < currentPartCount; groupIndex++) {
				int firstGroupItemIndex = (int) (firstItemIndex + (groupSize * groupIndex));
				int lastGroupItemIndex = (int) (firstItemIndex + (groupSize * (groupIndex + 1)));
				double sumValue = 0.0;
				for (int groupItemIndex = firstGroupItemIndex; groupItemIndex < lastGroupItemIndex; groupItemIndex++) {
					sumValue += items.getYValue(seriesIndex, groupItemIndex);
				}
				sumValue = sumValue / (lastGroupItemIndex - firstGroupItemIndex);
				double nearestPlotItem = Double.MAX_VALUE;
				int nearestIndex = firstGroupItemIndex;

				for (int groupItemIndex = firstGroupItemIndex; groupItemIndex < lastGroupItemIndex; groupItemIndex++) {
					double nearestCandidate = Math.abs(items.getYValue(seriesIndex, groupItemIndex) - sumValue);
					if (nearestCandidate < nearestPlotItem) {
						nearestIndex = groupItemIndex;
						nearestPlotItem = nearestCandidate;
					}
				}
				filteredMap.put(nearestIndex, previousFiltered);
				previousFiltered = nearestIndex;
			}
			return filteredMap;
		}

		public void reset() {
			filteredMap.clear();
			previousFirst = -1;
			previousLast = -1;
		}
	}

	@Override
	public boolean render(Graphics2D graphics2D, Rectangle2D dataArea, int datasetIndex, PlotRenderingInfo info,
			CrosshairState crosshairState) {
		boolean hasData = false;
		final XYDataset dataset = getDataset(datasetIndex);

		if (!DatasetUtilities.isEmptyOrNull(dataset)) {
			hasData = true;
			final ValueAxis xAxis = getDomainAxisForDataset(datasetIndex);
			final ValueAxis yAxis = getRangeAxisForDataset(datasetIndex);
			if (xAxis == null || yAxis == null)
				return hasData;

			XYItemRenderer renderer = getRenderer(datasetIndex);

			if (renderer == null) {
				renderer = getRenderer();
				if (renderer == null)
					return hasData;
			}

			proxyDataSet.setPlotWidth(dataArea.getBounds().width);
			final XYItemRendererState state = renderer.initialise(graphics2D, dataArea, this, dataset, info);
			int passCount = renderer.getPassCount();
			final SeriesRenderingOrder seriesOrder = getSeriesRenderingOrder();

			if (seriesOrder == SeriesRenderingOrder.REVERSE) {
				for (int passIndex = 0; passIndex < passCount; passIndex++) {
					int seriesCount = dataset.getSeriesCount();
					for (int seriesIndex = seriesCount - 1; seriesIndex >= 0; seriesIndex--) {
						int firstItem = 0;
						int lastItem = dataset.getItemCount(seriesIndex) - 1;

						if (lastItem == -1)
							continue;

						if (state.getProcessVisibleItemsOnly()) {
							int[] itemBounds = RendererUtilities.findLiveItems(dataset, seriesIndex,
									xAxis.getLowerBound(), xAxis.getUpperBound());
							firstItem = Math.max(itemBounds[0] - 1, 0);
							lastItem = Math.min(itemBounds[1] + 1, lastItem);
						}
						state.startSeriesPass(dataset, seriesIndex, firstItem, lastItem, passIndex, passCount);
						Map<Integer, Integer> filteredMap = proxyDataSet.workAsProxy(dataset, seriesIndex, firstItem,
								lastItem);

						if (renderer instanceof XYFilteringStepRenderer)
							((XYFilteringStepRenderer) renderer).setFilteredMap(filteredMap);

						if (filteredMap.size() == 0 || !(renderer instanceof XYFilteringStepRenderer)) {
							for (int itemIndex = firstItem; itemIndex <= lastItem; itemIndex++) {
								renderer.drawItem(graphics2D, state, dataArea, info, this, xAxis, yAxis, dataset,
										seriesIndex, itemIndex, crosshairState, passIndex);
							}
						} else {
							for (Integer itemIndex : filteredMap.keySet()) {
								renderer.drawItem(graphics2D, state, dataArea, info, this, xAxis, yAxis, dataset,
										seriesIndex, itemIndex, crosshairState, passIndex);
							}
						}
						state.endSeriesPass(dataset, seriesIndex, firstItem, lastItem, passIndex, passCount);
					}
				}
			} else {
				for (int pass = 0; pass < passCount; pass++) {
					int seriesCount = dataset.getSeriesCount();
					for (int series = 0; series < seriesCount; series++) {
						int firstItem = 0;
						int lastItem = dataset.getItemCount(series) - 1;
						if (lastItem == -1)
							continue;

						if (state.getProcessVisibleItemsOnly()) {
							int[] itemBounds = RendererUtilities.findLiveItems(dataset, series, xAxis.getLowerBound(),
									xAxis.getUpperBound());
							firstItem = Math.max(itemBounds[0] - 1, 0);
							lastItem = Math.min(itemBounds[1] + 1, lastItem);
						}
						state.startSeriesPass(dataset, series, firstItem, lastItem, pass, passCount);

						for (int itemIndex = firstItem; itemIndex <= lastItem; itemIndex++) {
							renderer.drawItem(graphics2D, state, dataArea, info, this, xAxis, yAxis, dataset, series,
									itemIndex, crosshairState, pass);
						}
						state.endSeriesPass(dataset, series, firstItem, lastItem, pass, passCount);
					}
				}
			}
		}
		return hasData;
	}
}
