package ru.bmstu.rk9.rdo.ui.graph;

import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import ru.bmstu.rk9.rdo.ui.graph.TreeGrapher.GraphInfo;
import ru.bmstu.rk9.rdo.ui.graph.TreeGrapher.Node;

import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;

public class Graph extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private void buildTree(mxGraph graph, HashMap<Integer, Node> map, Node node) {
		node.cell = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, node, 385, 100, 30, 30,
				"fontColor=000000;strokeColor=000000");
		if (node.parent != null)
			graph.insertEdge(graph.getDefaultParent(), null, null, node.parent.cell, node.cell, strokeColor);
		if (node.children.size() != 0)
			for (int i = 0; i < node.children.size(); i++)
				buildTree(graph, map, map.get(node.children.get(i).index));
	}
	
	final String solutionColor = "fillColor=32CD32;";
	final String strokeColor = "strokeColor=000000;";
	final String fontColor = "fontColor=000000;";
	
	public void colorNodes(Map<Integer, Node> map, ArrayList<Integer> solution) {
		if (!solution.isEmpty()) {
			int lastNodeNumber = solution.get(solution.size() - 1);
			map.get(lastNodeNumber).cell.setStyle(solutionColor + strokeColor + fontColor);
			int flag = map.get(lastNodeNumber).index;
			do {
				map.get(flag).parent.cell.setStyle(solutionColor + strokeColor + fontColor);
				flag = map.get(flag).parent.index;
			} while (map.get(flag).parent != null);
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

		graph.insertVertex(graph.getDefaultParent(), null, text, delta/2, delta/2, width, height);
	}
	
	public Graph(HashMap<Integer, Node> treeMap, GraphInfo info, ArrayList<Integer> solution) {

		final mxGraph graph = new mxGraph();

		graph.getModel().beginUpdate();
		try {
			buildTree(graph, treeMap, treeMap.get(0));
			colorNodes(treeMap, solution);
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