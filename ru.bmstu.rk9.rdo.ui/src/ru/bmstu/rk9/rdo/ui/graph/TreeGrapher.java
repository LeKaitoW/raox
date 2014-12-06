package ru.bmstu.rk9.rdo.ui.graph;

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
import com.mxgraph.view.mxGraph;

public class TreeGrapher extends JFrame {

	private static final long serialVersionUID = 1L;

	public class Node {

		Node ancestor;

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

	private void parseSearchEntry(Entry entry) {

		final ByteBuffer header = prepareBufferForReading(entry.getHeader());
		final ByteBuffer data = prepareBufferForReading(entry.getData());

		Node treeNode = new Node();
		skipPart(header, TypeSize.BYTE);
		final Database.SearchEntryType entryType = Database.SearchEntryType.values()[header.get()];
		switch (entryType) {
		case BEGIN: {
			treeNode.ancestor = null;
			treeNode.nodeNumber = 0;
			treeMap.put(treeNode.nodeNumber, null);
			treeNode.label = Integer.toString(treeNode.nodeNumber);
			treeMap.put(treeNode.nodeNumber, treeNode);
			break;
		}
		case END:
			break;
		case OPEN:
			break;
		case SPAWN: {
			final DecisionPointSearch.SpawnStatus spawnStatus = DecisionPointSearch.SpawnStatus.values()[data.get()];
			switch (spawnStatus) {
			case NEW: {
				final int nodeNumber = data.getInt();
				final int parentNumber = data.getInt();
				treeNode.ancestor = treeMap.get(parentNumber);
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
				treeNode.ancestor = treeMap.get(parentNumber);
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
		mxCell child = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, parent, 400, 100, 30, 30);
		parent.cell = child;
		if (parent.ancestor != null)
			graph.insertEdge(graph.getDefaultParent(), null, null, parent.ancestor.cell, child);
		if (parent.children.size() != 0)
			for (int i = 0; i < parent.children.size(); i++)
				buildTree(graph, map, map.get(parent.children.get(i).nodeNumber));

	}

	public void colorNodes(Map<Integer, Node> map) {
		int last = solution.get(solution.size() - 1);
		map.get(last).cell.setStyle("fillColor=green");
		int flag = map.get(last).nodeNumber;
		do {
			map.get(flag).ancestor.cell.setStyle("fillColor=green");
			flag = map.get(flag).ancestor.nodeNumber;
		} while (map.get(flag).ancestor != null);
	}

	public TreeGrapher() {

		final mxGraph graph = new mxGraph();

		graph.getModel().beginUpdate();
		try {
			fillTreeNodes();
			buildTree(graph, treeMap, treeMap.get(0));
			colorNodes(treeMap);
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