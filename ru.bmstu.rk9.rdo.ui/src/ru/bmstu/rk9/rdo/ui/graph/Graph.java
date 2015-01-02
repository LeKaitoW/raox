package ru.bmstu.rk9.rdo.ui.graph;

import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import ru.bmstu.rk9.rdo.lib.TreeGrapher.GraphInfo;
import ru.bmstu.rk9.rdo.lib.TreeGrapher.Node;

import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;

public class Graph extends JFrame {

	Map<Node, mxCell> vertexMap = new HashMap<Node, mxCell>();

	private void buildTree(mxGraph graph, HashMap<Integer, Node> nodeMap, Node parentNode) {
		mxCell vertex = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, parentNode, 385, 100, 30, 30,
				fontColor + strokeColor);
		vertexMap.put(parentNode, vertex);
		if (parentNode.parent != null)
			graph.insertEdge(graph.getDefaultParent(), null, null, vertexMap.get(parentNode.parent),
					vertexMap.get(parentNode), strokeColor);
		if (parentNode.children.size() != 0)
			for (int i = 0; i < parentNode.children.size(); i++)
				buildTree(graph, nodeMap, nodeMap.get(parentNode.children.get(i).index));
	}

	final String solutionColor = "fillColor=32CD32;";
	final String strokeColor = "strokeColor=000000;";
	final String fontColor = "fontColor=000000;";

	public void colorNodes(Map<Node, mxCell> vertexMap, HashMap<Integer, Node> treeMap, ArrayList<Node> solution) {
		if (!solution.isEmpty()) {
			Node rootNode = treeMap.get(0);
			vertexMap.get(rootNode).setStyle(solutionColor + fontColor + strokeColor);
			for (int i = 0; i < solution.size(); i++) {
				vertexMap.get(solution.get(i)).setStyle(solutionColor + fontColor + strokeColor);
			}
		}
	}

	public void insertInfo(mxGraph graph, GraphInfo info) {

		final String solutionCost = "Solution cost: " + Double.toString(info.solutionCost) + "\n";
		final String numOpened = "Nodes were opened: " + Integer.toString(info.numOpened) + "\n";
		final String numNodes = "Total number of nodes in graph: " + Integer.toString(info.numNodes);
		final String text = solutionCost + numOpened + numNodes;

		int fontSize = mxConstants.DEFAULT_FONTSIZE;

		int style = Font.PLAIN;
		Font font = new Font("Arial", style, fontSize);
		double scale = 1.0;
		mxRectangle bounds = mxUtils.getSizeForString(text, font, scale);

		final double delta = 20;

		double width = bounds.getWidth() + delta;
		double height = bounds.getHeight() + delta;

		graph.insertVertex(graph.getDefaultParent(), null, text, delta / 2, delta / 2, width, height);
	}

	public Graph(HashMap<Integer, Node> treeMap, GraphInfo info, ArrayList<Node> solution) {

		final mxGraph graph = new mxGraph();

		graph.getModel().beginUpdate();
		try {
			buildTree(graph, treeMap, treeMap.get(0));
			colorNodes(vertexMap, treeMap, solution);
			insertInfo(graph, info);
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

		graph.setCellsEditable(false);
		graph.setCellsDisconnectable(false);
		graph.setCellsResizable(false);
		graph.setAllowDanglingEdges(false);

	}
}