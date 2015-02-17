package ru.bmstu.rk9.rdo.ui.graph;

import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import javax.swing.JFrame;

import ru.bmstu.rk9.rdo.lib.GraphControl;
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
	
/*-----------------------REAL TIME OUTPUT-----------------------*/
	
	private Subscriber graphControlSubscriber = null;
	
	public void setGraphControlSubscriber(Subscriber subscriber) {
		this.graphControlSubscriber = subscriber;
	}
	
	public void notifyGraphControl() {
		graphControlSubscriber.fireChange();
	}
	
	private static volatile boolean haveNewRealTimeData = false;

	public static final Subscriber realTimeUpdater = new Subscriber() {
		@Override
		public void fireChange() {
			haveNewRealTimeData = true;
		}
	};

/*--------------------------------------------------------------*/

	Map<Node, mxCell> vertexMap = new HashMap<Node, mxCell>();

	private void drawGraph(mxGraph graph, HashMap<Integer, Node> nodeMap, Node parentNode) {
		mxCell vertex = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, parentNode, 385, 100, 30, 30,
				fontColor + strokeColor);
		vertexMap.put(parentNode, vertex);
		if (parentNode.parent != null)
			graph.insertEdge(graph.getDefaultParent(), null, null, vertexMap.get(parentNode.parent),
					vertexMap.get(parentNode), strokeColor);
		if (parentNode.children.size() != 0)
			for (int i = 0; i < parentNode.children.size(); i++)
				drawGraph(graph, nodeMap, nodeMap.get(parentNode.children.get(i).index));
	}

	final String solutionColor = "fillColor=32CD32;";
	final String strokeColor = "strokeColor=000000;";
	final String fontColor = "fontColor=000000;";

	public void colorNodes(Map<Node, mxCell> vertexMap, HashMap<Integer, Node> nodeMap, ArrayList<Node> solution) {
		if (!solution.isEmpty()) {
			Node rootNode = nodeMap.get(0);
			vertexMap.get(rootNode).setStyle(solutionColor + fontColor + strokeColor);
			for (int i = 0; i < solution.size(); i++) {
				vertexMap.get(solution.get(i)).setStyle(solutionColor + fontColor + strokeColor);
			}
		}
	}
	
	private void drawNewVertex(mxGraph graph) {
		int dptNum = Simulator.getTreeBuilder().getCurrentDptNumber();
		Node node = Simulator.getTreeBuilder().lastAddedNode.get(dptNum);
		mxCell vertex = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, node, 385, 100, 30, 30,
				fontColor + strokeColor);
		vertexMap.put(node, vertex);
		if (node.parent != null)
			graph.insertEdge(graph.getDefaultParent(), null, null, vertexMap.get(node.parent),
					vertexMap.get(node), strokeColor);
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
		
		return (mxCell) graph.insertVertex(graph.getDefaultParent(), null, text, 800 - width - (delta / 2), delta / 2, width, height);
	}
	
	public GraphFrame(HashMap<Integer, Node> treeMap, GraphInfo info, ArrayList<Node> solution) {

		final mxGraph graph = new mxGraph();
		
		mxCell graphInfoCell;
		
		graph.getModel().beginUpdate();
		try {
			drawGraph(graph, treeMap, treeMap.get(0));
			//colorNodes(vertexMap, treeMap, solution);
			//graphInfoCell = insertInfo(graph, info);
		} finally {
			graph.getModel().endUpdate();
		}
		
		this.setGraphControlSubscriber(GraphControl.timerTaskUpdater);

		GraphControl.newTimerTask = new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (haveNewRealTimeData) {
					haveNewRealTimeData = false;
					graph.getModel().beginUpdate();
					try {
						System.out.println("GraphFrame drawgraph");
						drawGraph(graph, treeMap, treeMap.get(0));
					} finally {
						graph.getModel().endUpdate();
					}
				}
			}

		};
		notifyGraphControl();
		
/*		Thread GraphOutputThread = new Thread(new Runnable() {
			public void run() {
				//TODO bad hack
				System.out.println("GraphOutputThread start");
				while(true)
				{
					if (haveNewRealTimeData) {
						haveNewRealTimeData = false;
						//drawNewVertex(graph);
						System.out.println("GraphOutputThread 1");
						graph.getModel().beginUpdate();
						System.out.println("GraphOutputThread 2");
						try {
							System.out.println("GraphFrame " + treeMap.size());
							drawGraph(graph, treeMap, treeMap.get(0));
							System.out.println("GraphOutputThread 3");
						} finally {
							graph.getModel().endUpdate();
							System.out.println("GraphOutputThread 4");
						}
					}
					try {
						System.out.println("GraphOutputThread wait");
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("GraphOutputThread 6");
					}
				}
			}
		});
		
		GraphOutputThread.start();*/

		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		graphComponent.setConnectable(false);
		graphComponent.zoomAndCenter();
		getContentPane().add(graphComponent);

		mxCompactTreeLayout layout = new mxCompactTreeLayout(graph, false);

		layout.setLevelDistance(30);
		layout.setNodeDistance(5);
		layout.setEdgeRouting(false);

		layout.execute(graph.getDefaultParent());

		graph.setCellsEditable(false);
		graph.setCellsDisconnectable(false);
		graph.setCellsResizable(false);
		graph.setAllowDanglingEdges(false);
		
		//double height = graphInfoCell.getGeometry().getHeight();
		
		graph.getSelectionModel().addListener(mxEvent.CHANGE, new mxIEventListener() {
			
			private mxCell cellInfo;
			
			@Override
			public void invoke(Object sender, mxEventObject evt) {
				// TODO Auto-generated method stub
				mxGraphSelectionModel selectionModel = (mxGraphSelectionModel) sender;
				mxCell cell = (mxCell) selectionModel.getCell();
				if (cell != null && cell.isVertex() && cell != cellInfo /*&& cell != graphInfoCell*/) {
					graph.getModel().beginUpdate();
					try {
						if (cellInfo != null)
							cellInfo.removeFromParent();
						cellInfo = showCellInfo(graph, cell);
					}
					finally {
						graph.getModel().endUpdate();
					}
				}
			}
		});
	}
}