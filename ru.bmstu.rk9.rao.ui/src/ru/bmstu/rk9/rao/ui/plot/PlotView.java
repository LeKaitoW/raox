package ru.bmstu.rk9.rao.ui.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
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
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ru.bmstu.rk9.rao.lib.database.CollectedDataNode;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.Index;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.PatternIndex;
import ru.bmstu.rk9.rao.lib.modeldata.ModelStructureConstants;
import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.ExecutionState;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager.SimulatorSubscriberInfo;
import ru.bmstu.rk9.rao.ui.notification.RealTimeSubscriberManager;
import ru.bmstu.rk9.rao.ui.plot.PlotDataParser.DataParserResult;
import ru.bmstu.rk9.rao.ui.plot.PlotDataParser.PlotItem;
import ru.bmstu.rk9.rao.ui.serialization.SerializedObjectsView.ConditionalMenuItem;

public class PlotView extends ViewPart {
	public static final String ID = "ru.bmstu.rk9.rao.ui.PlotView";
	private final static Map<CollectedDataNode, Integer> openedPlotMap = new HashMap<CollectedDataNode, Integer>();
	private static int secondaryID = 0;

	public static Map<CollectedDataNode, Integer> getOpenedPlotMap() {
		return openedPlotMap;
	}

	public static void addToOpenedPlotMap(final CollectedDataNode node, final int secondaryID) {
		openedPlotMap.put(node, secondaryID);
	}

	private PlotFrame plotFrame;
	private CollectedDataNode partNode;

	public PlotFrame getFrame() {
		return plotFrame;
	}

	static private class ExportMenuItem extends ConditionalMenuItem {
		public ExportMenuItem(Menu parent) {
			super(parent, "Export to JSON");
		}

		@Override
		public boolean isEnabled(CollectedDataNode node) {
			Index index = node.getIndex();

			if (PlotView.getOpenedPlotMap().containsKey(node)) {
				if (index == null)
					return false;

				switch (index.getType()) {
				case RESOURCE_PARAMETER:
					return true;
				case RESULT:
					return true;
				case PATTERN:
					PatternIndex patternIndex = (PatternIndex) index;
					int patternNumber = patternIndex.getNumber();
					String patternType = CurrentSimulator.getStaticModelData().getPatternType(patternNumber);
					return patternType.equals(ModelStructureConstants.OPERATION);
				default:
					return false;
				}
			} else
				return false;
		}

		@Override
		public void show(CollectedDataNode node) {

			FileDialog fileDialog = new FileDialog(getDisplay().getActiveShell(), SWT.SAVE);
			fileDialog.setText("Export to JSON");
			String[] filter = { "*.json", "*.*" };
			fileDialog.setFilterExtensions(filter);
			String fileName = fileDialog.open();
			if (fileName != null && fileName.length() > 0) {
				PlotDataParser parser = new PlotDataParser(node);
				DataParserResult result = parser.parseEntries();
				List<PlotItem> dataset = result.dataset;
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				String data = gson.toJson(dataset);
				try (Writer writer = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"))) {
					writer.write(data);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
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
				if (!openedPlotMap.isEmpty() && openedPlotMap.containsKey(partNode)) {
					openedPlotMap.remove(partNode);
				}
				deinitializeSubscribers();
			}
		});
	}

	private final void initialize(CollectedDataNode node) {
		partNode = node;
		setPartName(partNode.getName());
		plotDataParser = new PlotDataParser(partNode);

		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries series = new XYSeries(partNode.getName());
		dataset.addSeries(series);

		plotXY(dataset);
		initializeSubscribers();
	}

	private final void initializeSubscribers() {
		simulatorSubscriberManager.initialize(
				Arrays.asList(new SimulatorSubscriberInfo(commonSubcriber, ExecutionState.EXECUTION_STARTED),
						new SimulatorSubscriberInfo(endSubscriber, ExecutionState.EXECUTION_COMPLETED)));
		realTimeSubscriberManager.initialize(Arrays.asList(realTimeUpdateRunnable));
	}

	private final void deinitializeSubscribers() {
		simulatorSubscriberManager.deinitialize();
		realTimeSubscriberManager.deinitialize();
	}

