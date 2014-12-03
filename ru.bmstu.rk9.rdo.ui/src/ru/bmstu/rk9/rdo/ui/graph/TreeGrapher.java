package ru.bmstu.rk9.rdo.ui.graph;

import java.util.ArrayList;

import javax.swing.JFrame;

import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class TreeGrapher extends JFrame {

	public static int numchildren = 3; // number of children
	public static int kgen = 1; // number of generations
	public static int nodenum = 0;

	public static int frameSize_x = 800;
	public static int frameSize_y = 800;

	public static int cellSize_x = 50;
	public static int cellSize_y = 50;

	private static final long serialVersionUID = 1L;

	public double RelativeX(double x) {
		return x /= frameSize_x;
	}

	public double RelativeY(double y) {
		return y /= frameSize_y;
	}

	public mxCell MakeRootNode(mxGraph g) {
		Node node = new Node();

		node.num = nodenum;
		nodenum++;
		node.ancestor = null;
		node.label = "Root";

		mxCell root = (mxCell) g.insertVertex(g.getDefaultParent(), null, node,
				RelativeX((frameSize_x - cellSize_x) / 2), 400, 50, 50, "shadow=true");

		node.cell = root;

		return root;
	}

	public class Node {
		Node ancestor;

		mxCell cell;

		ArrayList<Node> children = new ArrayList<Node>();

		public int num;

		public String label = "qqq";

		public String style;

		@Override
		public String toString() {
			return label;
		}

		public void addChild(mxCell parent, Node child) {
			Node x = new Node();
			x = (Node) parent.getValue();
			x.children.add(child);
			parent.setValue(x);
		}

		public String getColor() {

			return this.cell.getStyle();
		}

		public void setColor(String color) {

			this.cell.setStyle(color);
		}

	};

	public void MakeChildren(mxGraph graph, mxCell parent, int kg) {
		for (int i = 0; i < numchildren; i++) {
			Node node = new Node();
			node.ancestor = (Node) parent.getValue();
			node.num = nodenum;
			nodenum++;
			node.label = Integer.toString(node.num);
			mxCell child = (mxCell) graph.insertVertex(graph.getDefaultParent(), Integer.toString(node.num), node, 30,
					30, 50, 50);
			node.cell = child;
			((Node) parent.getValue()).addChild(parent, (Node) child.getValue());
			graph.insertEdge(graph.getDefaultParent(), null, null, parent, child);
			if (kg > 0)
				MakeChildren(graph, child, kg - 1);
		}
	}

	public void colorNodes(Node node) {
		if (node.ancestor == null)
			node.setColor("fillColor=green");
		int size = node.children.size();
		for (int i = 0; i < size; i++) {
			if (i == 0 && node.cell.getStyle() == "fillColor=green") {
				node.children.get(i).setColor("fillColor=green");
			}
			colorNodes(node.children.get(i));
		}
	}

	public TreeGrapher() {

		final mxGraph graph = new mxGraph();

		graph.getModel().beginUpdate();
		try {
			mxCell root = MakeRootNode(graph);
			MakeChildren(graph, root, kgen);
			Node rootnode = (Node) root.getValue();
			colorNodes(rootnode);
		} finally {
			graph.getModel().endUpdate();
		}

		final mxGraphComponent graphComponent = new mxGraphComponent(graph);
		graphComponent.setConnectable(false);
		getContentPane().add(graphComponent);

		mxCompactTreeLayout layout = new mxCompactTreeLayout(graph, false);

		layout.setLevelDistance(30);
		layout.setNodeDistance(5);
		layout.setEdgeRouting(false);

		layout.execute(graph.getDefaultParent());

		graph.setCellsEditable(false);
		graph.setCellsSelectable(false);
		graph.setCellsMovable(false);
		graph.setCellsLocked(false);
		graph.setSplitEnabled(false);
		graph.setCellsDisconnectable(true);

	}
}