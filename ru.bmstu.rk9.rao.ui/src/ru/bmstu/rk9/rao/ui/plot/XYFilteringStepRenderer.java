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
	public void drawItem(Graphics2D graphics2D, XYItemRendererState state, Rectangle2D dataArea, PlotRenderingInfo info,
			XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int seriesIndex, int itemIndex,
			CrosshairState crosshairState, int passIndex) {

		if (!getItemVisible(seriesIndex, itemIndex))
			return;

		final PlotOrientation orientation = plot.getOrientation();

		final Paint seriesPaint = getItemPaint(seriesIndex, itemIndex);
		final Stroke seriesStroke = getItemStroke(seriesIndex, itemIndex);
		graphics2D.setPaint(seriesPaint);
		graphics2D.setStroke(seriesStroke);

		final RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
		final RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
		double valueX1 = dataset.getXValue(seriesIndex, itemIndex);
		double valueY1 = dataset.getYValue(seriesIndex, itemIndex);
		double screenX1 = domainAxis.valueToJava2D(valueX1, dataArea, xAxisLocation);
		double screenY1 = (Double.isNaN(valueY1) ? Double.NaN
				: rangeAxis.valueToJava2D(valueY1, dataArea, yAxisLocation));

		if (passIndex == 0 && itemIndex > 0) {
			Integer previousPoint = filteredLinesMap.size() > 0 ? filteredLinesMap.get(itemIndex) : null;
			if (previousPoint == null)
				previousPoint = itemIndex - 1;

			double valueX0 = dataset.getXValue(seriesIndex, previousPoint);
			double valueY0 = dataset.getYValue(seriesIndex, previousPoint);
			double screenX0 = domainAxis.valueToJava2D(valueX0, dataArea, xAxisLocation);
			double screenY0 = (Double.isNaN(valueY0) ? Double.NaN
					: rangeAxis.valueToJava2D(valueY0, dataArea, yAxisLocation));

			if (orientation.isHorizontal()) {
				if (screenY0 == screenY1)
					drawLine(graphics2D, state.workingLine, screenY0, screenX0, screenY1, screenX1);
				else {
					double screenXFromStep = screenX0 + (getStepPoint() * (screenX1 - screenX0));
					drawLine(graphics2D, state.workingLine, screenY0, screenX0, screenY0, screenXFromStep);
					drawLine(graphics2D, state.workingLine, screenY0, screenXFromStep, screenY1, screenXFromStep);
					drawLine(graphics2D, state.workingLine, screenY1, screenXFromStep, screenY1, screenX1);
				}
			} else if (orientation.isVertical()) {
				if (screenY0 == screenY1)
					drawLine(graphics2D, state.workingLine, screenX0, screenY0, screenX1, screenY1);
				else {
					double screenXFromStep = screenX0 + (getStepPoint() * (screenX1 - screenX0));
					drawLine(graphics2D, state.workingLine, screenX0, screenY0, screenXFromStep, screenY0);
					drawLine(graphics2D, state.workingLine, screenXFromStep, screenY0, screenXFromStep, screenY1);
					drawLine(graphics2D, state.workingLine, screenXFromStep, screenY1, screenX1, screenY1);
				}
			}
			int domainAxisIndex = plot.getDomainAxisIndex(domainAxis);
			int rangeAxisIndex = plot.getRangeAxisIndex(rangeAxis);
			updateCrosshairValues(crosshairState, valueX1, valueY1, domainAxisIndex, rangeAxisIndex, screenX1, screenY1,
					orientation);

			EntityCollection entities = state.getEntityCollection();
			if (entities != null)
				addEntity(entities, null, dataset, seriesIndex, itemIndex, screenX1, screenY1);
		}
	}

	private void drawLine(Graphics2D graphics2D, Line2D line, double x0, double y0, double x1, double y1) {
		if (Double.isNaN(x0) || Double.isNaN(x1) || Double.isNaN(y0) || Double.isNaN(y1))
			return;
		if (Double.isInfinite(x0) || Double.isInfinite(x1) || Double.isInfinite(y0) || Double.isInfinite(y1))
			return;

		line.setLine(x0, y0, x1, y1);
		graphics2D.draw(line);
	}

	public Map<Integer, Integer> getFilteredMap() {
		return filteredLinesMap;
	}

	public void setFilteredMap(Map<Integer, Integer> filteredMap) {
		this.filteredLinesMap = filteredMap;
	}
}
