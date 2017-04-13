package ru.bmstu.rk9.rao.ui.plot;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

public class XYFilteringStepRenderer extends XYStepRenderer {
	public XYFilteringStepRenderer(XYToolTipGenerator toolTipGenerator, XYURLGenerator urlGenerator) {
		super(toolTipGenerator, urlGenerator);
	}

	private Map<Integer, Integer> filteredLinesMap = new LinkedHashMap<>();

	@Override
	public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, PlotRenderingInfo info,
			XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item,
			CrosshairState crosshairState, int pass) {

		if (!getItemVisible(series, item)) {
			return;
		}

		PlotOrientation orientation = plot.getOrientation();

		Paint seriesPaint = getItemPaint(series, item);
		Stroke seriesStroke = getItemStroke(series, item);
		g2.setPaint(seriesPaint);
		g2.setStroke(seriesStroke);

		double x1 = dataset.getXValue(series, item);
		double y1 = dataset.getYValue(series, item);

		RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
		RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
		double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
		double transY1 = (Double.isNaN(y1) ? Double.NaN : rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation));

		if (pass == 0 && item > 0) {
			Integer prevPoint = filteredLinesMap.size() > 0 ? filteredLinesMap.get(item) : null;
			if (prevPoint == null) {
				prevPoint = item - 1;
			}
			double x0 = dataset.getXValue(series, prevPoint);
			double y0 = dataset.getYValue(series, prevPoint);
			double transX0 = domainAxis.valueToJava2D(x0, dataArea, xAxisLocation);
			double transY0 = (Double.isNaN(y0) ? Double.NaN : rangeAxis.valueToJava2D(y0, dataArea, yAxisLocation));

			if (orientation == PlotOrientation.HORIZONTAL) {
				if (transY0 == transY1) {
					drawLine(g2, state.workingLine, transY0, transX0, transY1, transX1);
				} else {
					double transXs = transX0 + (getStepPoint() * (transX1 - transX0));
					drawLine(g2, state.workingLine, transY0, transX0, transY0, transXs);
					drawLine(g2, state.workingLine, transY0, transXs, transY1, transXs);
					drawLine(g2, state.workingLine, transY1, transXs, transY1, transX1);
				}
			} else if (orientation == PlotOrientation.VERTICAL) {
				if (transY0 == transY1) {
					drawLine(g2, state.workingLine, transX0, transY0, transX1, transY1);
				} else {
					double transXs = transX0 + (getStepPoint() * (transX1 - transX0));
					drawLine(g2, state.workingLine, transX0, transY0, transXs, transY0);
					drawLine(g2, state.workingLine, transXs, transY0, transXs, transY1);
					drawLine(g2, state.workingLine, transXs, transY1, transX1, transY1);
				}
			}
			int domainAxisIndex = plot.getDomainAxisIndex(domainAxis);
			int rangeAxisIndex = plot.getRangeAxisIndex(rangeAxis);
			updateCrosshairValues(crosshairState, x1, y1, domainAxisIndex, rangeAxisIndex, transX1, transY1,
					orientation);

			EntityCollection entities = state.getEntityCollection();
			if (entities != null) {
				addEntity(entities, null, dataset, series, item, transX1, transY1);
			}
		}
		if (pass == 1) {
			if (isItemLabelVisible(series, item)) {
				double xx = transX1;
				double yy = transY1;
				if (orientation == PlotOrientation.HORIZONTAL) {
					xx = transY1;
					yy = transX1;
				}
				drawItemLabel(g2, orientation, dataset, series, item, xx, yy, (y1 < 0.0));
			}
		}
	}

	private void drawLine(Graphics2D g2, Line2D line, double x0, double y0, double x1, double y1) {
		if (Double.isNaN(x0) || Double.isNaN(x1) || Double.isNaN(y0) || Double.isNaN(y1)) {
			return;
		}
		line.setLine(x0, y0, x1, y1);
		g2.draw(line);
	}

	public Map<Integer, Integer> getFilteredMap() {
		return filteredLinesMap;
	}

	public void setFilteredMap(Map<Integer, Integer> filteredMap) {
		this.filteredLinesMap = filteredMap;
	}
}
