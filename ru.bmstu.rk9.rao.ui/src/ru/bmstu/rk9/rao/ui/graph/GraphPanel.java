package ru.bmstu.rk9.rao.ui.graph;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;

import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;

import ru.bmstu.rk9.rao.lib.database.CollectedDataNode;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.Index;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.IndexType;
import ru.bmstu.rk9.rao.lib.notification.Notifier;
import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.Simulator.ExecutionState;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager.SimulatorSubscriberInfo;
import ru.bmstu.rk9.rao.ui.graph.GraphControl.FrameInfo;
import ru.bmstu.rk9.rao.ui.graph.GraphInfoWindow.InfoElement;
import ru.bmstu.rk9.rao.ui.graph.TreeBuilder.GraphInfo;
import ru.bmstu.rk9.rao.ui.graph.TreeBuilder.Node;
import ru.bmstu.rk9.rao.ui.notification.RealTimeSubscriberManager;
import ru.bmstu.rk9.rao.ui.serialization.SerializedObjectsView.ConditionalMenuItem;

public class GraphPanel extends JPanel implements GraphApi {

	private static final long serialVersionUID = 1668866556340389760L;

	static private class GraphMenuItem extends ConditionalMenuItem {

		public GraphMenuItem(Menu parent) {
			super(parent, "Graph");
		}

		@Override
		public boolean isEnabled(CollectedDataNode node) {
			Index index = node.getIndex();
			if (index == null)
				return false;

			return index.getType() == IndexType.SEARCH;
		}

		@Override
		public void show(CollectedDataNode node) {
			Index index = node.getIndex();
			if (index == null)
				return;

			int dptNumber = index.getNumber();
			String frameName = node.getName();
			FrameInfo frameInfo = new FrameInfo(dptNumber, frameName);
			GraphControl.openFrameWindow(frameInfo);
		}
	}

	static public ConditionalMenuItem createConditionalMenuItem(Menu parent) {
		return new GraphMenuItem(parent);
	}

	@Override
	public mxGraph getGraph() {
		return graph;
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// -------------------------- INITIALIZATION --------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	private final static int minNodeSize = 20;
	private final static int sizeToNodeDistanceRatio = 10;
	private final static int sizeToLevelDistanceRatio = 4;
	private final static int minNodeDistance = minNodeSize / sizeToNodeDistanceRatio;
	private final static int minLevelDistance = minNodeSize / sizeToLevelDistanceRatio;
	private final static int minLevelOffset = 40;

	private final static int fontSize = mxConstants.DEFAULT_FONTSIZE;

	private final static String solutionFillColor = mxConstants.STYLE_FILLCOLOR + "=32CD32;";
	private final static String strokeColor = mxConstants.STYLE_STROKECOLOR + "=000000;";
	private final static String fontColor = mxConstants.STYLE_FONTCOLOR + "=000000;";

	private final static String regularCellStyle = strokeColor + fontColor;
	private final static String solutionCellStyle = solutionFillColor + strokeColor + fontColor;
	private final static String edgeStyle = strokeColor;

	private boolean isFinished = false;

	private int nodeSize;
	private int nodeDistance;
	private int levelDistance;

	private final Font font;
	private final double scale = 1.0;

	private final GraphShell graphShell;

	public GraphPanel(int dptNum, GraphShell shell) {
		this.graphShell = shell;
		final FontRegistry fontRegistry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme()
				.getFontRegistry();
		final String fontName = fontRegistry.get(PreferenceConstants.EDITOR_TEXT_FONT).getFontData()[0].getName();
		font = new Font(fontName, Font.PLAIN, fontSize);

		setProportions();

		treeBuilder = new TreeBuilder(dptNum);
		isFinished = treeBuilder.updateTree();

		graph = new mxGraph();
		graph.getModel().beginUpdate();
		try {
			drawNewVertices();
			if (isFinished)
				onFinish();
		} finally {
			graph.getModel().endUpdate();
		}

		graphComponent = new mxGraphComponent(graph);
		graphComponent.setConnectable(false);
		graphComponent.setFont(font);
		graphComponent.zoomAndCenter();

		setLayout(new BorderLayout());
		add(BorderLayout.CENTER, graphComponent);
		validate();

		layout = new mxCompactTreeLayout(graph, false);
		layout.setLevelDistance(levelDistance);
		layout.setNodeDistance(nodeDistance);
		layout.setEdgeRouting(false);
		layout.execute(graph.getDefaultParent());

		graph.setCellsEditable(false);
		graph.setCellsDisconnectable(false);
		graph.setDisconnectOnMove(false);
		graph.setDropEnabled(false);
		graph.setCellsResizable(false);
		graph.setAllowDanglingEdges(false);

		addComponentListener(zoomToFitComponentListener);
		addKeyListener(zoomKeyListener);
		graphComponent.addKeyListener(zoomKeyListener);
		graphComponent.getGraphControl().addMouseListener(mouseListener);

		graph.getSelectionModel().addListener(mxEvent.CHANGE, new mxIEventListener() {
			@Override
			public void invoke(Object sender, mxEventObject evt) {
				mxCell cell = GraphUtil.getSelectionVertex(graph);
				if (cell == null)
					return;
				updateButtonState(cell);
				updateInfo(cell);
			}
		});

		graphEventNotifier.addSubscriber(graphInfoWindowOpenedSubscriber, GraphEvent.GRAPHINFO_WINDOW_OPENED);
		if (!isFinished)
			initializeSubscribers();
	}

	final void onDispose() {
		if (!isFinished)
			deinitializeSubscribers();

		if (graphInfoAccessible()) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(() -> graphInfoWindow.close());
		}
	}