	private final SimulatorSubscriberManager simulatorSubscriberManager = new SimulatorSubscriberManager();
	private final RealTimeSubscriberManager realTimeSubscriberManager = new RealTimeSubscriberManager();

	private PlotDataParser plotDataParser;

	private final boolean readyForInput() {
		return plotFrame != null && !plotFrame.isDisposed();
	}

	private class RealTimeUpdateRunnable implements Runnable {
		private boolean isLastEntry;

		RealTimeUpdateRunnable(boolean isLastEntry) {
			this.isLastEntry = isLastEntry;
		}

		private void changeLastValueFlag() {
			isLastEntry = true;
		}

		@Override
		public void run() {
			if (!readyForInput())
				return;

			final DataParserResult dataParserResult = plotDataParser.parseEntries();

			if (dataParserResult.axisHelper.axisChanged) {
				XYPlot plot = (XYPlot) plotFrame.getChart().getPlot();
				SymbolAxis rangeAxis;
				String[] labels = new String[dataParserResult.axisHelper.axisValues.size()];
				labels = dataParserResult.axisHelper.axisValues.toArray(labels);

				final String fontName = plotFrame.getChart().getTitle().getFont().getName();

				final Font font = new Font(fontName, Font.PLAIN, 10);
				rangeAxis = new SymbolAxis("", labels);
				rangeAxis.setAutoRangeIncludesZero(true);
				plot.setRangeAxis(rangeAxis);
				rangeAxis.setTickLabelFont(font);
			}

			final List<PlotItem> items = dataParserResult.dataset;

			if (!items.isEmpty()) {
				final XYSeriesCollection newDataset = (XYSeriesCollection) plotFrame.getChart().getXYPlot()
						.getDataset();
				final XYSeries newSeries = newDataset.getSeries(0);
				for (int i = 0; i < items.size(); i++) {
					final PlotItem item = items.get(i);
					newSeries.add(item.x, item.y);
				}

				plotFrame.setChartMaximum(newSeries.getMaxX(), newSeries.getMaxY());
				plotFrame.updateSliders();
			}

			if (isLastEntry) {
				final XYSeriesCollection newDataset = (XYSeriesCollection) plotFrame.getChart().getXYPlot()
						.getDataset();
				final XYSeries newSeries = newDataset.getSeries(0);
				if (newSeries.getItemCount() == 2) {
					@SuppressWarnings("unchecked")
					List<XYDataItem> seriesitems = newSeries.getItems();
					if (seriesitems.get(0).equals(seriesitems.get(1))) {
						newSeries.add(CurrentSimulator.getTime(), seriesitems.get(1).getY());
						plotFrame.setChartMaximum(newSeries.getMaxX(), newSeries.getMaxY());
						plotFrame.updateSliders();
					}
				} else if (newSeries.getItemCount() == 1) {
					@SuppressWarnings("unchecked")
					List<XYDataItem> seriesitems = newSeries.getItems();
					newSeries.add(CurrentSimulator.getTime(), seriesitems.get(0).getY());
					plotFrame.setChartMaximum(newSeries.getMaxX(), newSeries.getMaxY());
					plotFrame.updateSliders();
				}
			}
		}
	};

	private final RealTimeUpdateRunnable realTimeUpdateRunnable = new RealTimeUpdateRunnable(false);

	private final Subscriber commonSubcriber = new Subscriber() {
		@Override
		public void fireChange() {
			PlatformUI.getWorkbench().getDisplay().asyncExec(realTimeUpdateRunnable);
		}
	};

	private final Subscriber endSubscriber = new Subscriber() {
		@Override
		public void fireChange() {
			realTimeUpdateRunnable.changeLastValueFlag();
			PlatformUI.getWorkbench().getDisplay().asyncExec(realTimeUpdateRunnable);
		}
	};

	@Override
	public void setFocus() {
	}

	public void plotXY(final XYSeriesCollection dataset) {
		final JFreeChart chart = createChart(dataset);
		plotFrame.setChart(chart);
		plotFrame.setDomainZoomable(false);
		plotFrame.setRangeZoomable(false);
	}

