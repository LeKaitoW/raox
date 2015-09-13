package ru.bmstu.rk9.rao.ui.graph;

import java.awt.Container;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;

import ru.bmstu.rk9.rao.lib.database.CollectedDataNode;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.Index;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.IndexType;
import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.Simulator.ExecutionState;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager.SimulatorSubscriberInfo;
import ru.bmstu.rk9.rao.ui.graph.GraphControl.FrameInfo;
import ru.bmstu.rk9.rao.ui.graph.TreeBuilder.GraphInfo;
import ru.bmstu.rk9.rao.ui.graph.TreeBuilder.Node;
import ru.bmstu.rk9.rao.ui.notification.RealTimeSubscriberManager;
import ru.bmstu.rk9.rao.ui.serialization.SerializedObjectsView.ConditionalMenuItem;

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

public class GraphView extends JFrame implements GraphApi {

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
	private final static int minNodeDistance = minNodeSize
			/ sizeToNodeDistanceRatio;
	private final static int minLevelDistance = minNodeSize
			/ sizeToLevelDistanceRatio;
	private final static int minLevelOffset = 40;

	private final static int fontSize = mxConstants.DEFAULT_FONTSIZE;

	private final static String solutionFillColor = mxConstants.STYLE_FILLCOLOR
			+ "=32CD32;";
	private final static String strokeColor = mxConstants.STYLE_STROKECOLOR
			+ "=000000;";
	private final static String fontColor = mxConstants.STYLE_FONTCOLOR
			+ "=000000;";

	private final static String regularCellStyle = strokeColor + fontColor;
	private final static String solutionCellStyle = solutionFillColor
			+ strokeColor + fontColor;
	private final static String edgeStyle = strokeColor;

	private boolean isFinished = false;

	private int nodeSize;
	private int nodeDistance;
	private int levelDistance;

	private final Font font;
	private final double scale = 1.0;

	public GraphView(int dptNum, int width, int height) {
		this.setSize(width, height);

		final FontRegistry fontRegistry = PlatformUI.getWorkbench()
				.getThemeManager().getCurrentTheme().getFontRegistry();
		final String fontName = fontRegistry.get(
				PreferenceConstants.EDITOR_TEXT_FONT).getFontData()[0]
				.getName();
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

		Container pane = getContentPane();
		pane.add(graphComponent);

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

		this.addComponentListener(componentListener);
		this.addKeyListener(keyListener);
		graphComponent.addKeyListener(keyListener);
		graphComponent.getGraphControl().addMouseListener(mouseListener);

		graph.getSelectionModel().addListener(mxEvent.CHANGE,
				new mxIEventListener() {
					@Override
					public void invoke(Object sender, mxEventObject evt) {
						mxCell cell = getSelectionVertex();
						if (cell == null)
							return;
						updateButtonState(cell);
					}
				});

		if (!isFinished)
			initializeSubscribers();
	}

	@Override
	public void dispose() {
		if (!isFinished)
			deinitializeSubscribers();

		if (graphInfoWindow != null && !graphInfoWindow.isDisposed()) {
			PlatformUI.getWorkbench().getDisplay()
					.asyncExec(() -> graphInfoWindow.close());
		}

		super.dispose();
	}

	private final mxCell getSelectionVertex() {
		if (graph.getSelectionCell() == null)
			return null;

		mxCell cell = (mxCell) graph.getSelectionCell();

		if (!cell.isVertex())
			return null;

		if (!(cell.getValue() instanceof Node))
			return null;

		return cell;
	}

	private final ComponentListener componentListener = new ComponentListener() {
		@Override
		public void componentShown(ComponentEvent e) {
			if (!treeBuilder.nodeByNumber.isEmpty())
				zoomToFit();
		}

		@Override
		public void componentResized(ComponentEvent e) {
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
			mxCell cell = getSelectionVertex();
			if (cell == null)
				return;

			if (e.getClickCount() > 1) {
				openGraphInfo();
			}

			updateInfo(cell);
			updateButtonState(cell);
		}
	};

