package ru.bmstu.rk9.rdo.ui.contributions;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;

import ru.bmstu.rk9.rdo.lib.CollectedDataNode;
import ru.bmstu.rk9.rdo.lib.PlotDataParser;

public class RDOPlotView extends ViewPart {

	public static final String ID = "ru.bmstu.rk9.rdo.ui.RDOPlotView";
	private final static Map<CollectedDataNode, Integer> openedPlotMap = new HashMap<CollectedDataNode, Integer>();

	private ChartComposite frame;
	private CollectedDataNode partNode;

	public ChartComposite getFrame() {
		return frame;
	}

	public static Map<CollectedDataNode, Integer> getOpenedPlotMap() {
		return openedPlotMap;
	}

	public void setName(final String name) {
		setPartName(name);
	}

	public void setNode(final CollectedDataNode node) {
		partNode = node;
	}

	public static void addToOpenedPlotMap(final CollectedDataNode node,
			final int secondaryID) {
		openedPlotMap.put(node, secondaryID);
	}

	@Override
	public void createPartControl(Composite parent) {
		frame = new KeyChartComposite(parent, SWT.NONE, null,
				ChartComposite.DEFAULT_WIDTH, ChartComposite.DEFAULT_HEIGHT, 0,
				0, Integer.MAX_VALUE, Integer.MAX_VALUE,
				ChartComposite.DEFAULT_BUFFER_USED, true, true, true, true,
				true);
		frame.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent event) {
				if (!openedPlotMap.isEmpty()
						&& openedPlotMap.containsKey(partNode)) {
					openedPlotMap.remove(partNode);
				}
				if (partNode != null) {
					PlotDataParser.removeIndexFromMaps(partNode.getIndex());
				}
			}
		});
	}

	@Override
	public void setFocus() {
	}

	public void plotXY(final XYSeriesCollection dataset, List<String> enumNames) {
		final JFreeChart chart = createChart(dataset, enumNames);
		frame.setChart(chart);
		frame.setRangeZoomable(false);
	}

	private JFreeChart createChart(final XYDataset dataset,
			final List<String> enumNames) {

		final JFreeChart chart = ChartFactory.createXYStepChart("", "Time",
				"Value", dataset, PlotOrientation.VERTICAL, true, true, false);

		final XYPlot plot = (XYPlot) chart.getPlot();
		Color white = new Color(0xFF, 0XFF, 0xFF);
		plot.setBackgroundPaint(white);
		Color grey = new Color(0x99, 0x99, 0x99);
		plot.setDomainGridlinePaint(grey);
		plot.setRangeGridlinePaint(grey);
		plot.getRenderer().setSeriesStroke(0, new BasicStroke((float) 2.5));

		if (enumNames != null) {
			String[] enumLabels = new String[enumNames.size()];
			enumLabels = enumNames.toArray(enumLabels);

			final FontRegistry fontRegistry = PlatformUI.getWorkbench()
					.getThemeManager().getCurrentTheme().getFontRegistry();
			final String fontName = fontRegistry.get(
					PreferenceConstants.EDITOR_TEXT_FONT).getFontData()[0]
					.getName();

			final Font font = new Font(fontName, Font.PLAIN, 10);
			final SymbolAxis rangeAxis = new SymbolAxis("", enumLabels);
			rangeAxis.setAutoRangeIncludesZero(true);
			plot.setRangeAxis(rangeAxis);
			rangeAxis.setTickLabelFont(font);

		} else {
			final NumberAxis rangeAxis = new NumberAxis();
			rangeAxis.setAutoRangeIncludesZero(true);
			plot.setRangeAxis(rangeAxis);
		}

		final NumberAxis domainAxis = new NumberAxis();
		domainAxis.setAutoRangeIncludesZero(true);
		plot.setDomainAxis(domainAxis);

		return chart;
	}

}

class KeyChartComposite extends ChartComposite implements KeyListener {
	public KeyChartComposite(Composite comp, int style, JFreeChart jfreechart,
			int width, int height, int minimumDrawW, int minimumDrawH,
			int maximumDrawW, int maximumDrawH, boolean usingBuffer,
			boolean properties, boolean save, boolean print, boolean zoom,
			boolean tooltips) {
		super(comp, style, jfreechart, width, height, minimumDrawW,
				minimumDrawH, maximumDrawW, maximumDrawH, usingBuffer,
				properties, save, print, zoom, tooltips);
		addSWTListener(this);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.keyCode == SWT.CTRL) {
			this.setRangeZoomable(true);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.keyCode == SWT.CTRL) {
			this.setRangeZoomable(false);
		}
	}
}
