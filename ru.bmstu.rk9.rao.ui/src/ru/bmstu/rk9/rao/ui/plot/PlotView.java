package ru.bmstu.rk9.rao.ui.plot;

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
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ru.bmstu.rk9.rao.lib.database.CollectedDataNode;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.Index;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.PatternIndex;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.ResultIndex;
import ru.bmstu.rk9.rao.lib.database.Database.ResultType;
import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.Simulator.ExecutionState;
import ru.bmstu.rk9.rao.ui.notification.SubscriberRegistrationManager;
import ru.bmstu.rk9.rao.ui.plot.PlotDataParser.PlotItem;
import ru.bmstu.rk9.rao.ui.serialization.SerializedObjectsView.ConditionalMenuItem;

public class PlotView extends ViewPart {

	public static final String ID = "ru.bmstu.rk9.rao.ui.PlotView";
	private final static Map<CollectedDataNode, Integer> openedPlotMap = new HashMap<CollectedDataNode, Integer>();
	private static int secondaryID = 0;
	private PlotFrame plotFrame;
	private CollectedDataNode partNode;

	public PlotFrame getFrame() {
		return plotFrame;
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
		plotFrame = new PlotFrame(parent, SWT.NONE);
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
		plotFrame.setLayoutData(graphLayoutData);

		parent.layout(true);
		horizontalSlider.setEnabled(false);
		horizontalSlider.setVisible(false);
		verticalSlider.setEnabled(false);
		verticalSlider.setVisible(false);
		plotFrame.setSliders(horizontalSlider, verticalSlider);

		plotFrame.addDisposeListener(new DisposeListener() {
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

		subscriberRegistrationManager.enlistCommonSubscriber(commonSubcriber,
				ExecutionState.EXECUTION_STARTED).enlistCommonSubscriber(
				commonSubcriber, ExecutionState.EXECUTION_COMPLETED);
		subscriberRegistrationManager
				.enlistRealTimeSubscriber(realTimeUpdateRunnable);
		subscriberRegistrationManager.initialize();
	}

	private final boolean readyForInput() {
		return plotFrame != null && !plotFrame.isDisposed();
	}

	private final Runnable realTimeUpdateRunnable = new Runnable() {
		@Override
		public void run() {
			if (!readyForInput())
				return;

			final List<PlotItem> items = PlotDataParser.parseEntries(partNode);
			if (!items.isEmpty()) {
				final XYSeriesCollection newDataset = (XYSeriesCollection) plotFrame
						.getChart().getXYPlot().getDataset();
				final XYSeries newSeries = newDataset.getSeries(0);
				for (int i = 0; i < items.size(); i++) {
					final PlotItem item = items.get(i);
					newSeries.add(item.x, item.y);
				}
				plotFrame.setChartMaximum(newSeries.getMaxX(),
						newSeries.getMaxY());
				plotFrame.updateSliders();
			}
		}
	};

	private final Subscriber commonSubcriber = new Subscriber() {
		@Override
		public void fireChange() {
			PlatformUI.getWorkbench().getDisplay()
					.asyncExec(realTimeUpdateRunnable);
		}
	};

	private final SubscriberRegistrationManager subscriberRegistrationManager = new SubscriberRegistrationManager();

	@Override
	public void setFocus() {
	}

	@Override
	public void dispose() {
		subscriberRegistrationManager.deinitialize();
		super.dispose();
	}

	public void plotXY(final XYSeriesCollection dataset,
			List<String> axisSymbols) {
		final JFreeChart chart = createChart(dataset, axisSymbols);
		plotFrame.setChart(chart);
		plotFrame.setRangeZoomable(false);
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
		plotFrame.setChartMaximum(horizontalMaximum, verticalMaximum);

		return chart;
	}

	static private class PlotMenuItem extends ConditionalMenuItem {

		public PlotMenuItem(Menu parent) {
			super(parent, "Plot");
		}

		@Override
		public boolean isEnabled(CollectedDataNode node) {
			Index index = node.getIndex();
			if (index == null)
				return false;

			switch (index.getType()) {
			case RESOURCE_PARAMETER:
				return true;

			case RESULT:
				ResultIndex resultIndex = (ResultIndex) index;
				ResultType resultType = resultIndex.getResultType();
				return resultType != ResultType.GET_VALUE;

			case PATTERN:
				PatternIndex patternIndex = (PatternIndex) index;
				String patternType = patternIndex.getStructrure().getString(
						"type");
				return patternType.equals("operation");

			default:
				return false;
			}
		}

		@Override
		public void show(CollectedDataNode node) {
			try {
				if (PlotView.getOpenedPlotMap().containsKey(node)) {
					PlatformUI
							.getWorkbench()
							.getActiveWorkbenchWindow()
							.getActivePage()
							.showView(
									PlotView.ID,
									String.valueOf(PlotView.getOpenedPlotMap()
											.get(node)),
									IWorkbenchPage.VIEW_ACTIVATE);
				} else {
					XYSeriesCollection dataset = new XYSeriesCollection();
					XYSeries series = new XYSeries(node.getName());
					dataset.addSeries(series);
					List<PlotItem> items = PlotDataParser.parseEntries(node);
					for (int i = 0; i < items.size(); i++) {
						PlotItem item = items.get(i);
						series.add(item.x, item.y);
					}
					PlotView newView = (PlotView) PlatformUI
							.getWorkbench()
							.getActiveWorkbenchWindow()
							.getActivePage()
							.showView(PlotView.ID, String.valueOf(secondaryID),
									IWorkbenchPage.VIEW_ACTIVATE);
					newView.setName(String.valueOf(dataset.getSeriesKey(0)));
					newView.setNode(node);
					PlotView.addToOpenedPlotMap(node, secondaryID);

					newView.plotXY(dataset, PlotDataParser.getEnumNames(node));
					secondaryID++;
				}
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
	}

	static public ConditionalMenuItem createConditionalMenuItem(Menu parent) {
		return new PlotMenuItem(parent);
	}
}
