package ru.bmstu.rk9.rao.ui.plot;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.xy.XYDataset;

public class FilteringPlotFactory {
	public static JFreeChart createXYStepChart(String title, String xAxisLabel, String yAxisLabel, XYDataset dataset,
			PlotOrientation orientation, boolean legend, boolean tooltips, boolean urls) {
		ParamChecks.nullNotPermitted(orientation, "orientation");
		final DateAxis xAxis = new DateAxis(xAxisLabel);
		final NumberAxis yAxis = new NumberAxis(yAxisLabel);
		yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		final XYItemRenderer renderer = new XYFilteringStepRenderer(null, null);
		final XYPlotWithFiltering plot = new XYPlotWithFiltering(dataset, xAxis, yAxis, null);

		plot.setRenderer(renderer);
		plot.setOrientation(orientation);
		plot.setDomainCrosshairVisible(false);
		plot.setRangeCrosshairVisible(false);
		final JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend);
		ChartFactory.getChartTheme().apply(chart);

		return chart;
	}
}
