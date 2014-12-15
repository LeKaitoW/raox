package ru.bmstu.rk9.rdo.ui.graph;

import java.awt.Font;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import ru.bmstu.rk9.rdo.lib.Database;
import ru.bmstu.rk9.rdo.lib.DecisionPointSearch;
import ru.bmstu.rk9.rdo.lib.Simulator;
import ru.bmstu.rk9.rdo.lib.Database.Entry;
import ru.bmstu.rk9.rdo.lib.Database.TypeSize;

import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;

public class TreeGrapher extends JFrame {

	private static final long serialVersionUID = 1L;

	public class Node {

		Node parent;

		mxCell cell;

		ArrayList<Node> children = new ArrayList<Node>();

		public int nodeNumber;

		public String label = "qqq";

		public String style;

		@Override
		public String toString() {
			return label;
		}

		public void addChild(mxCell parent, Node child) {
			((Node) parent.getValue()).children.add(child);
			parent.setValue((Node) parent.getValue());
		}

		public String getColor() {

			return this.cell.getStyle();
		}

		public void setColor(String color) {

			this.cell.setStyle(color);
		}

	}

	private final void fillTreeNodes() {
		final ArrayList<Database.Entry> entries = Simulator.getDatabase().getAllEntries();

		for (Database.Entry entry : entries) {
			parseSerializedData(entry);
		}
	}

	private void parseSerializedData(Database.Entry entry) {
		final Database.EntryType type = Database.EntryType.values()[entry.getHeader().get(
				TypeSize.Internal.ENTRY_TYPE_OFFSET)];
		switch (type) {
		case SYSTEM:
			break;
		case RESOURCE:
			break;
		case PATTERN:
			break;
		case SEARCH:
			parseSearchEntry(entry);
			break;
		case RESULT:
			break;
		}
	}

	final static ByteBuffer prepareBufferForReading(final ByteBuffer buffer) {
		return (ByteBuffer) buffer.duplicate().rewind();
	}

	final static void skipPart(final ByteBuffer buffer, final int size) {
		for (int i = 0; i < size; i++)
			buffer.get();
	}

	Map<Integer, Node> treeMap = new HashMap<Integer, Node>();

	ArrayList<Integer> solution = new ArrayList<Integer>();

	private class GraphInfo {
		double solutionCost;
		int numOpened;
		int numNodes;
	}

	GraphInfo graphInfo = new GraphInfo();

	public void insertInfo(mxGraph graph, GraphInfo info) {

		final String label = "Graph Info\n";
		String solutionCost = "Solution cost: " + Double.toString(info.solutionCost) + "\n";
		String numOpened = "Nodes were opened: " + Integer.toString(info.numOpened) + "\n";
		String numNodes = "Total number of nodes in graph: " + Integer.toString(info.numNodes) + "\n";

		int fontSize = mxConstants.DEFAULT_FONTSIZE;

		int style = Font.PLAIN;
		Font font = new Font("Arial", style, fontSize);
		mxRectangle bounds = mxUtils.getSizeForString(numNodes, font, 1.0);

		double width = bounds.getWidth() + 20;
		double height = 5 * bounds.getHeight() + 20;

		String text = label + "\n" + solutionCost + numOpened + numNodes;
		graph.insertVertex(graph.getDefaultParent(), null, text, 10, 10, width, height);
	}

