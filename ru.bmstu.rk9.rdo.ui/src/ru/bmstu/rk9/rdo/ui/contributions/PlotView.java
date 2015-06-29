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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;

import ru.bmstu.rk9.rdo.lib.CollectedDataNode;
import ru.bmstu.rk9.rdo.lib.PlotDataParser;

public class PlotView extends ViewPart {

	public static final String ID = "ru.bmstu.rk9.rdo.ui.PlotView";
	private final static Map<CollectedDataNode, Integer> openedPlotMap = new HashMap<CollectedDataNode, Integer>();

	private ChartFrame chartFrame;
	private CollectedDataNode partNode;

	public ChartFrame getFrame() {
		return chartFrame;
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
		chartFrame = new ChartFrame(parent, SWT.NONE);
		FormLayout layout = new FormLayout();
		parent.setLayout(layout);

		Slider verticalSlider = new Slider(parent, SWT.VERTICAL);
		FormData verticalSliderLayoutData = new FormData();

		Slider horizontalSlider = new Slider(parent, SWT.HORIZONTAL);
		FormData horizontalSliderLayoutData = new FormData();

		horizontalSliderLayoutData.left = new FormAttachment(0, 0);
		horizontalSliderLayoutData.bottom = new FormAttachment(100, 0);
		horizontalSliderLayoutData.right = new FormAttachment(verticalSlider);
		horizontalSlider.setLayoutData(horizontalSliderLayoutData);

		verticalSliderLayoutData.right = new FormAttachment(100, 0);
		verticalSliderLayoutData.bottom = new FormAttachment(horizontalSlider);
		verticalSliderLayoutData.top = new FormAttachment(0, 0);
		verticalSlider.setLayoutData(verticalSliderLayoutData);

		FormData graphLayoutData = new FormData();
		graphLayoutData.left = new FormAttachment(0, 0);
		graphLayoutData.right = new FormAttachment(verticalSlider);
		graphLayoutData.top = new FormAttachment(0, 0);
		graphLayoutData.bottom = new FormAttachment(horizontalSlider);
		chartFrame.setLayoutData(graphLayoutData);

		parent.layout(true);
		horizontalSlider.setEnabled(false);
		horizontalSlider.setVisible(false);
		verticalSlider.setEnabled(false);
		verticalSlider.setVisible(false);
		chartFrame.setSliders(horizontalSlider, verticalSlider);

		chartFrame.addDisposeListener(new DisposeListener() {
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

	public void plotXY(final XYSeriesCollection dataset,
			List<String> axisSymbols) {
		final JFreeChart chart = createChart(dataset, axisSymbols);
		chartFrame.setChart(chart);
		chartFrame.setRangeZoomable(false);
	}

	private JFreeChart createChart(final XYDataset dataset,
			final List<String> axisSymbols) {

		final JFreeChart chart = ChartFactory.createXYStepChart("", "Time",
				"Value", dataset, PlotOrientation.VERTICAL, true, true, false);

		final XYPlot plot = (XYPlot) chart.getPlot();
		Color white = new Color(0xFF, 0XFF, 0xFF);
		plot.setBackgroundPaint(white);
		Color grey = new Color(0x99, 0x99, 0x99);
		plot.setDomainGridlinePaint(grey);
		plot.setRangeGridlinePaint(grey);
		plot.getRenderer().setSeriesStroke(0, new BasicStroke((float) 2.5));

		NumberAxis rangeAxis;
		if (axisSymbols != null) {
			String[] enumLabels = new String[axisSymbols.size()];
			enumLabels = axisSymbols.toArray(enumLabels);

			final FontRegistry fontRegistry = PlatformUI.getWorkbench()
					.getThemeManager().getCurrentTheme().getFontRegistry();
			final String fontName = fontRegistry.get(
					PreferenceConstants.EDITOR_TEXT_FONT).getFontData()[0]
					.getName();

			final Font font = new Font(fontName, Font.PLAIN, 10);
			rangeAxis = new SymbolAxis("", enumLabels);
			rangeAxis.setAutoRangeIncludesZero(true);
			plot.setRangeAxis(rangeAxis);
			rangeAxis.setTickLabelFont(font);
		} else {
			rangeAxis = new NumberAxis();
			rangeAxis.setAutoRangeIncludesZero(true);
			plot.setRangeAxis(rangeAxis);
		}

		final NumberAxis domainAxis = new NumberAxis();
		domainAxis.setAutoRangeIncludesZero(true);
		plot.setDomainAxis(domainAxis);

		double horizontalMaximum = domainAxis.getRange().getLength();
		double verticalMaximum = rangeAxis.getRange().getLength();
		chartFrame.setChartMaximum(horizontalMaximum, verticalMaximum);

		return chart;
	}
}

class ChartFrame extends ChartComposite implements KeyListener {

	private interface Axis {
		public ValueAxis get();
	}

	private interface Position {
		public double get();
	}

	class ChartSlider {

		private ChartSlider(Slider slider, Axis axis, Position position,
				SelectionListener selectionListener) {
			this.slider = slider;
			this.axis = axis;
			this.slider.setMinimum(0);
			this.slider.addSelectionListener(selectionListener);
			this.position = position;
		}

		private final void update() {
			if (maximum > axis.get().getRange().getUpperBound()) {
				slider.setVisible(true);
				slider.setEnabled(true);

				ratio = slider.getMaximum() / maximum;
				slider.setThumb((int) Math
						.round((axis.get().getUpperBound() - axis.get()
								.getLowerBound()) * ratio));
				slider.setSelection((int) Math.round(position.get() * ratio));
			} else {
				slider.setVisible(false);
				slider.setEnabled(false);
			}
		}

		private final Slider slider;
		private final Axis axis;
		private final Position position;
		private double maximum;
		private double ratio;
	}

	private ChartSlider horizontalSlider;
	private ChartSlider verticalSlider;

	public ChartFrame(final Composite comp, final int style) {
		super(comp, style, null, ChartComposite.DEFAULT_WIDTH,
				ChartComposite.DEFAULT_HEIGHT, 0, 0, Integer.MAX_VALUE,
				Integer.MAX_VALUE, ChartComposite.DEFAULT_BUFFER_USED, true,
				true, true, true, true);
		addSWTListener(this);
	}

	public final void setSliders(Slider horizontalSlider_,
			Slider verticalSlider_) {
		horizontalSlider = new ChartSlider(horizontalSlider_, new Axis() {
			@Override
			public ValueAxis get() {
				return getChart().getXYPlot().getDomainAxis();
			}
		}, new Position() {
			@Override
			public double get() {
				return horizontalSlider.axis.get().getLowerBound();
			}
		}, new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ValueAxis axis = horizontalSlider.axis.get();
				axis.setLowerBound(horizontalSlider.slider.getSelection()
						/ horizontalSlider.ratio);
				axis.setUpperBound((horizontalSlider.slider.getThumb() + horizontalSlider.slider
						.getSelection()) / horizontalSlider.ratio);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		verticalSlider = new ChartSlider(verticalSlider_, new Axis() {
			@Override
			public ValueAxis get() {
				return getChart().getXYPlot().getRangeAxis();
			}
		}, new Position() {
			@Override
			public double get() {
				ValueAxis axis = verticalSlider.axis.get();
				return verticalSlider.maximum - axis.getUpperBound();
			}
		}, new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ValueAxis axis = verticalSlider.axis.get();
				axis.setLowerBound((verticalSlider.slider.getMaximum()
						- verticalSlider.slider.getSelection() - verticalSlider.slider
							.getThumb()) / verticalSlider.ratio);
				axis.setUpperBound((verticalSlider.slider.getMaximum() - verticalSlider.slider
						.getSelection()) / verticalSlider.ratio);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	public final void setChartMaximum(double horizontalSliderMaximum,
			double verticalSliderMaximum) {
		horizontalSlider.maximum = horizontalSliderMaximum;
		verticalSlider.maximum = verticalSliderMaximum;
	}

	public final void updateSliders() {
		horizontalSlider.update();
		verticalSlider.update();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.keyCode == SWT.SHIFT) {
			this.setRangeZoomable(true);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.keyCode == SWT.SHIFT) {
			this.setRangeZoomable(false);
		}
	}

	@Override
	public void zoomInDomain(double x, double y) {
		super.zoomInDomain(x, y);
		horizontalSlider.update();
	}

	@Override
	public void zoomInRange(double x, double y) {
		super.zoomInRange(x, y);
		verticalSlider.update();
	}

	@Override
	public void zoomOutDomain(double x, double y) {
		super.zoomOutDomain(x, y);
		horizontalSlider.update();
	}

	@Override
	public void zoomOutRange(double x, double y) {
		super.zoomOutRange(x, y);
		verticalSlider.update();
	}

	@Override
	public void zoom(Rectangle selection) {
		super.zoom(selection);
		updateSliders();
	}

	@Override
	public void restoreAutoDomainBounds() {
		super.restoreAutoDomainBounds();
		horizontalSlider.update();
	}

	@Override
	public void restoreAutoRangeBounds() {
		super.restoreAutoRangeBounds();
		verticalSlider.update();
	}
}
