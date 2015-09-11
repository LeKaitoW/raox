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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.resource.FontRegistry;
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
import com.mxgraph.view.mxGraphSelectionModel;

public class GraphView extends JFrame {

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
	private final static int minLevelOffset = 20;

	private final static int fontSize = mxConstants.DEFAULT_FONTSIZE;

	private final Font font;
	private final double scale = 1.0;

	private final static String solutionColor = "fillColor=32CD32;";
	private final static String strokeColor = "strokeColor=000000;";
	private final static String fontColor = "fontColor=000000;";

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
		nodeList = treeBuilder.nodeList;

		graph = new mxGraph();
		graph.getModel().beginUpdate();
		try {
			if (!nodeList.isEmpty())
				drawGraph(nodeList.get(0));
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

		graph.getSelectionModel().addListener(mxEvent.CHANGE,
				new mxIEventListener() {

					private mxCell cellInfo;

					@Override
					public void invoke(Object sender, mxEventObject evt) {
						mxGraphSelectionModel selectionModel = (mxGraphSelectionModel) sender;
						mxCell cell = (mxCell) selectionModel.getCell();
						if (cell != null && cell.isVertex() && cell != cellInfo) {
							graph.getModel().beginUpdate();
							try {
								if (cellInfo != null)
									cellInfo.removeFromParent();
								cellInfo = showCellInfo(graph, cell);
							} finally {
								graph.getModel().endUpdate();
							}
						}
					}
				});

		this.addComponentListener(componentListener);
		this.addKeyListener(keyListener);
		graphComponent.addKeyListener(keyListener);

		if (!isFinished)
			initializeSubscribers();
	}

