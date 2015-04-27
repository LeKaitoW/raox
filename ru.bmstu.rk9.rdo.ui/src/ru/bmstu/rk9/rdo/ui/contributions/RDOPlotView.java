package ru.bmstu.rk9.rdo.ui.contributions;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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

import ru.bmstu.rk9.rdo.lib.CollectedDataNode.AbstractIndex;

public class RDOPlotView extends ViewPart {

	public static final String ID = "ru.bmstu.rk9.rdo.ui.RDOPlotView";
	private final static HashMap<AbstractIndex, Integer> openedPlotMap = new HashMap<AbstractIndex, Integer>();

	private ChartComposite frame;
	private AbstractIndex partIndex;

	public static HashMap<AbstractIndex, Integer> getOpenedplotmap() {
		return openedPlotMap;
	}

	public void setName(final String name) {
		setPartName(name);
	}

	public void setIndex(final AbstractIndex index) {
		partIndex = index;
	}

	public static void addToOpenedPlotMap(final AbstractIndex index,
			final int secondaryID) {
		openedPlotMap.put(index, secondaryID);
	}

	@Override
	public void createPartControl(Composite parent) {
		frame = new ChartComposite(parent, SWT.NONE);
		frame.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent event) {
				openedPlotMap.remove(partIndex);
			}
		});
	}

	@Override
	public void setFocus() {
	}

	public void plotXY(final XYSeriesCollection dataset) {
		final JFreeChart chart = createChart(dataset);
		frame.setChart(chart);
	}

	private JFreeChart createChart(final XYDataset dataset) {

		final JFreeChart chart = ChartFactory.createXYStepChart("", "Time",
				"Value", dataset, PlotOrientation.VERTICAL, true, true, false);

		final XYPlot plot = (XYPlot) chart.getPlot();
		Color white = new Color(0xFF, 0XFF, 0xFF);
		plot.setBackgroundPaint(white);
		Color grey = new Color(0x99, 0x99, 0x99);
		plot.setDomainGridlinePaint(grey);
		plot.setRangeGridlinePaint(grey);
		plot.getRenderer().setSeriesStroke(0, new BasicStroke((float) 2.5));

		final NumberAxis rangeAxis = new NumberAxis();
		rangeAxis.setAutoRangeIncludesZero(true);
		plot.setRangeAxis(rangeAxis);

		final NumberAxis domainAxis = new NumberAxis();
		domainAxis.setAutoRangeIncludesZero(true);
		plot.setDomainAxis(domainAxis);

		return chart;
	}

}