	public final ComponentListener getZoomToFitComponentListener() {
		return zoomToFitComponentListener;
	}

	private final ComponentListener zoomToFitComponentListener = new ComponentListener() {
		@Override
		public void componentShown(ComponentEvent e) {
			if (!treeBuilder.nodeByNumber.isEmpty())
				zoomToFit();
		}

		@Override
		public void componentResized(ComponentEvent e) {
			if (!treeBuilder.nodeByNumber.isEmpty())
				zoomToFit();
		}

		@Override
		public void componentMoved(ComponentEvent e) {
		}

		@Override
		public void componentHidden(ComponentEvent e) {
		}
	};

	private final MouseListener mouseListener = new MouseListener() {
		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			mxCell cell = GraphUtil.getSelectionVertex(graph);
			if (cell == null || e.getClickCount() < 2)
				return;

			PlatformUI.getWorkbench().getDisplay().syncExec(() -> openGraphInfo());

		}
	};

	public final KeyListener getZoomKeyListener() {
		return zoomKeyListener;
	}

	private final KeyListener zoomKeyListener = new KeyListener() {
		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {
			char keyChar = e.getKeyChar();
			if (e.isControlDown() && (keyChar == '+' || keyChar == '=')) {
				zoomIn();
			}
			if (e.isControlDown() && (keyChar == '-' || keyChar == '_')) {
				zoomOut();
			}
			if (e.isControlDown() && (keyChar == '0' || keyChar == ')')) {
				zoomToFit();
			}
		}
	};

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// --------------------------- SUBSCRIPTIONS --------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	private final void initializeSubscribers() {
		simulationSubscriberManager.initialize(
				Arrays.asList(new SimulatorSubscriberInfo(commonSubscriber, ExecutionState.EXECUTION_STARTED),
						new SimulatorSubscriberInfo(commonSubscriber, ExecutionState.EXECUTION_COMPLETED)));
		realTimeSubscriberManager.initialize(Arrays.asList(realTimeUpdateRunnable));
	}

	final void deinitializeSubscribers() {
		simulationSubscriberManager.deinitialize();
		realTimeSubscriberManager.deinitialize();
	}

	private final SimulatorSubscriberManager simulationSubscriberManager = new SimulatorSubscriberManager();
	private final RealTimeSubscriberManager realTimeSubscriberManager = new RealTimeSubscriberManager();

	private final mxGraph graph;
	private final mxCompactTreeLayout layout;
	private final mxGraphComponent graphComponent;

	private final Subscriber commonSubscriber = new Subscriber() {
		@Override
		public void fireChange() {
			PlatformUI.getWorkbench().getDisplay().asyncExec(realTimeUpdateRunnable);
		}
	};

	private final Runnable realTimeUpdateRunnable = new Runnable() {
		@Override
		public void run() {
			if (isFinished)
				return;
			graph.getModel().beginUpdate();
			isFinished = treeBuilder.updateTree();
			try {
				drawNewVertices();
				mxRectangle graphBounds = graph.getBoundsForCells(vertexByNode.values().toArray(), false, false, false);
				if (graphBounds == null)
					return;

				if (graphBounds.getWidth() > getWidth()) {
					layout.setMoveTree(true);
					zoomToFit();
				} else {
					layout.execute(graph.getDefaultParent());
				}

				if (isFinished)
					onFinish();
			} finally {
				if (isFinished)
					deinitializeSubscribers();
				graph.getModel().endUpdate();
			}
		}
	};

	private final void onFinish() {
		colorNodes();

		setGraphInfo();
		graph.refresh();

		mxCell cell = GraphUtil.getSelectionVertex(graph);
		if (cell == null)
			return;
		updateButtonState(cell);
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ------------------------- GRAPH INFO WINDOW ------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	public enum GraphEvent {
		GRAPHINFO_WINDOW_OPENED
	};

	private final Notifier<GraphEvent> graphEventNotifier = new Notifier<>(GraphEvent.class);

	@Override
	public final Notifier<GraphEvent> getGraphEventNotifier() {
		return graphEventNotifier;
	}

	private final Subscriber graphInfoWindowOpenedSubscriber = new Subscriber() {
		@Override
		public void fireChange() {
			setGraphInfoButtonListeners();
			mxCell cell = GraphUtil.getSelectionVertex(graph);
			if (cell == null)
				return;
			updateInfo(cell);
			updateButtonState(cell);
		}
	};

	private GraphInfoWindow graphInfoWindow = null;

	@Override
	public final GraphInfoWindow getGraphInfoWindow() {
		return graphInfoWindow;
	}

	private final void setGraphInfoButtonListeners() {
		graphInfoWindow.getButtonNext().addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mxCell cell = GraphUtil.getSelectionVertex(graph);
				if (cell == null)
					return;

				List<Node> solutionList = treeBuilder.solutionList;
				Node node = (Node) cell.getValue();
				if (!solutionList.contains(node))
					return;

				int nodeIndex = solutionList.indexOf(node);
				if (nodeIndex == solutionList.size() - 1)
					return;
				Node nextNode = solutionList.get(nodeIndex + 1);

				mxCell nextCell = vertexByNode.get(nextNode);
				graph.setSelectionCell(nextCell);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		graphInfoWindow.getButtonPrevious().addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mxCell cell = GraphUtil.getSelectionVertex(graph);
				if (cell == null)
					return;

				Node node = (Node) cell.getValue();
				Node previousNode = node.parent;

				mxCell previousCell = vertexByNode.get(previousNode);
				graph.setSelectionCell(previousCell);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	public final boolean graphInfoAccessible() {
		return graphInfoWindow != null && graphInfoWindow.getShell() != null
				&& !graphInfoWindow.getShell().isDisposed();
	}

	private final void openGraphInfo() {
		if (graphInfoAccessible())
			return;

		graphInfoWindow = new GraphInfoWindow(graphShell.getShell());
		graphInfoWindow.setBlockOnOpen(false);
		graphInfoWindow.open();

		graphEventNotifier.notifySubscribers(GraphEvent.GRAPHINFO_WINDOW_OPENED);
		graphInfoWindow.getShell().setMinimumSize(graphInfoWindow.getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private final void updateButtonState(mxCell cell) {
		PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
			if (!graphInfoAccessible())
				return;

			List<Node> solutionList = treeBuilder.solutionList;
			Node node = (Node) cell.getValue();
			boolean inSolution = solutionList.contains(node);
			boolean nextEnabled = inSolution && solutionList.indexOf(node) != solutionList.size() - 1;
			boolean previousEnabled = node.parent != null;

			graphInfoWindow.getButtonNext().setEnabled(nextEnabled);
			graphInfoWindow.getButtonPrevious().setEnabled(previousEnabled);
		});
	}

	private final void updateInfo(mxCell cell) {
		Node node = (Node) cell.getValue();
		List<InfoElement> cellInfoList = getCellInfo(node);
		PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
			if (!graphInfoAccessible())
				return;

			setCellInfo(cellInfoList);
			setGraphInfo();
		});
	}

	private final List<InfoElement> getCellInfo(Node node) {
		List<InfoElement> cellInfoList = new ArrayList<InfoElement>();

		cellInfoList.add(new InfoElement("Node number", String.valueOf(node.index)));
		cellInfoList
				.add(new InfoElement("Parent number", node.parent == null ? "" : String.valueOf(node.parent.index)));
		cellInfoList.add(new InfoElement("Current path cost (g)", String.valueOf(node.g)));
		cellInfoList.add(new InfoElement("Remaining path cost (h)", String.valueOf(node.h)));
		cellInfoList.add(new InfoElement("Full path cost (f)", String.valueOf(node.h + node.g)));
		cellInfoList.add(new InfoElement("Rule used", node.ruleName));
		cellInfoList.add(new InfoElement("Relevant resources", node.relevantResources));
		cellInfoList.add(new InfoElement("Rule cost", String.valueOf(node.ruleCost)));

		return cellInfoList;
	}

	private final List<InfoElement> getGraphInfo() {
		List<InfoElement> graphInfoList = new ArrayList<InfoElement>();
		GraphInfo info = treeBuilder.graphInfo;

		graphInfoList.add(new InfoElement("Solution cost", String.valueOf(info.solutionCost)));
		graphInfoList.add(new InfoElement("Nodes opened", String.valueOf(info.numOpened)));
		graphInfoList.add(new InfoElement("Nodes total", String.valueOf(info.numNodes)));
		graphInfoList.add(new InfoElement("Max depth", String.valueOf(info.depth)));
		graphInfoList.add(new InfoElement("Max width", String.valueOf(info.width)));

		return graphInfoList;
	}

	private final void setCellInfo(List<InfoElement> cellInfo) {
		if (!graphInfoAccessible())
			return;

		graphInfoWindow.setCellInfoInput(cellInfo);
	}

	private final void setGraphInfo() {
		if (!isFinished)
			return;

		if (!graphInfoAccessible())
			return;

		graphInfoWindow.setGraphInfoInput(getGraphInfo());
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// --------------------------- VISUALISATION --------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	private final TreeBuilder treeBuilder;
	final Map<Node, mxCell> vertexByNode = new HashMap<Node, mxCell>();
	private int lastAddedVertexIndex = -1;

	private final void drawNewVertices() {
		int lastAddedNodeIndex = treeBuilder.lastAddedNodeIndex;

		while (lastAddedVertexIndex < lastAddedNodeIndex) {
			lastAddedVertexIndex++;

			Node node = treeBuilder.nodeByNumber.get(lastAddedVertexIndex);
			mxCell vertex = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, node, getAbsoluteX(0.5),
					getAbsoluteY(0.05), nodeSize, nodeSize, regularCellStyle);
			vertexByNode.put(node, vertex);
			updateTypicalDimensions(node);

			if (node.parent != null)
				graph.insertEdge(graph.getDefaultParent(), null, null, vertexByNode.get(node.parent),
						vertexByNode.get(node), edgeStyle);
		}
	}

	private final void colorNodes() {
		if (!treeBuilder.solutionList.isEmpty()) {
			for (Node node : treeBuilder.solutionList) {
				vertexByNode.get(node).setStyle(solutionCellStyle);
			}
		}
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ------------------------------ ZOOM --------------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	private final static double zoomScale = 1.2;

	private void setProportions() {
		setNodesSize(getRelativeWidth(0.05));
	}

	private final void setNodesSize(double size) {
		setNodesSize((int) size);
	}

	private final void setNodesSize(int size) {
		int levelDistance = size / sizeToLevelDistanceRatio;
		int nodeDistance = size / sizeToNodeDistanceRatio;

		this.levelDistance = levelDistance > minLevelDistance ? levelDistance : minLevelDistance;
		this.nodeSize = size > minNodeSize ? size : minNodeSize;
		this.nodeDistance = nodeDistance > minNodeDistance ? nodeDistance : minNodeDistance;
	}

	private final void zoomToFit() {
		// TODO implement fitInWidth() algorithm and choose
		// appropriate strategy depending on graph width a
		fitInDepth();
	}

	private final void fitInDepth() {
		int height = getHeight();
		int depth = treeBuilder.graphInfo.depth;

		double levelDistanceCoef = (sizeToLevelDistanceRatio + 1.0) / sizeToLevelDistanceRatio;
		double prefferredHeight = (height - minLevelOffset) / (depth * levelDistanceCoef);

		setNodesSize(prefferredHeight);

		resizeGraph();
		setViewOnRoot();
	}

	private final void setViewOnRoot() {
		Rectangle paneBounds = graphComponent.getViewport().getViewRect();
		mxRectangle graphBounds = graph.getBoundsForCells(vertexByNode.values().toArray(), false, false, false);

		int x = (int) ((graphBounds.getWidth() - paneBounds.width) / 2);

		if (x < 0)
			return;

		graphComponent.getViewport().setViewPosition(new Point(x, 0));
	}

	private final Dimension small = new Dimension();
	private final Dimension medium = new Dimension();
	private final Dimension large = new Dimension();

	private final void updateTypicalDimensions(Node node) {
		final String number = Integer.toString(node.index);
		final String costFunction = Double.toString(node.g + node.h) + " = " + Double.toString(node.g) + " + "
				+ Double.toString(node.h);
		final String rule = node.ruleName + " = " + Double.toString(node.ruleCost);
		final String shortRule = rule.replaceAll("\\(.*\\)", "");

		final String smallText = number;
		final String mediumText = number + "\n" + costFunction;
		final String largeText = mediumText + "\n" + shortRule;

		final mxRectangle smallBounds = mxUtils.getSizeForString(smallText, font, scale);
		final mxRectangle mediumBounds = mxUtils.getSizeForString(mediumText, font, scale);
		final mxRectangle largeBounds = mxUtils.getSizeForString(largeText, font, scale);

		if (smallBounds.getWidth() > small.width)
			small.setSize(smallBounds.getWidth(), smallBounds.getHeight());
		if (mediumBounds.getWidth() > medium.width)
			medium.setSize(mediumBounds.getWidth(), mediumBounds.getHeight());
		if (largeBounds.getWidth() > large.width)
			large.setSize(largeBounds.getWidth(), largeBounds.getHeight());
	}

	private enum SizeType {
		BRIEF, NORMAL, FULL
	}

	private final SizeType checkSize() {
		if (nodeSize > medium.width && nodeSize < large.width)
			return SizeType.NORMAL;
		if (nodeSize > large.width)
			return SizeType.FULL;

		return SizeType.BRIEF;
	}

	private final void setTextForVertexes() {
		SizeType type = checkSize();
		switch (type) {
		case BRIEF:
			for (mxCell cell : vertexByNode.values()) {
				Node node = (Node) cell.getValue();
				final String number = Integer.toString(node.index);
				node.label = number;
				cell.setValue(node);
			}
			break;
		case NORMAL:
			for (mxCell cell : vertexByNode.values()) {
				Node node = (Node) cell.getValue();
				final String number = Integer.toString(node.index);
				final String costFunction = Double.toString(node.g + node.h) + " = " + Double.toString(node.g) + " + "
						+ Double.toString(node.h);
				final String text = number + "\n" + costFunction;
				node.label = text;
				cell.setValue(node);
			}
			break;
		case FULL:
			for (mxCell cell : vertexByNode.values()) {
				Node node = (Node) cell.getValue();
				final String number = Integer.toString(node.index);
				final String costFunction = Double.toString(node.g + node.h) + " = " + Double.toString(node.g) + " + "
						+ Double.toString(node.h);
				final String rule = node.ruleName + " = " + Double.toString(node.ruleCost);
				final String shortRule = rule.replaceAll("\\(.*\\)", "");
				final String text = number + "\n" + costFunction + "\n" + shortRule;
				node.label = text;
				cell.setValue(node);
			}
			break;
		}
	}

	private final void resizeGraph() {
		final mxRectangle bound = new mxRectangle(0, 0, nodeSize, nodeSize);
		final mxRectangle[] bounds = new mxRectangle[vertexByNode.size()];
		for (int i = 0; i < vertexByNode.size(); i++) {
			bounds[i] = bound;
		}

		final Object[] cells = vertexByNode.values().toArray();
		graph.getModel().beginUpdate();
		try {
			graph.resizeCells(cells, bounds);
			setTextForVertexes();
			layout.setLevelDistance(levelDistance);
			layout.setNodeDistance(nodeDistance);
			layout.execute(graph.getDefaultParent());
		} finally {
			graph.getModel().endUpdate();
		}
	}

	private final void zoomIn() {
		setNodesSize(nodeSize * zoomScale);
		resizeGraph();
	}

	private final void zoomOut() {
		setNodesSize(nodeSize / zoomScale);
		resizeGraph();
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ----------------------- RELATIVE COORDINATES ------------------------ //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	private final double getAspectRatio() {
		return ((double) this.getWidth()) / this.getHeight();
	}

	public final int getAbsoluteX(double ratio) {
		return (int) (getWidth() * ratio);
	}

	public final int getAbsoluteY(double ratio) {
		return (int) (getHeight() * ratio);
	}

	public final int getRelativeWidth(double ratio) {
		return (int) (getWidth() * ratio / getAspectRatio());
	}

	public final int getRelativeHeight(double ratio) {
		return (int) (getHeight() * ratio);
	}
}
