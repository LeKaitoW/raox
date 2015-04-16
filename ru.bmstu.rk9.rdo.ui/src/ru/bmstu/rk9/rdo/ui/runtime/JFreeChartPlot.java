package ru.bmstu.rk9.rdo.ui.runtime;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;

public class JFreeChartPlot {
	private static JFreeChart createChart(final XYDataset dataset) {

		final JFreeChart chart = ChartFactory.createXYStepChart("Plot", "Time",
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

	public static void plotXY(final XYSeriesCollection dataset) {
		final JFreeChart chart = createChart(dataset);
		final Display display = PlatformUI.getWorkbench().getDisplay();
		final Shell shell = new Shell(display);
		shell.setSize(600, 300);
		shell.setLayout(new FillLayout());
		shell.setText("Plot");
		final ChartComposite frame = new ChartComposite(shell, SWT.NONE, chart,
				true);
		frame.setDisplayToolTips(true);
		frame.setHorizontalAxisTrace(false);
		frame.setVerticalAxisTrace(false);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
}
