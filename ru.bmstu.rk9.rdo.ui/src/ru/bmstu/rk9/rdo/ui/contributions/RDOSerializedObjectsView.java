package ru.bmstu.rk9.rdo.ui.contributions;

import java.net.URL;
import java.util.List;
import java.util.TimerTask;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ru.bmstu.rk9.rdo.lib.CollectedDataNode;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.AbstractIndex;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.IndexType;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.ResourceIndex;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.ResultIndex;
import ru.bmstu.rk9.rdo.lib.Database.ResultType;
import ru.bmstu.rk9.rdo.lib.PlotDataParser;
import ru.bmstu.rk9.rdo.lib.PlotDataParser.PlotItem;
import ru.bmstu.rk9.rdo.lib.Simulator;
import ru.bmstu.rk9.rdo.lib.Subscriber;

public class RDOSerializedObjectsView extends ViewPart {

	static TreeViewer serializedObjectsTreeViewer;
	public static final String ID = "ru.bmstu.rk9.rdo.ui.RDOSerializedObjectsView";
	public static int secondaryID;

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
					final XYSeriesCollection dataset = new XYSeriesCollection();

					final XYSeries series = new XYSeries(String.valueOf(node
							.getName()));
					dataset.addSeries(series);
					final List<PlotItem> items = PlotDataParser
							.parseEntries(node);
					for (int i = 0; i < items.size(); i++) {
						final PlotItem item = items.get(i);
						series.add(item.x, item.y);
					}

					RDOPlotView.setName(String.valueOf(dataset.getSeriesKey(0)));
					PlatformUI
							.getWorkbench()
							.getActiveWorkbenchWindow()
							.getActivePage()
							.showView("ru.bmstu.rk9.rdo.ui.RDOPlotView",
									String.valueOf(secondaryID),
									IWorkbenchPage.VIEW_ACTIVATE);
					secondaryID++;

					RDOPlotView.plotXY(dataset);
				} catch (PartInitException e) {
					System.out.println("Code exception caught");
					e.printStackTrace();
				}

			}
		});
		serializedObjectsTreeViewer.getTree().setMenu(popupMenu);

		if (Simulator.isInitialized()) {
			commonUpdater.fireChange();
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
					if (!readyForInput())
						return;
					RDOSerializedObjectsView.serializedObjectsTreeViewer
							.refresh();
				}
			};

			@Override
			public void run() {
				if (haveNewRealTimeData && readyForInput()
						&& !display.isDisposed()) {
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
		CollectedDataNode node = (CollectedDataNode) element;
		AbstractIndex index = node.getIndex();
		URL url;
		Display display = PlatformUI.getWorkbench().getDisplay();

		if (index == null) {
			url = FileLocator.find(Platform.getBundle("ru.bmstu.rk9.rdo.ui"),
					new org.eclipse.core.runtime.Path(
							"icons/cross-small-white.png"), null);
		} else if (index.getType() == IndexType.RESOURCE
				&& ((ResourceIndex) index).isErased()) {
			url = FileLocator.find(Platform.getBundle("ru.bmstu.rk9.rdo.ui"),
					new org.eclipse.core.runtime.Path("icons/cross.png"), null);
		} else {
			url = FileLocator.find(Platform.getBundle("ru.bmstu.rk9.rdo.ui"),
					new org.eclipse.core.runtime.Path("icons/globe-small.png"),
					null);
		}

		ImageDescriptor img = ImageDescriptor.createFromURL(url);
		return new Image(display, img.getImageData());
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