	private final ComponentListener componentListener = new ComponentListener() {
		@Override
		public void componentShown(ComponentEvent e) {
			if (!nodeList.isEmpty())
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

	@Override
	public void dispose() {
		if (!isFinished)
			deinitializeSubscribers();
		super.dispose();
	}

	private final TreeBuilder treeBuilder;

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
	private List<Node> nodeList;
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
				drawNewVertex(graph, nodeList);
				Dimension frameDimension = getSize();
				Object[] cells = vertexMap.values().toArray();
				mxRectangle graphBounds = graph.getBoundsForCells(cells, false,
						false, false);
				if (graphBounds.getWidth() > frameDimension.getWidth()) {
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
		GraphInfo info = treeBuilder.graphInfo;
		List<Node> solution = treeBuilder.solutionList;
		colorNodes(solution);
		insertInfo(info);

		graph.refresh();
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// --------------------------- VISUALISATION --------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	final Map<Node, mxCell> vertexMap = new HashMap<Node, mxCell>();
	private int lastAddedVertexIndex = 0;

	/*
	 * TODO: two methods below looks the same, but are magically not
	 * interchangeable. They should be merged in one.
	 */
	private void drawGraph(Node parentNode) {
		mxCell vertex = (mxCell) graph.insertVertex(graph.getDefaultParent(),
				null, parentNode, getAbsoluteX(0.5), getAbsoluteY(0.05),
				nodeWidth, nodeHeight, fontColor + strokeColor);
		vertexMap.put(parentNode, vertex);
		lastAddedVertexIndex = parentNode.index;
		updateTypicalDimensions(parentNode);
		if (parentNode.parent != null)
			graph.insertEdge(graph.getDefaultParent(), null, null,
					vertexMap.get(parentNode.parent),
					vertexMap.get(parentNode), strokeColor);
		if (parentNode.children.size() != 0)
			for (int i = 0; i < parentNode.children.size(); i++)
				drawGraph(nodeList.get(parentNode.children.get(i).index));
	}

	private void drawNewVertex(mxGraph graph, List<Node> nodeList) {
		int lastAddedNodeIndex = treeBuilder.lastAddedNodeIndex;
		for (int i = lastAddedVertexIndex; i <= lastAddedNodeIndex; i++) {
			Node node = nodeList.get(i);
			if (!vertexMap.containsKey(node)) {
				mxCell vertex = (mxCell) graph.insertVertex(
						graph.getDefaultParent(), null, node,
						getAbsoluteX(0.5), getAbsoluteY(0.05), nodeWidth,
						nodeHeight, fontColor + strokeColor);
				vertexMap.put(node, vertex);
				lastAddedVertexIndex = node.index;
				updateTypicalDimensions(node);
				if (node.parent != null)
					graph.insertEdge(graph.getDefaultParent(), null, null,
							vertexMap.get(node.parent), vertexMap.get(node),
							strokeColor);
			}
		}
	}

	public void colorNodes(List<Node> solution) {
		if (!solution.isEmpty()) {
			Node rootNode = nodeList.get(0);
			vertexMap.get(rootNode).setStyle(
					solutionColor + fontColor + strokeColor);
			for (int i = 0; i < solution.size(); i++) {
				vertexMap.get(solution.get(i)).setStyle(
						solutionColor + fontColor + strokeColor);
			}
		}
	}

	public mxCell insertInfo(GraphInfo info) {
		final String solutionCost = "Стоимость решения: "
				+ Double.toString(info.solutionCost) + "\n";
		final String numOpened = "Количество раскрытых вершин: "
				+ Integer.toString(info.numOpened) + "\n";
		final String numNodes = "Количество вершин в графе: "
				+ Integer.toString(info.numNodes);
		final String text = solutionCost + numOpened + numNodes;

		mxRectangle bounds = mxUtils.getSizeForString(text, font, scale);

		final double delta = getRelativeWidth(0.01);

		double width = bounds.getWidth() + delta;
		double height = bounds.getHeight() + delta;

		return (mxCell) graph.insertVertex(graph.getDefaultParent(), null,
				text, delta, delta, width, height);
	}

	public mxCell showCellInfo(mxGraph graph, mxCell cell) {
		if (!(cell.getValue() instanceof Node))
			return null;

		Node cellNode = (Node) cell.getValue();
		final String nodeIndex = "Номер вершины: "
				+ Integer.toString(cellNode.index) + "\n";
		String parentIndex = "Корневая вершина\n";
		if (cellNode.parent != null) {
			parentIndex = "Номер вершины родителя: "
					+ Integer.toString(cellNode.parent.index) + "\n";
		}
		final String g = "Стоимость пути g = " + Double.toString(cellNode.g)
				+ "\n";
		final String h = "Стоимость оставшегося пути h = "
				+ Double.toString(cellNode.h) + "\n";
		final String ruleName = cellNode.ruleDesсription + "\n";
		final String ruleCost = "Стоимость применения правила = "
				+ Double.toString(cellNode.ruleCost);
		final String text = nodeIndex + parentIndex + g + h + ruleName
				+ ruleCost;

		mxRectangle bounds = mxUtils.getSizeForString(text, font, scale);

		final double delta = getRelativeWidth(0.01);

		double width = bounds.getWidth() + delta;
		double height = bounds.getHeight() + delta;

		return (mxCell) graph.insertVertex(graph.getDefaultParent(), null,
				text, getRelativeWidth(1) - width - (delta), delta, width,
				height);
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ------------------------------ ZOOM --------------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	private boolean isFinished = false;
	private int nodeWidth;
	private int nodeHeight;
	private int nodeDistance;
	private int levelDistance;

	private final static double zoomScale = 1.2;

	private void setProportions() {
		setNodesSize(getRelativeWidth(0.05), getWidth() * 0.01,
				getHeight() * 0.1);
	}

	private final void setNodesSize(double size, double nodeDistance,
			double levelDistance) {
		setNodesSize((int) size, (int) nodeDistance, (int) levelDistance);
	}

	private final void setNodesSize(int size, int nodeDistance,
			int levelDistance) {
		this.levelDistance = levelDistance > minLevelDistance ? levelDistance
				: minLevelDistance;
		this.nodeWidth = size > minNodeSize ? size : minNodeSize;
		this.nodeHeight = this.nodeWidth;
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
		double prefferredNodeDistance = prefferredHeight
				/ sizeToNodeDistanceRatio;
		double prefferredLevelDistance = prefferredHeight
				/ sizeToLevelDistanceRatio;

		setNodesSize(prefferredHeight, prefferredNodeDistance,
				prefferredLevelDistance);

		resizeGraph();
		setScrollsToCenter();
	}

	private void setScrollsToCenter() {
		Rectangle paneBounds = graphComponent.getViewport().getViewRect();
		Dimension paneSize = graphComponent.getViewport().getViewSize();

		int x = (paneSize.width - paneBounds.width) / 2;
		int y = (paneSize.height - paneBounds.height) / 2;
		graphComponent.getViewport().setViewPosition(new Point(x, y));
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
		if (nodeWidth > medium.width && nodeWidth < large.width)
			return SizeType.NORMAL;
		if (nodeWidth > large.width)
			return SizeType.FULL;

		return SizeType.BRIEF;
	}

	private void setTextForVertexes() {
		SizeType type = checkSize();
		switch (type) {
		case BRIEF:
			for (mxCell cell : vertexMap.values()) {
				Node node = (Node) cell.getValue();
				final String number = Integer.toString(node.index);
				node.label = number;
				cell.setValue(node);
			}
			break;
		case NORMAL:
			for (mxCell cell : vertexMap.values()) {
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
			for (mxCell cell : vertexMap.values()) {
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
		final mxRectangle bound = new mxRectangle(0, 0, nodeWidth, nodeWidth);
		final mxRectangle[] bounds = new mxRectangle[vertexMap.size()];
		for (int i = 0; i < vertexMap.size(); i++) {
			bounds[i] = bound;
		}

		final Object[] cells = vertexMap.values().toArray();
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
		setNodesSize(nodeWidth * zoomScale, nodeDistance * zoomScale,
				levelDistance * zoomScale);
		resizeGraph();
	}

	private void zoomOut() {
		setNodesSize(nodeWidth / zoomScale, nodeDistance / zoomScale,
				levelDistance / zoomScale);
		resizeGraph();
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ----------------------- RELATIVE COORINATES ------------------------- //
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
