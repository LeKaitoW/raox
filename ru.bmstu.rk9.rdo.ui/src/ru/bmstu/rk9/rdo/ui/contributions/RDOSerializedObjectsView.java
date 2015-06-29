package ru.bmstu.rk9.rdo.ui.contributions;

import java.net.URL;
import java.util.List;
import java.util.TimerTask;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ru.bmstu.rk9.rdo.lib.CollectedDataNode;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.AbstractIndex;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.IndexType;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.PatternIndex;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.ResourceIndex;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.ResultIndex;
import ru.bmstu.rk9.rdo.lib.Database.ResultType;
import ru.bmstu.rk9.rdo.lib.PlotDataParser;
import ru.bmstu.rk9.rdo.lib.PlotDataParser.PlotItem;
import ru.bmstu.rk9.rdo.lib.Simulator;
import ru.bmstu.rk9.rdo.lib.Subscriber;
import ru.bmstu.rk9.rdo.ui.contributions.PlotView;
import ru.bmstu.rk9.rdo.ui.graph.GraphControl;
import ru.bmstu.rk9.rdo.ui.graph.GraphControl.FrameInfo;

public class RDOSerializedObjectsView extends ViewPart {

	static TreeViewer serializedObjectsTreeViewer;
	public static final String ID = "ru.bmstu.rk9.rdo.ui.RDOSerializedObjectsView";
	public static int secondaryID = 0;

