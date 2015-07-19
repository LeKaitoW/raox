package ru.bmstu.rk9.rao.ui.graph;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import javax.swing.JFrame;

import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.ui.PlatformUI;

import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.ui.graph.TreeBuilder.GraphInfo;
import ru.bmstu.rk9.rao.ui.graph.TreeBuilder.Node;

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

public class GraphFrame extends JFrame {

	private static final long serialVersionUID = 1668866556340389760L;

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ------------------------- REAL TIME OUTPUT -------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	private static volatile boolean haveNewRealTimeData = false;

	public static final Subscriber realTimeUpdater = new Subscriber() {
		@Override
		public void fireChange() {
			haveNewRealTimeData = true;
		}
	};

	final Map<Node, mxCell> vertexMap = new HashMap<Node, mxCell>();

	private void drawGraph(mxGraph graph, List<Node> nodeList, Node parentNode) {
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
				drawGraph(graph, nodeList,
						nodeList.get(parentNode.children.get(i).index));
	}

	final String solutionColor = "fillColor=32CD32;";
	final String strokeColor = "strokeColor=000000;";
	final String fontColor = "fontColor=000000;";

	public void colorNodes(Map<Node, mxCell> vertexMap, List<Node> nodeList,
			List<Node> solution) {
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

	private int lastAddedVertexIndex = 0;

	private void drawNewVertex(mxGraph graph, List<Node> nodeList, int dptNum) {
		GraphControl.treeBuilder.rwLock.readLock().lock();
		try {
			int lastAddedNodeIndex = GraphControl.treeBuilder
					.getLastAddedNodeIndexMap().get(dptNum);
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
								vertexMap.get(node.parent),
								vertexMap.get(node), strokeColor);
				}
			}
		} finally {
			GraphControl.treeBuilder.rwLock.readLock().unlock();
		}
	}

	final Font font;
	double scale = 1.0;

	public mxCell insertInfo(mxGraph graph, GraphInfo info) {
		final String solutionCost = "Стоимость решения: "
				+ Double.toString(info.solutionCost) + "\n";
		final String numOpened = "Количество раскрытых вершин: "
				+ Integer.toString(info.numOpened) + "\n";
		final String numNodes = "Количество вершин в графе: "
				+ Integer.toString(info.numNodes);
		final String text = solutionCost + numOpened + numNodes;

		mxRectangle bounds = mxUtils.getSizeForString(text, font, scale);

		final double delta = setWidth(frameDimension, 0.01);

		double width = bounds.getWidth() + delta;
		double height = bounds.getHeight() + delta;

		return (mxCell) graph.insertVertex(graph.getDefaultParent(), null,
				text, delta, delta, width, height);
	}

	public mxCell showCellInfo(mxGraph graph, mxCell cell) {
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

		final double delta = setWidth(frameDimension, 0.01);

		double width = bounds.getWidth() + delta;
		double height = bounds.getHeight() + delta;

		return (mxCell) graph.insertVertex(graph.getDefaultParent(), null,
				text, setWidth(frameDimension, 1) - width - (delta), delta,
				width, height);
	}

	private TimerTask graphFrameUpdateTimerTask;

	public TimerTask getGraphFrameUpdateTimerTask() {
		return graphFrameUpdateTimerTask;
	}

	private TimerTask graphFrameFinTimerTask;

	public TimerTask getGraphFrameFinTimerTask() {
		return graphFrameFinTimerTask;
	}

	boolean isFinished = false;
	Dimension frameDimension;
	int nodeWidth;
	int nodeHeight;
	int minNodeWidth;
	int nodeDistance;
	int levelDistance;

	private void setProportions(Dimension d) {
		nodeWidth = setWidth(d, 0.05);
		nodeHeight = setHeight(d, 0.05);
		minNodeWidth = setWidth(d, 0.01);
		nodeDistance = (int) (d.width * 0.01);
		levelDistance = (int) (d.height * 0.1);
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ------------------------------ ZOOM --------------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	int maxNumOfNodesInWidth;

	private void zoomToFit(mxGraph graph, mxCompactTreeLayout layout,
			mxGraphComponent graphComponent) {
		Dimension frameDimension = this.getSize();

		Object[] cells = vertexMap.values().toArray();
		mxRectangle graphBounds = graph.getBoundsForCells(cells, false, false,
				false);

		maxNumOfNodesInWidth = (int) ((graphBounds.getWidth() + 2 * nodeDistance) / (nodeWidth + 2 * nodeDistance));
		double period = frameDimension.getWidth() / maxNumOfNodesInWidth;

		if (nodeWidth < minNodeWidth) {
			nodeWidth = minNodeWidth;
			nodeHeight = nodeWidth;
			nodeDistance = nodeWidth;
		} else {
			nodeWidth = (int) (0.8 * period);
			nodeHeight = nodeWidth;
			nodeDistance = (int) (0.1 * period);
		}

		resizeGraph(graph, layout);
		setScrollsToCenter(graphComponent);
		graph.refresh();
	}

	private void setScrollsToCenter(mxGraphComponent graphComponent) {
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

	enum SizeType {
		NOTEXT, BRIEF, NORMAL, FULL
	}

	private SizeType checkSize() {
		if (nodeWidth <= minNodeWidth)
			return SizeType.NOTEXT;
		if (nodeWidth > medium.width && nodeWidth < large.width)
			return SizeType.NORMAL;
		if (nodeWidth > large.width)
			return SizeType.FULL;
		return SizeType.BRIEF;
	}

	private void setTextForVertexes() {
		SizeType type = checkSize();
		switch (type) {
		case NOTEXT:
			for (mxCell cell : vertexMap.values()) {
				Node node = (Node) cell.getValue();
				node.label = "";
				cell.setValue(node);
			}
			break;
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

	private void resizeGraph(mxGraph graph, mxCompactTreeLayout layout) {
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
			layout.setNodeDistance(nodeDistance);
			layout.execute(graph.getDefaultParent());
		} finally {
			graph.getModel().endUpdate();
		}
		graph.refresh();
	}

	final double zoomScale = 1.1;

	private void zoomIn(mxGraph graph, mxCompactTreeLayout layout) {
		nodeWidth *= zoomScale;
		nodeDistance *= zoomScale;
		levelDistance *= zoomScale;
		resizeGraph(graph, layout);
	}

	private void zoomOut(mxGraph graph, mxCompactTreeLayout layout) {
		nodeWidth /= zoomScale;
		nodeDistance /= zoomScale;
		levelDistance /= zoomScale;
		resizeGraph(graph, layout);
	}

	public GraphFrame(int dptNum, int width, int height) {
		this.setSize(width, height);
		setAspectRatio();

		final FontRegistry fontRegistry = PlatformUI.getWorkbench()
				.getThemeManager().getCurrentTheme().getFontRegistry();
		final String fontName = fontRegistry.get(
				PreferenceConstants.EDITOR_TEXT_FONT).getFontData()[0]
				.getName();
		font = new Font(fontName, Font.PLAIN, mxConstants.DEFAULT_FONTSIZE);

		frameDimension = this.getSize();
		setProportions(frameDimension);

		List<Node> nodeList;
		GraphControl.treeBuilder.rwLock.readLock().lock();
		try {
			nodeList = GraphControl.treeBuilder.listMap.get(dptNum);
		} finally {
			GraphControl.treeBuilder.rwLock.readLock().unlock();
		}

		final mxGraph graph = new mxGraph();
		graph.getModel().beginUpdate();
		try {
			drawGraph(graph, nodeList, nodeList.get(0));

			boolean isFinished;
			GraphControl.treeBuilder.rwLock.readLock().lock();
			try {
				isFinished = GraphControl.treeBuilder.dptSimulationInfoMap
						.get(dptNum).isFinished;
			} finally {
				GraphControl.treeBuilder.rwLock.readLock().unlock();
			}
			if (isFinished) {
				GraphInfo info = GraphControl.treeBuilder.infoMap.get(dptNum);
				List<Node> solution = GraphControl.treeBuilder.solutionMap
						.get(dptNum);
				colorNodes(vertexMap, nodeList, solution);
				insertInfo(graph, info);
			}
		} finally {
			graph.getModel().endUpdate();
		}

		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		graphComponent.setConnectable(false);
		graphComponent.zoomAndCenter();
		getContentPane().add(graphComponent);

		mxCompactTreeLayout layout = new mxCompactTreeLayout(graph, false);
		layout.setLevelDistance(levelDistance);
		layout.setNodeDistance(nodeDistance);
		layout.setEdgeRouting(false);
		layout.execute(graph.getDefaultParent());

		graphFrameUpdateTimerTask = new TimerTask() {
			@Override
			public void run() {
				if (!haveNewRealTimeData)
					return;
				haveNewRealTimeData = false;
				graph.getModel().beginUpdate();
				try {
					drawNewVertex(graph, nodeList, dptNum);

					Dimension frameDimension = getSize();

					Object[] cells = vertexMap.values().toArray();
					mxRectangle graphBounds = graph.getBoundsForCells(cells,
							false, false, false);
					if (graphBounds.getWidth() > frameDimension.getWidth()) {
						layout.setMoveTree(true);
						zoomToFit(graph, layout, graphComponent);
						graph.refresh();
					} else
						layout.execute(graph.getDefaultParent());
				} finally {
					graph.getModel().endUpdate();
				}
				boolean isFinished;

				GraphControl.treeBuilder.rwLock.readLock().lock();
				try {
					isFinished = GraphControl.treeBuilder.dptSimulationInfoMap
							.get(dptNum).isFinished;
				} finally {
					GraphControl.treeBuilder.rwLock.readLock().unlock();
				}

				if (isFinished)
					cancel();
			}
		};

		graphFrameFinTimerTask = new TimerTask() {
			@Override
			public void run() {
				GraphControl.treeBuilder.rwLock.readLock().lock();
				try {
					isFinished = GraphControl.treeBuilder.dptSimulationInfoMap
							.get(dptNum).isFinished;
					if (isFinished) {
						graph.getModel().beginUpdate();
						try {
							GraphInfo info = GraphControl.treeBuilder.infoMap
									.get(dptNum);
							List<Node> solution = GraphControl.treeBuilder.solutionMap
									.get(dptNum);
							colorNodes(vertexMap, nodeList, solution);
							insertInfo(graph, info);
						} finally {
							graph.getModel().endUpdate();
						}
						graph.refresh();
						cancel();
					}
				} finally {
					GraphControl.treeBuilder.rwLock.readLock().unlock();
				}
			}
		};

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

		this.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.isControlDown() && e.getKeyChar() == '+') {
					zoomIn(graph, layout);
				}
				if (e.isControlDown() && e.getKeyChar() == '-') {
					zoomOut(graph, layout);
				}
				if (e.isControlDown() && e.getKeyChar() != 'z'
						&& e.getKeyCode() == 90) {
					Dimension frameDimension = getSize();
					Object[] cells = vertexMap.values().toArray();
					mxRectangle graphBounds = graph.getBoundsForCells(cells,
							false, false, false);
					while ((graphBounds.getWidth() >= frameDimension.getWidth())
							&& (nodeWidth > minNodeWidth)) {
						zoomToFit(graph, layout, graphComponent);
					}
				}
			}
		});
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ----------------------- RELATIVE COORINATES ------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	private double frameAspectRatio;

	private void setAspectRatio() {
		frameAspectRatio = (double) this.getWidth() / this.getHeight();
	}

	public int getAbsoluteX(double relativeX) {
		return (int) (this.getSize().width * relativeX);
	}

	public int getAbsoluteY(double relativeY) {
		return (int) (this.getSize().height * relativeY);
	}

	public int setWidth(Dimension d, double relativeWidth) {
		return (int) (d.width * relativeWidth / frameAspectRatio);
	}

	public int setHeight(Dimension d, double relativeHeight) {
		return (int) (d.height * relativeHeight);
	}
}
