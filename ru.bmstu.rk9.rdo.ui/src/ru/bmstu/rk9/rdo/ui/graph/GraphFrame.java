package ru.bmstu.rk9.rdo.ui.graph;

import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import javax.swing.JFrame;

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

	Map<Node, mxCell> vertexMap = new HashMap<Node, mxCell>();

	private void drawGraph(mxGraph graph, ArrayList<Node> nodeList, Node parentNode) {
		mxCell vertex = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, parentNode, 385, 100, 30, 30,
				fontColor + strokeColor);
		vertexMap.put(parentNode, vertex);
		lastAddedVertexIndex = parentNode.index;
		if (parentNode.parent != null)
			graph.insertEdge(graph.getDefaultParent(), null, null, vertexMap.get(parentNode.parent),
					vertexMap.get(parentNode), strokeColor);
		if (parentNode.children.size() != 0)
			for (int i = 0; i < parentNode.children.size(); i++)
				drawGraph(graph, nodeList, nodeList.get(parentNode.children.get(i).index));
	}

	final String solutionColor = "fillColor=32CD32;";
	final String strokeColor = "strokeColor=000000;";
	final String fontColor = "fontColor=000000;";
	
	public void colorNodes(Map<Node, mxCell> vertexMap, ArrayList<Node> nodeList, ArrayList<Node> solution) {
		System.out.println("solution is empty? " + solution.isEmpty());
		if (!solution.isEmpty()) {
			Node rootNode = nodeList.get(0);
			vertexMap.get(rootNode).setStyle(solutionColor + fontColor + strokeColor);
			for (int i = 0; i < solution.size(); i++) {
				String style = vertexMap.get(solution.get(i)).getStyle();
				System.out.println("style before = " + style);
				vertexMap.get(solution.get(i)).setStyle(solutionColor + fontColor + strokeColor);
				System.out.println("style after 1 = " + style);
				style = vertexMap.get(solution.get(i)).getStyle();
				System.out.println("style after 2 = " + style);
			}
		}
	}
	
	private int lastAddedVertexIndex = 0;

	private void drawNewVertex(mxGraph graph, ArrayList<Node> nodeList, int dptNum) {
		Simulator.getTreeBuilder().rwLock.readLock().lock();
		try {
			int lastAddedNodeIndex = Simulator.getTreeBuilder().getLastAddedNodeIndexMap().get(dptNum);
			for (int i = lastAddedVertexIndex; i <= lastAddedNodeIndex; i++) {
				Node node = nodeList.get(i);
				System.out.println("drawV lastAddedVertexIndex before = " + lastAddedVertexIndex + " " + "dpt = " + dptNum);
				if (!vertexMap.containsKey(node)) {
					mxCell vertex = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, node, 385, 100, 30, 30,
							fontColor + strokeColor);
					vertexMap.put(node, vertex);
					lastAddedVertexIndex = node.index;
					System.out.println("drawV lastAddedVertexIndex after = " + lastAddedVertexIndex + " " + "dpt = " + dptNum);
					if (node.parent != null)
						graph.insertEdge(graph.getDefaultParent(), null, null, vertexMap.get(node.parent),
								vertexMap.get(node), strokeColor);
				}
			}
		} finally {
			Simulator.getTreeBuilder().rwLock.readLock().unlock();
		}
	}

	public mxCell insertInfo(mxGraph graph, GraphInfo info) {
		final String solutionCost = "Стоимость решения: " + Double.toString(info.solutionCost) + "\n";
		final String numOpened = "Количество раскрытых вершин: " + Integer.toString(info.numOpened) + "\n";
		final String numNodes = "Количество вершин в графе: " + Integer.toString(info.numNodes);
		final String text = solutionCost + numOpened + numNodes;

		int fontSize = mxConstants.DEFAULT_FONTSIZE;

		int style = Font.PLAIN;
		Font font = new Font("Arial", style, fontSize);
		double scale = 1.0;
		mxRectangle bounds = mxUtils.getSizeForString(text, font, scale);

		final double delta = 20;

		double width = bounds.getWidth() + delta;
		double height = bounds.getHeight() + delta;

		return (mxCell) graph.insertVertex(graph.getDefaultParent(), null, text, delta / 2, delta / 2, width, height);
	}

	public mxCell showCellInfo(mxGraph graph, mxCell cell) {
		Node cellNode = (Node) cell.getValue();
		final String nodeIndex = "Номер вершины: " + Integer.toString(cellNode.index) + "\n";
		String parentIndex = "Корневая вершина\n";
		if (cellNode.parent != null) {
			parentIndex = "Номер вершины родителя: " + Integer.toString(cellNode.parent.index) + "\n";
		}
		final String g = "Стоимость пути g = " + Double.toString(cellNode.g) + "\n";
		final String h = "Стоимость оставшегося пути h = " + Double.toString(cellNode.h) + "\n";
		final String ruleName = cellNode.ruleName + "\n";
		final String ruleCost = "Стоимость применения правила = " + Double.toString(cellNode.ruleCost);
		final String text = nodeIndex + parentIndex + g + h + ruleName + ruleCost;

		int fontSize = mxConstants.DEFAULT_FONTSIZE;

		int style = Font.PLAIN;
		Font font = new Font("Arial", style, fontSize);
		double scale = 1.0;
		mxRectangle bounds = mxUtils.getSizeForString(text, font, scale);

		final double delta = 20;

		double width = bounds.getWidth() + delta;
		double height = bounds.getHeight() + delta;

		return (mxCell) graph.insertVertex(graph.getDefaultParent(), null, text, 800 - width - (delta / 2), delta / 2,
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

	public GraphFrame(int dptNum) {

		ArrayList<Node> nodeList = Simulator.getTreeBuilder().listMap.get(dptNum);
		
		final mxGraph graph = new mxGraph();

		graph.getModel().beginUpdate();
		try {
			drawGraph(graph, nodeList, nodeList.get(0));
			
			boolean isFinished;
			
			Simulator.getTreeBuilder().rwLock.readLock().lock();
			try {
				isFinished = Simulator.getTreeBuilder().dptSimulationInfoMap.get(dptNum).isFinished;
			} finally {
				Simulator.getTreeBuilder().rwLock.readLock().unlock();
			}
			if (isFinished) {
				GraphInfo info = Simulator.getTreeBuilder().infoMap.get(dptNum);
				ArrayList<Node> solution = Simulator.getTreeBuilder().solutionMap.get(dptNum);
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

		layout.setLevelDistance(30);
		layout.setNodeDistance(5);
		layout.setEdgeRouting(false);

		layout.execute(graph.getDefaultParent());

		graphFrameUpdateTimerTask = new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (!haveNewRealTimeData)
					return;
				haveNewRealTimeData = false;
				graph.getModel().beginUpdate();
				try {
					drawNewVertex(graph, nodeList, dptNum);
					layout.execute(graph.getDefaultParent());
				} finally {
					graph.getModel().endUpdate();
				}
				boolean isFinished;
				
				Simulator.getTreeBuilder().rwLock.readLock().lock();
				try {
					isFinished = Simulator.getTreeBuilder().dptSimulationInfoMap.get(dptNum).isFinished;
				} finally {
					Simulator.getTreeBuilder().rwLock.readLock().unlock();
				}
				
				if (isFinished)
					this.cancel();
			}
		};
		
		graphFrameFinTimerTask = new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Simulator.getTreeBuilder().rwLock.readLock().lock();
				try {
					isFinished = Simulator.getTreeBuilder().dptSimulationInfoMap.get(dptNum).isFinished;
				} finally {
					Simulator.getTreeBuilder().rwLock.readLock().unlock();
				}
				if (isFinished) {
					graph.getModel().beginUpdate();
					try {
						GraphInfo info = Simulator.getTreeBuilder().infoMap.get(dptNum);
						ArrayList<Node> solution = Simulator.getTreeBuilder().solutionMap.get(dptNum);
						colorNodes(vertexMap, nodeList, solution);
						insertInfo(graph, info);
					} finally {
						graph.getModel().endUpdate();
					}
					this.cancel();
				}
			}
		};

		graph.setCellsEditable(false);
		graph.setCellsDisconnectable(false);
		graph.setCellsResizable(false);
		graph.setAllowDanglingEdges(false);

		// TODO temporarily commented
		// double height = graphInfoCell.getGeometry().getHeight();

		graph.getSelectionModel().addListener(mxEvent.CHANGE, new mxIEventListener() {

			private mxCell cellInfo;

			@Override
			public void invoke(Object sender, mxEventObject evt) {
				// TODO Auto-generated method stub
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
	}
}