	private final KeyListener keyListener = new KeyListener() {
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
		simulationSubscriberManager.initialize(Arrays.asList(
				new SimulatorSubscriberInfo(commonSubscriber,
						ExecutionState.EXECUTION_STARTED),
				new SimulatorSubscriberInfo(commonSubscriber,
						ExecutionState.EXECUTION_COMPLETED)));
		realTimeSubscriberManager.initialize(Arrays
				.asList(realTimeUpdateRunnable));
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
			PlatformUI.getWorkbench().getDisplay()
					.asyncExec(realTimeUpdateRunnable);
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
				mxRectangle graphBounds = graph.getBoundsForCells(vertexByNode
						.values().toArray(), false, false, false);
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

		createGraphInfo();
		graph.refresh();

		mxCell cell = getSelectionVertex();
		if (cell == null)
			return;
		updateButtonState(cell);
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ------------------------- GRAPH INFO WINDOW ------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	private GraphInfoWindow graphInfoWindow = null;
	private Label cellInfoLabel = null;
	private Label graphInfoLabel = null;
	private Group cellInfoGroup = null;
	private Group graphInfoGroup = null;

	public final GraphInfoWindow getGraphInfoWindow() {
		return graphInfoWindow;
	}

	private final void openGraphInfo() {
		Display display = PlatformUI.getWorkbench().getDisplay();

		if (graphInfoWindow != null && !graphInfoWindow.isDisposed()) {
			display.syncExec(() -> graphInfoWindow.forceActive());
		} else {
			display.syncExec(() -> {
				graphInfoWindow = new GraphInfoWindow(display);
				graphInfoWindow.open();

				graphInfoWindow.getButtonNext().addSelectionListener(
						new SelectionListener() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								mxCell cell = getSelectionVertex();
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
								updateInfo(nextCell);
							}

							@Override
							public void widgetDefaultSelected(SelectionEvent e) {
							}
						});

				graphInfoWindow.getButtonPrevious().addSelectionListener(
						new SelectionListener() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								mxCell cell = getSelectionVertex();
								if (cell == null)
									return;

								List<Node> solutionList = treeBuilder.solutionList;
								Node node = (Node) cell.getValue();
								if (!solutionList.contains(node))
									return;

								int nodeIndex = solutionList.indexOf(node);
								if (nodeIndex == 0)
									return;
								Node previousNode = solutionList
										.get(nodeIndex - 1);

								mxCell previousCell = vertexByNode
										.get(previousNode);
								graph.setSelectionCell(previousCell);
								updateInfo(previousCell);
							}

							@Override
							public void widgetDefaultSelected(SelectionEvent e) {
							}
						});
			});
		}
	}

	private final void updateButtonState(mxCell cell) {
		if (graphInfoWindow == null || graphInfoWindow.isDisposed())
			return;

		PlatformUI
				.getWorkbench()
				.getDisplay()
				.syncExec(
						() -> {
							List<Node> solutionList = treeBuilder.solutionList;
							Node node = (Node) cell.getValue();
							boolean inSolution = solutionList.contains(node);
							boolean inSolutionNext = inSolution
									&& solutionList.indexOf(node) != solutionList
											.size() - 1;
							boolean inSolutionPrevious = inSolution
									&& solutionList.indexOf(node) != 0;

							graphInfoWindow.getButtonNext().setEnabled(
									inSolutionNext);
							graphInfoWindow.getButtonPrevious().setEnabled(
									inSolutionPrevious);
						});
	}

	private final void updateInfo(mxCell cell) {
		Node node = (Node) cell.getValue();
		String cellInfo = getCellInfo(node);
		addInfo(cellInfo);
	}

	private final String getCellInfo(Node node) {
		String cellInfo = "";
		cellInfo += "Node number: " + String.valueOf(node.index) + "\n";

		String parentName = "Root";
		if (node.parent != null) {
			parentName = "Parent node number: "
					+ String.valueOf(node.parent.index);
		}
		parentName += "\n";

		cellInfo += parentName;
		cellInfo += "Path cost (g) = " + Double.toString(node.g) + "\n";
		cellInfo += "Remaining path cost (h) = " + Double.toString(node.h)
				+ "\n";
		cellInfo += node.ruleDesсription + "\n";
		cellInfo += "Rule cost = " + Double.toString(node.ruleCost);

		return cellInfo;
	}

	private final String getGraphInfo() {
		GraphInfo info = treeBuilder.graphInfo;
		String graphInfo = "";
		graphInfo += "Solution cost: " + String.valueOf(info.solutionCost)
				+ "\n";
		graphInfo += "Nodes opened: " + String.valueOf(info.numOpened) + "\n";
		graphInfo += "Nodes total: " + String.valueOf(info.numNodes) + "\n";
		graphInfo += "Max depth: " + String.valueOf(info.depth) + "\n";
		graphInfo += "Max width: " + String.valueOf(info.width);

		return graphInfo;
	}

	private final void createCellInfo(String cellInfo) {
		if (graphInfoWindow == null || graphInfoWindow.isDisposed())
			return;

		if (cellInfoGroup == null || cellInfoGroup.isDisposed()) {
			cellInfoGroup = new Group(graphInfoWindow.getInfoArea(), SWT.NONE);
			cellInfoGroup.setText("Cell Info");
			FormData cellInfoGroupData = new FormData();
			cellInfoGroupData.left = new FormAttachment(0, 0);
			cellInfoGroupData.top = new FormAttachment(0, 5);
			cellInfoGroup.setLayoutData(cellInfoGroupData);

			FillLayout cellInfoLayout = new FillLayout();
			cellInfoLayout.marginHeight = 2;
			cellInfoLayout.marginWidth = 4;
			cellInfoGroup.setLayout(cellInfoLayout);
			cellInfoLabel = new Label(cellInfoGroup, SWT.NONE);
		}

		cellInfoLabel.setText(cellInfo);
		graphInfoWindow.updateContents();
	}

	private final void createGraphInfo() {
		if (!isFinished)
			return;

		if (graphInfoWindow == null || graphInfoWindow.isDisposed())
			return;

		if (graphInfoGroup == null || graphInfoGroup.isDisposed()) {
			graphInfoGroup = new Group(graphInfoWindow.getInfoArea(), SWT.NONE);
			graphInfoGroup.setText("Graph Info");
			FormData graphInfoGroupData = new FormData();
			graphInfoGroupData.left = new FormAttachment(cellInfoGroup, 5);
			graphInfoGroupData.top = new FormAttachment(0, 5);
			graphInfoGroup.setLayoutData(graphInfoGroupData);

			FillLayout graphInfoLayout = new FillLayout();
			graphInfoLayout.marginHeight = 2;
			graphInfoLayout.marginWidth = 4;
			graphInfoGroup.setLayout(graphInfoLayout);
			graphInfoLabel = new Label(graphInfoGroup, SWT.NONE);
		}

		graphInfoLabel.setText(getGraphInfo());
		graphInfoWindow.updateContents();
	}

	private final void addInfo(String cellInfo) {
		Display display = PlatformUI.getWorkbench().getDisplay();

		display.syncExec(() -> {
			if (graphInfoWindow == null || graphInfoWindow.isDisposed())
				return;

			createCellInfo(cellInfo);
			createGraphInfo();
		});
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
			mxCell vertex = (mxCell) graph.insertVertex(
					graph.getDefaultParent(), null, node, getAbsoluteX(0.5),
					getAbsoluteY(0.05), nodeSize, nodeSize, regularCellStyle);
			vertexByNode.put(node, vertex);
			updateTypicalDimensions(node);

			if (node.parent != null)
				graph.insertEdge(graph.getDefaultParent(), null, null,
						vertexByNode.get(node.parent), vertexByNode.get(node),
						edgeStyle);
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

		this.levelDistance = levelDistance > minLevelDistance ? levelDistance
				: minLevelDistance;
		this.nodeSize = size > minNodeSize ? size : minNodeSize;
		this.nodeDistance = nodeDistance > minNodeDistance ? nodeDistance
				: minNodeDistance;
	}

	private void zoomToFit() {
		// TODO implement fitInWidth() algorithm and choose
		// appropriate strategy depending on graph width a
		fitInDepth();
	}

	private void fitInDepth() {
		int height = getContentPane().getHeight();
		int depth = treeBuilder.graphInfo.depth;

		double levelDistanceCoef = (sizeToLevelDistanceRatio + 1.0)
				/ sizeToLevelDistanceRatio;
		double prefferredHeight = (height - minLevelOffset)
				/ (depth * levelDistanceCoef);

		setNodesSize(prefferredHeight);

		resizeGraph();
		setViewOnRoot();
	}

	private void setViewOnRoot() {
		Rectangle paneBounds = graphComponent.getViewport().getViewRect();
		mxRectangle graphBounds = graph.getBoundsForCells(vertexByNode.values()
				.toArray(), false, false, false);

		int x = (int) ((graphBounds.getWidth() - paneBounds.width) / 2);

		if (x < 0)
			return;

		graphComponent.getViewport().setViewPosition(new Point(x, 0));
	}

	private Dimension small = new Dimension();
	private Dimension medium = new Dimension();
	private Dimension large = new Dimension();

	private void updateTypicalDimensions(Node node) {
		final String number = Integer.toString(node.index);
		final String costFunction = Double.toString(node.g + node.h) + " = "
				+ Double.toString(node.g) + " + " + Double.toString(node.h);
		final String rule = node.ruleDesсription + " = "
				+ Double.toString(node.ruleCost);
		final String shortRule = rule.replaceAll("\\(.*\\)", "");

		final String smallText = number;
		final String mediumText = number + "\n" + costFunction;
		final String largeText = mediumText + "\n" + shortRule;

		final mxRectangle smallBounds = mxUtils.getSizeForString(smallText,
				font, scale);
		final mxRectangle mediumBounds = mxUtils.getSizeForString(mediumText,
				font, scale);
		final mxRectangle largeBounds = mxUtils.getSizeForString(largeText,
				font, scale);

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

	private void setTextForVertexes() {
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
				final String costFunction = Double.toString(node.g + node.h)
						+ " = " + Double.toString(node.g) + " + "
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
				final String costFunction = Double.toString(node.g + node.h)
						+ " = " + Double.toString(node.g) + " + "
						+ Double.toString(node.h);
				final String rule = node.ruleDesсription + " = "
						+ Double.toString(node.ruleCost);
				final String shortRule = rule.replaceAll("\\(.*\\)", "");
				final String text = number + "\n" + costFunction + "\n"
						+ shortRule;
				node.label = text;
				cell.setValue(node);
			}
			break;
		}
	}

	private void resizeGraph() {
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

	private void zoomIn() {
		setNodesSize(nodeSize * zoomScale);
		resizeGraph();
	}

	private void zoomOut() {
		setNodesSize(nodeSize / zoomScale);
		resizeGraph();
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ----------------------- RELATIVE COORDINATES ------------------------ //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	private double getAspectRatio() {
		return ((double) this.getWidth()) / this.getHeight();
	}

	public int getAbsoluteX(double ratio) {
		return (int) (getWidth() * ratio);
	}

	public int getAbsoluteY(double ratio) {
		return (int) (getHeight() * ratio);
	}

	public int getRelativeWidth(double ratio) {
		return (int) (getWidth() * ratio / getAspectRatio());
	}

	public int getRelativeHeight(double ratio) {
		return (int) (getHeight() * ratio);
	}
}
