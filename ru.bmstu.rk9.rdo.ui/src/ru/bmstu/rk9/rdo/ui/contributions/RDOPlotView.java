package ru.bmstu.rk9.rdo.ui.contributions;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;

public class RDOPlotView extends ViewPart {

	public static final String ID = "ru.bmstu.rk9.rdo.ui.RDOPlotView";
	private static ChartComposite frame;
	private static String partName;

	public static void setName(String name) {
		partName = name;
	}

	@Override
	public void createPartControl(Composite parent) {
		setPartName(partName);
		frame = new ChartComposite(parent, SWT.NONE);
	}

	@Override
	public void setFocus() {
	}

	public static void plotXY(final XYSeriesCollection dataset) {
		final JFreeChart chart = createChart(dataset);
		frame.setChart(chart);
	}

	private static JFreeChart createChart(final XYDataset dataset) {

		final JFreeChart chart = ChartFactory.createXYStepChart("", "Time",
				"Value", dataset, PlotOrientation.VERTICAL, true, true, false);

		final XYPlot plot = (XYPlot) chart.getPlot();

		final NumberAxis rangeAxis = new NumberAxis();
		rangeAxis.setAutoRangeIncludesZero(true);
		plot.setRangeAxis(rangeAxis);

		final NumberAxis domainAxis = new NumberAxis();
		domainAxis.setAutoRangeIncludesZero(true);
		plot.setDomainAxis(domainAxis);

		return chart;
	}

}