	@Override
	public void createPartControl(Composite parent) {
		serializedObjectsTreeViewer = new TreeViewer(parent);
		Tree serializedObjectsTree = serializedObjectsTreeViewer.getTree();
		serializedObjectsTree.setLayoutData(new GridLayout());
		serializedObjectsTree.setLinesVisible(true);

		serializedObjectsTreeViewer
				.setContentProvider(new RDOSerializedObjectsContentProvider());
		serializedObjectsTreeViewer
				.setLabelProvider(new RDOSerializedObjectsLabelProvider());

		final Menu popupMenu = new Menu(serializedObjectsTreeViewer.getTree());
		final MenuItem graphMenuItem = new MenuItem(popupMenu, SWT.CASCADE);
		graphMenuItem.setText("Build graph");
		serializedObjectsTree.setMenu(popupMenu);

		popupMenu.addListener(SWT.Show, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				boolean enabled = false;

				final CollectedDataNode node = (CollectedDataNode) serializedObjectsTreeViewer
						.getTree().getSelection()[0].getData();
				IndexType type = node.getIndex().getType();

				if (type == IndexType.SEARCH) {
					enabled = true;
				}

				graphMenuItem.setEnabled(enabled);
			}
		});

		graphMenuItem.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				final CollectedDataNode node = (CollectedDataNode) serializedObjectsTreeViewer
						.getTree().getSelection()[0].getData();

				int dptNum = node.getIndex().getNumber();
				String frameName = node.getName();
				FrameInfo frameInfo = new FrameInfo(dptNum, frameName);
				
				GraphControl.openFrameWindow(frameInfo);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {

			}
		});

		final MenuItem plot = new MenuItem(popupMenu, SWT.CASCADE);
		plot.setText("Plot");

		popupMenu.addListener(SWT.Show, new Listener() {
			public void handleEvent(Event event) {
				final CollectedDataNode node = (CollectedDataNode) serializedObjectsTreeViewer
						.getTree().getSelection()[0].getData();
				final AbstractIndex index = node.getIndex();
				Boolean enabled = false;
				if (index != null) {
					switch (index.getType()) {
					case RESOURCE_PARAMETER:
						enabled = true;
						break;
					case RESULT:
						final ResultIndex resultIndex = (ResultIndex) index;
						final ResultType resultType = resultIndex
								.getResultType();
						if (resultType != ResultType.GET_VALUE) {
							enabled = true;
						}
						break;
					case PATTERN:
						final PatternIndex patternIndex = (PatternIndex) index;
						final String patternType = patternIndex.getStructrure()
								.getString("type");
						if (patternType.equals("operation")) {
							enabled = true;
						}
						break;
					default:
						break;
					}
				}
				plot.setEnabled(enabled);
			}
		});

		plot.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent event) {
				try {
					final CollectedDataNode node = (CollectedDataNode) serializedObjectsTreeViewer
							.getTree().getSelection()[0].getData();

					if (PlotView.getOpenedPlotMap().containsKey(node)) {
						PlatformUI
								.getWorkbench()
								.getActiveWorkbenchWindow()
								.getActivePage()
								.showView(
										PlotView.ID,
										String.valueOf(PlotView
												.getOpenedPlotMap().get(node)),
										IWorkbenchPage.VIEW_ACTIVATE);
					} else {
						final XYSeriesCollection dataset = new XYSeriesCollection();
						final XYSeries series = new XYSeries(node.getName());
						dataset.addSeries(series);
						final List<PlotItem> items = PlotDataParser
								.parseEntries(node);
						for (int i = 0; i < items.size(); i++) {
							final PlotItem item = items.get(i);
							series.add(item.x, item.y);
						}
						final PlotView newView = (PlotView) PlatformUI
								.getWorkbench()
								.getActiveWorkbenchWindow()
								.getActivePage()
								.showView(PlotView.ID,
										String.valueOf(secondaryID),
										IWorkbenchPage.VIEW_ACTIVATE);
						newView.setName(String.valueOf(dataset.getSeriesKey(0)));
						newView.setNode(node);
						PlotView.addToOpenedPlotMap(node, secondaryID);

						newView.plotXY(dataset,
								PlotDataParser.getEnumNames(node));
						secondaryID++;
					}
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		});

		serializedObjectsTreeViewer.getTree().setMenu(popupMenu);

		serializedObjectsTreeViewer
				.addDoubleClickListener(new IDoubleClickListener() {
					@Override
					public void doubleClick(DoubleClickEvent event) {
						final Object item = serializedObjectsTreeViewer
								.getTree().getSelection()[0].getData();
						if (item == null)
							return;

						if (serializedObjectsTreeViewer.getExpandedState(item))
							serializedObjectsTreeViewer
									.collapseToLevel(item, 1);
						else
							serializedObjectsTreeViewer.expandToLevel(item, 1);
					}
				});

		if (Simulator.isInitialized()) {
			commonUpdater.fireChange();
		}
	}

	private static final void updateAllOpenedCharts() {
		for (CollectedDataNode node : PlotView.getOpenedPlotMap().keySet()) {
			final int currentSecondaryID = PlotView.getOpenedPlotMap()
					.get(node);
			final IViewReference viewReference = PlatformUI
					.getWorkbench()
					.getActiveWorkbenchWindow()
					.getActivePage()
					.findViewReference(PlotView.ID,
							String.valueOf(currentSecondaryID));
			final PlotView viewPart = (PlotView) viewReference.getView(false);
			final List<PlotItem> items = PlotDataParser.parseEntries(node);

			if (!items.isEmpty()) {
				final XYSeriesCollection newDataset = (XYSeriesCollection) viewPart
						.getFrame().getChart().getXYPlot().getDataset();
				final XYSeries newSeries = newDataset.getSeries(0);
				for (int i = 0; i < items.size(); i++) {
					final PlotItem item = items.get(i);
					newSeries.add(item.x, item.y);
				}
				viewPart.getFrame().setChartMaximum(newSeries.getMaxX(),
						newSeries.getMaxY());
				viewPart.getFrame().updateSliders();
			}
		}
	}

	@Override
	public void setFocus() {
	}

	public final static boolean readyForInput() {
		return serializedObjectsTreeViewer != null
				&& !serializedObjectsTreeViewer.getTree().isDisposed()
				&& serializedObjectsTreeViewer.getContentProvider() != null
				&& serializedObjectsTreeViewer.getLabelProvider() != null;
	}

	private static boolean haveNewRealTimeData = false;

	public static final Subscriber realTimeUpdater = new Subscriber() {
		@Override
		public void fireChange() {
			haveNewRealTimeData = true;
		}
	};

	public static TimerTask getRealTimeUpdaterTask() {
		return new TimerTask() {
			private final Display display = PlatformUI.getWorkbench()
					.getDisplay();
			private final Runnable updater = new Runnable() {
				@Override
				public void run() {
					updateAllOpenedCharts();
					if (readyForInput()) {
						RDOSerializedObjectsView.serializedObjectsTreeViewer
								.refresh();
					}
				}
			};

			@Override
			public void run() {
				if (haveNewRealTimeData && !display.isDisposed()) {
					haveNewRealTimeData = false;
					display.asyncExec(updater);
				}
			}
		};
	}

	public static final Subscriber commonUpdater = new Subscriber() {
		@Override
		public void fireChange() {
			if (!readyForInput())
				return;

			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!readyForInput())
						return;

					CollectedDataNode root = Simulator.getDatabase()
							.getIndexHelper().getTree();

					PlatformUI
							.getWorkbench()
							.getDisplay()
							.asyncExec(
									() -> serializedObjectsTreeViewer
											.setInput(root));
					updateAllOpenedCharts();
				}
			});
		}
	};
}

