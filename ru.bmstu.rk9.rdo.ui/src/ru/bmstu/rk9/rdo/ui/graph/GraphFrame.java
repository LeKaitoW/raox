package ru.bmstu.rk9.rdo.ui.graph;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import ru.bmstu.rk9.rdo.lib.Simulator;
import ru.bmstu.rk9.rdo.lib.Subscriber;
import ru.bmstu.rk9.rdo.lib.TreeBuilder.GraphInfo;
import ru.bmstu.rk9.rdo.lib.TreeBuilder.Node;

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

	/*-----------------------REAL TIME OUTPUT-----------------------*/

	private static volatile boolean haveNewRealTimeData = false;

	public static final Subscriber realTimeUpdater = new Subscriber() {
		@Override
		public void fireChange() {
			haveNewRealTimeData = true;
		}
	};

	/*--------------------------------------------------------------*/

	final Map<Node, mxCell> vertexMap = new HashMap<Node, mxCell>();

	private void drawGraph(mxGraph graph, ArrayList<Node> nodeList,
			Node parentNode) {
		mxCell vertex = (mxCell) graph.insertVertex(graph.getDefaultParent(),
				null, parentNode, 0, 0, 30, 30, fontColor + strokeColor);
		vertexMap.put(parentNode, vertex);
		lastAddedVertexIndex = parentNode.index;
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

	public void colorNodes(Map<Node, mxCell> vertexMap,
			ArrayList<Node> nodeList, ArrayList<Node> solution) {
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

	private void drawNewVertex(mxGraph graph, ArrayList<Node> nodeList,
			int dptNum) {
		Simulator.getTreeBuilder().rwLock.readLock().lock();
		try {
			int lastAddedNodeIndex = Simulator.getTreeBuilder()
					.getLastAddedNodeIndexMap().get(dptNum);
			for (int i = lastAddedVertexIndex; i <= lastAddedNodeIndex; i++) {
				Node node = nodeList.get(i);
				if (!vertexMap.containsKey(node)) {
					mxCell vertex = (mxCell) graph.insertVertex(
							graph.getDefaultParent(), null, node, 0, 0, 30,
							30, fontColor + strokeColor);
					vertexMap.put(node, vertex);
					lastAddedVertexIndex = node.index;
					if (node.parent != null)
						graph.insertEdge(graph.getDefaultParent(), null, null,
								vertexMap.get(node.parent),
								vertexMap.get(node), strokeColor);
				}
			}
		} finally {
			Simulator.getTreeBuilder().rwLock.readLock().unlock();
		}
	}

	public mxCell insertInfo(mxGraph graph, GraphInfo info) {
		final String solutionCost = "Стоимость решения: "
				+ Double.toString(info.solutionCost) + "\n";
		final String numOpened = "Количество раскрытых вершин: "
				+ Integer.toString(info.numOpened) + "\n";
		final String numNodes = "Количество вершин в графе: "
				+ Integer.toString(info.numNodes);
		final String text = solutionCost + numOpened + numNodes;

		final int fontSize = mxConstants.DEFAULT_FONTSIZE;

		final int style = Font.PLAIN;
		final Font font = new Font("Arial", style, fontSize);
		double scale = 1.0;
		mxRectangle bounds = mxUtils.getSizeForString(text, font, scale);

		final double delta = 20;

		double width = bounds.getWidth() + delta;
		double height = bounds.getHeight() + delta;

		return (mxCell) graph.insertVertex(graph.getDefaultParent(), null,
				text, delta / 2, delta / 2, width, height);
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
		final String ruleName = cellNode.ruleName + "\n";
		final String ruleCost = "Стоимость применения правила = "
				+ Double.toString(cellNode.ruleCost);
		final String text = nodeIndex + parentIndex + g + h + ruleName
				+ ruleCost;

		final int fontSize = mxConstants.DEFAULT_FONTSIZE;

		final int style = Font.PLAIN;
		Font font = new Font("Arial", style, fontSize);
		double scale = 1.0;
		mxRectangle bounds = mxUtils.getSizeForString(text, font, scale);

		final double delta = 20;

		double width = bounds.getWidth() + delta;
		double height = bounds.getHeight() + delta;

		return (mxCell) graph.insertVertex(graph.getDefaultParent(), null,
				text, 800 - width - (delta / 2), delta / 2, width, height);
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

	int maxNumOfNodesInWidth;

	double nodeWidth = 30;

	double delta = nodeWidth / 2;
	
	double minNodeWidth = 5;

	private void zoomToF(mxGraph graph, mxCompactTreeLayout layout,
			mxGraphComponent graphComponent) {
		Dimension frameDimension = this.getSize();

		Object[] cells = vertexMap.values().toArray();
		mxRectangle graphBounds = graph.getBoundsForCells(cells, false, false,
				false);

		maxNumOfNodesInWidth = (int) ((graphBounds.getWidth() + 2 * delta) / (nodeWidth + 2 * delta));

		double period = frameDimension.getWidth() / maxNumOfNodesInWidth;

		if (nodeWidth < minNodeWidth) {
			nodeWidth = minNodeWidth;
			delta = nodeWidth;
		} else {
			nodeWidth = 0.8 * period;
			delta = 0.1 * period;
		}

		mxRectangle bound = new mxRectangle(0, 0, nodeWidth, nodeWidth);
		mxRectangle[] bounds = new mxRectangle[vertexMap.size()];
		for (int i = 0; i < vertexMap.size(); i++) {
			bounds[i] = bound;
		}

		graph.getModel().beginUpdate();
		try {
			graphComponent.setAlignmentX(0);
			graph.resizeCells(cells, bounds);
			layout.setNodeDistance((int) delta);
			layout.execute(graph.getDefaultParent());
		} finally {
			graph.getModel().endUpdate();
		}
		layout.execute(graph.getDefaultParent());
		setScrollsToCenter(graphComponent);
		graph.refresh();
	}
	
	private void setScrollsToCenter(mxGraphComponent graphComponent) {
		Rectangle paneBounds = graphComponent.getViewport().getViewRect();
		Dimension paneSize = graphComponent.getViewport().getViewSize();
		
		int x = (paneSize.width - paneBounds.width) / 2;
		int y = (paneSize.height - paneBounds.height) / 2;
		graphComponent.getViewport().setViewPosition(new Point(x,y));
	}

	public GraphFrame(int dptNum, int width, int height) {

		this.setSize(width, height);

		ArrayList<Node> nodeList;

		Simulator.getTreeBuilder().rwLock.readLock().lock();
		try {
			nodeList = Simulator.getTreeBuilder().listMap.get(dptNum);
		} finally {
			Simulator.getTreeBuilder().rwLock.readLock().unlock();
		}

		final mxGraph graph = new mxGraph();

		graph.getModel().beginUpdate();
		try {
			drawGraph(graph, nodeList, nodeList.get(0));

			boolean isFinished;

			Simulator.getTreeBuilder().rwLock.readLock().lock();
			try {
				isFinished = Simulator.getTreeBuilder().dptSimulationInfoMap
						.get(dptNum).isFinished;
			} finally {
				Simulator.getTreeBuilder().rwLock.readLock().unlock();
			}
			if (isFinished) {
				GraphInfo info = Simulator.getTreeBuilder().infoMap.get(dptNum);
				ArrayList<Node> solution = Simulator.getTreeBuilder().solutionMap
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

		layout.setLevelDistance(40);
		layout.setNodeDistance((int) delta);
		layout.setEdgeRouting(false);

		layout.execute(graph.getDefaultParent());

		graphFrameUpdateTimerTask = new TimerTask() {
			@Override
			public void run() {
				if (!haveNewRealTimeData)
					return;
				haveNewRealTimeData = false;
				graph.getModel().beginUpdate();
				boolean updated = false;
				try {
					drawNewVertex(graph, nodeList, dptNum);

					Dimension frameDimension = getSize();

					Object[] cells = vertexMap.values().toArray();
					mxRectangle graphBounds = graph.getBoundsForCells(cells,
							false, false, false);
					if (graphBounds.getWidth() > frameDimension.getWidth()) {
						updated = true;
						// System.out.println("g b width = " +
						// graphBounds.getWidth());
						// System.out.println("f b width = " +
						// frameDimension.getWidth());
						zoomToF(graph, layout, graphComponent);
						graph.setMaximumGraphBounds(new mxRectangle(
								frameDimension.getWidth() / 2, frameDimension
										.getHeight() / 2, frameDimension
										.getWidth(), frameDimension.getHeight()));
						layout.execute(graph.getDefaultParent());
						graph.refresh();
					} else
						layout.execute(graph.getDefaultParent());
				} finally {
					graph.getModel().endUpdate();
//					if (updated) {
//						Object[] cells = vertexMap.values().toArray();
//						System.out.println("g a width = "
//								+ graph.getBoundsForCells(cells, false, false,
//										false).getWidth());
//						Dimension frameDimension = getSize();
//						System.out.println("f a width = "
//								+ frameDimension.getWidth());
//					}
				}
				boolean isFinished;

				Simulator.getTreeBuilder().rwLock.readLock().lock();
				try {
					isFinished = Simulator.getTreeBuilder().dptSimulationInfoMap
							.get(dptNum).isFinished;
				} finally {
					Simulator.getTreeBuilder().rwLock.readLock().unlock();
				}

				if (isFinished)
					cancel();
			}
		};

		graphFrameFinTimerTask = new TimerTask() {
			@Override
			public void run() {
				Simulator.getTreeBuilder().rwLock.readLock().lock();
				try {
					isFinished = Simulator.getTreeBuilder().dptSimulationInfoMap
							.get(dptNum).isFinished;
					if (isFinished) {
						graph.getModel().beginUpdate();
						try {
							GraphInfo info = Simulator.getTreeBuilder().infoMap
									.get(dptNum);
							ArrayList<Node> solution = Simulator
									.getTreeBuilder().solutionMap.get(dptNum);
							colorNodes(vertexMap, nodeList, solution);
							insertInfo(graph, info);
						} finally {
							graph.getModel().endUpdate();
						}
						graph.refresh();
						cancel();
					}
				} finally {
					Simulator.getTreeBuilder().rwLock.readLock().unlock();
				}
			}
		};

		graph.setCellsEditable(false);
		graph.setCellsDisconnectable(false);
		graph.setDisconnectOnMove(false);
		graph.setDropEnabled(false);
		graph.setCellsResizable(false);
		graph.setAllowDanglingEdges(false);

		// TODO temporarily commented
		// double height = graphInfoCell.getGeometry().getHeight();

		graph.getSelectionModel().addListener(mxEvent.CHANGE,
				new mxIEventListener() {

					private mxCell cellInfo;

					@Override
					public void invoke(Object sender, mxEventObject evt) {
						mxGraphSelectionModel selectionModel = (mxGraphSelectionModel) sender;
						mxCell cell = (mxCell) selectionModel.getCell();
						// TODO temporarily commented
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
				if (e.getKeyChar() == 'i') {
					System.err.println("ZOOM in");
					graphComponent.zoomIn();
				}
				if (e.getKeyChar() == 'j') {
					System.err.println("ZOOM out");
					graphComponent.zoomOut();
				}

				if (e.getKeyChar() == 'k') {
					mxRectangle bound = new mxRectangle(0, 0, 5, 5);
					mxRectangle[] bounds = new mxRectangle[vertexMap.size()];
					for (int i = 0; i < vertexMap.size(); i++) {
						bounds[i] = bound;
					}
					Object[] cells = new mxCell[vertexMap.size()];
					cells = vertexMap.values().toArray();
					graph.getModel().beginUpdate();
					try {
						graph.resizeCells(cells, bounds);
					} finally {
						graph.getModel().endUpdate();
					}
					layout.execute(graph.getDefaultParent());
					graph.refresh();
				}
				if (e.getKeyChar() == 'p') {
					System.err.println("ppp");
					Object[] cells = vertexMap.values().toArray();
					mxRectangle bounds = graph.getBoundsForCells(cells, false,
							false, false);
					System.err.println("bounds: " + bounds.toString());
					graph.insertVertex(graph.getDefaultParent(), null, null, 0,
							0, bounds.getWidth(), bounds.getHeight(), fontColor
									+ strokeColor + "opacity=20;");
				}
				if (e.getKeyChar() == 'z') {
					zoomToF(graph, layout, graphComponent);
				}
				if (e.getKeyChar() == 'e') {
					graph.repaint();
					System.err.println("repaint");
				}
			}
		});
	}
}
