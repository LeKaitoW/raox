package ru.bmstu.rk9.rdo.ui.runtime;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;

public class JFreeChartPlot {
	private static JFreeChart createChart(XYDataset dataset) {

		final JFreeChart chart = ChartFactory.createXYStepChart("Queue length",
				"Time", "Value", dataset, PlotOrientation.VERTICAL, true, true,
				false);

		final XYPlot plot = (XYPlot) chart.getPlot();

		final NumberAxis rangeAxis = new NumberAxis();
		rangeAxis.setAutoRangeIncludesZero(true);
		plot.setRangeAxis(rangeAxis);

		final NumberAxis domainAxis = new NumberAxis();
		domainAxis.setAutoRangeIncludesZero(true);
		plot.setDomainAxis(domainAxis);

		return chart;
	}

	public static void plotXY(XYSeriesCollection dataset) {
		final JFreeChart chart = createChart(dataset);
		final Display display = PlatformUI.getWorkbench().getDisplay();
		final Shell shell = new Shell(display);
		shell.setSize(600, 300);
		shell.setLayout(new FillLayout());
		shell.setText("Plot");
		final ChartComposite frame = new MyChartComposite(shell, SWT.NONE,
				chart, true);
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

class MyChartComposite extends ChartComposite implements KeyListener {

	public MyChartComposite(Composite comp, int style, JFreeChart chart,
			boolean useBuffer) {
		super(comp, style, chart, useBuffer);
		addSWTListener(this);
	}

	@Override
	public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
		if (e.keyCode == ' ') {
			final XYSeriesCollection dataset = (XYSeriesCollection) this
					.getChart().getXYPlot().getDataset();
			final XYSeries series2 = dataset.getSeries(0);

			final int pointNumber = dataset.getItemCount(0);
			final double lastX = dataset.getXValue(0, pointNumber - 1);
			final double x = lastX + 1.0;
			final int y = (int) (pointNumber + 10 * Math.random());
			series2.add(x, y);
		}
	}

	@Override
	public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
	}
}