	private JFreeChart createChart(final XYDataset dataset) {
		final JFreeChart chart = ChartFactory.createXYStepChart("", "Time", "Value", dataset, PlotOrientation.VERTICAL,
				true, true, false);

		final XYPlot plot = (XYPlot) chart.getPlot();

		Color white = new Color(0xFF, 0XFF, 0xFF);
		plot.setBackgroundPaint(white);
		Color grey = new Color(0x99, 0x99, 0x99);
		plot.setDomainGridlinePaint(grey);
		plot.setRangeGridlinePaint(grey);
		plot.getRenderer().setSeriesStroke(0, new BasicStroke((float) 2.5));

		NumberAxis rangeAxis = new NumberAxis();
		rangeAxis.setAutoRangeIncludesZero(true);
		plot.setRangeAxis(rangeAxis);

		final NumberAxis domainAxis = new NumberAxis();
		domainAxis.setAutoRangeIncludesZero(true);
		plot.setDomainAxis(domainAxis);

		double horizontalMaximum = domainAxis.getRange().getLength();
		double verticalMaximum = rangeAxis.getRange().getLength();
		plotFrame.setChartMaximum(horizontalMaximum, verticalMaximum);

		return chart;
	}

	static private class ExportCSVMenuItem extends ConditionalMenuItem {
		public ExportCSVMenuItem(Menu parent) {
			super(parent, "Export to CSV");
		}

		@Override
		public boolean isEnabled(CollectedDataNode node) {
			Index index = node.getIndex();

			if (PlotView.getOpenedPlotMap().containsKey(node)) {
				if (index == null)
					return false;

				switch (index.getType()) {
				case RESOURCE_PARAMETER:
					return true;
				case RESULT:
					return true;
				case PATTERN:
					PatternIndex patternIndex = (PatternIndex) index;
					int patternNumber = patternIndex.getNumber();
					String patternType = CurrentSimulator.getStaticModelData().getPatternType(patternNumber);
					return patternType.equals(ModelStructureConstants.OPERATION);
				default:
					return false;
				}
			} else
				return false;
		}

		@Override
		public void show(CollectedDataNode node) {
			FileDialog fileDialog = new FileDialog(getDisplay().getActiveShell(), SWT.SAVE);
			fileDialog.setText("Save");
			String[] filter = { "*.csv", "*.*" };
			fileDialog.setFilterExtensions(filter);

			String fileName = fileDialog.open();
			if (fileName != null && fileName.length() > 0) {
				PlotDataParser parser = new PlotDataParser(node);
				DataParserResult result = parser.parseEntries();

				try (Writer writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(fileName), "UTF-8"))) {
					writer.write("x,y\n");
					for (PlotItem pi : result.dataset) {
						writer.write(String.valueOf(pi.x) + "," + String.valueOf(pi.y) + "\n");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
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
				return true;
			case PATTERN:
				PatternIndex patternIndex = (PatternIndex) index;
				int patternNumber = patternIndex.getNumber();
				String patternType = CurrentSimulator.getStaticModelData().getPatternType(patternNumber);
				return patternType.equals(ModelStructureConstants.OPERATION);
			default:
				return false;
			}
		}

		@Override
		public void show(CollectedDataNode node) {
			try {
				if (PlotView.getOpenedPlotMap().containsKey(node)) {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(PlotView.ID,
							String.valueOf(PlotView.getOpenedPlotMap().get(node)), IWorkbenchPage.VIEW_ACTIVATE);
				} else {
					PlotView newView = (PlotView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.showView(PlotView.ID, String.valueOf(secondaryID), IWorkbenchPage.VIEW_ACTIVATE);
					PlotView.addToOpenedPlotMap(node, secondaryID);
					secondaryID++;

					newView.initialize(node);
				}
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
	}

	static public ConditionalMenuItem createConditionalMenuItemExportCSV(Menu parent) {
		return new ExportCSVMenuItem(parent);
	}

	static public ConditionalMenuItem createConditionalMenuItemExport(Menu parent) {
		return new ExportMenuItem(parent);
	}

	static public ConditionalMenuItem createConditionalMenuItem(Menu parent) {
		return new PlotMenuItem(parent);
	}
}