	private void parseSearchEntry(Entry entry) {

		final ByteBuffer header = prepareBufferForReading(entry.getHeader());
		final ByteBuffer data = prepareBufferForReading(entry.getData());

		Node treeNode = new Node();
		skipPart(header, TypeSize.BYTE);
		final Database.SearchEntryType entryType = Database.SearchEntryType.values()[header.get()];
		switch (entryType) {
		case BEGIN: {
			treeNode.parent = null;
			treeNode.nodeNumber = 0;
			treeMap.put(treeNode.nodeNumber, null);
			treeNode.label = Integer.toString(treeNode.nodeNumber);
			treeMap.put(treeNode.nodeNumber, treeNode);
			break;
		}
		case END: {
			final DecisionPointSearch.StopCode endStatus = DecisionPointSearch.StopCode.values()[data.get()];
			switch (endStatus) {
			case SUCCESS: {
				skipPart(data, TypeSize.DOUBLE + TypeSize.LONG * 2);
				final double finalCost = data.getDouble();
				final int totalOpened = data.getInt();
				final int totalNodes = data.getInt();
				graphInfo.solutionCost = finalCost;
				graphInfo.numOpened = totalOpened;
				graphInfo.numNodes = totalNodes;
				break;
			}
			case FAIL: {
				skipPart(data, TypeSize.DOUBLE + TypeSize.LONG * 2 + TypeSize.DOUBLE);
				final int totalOpened = data.getInt();
				final int totalNodes = data.getInt();
				graphInfo.solutionCost = 0;
				graphInfo.numOpened = totalOpened;
				graphInfo.numNodes = totalNodes;
				break;
			}
			default:
				break;
			}
		}
			break;
		case OPEN:
			break;
		case SPAWN: {
			final DecisionPointSearch.SpawnStatus spawnStatus = DecisionPointSearch.SpawnStatus.values()[data.get()];
			switch (spawnStatus) {
			case NEW: {
				final int nodeNumber = data.getInt();
				final int parentNumber = data.getInt();
				treeNode.parent = treeMap.get(parentNumber);
				treeMap.get(parentNumber).children.add(treeNode);
				treeNode.nodeNumber = nodeNumber;
				treeNode.label = Integer.toString(treeNode.nodeNumber);
				treeMap.put(nodeNumber, treeNode);
				break;
			}
			case WORSE: {
				break;
			}
			case BETTER: {
				final int nodeNumber = data.getInt();
				final int parentNumber = data.getInt();
				treeNode.parent = treeMap.get(parentNumber);
				treeMap.get(parentNumber).children.add(treeNode);
				treeNode.nodeNumber = nodeNumber;
				treeNode.label = Integer.toString(treeNode.nodeNumber);
				treeMap.put(nodeNumber, treeNode);
				break;
			}
			}
			break;
		}
		case DECISION: {
			final int number = data.getInt();
			solution.add(number);
			break;
		}
		}
	}

	private void buildTree(mxGraph graph, Map<Integer, Node> map, Node parent) {
		mxCell child = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, parent, 385, 100, 30, 30,
				"fontColor=000000;strokeColor=000000");
		parent.cell = child;
		if (parent.parent != null)
			graph.insertEdge(graph.getDefaultParent(), null, null, parent.parent.cell, child, "strokeColor=000000");
		if (parent.children.size() != 0)
			for (int i = 0; i < parent.children.size(); i++)
				buildTree(graph, map, map.get(parent.children.get(i).nodeNumber));

	}

	final String solutionColor = "fillColor=32CD32;fontColor=000000;strokeColor=000000";

	public void colorNodes(Map<Integer, Node> map) {
		if (!solution.isEmpty()) {
			int lastNodeNumber = solution.get(solution.size() - 1);
			map.get(lastNodeNumber).cell.setStyle(solutionColor);
			int flag = map.get(lastNodeNumber).nodeNumber;
			do {
				map.get(flag).parent.cell.setStyle(solutionColor);
				flag = map.get(flag).parent.nodeNumber;
			} while (map.get(flag).parent != null);
		}
	}

	public TreeGrapher() {

		final mxGraph graph = new mxGraph();

		graph.getModel().beginUpdate();
		try {
			fillTreeNodes();
			buildTree(graph, treeMap, treeMap.get(0));
			colorNodes(treeMap);
			insertInfo(graph, graphInfo);
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
		graph.setCellsResizable(false);
		graph.setAllowDanglingEdges(false);

	}
}