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

	public XYPlotWithFiltering() {
		super();
	}

	public XYPlotWithFiltering(XYDataset dataset, ValueAxis domainAxis, ValueAxis rangeAxis, XYItemRenderer renderer) {
		super(dataset, domainAxis, rangeAxis, renderer);
	}

	private ProxyDataSet proxy = new ProxyDataSet();

	public ProxyDataSet getProxyDataSet() {
		return proxy;
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
					/ ((double) Math.min(currentPartCount, newPartCount)) > 2) {
				this.currentPartCount = newPartCount;
			}
		}

		public Map<Integer, Integer> workAsProxy(XYDataset items, int series, int first, int last) {
			if (previousFirst == first && previousLast == last && previousPartCount == currentPartCount) {
				return filteredMap;
			}
			filteredMap.clear();
			if (last - first < currentPartCount) {
				return filteredMap;
			}
			int prevFiltered = first;
			double groupSize = (last - first) / (double) currentPartCount;

			previousFirst = first;
			previousLast = last;
			previousPartCount = currentPartCount;

			for (int i = 0; i < currentPartCount; i++) {
				int innerBegin = (int) (first + (groupSize * i));
				int innerEnd = (int) (first + (groupSize * (i + 1)));
				double sumVal = 0.0;
				for (int inner = innerBegin; inner < innerEnd; inner++) {
					sumVal += items.getYValue(series, inner);
				}
				sumVal = sumVal / (innerEnd - innerBegin);
				double nearest = Double.MAX_VALUE;
				int nearestIndex = innerBegin;

				for (int inner = innerBegin; inner < innerEnd; inner++) {
					double nearestCandidate = Math.abs(items.getYValue(series, inner) - sumVal);
					if (nearestCandidate < nearest) {
						nearestIndex = inner;
						nearest = nearestCandidate;
					}
				}
				filteredMap.put(nearestIndex, prevFiltered);
				prevFiltered = nearestIndex;
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
	public boolean render(Graphics2D g2, Rectangle2D dataArea, int index, PlotRenderingInfo info,
			CrosshairState crosshairState) {

		boolean foundData = false;
		XYDataset dataset = getDataset(index);
		if (!DatasetUtilities.isEmptyOrNull(dataset)) {
			foundData = true;
			ValueAxis xAxis = getDomainAxisForDataset(index);
			ValueAxis yAxis = getRangeAxisForDataset(index);
			if (xAxis == null || yAxis == null) {
				return foundData;
			}
			XYItemRenderer renderer = getRenderer(index);
			if (renderer == null) {
				renderer = getRenderer();
				if (renderer == null) {
					return foundData;
				}
			}
			proxy.setPlotWidth(dataArea.getBounds().width);
			XYItemRendererState state = renderer.initialise(g2, dataArea, this, dataset, info);
			int passCount = renderer.getPassCount();
			SeriesRenderingOrder seriesOrder = getSeriesRenderingOrder();

			if (seriesOrder == SeriesRenderingOrder.REVERSE) {
				for (int pass = 0; pass < passCount; pass++) {
					int seriesCount = dataset.getSeriesCount();
					for (int series = seriesCount - 1; series >= 0; series--) {
						int firstItem = 0;
						int lastItem = dataset.getItemCount(series) - 1;
						if (lastItem == -1) {
							continue;
						}
						if (state.getProcessVisibleItemsOnly()) {
							int[] itemBounds = RendererUtilities.findLiveItems(dataset, series, xAxis.getLowerBound(),
									xAxis.getUpperBound());
							firstItem = Math.max(itemBounds[0] - 1, 0);
							lastItem = Math.min(itemBounds[1] + 1, lastItem);
						}
						state.startSeriesPass(dataset, series, firstItem, lastItem, pass, passCount);
						Map<Integer, Integer> filteredMap = proxy.workAsProxy(dataset, series, firstItem, lastItem);

						if (renderer instanceof XYFilteringStepRenderer) {
							((XYFilteringStepRenderer) renderer).setFilteredMap(filteredMap);
						}
						if (filteredMap.size() == 0 || !(renderer instanceof XYFilteringStepRenderer)) {
							for (int item = firstItem; item <= lastItem; item++) {
								renderer.drawItem(g2, state, dataArea, info, this, xAxis, yAxis, dataset, series, item,
										crosshairState, pass);
							}
						} else {
							for (Integer item : filteredMap.keySet()) {
								renderer.drawItem(g2, state, dataArea, info, this, xAxis, yAxis, dataset, series, item,
										crosshairState, pass);
							}
						}
						state.endSeriesPass(dataset, series, firstItem, lastItem, pass, passCount);
					}
				}
			} else {
				for (int pass = 0; pass < passCount; pass++) {
					int seriesCount = dataset.getSeriesCount();
					for (int series = 0; series < seriesCount; series++) {
						int firstItem = 0;
						int lastItem = dataset.getItemCount(series) - 1;
						if (lastItem == -1) {
							continue;
						}
						if (state.getProcessVisibleItemsOnly()) {
							int[] itemBounds = RendererUtilities.findLiveItems(dataset, series, xAxis.getLowerBound(),
									xAxis.getUpperBound());
							firstItem = Math.max(itemBounds[0] - 1, 0);
							lastItem = Math.min(itemBounds[1] + 1, lastItem);
						}
						state.startSeriesPass(dataset, series, firstItem, lastItem, pass, passCount);

						for (int item = firstItem; item <= lastItem; item++) {
							renderer.drawItem(g2, state, dataArea, info, this, xAxis, yAxis, dataset, series, item,
									crosshairState, pass);
						}
						state.endSeriesPass(dataset, series, firstItem, lastItem, pass, passCount);
					}
				}
			}
		}
		return foundData;
	}
}