class RDOSerializedObjectsContentProvider implements ITreeContentProvider {
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		CollectedDataNode root = (CollectedDataNode) inputElement;
		if (!root.hasChildren())
			return new Object[] {};
		return root.getChildren().values().toArray();
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		CollectedDataNode parent = (CollectedDataNode) parentElement;
		if (!parent.hasChildren())
			return new Object[] {};
		return parent.getChildren().values().toArray();
	}

	@Override
	public Object getParent(Object element) {
		CollectedDataNode node = (CollectedDataNode) element;
		return node.getParent();
	}

	@Override
	public boolean hasChildren(Object element) {
		CollectedDataNode node = (CollectedDataNode) element;
		return node.hasChildren();
	}
}

class RDOSerializedObjectsLabelProvider implements ILabelProvider,
		IColorProvider {
	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public Image getImage(Object element) {
		final CollectedDataNode collectedDataNode = (CollectedDataNode) element;
		final Display display = PlatformUI.getWorkbench().getDisplay();
		final ImageDescriptor img = ImageDescriptor
				.createFromURL(getImageUrl(collectedDataNode));

		return new Image(display, img.getImageData());
	}

	final private URL getImageUrl(final CollectedDataNode collectedDataNode) {
		final URL imageUrl;
		final AbstractIndex index = collectedDataNode.getIndex();

		if (index == null) {
			imageUrl = FileLocator.find(Platform
					.getBundle("ru.bmstu.rk9.rdo.ui"),
					new org.eclipse.core.runtime.Path(
							"icons/cross-small-white.png"), null);
		} else if (index.getType() == IndexType.RESOURCE
				&& ((ResourceIndex) index).isErased()) {
			imageUrl = FileLocator.find(
					Platform.getBundle("ru.bmstu.rk9.rdo.ui"),
					new org.eclipse.core.runtime.Path("icons/cross.png"), null);
		} else {
			imageUrl = FileLocator.find(
					Platform.getBundle("ru.bmstu.rk9.rdo.ui"),
					new org.eclipse.core.runtime.Path("icons/globe-small.png"),
					null);
		}

		return imageUrl;
	}

	@Override
	public String getText(Object element) {
		CollectedDataNode node = (CollectedDataNode) element;
		return node.getName();
	}

	@Override
	public Color getForeground(Object element) {
		CollectedDataNode node = (CollectedDataNode) element;
		AbstractIndex index = node.getIndex();
		if (index != null && index.getType() == IndexType.RESOURCE
				&& ((ResourceIndex) index).isErased()) {
			Display display = PlatformUI.getWorkbench().getDisplay();
			return new Color(display, 0x88, 0x88, 0x88);
		}
		return null;
	}

	@Override
	public Color getBackground(Object element) {
		return null;
	}
}
