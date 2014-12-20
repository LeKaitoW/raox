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
import ru.bmstu.rk9.rdo.lib.Database.TypeSize;

import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;

public class TreeGrapher {

	public HashMap<Integer, HashMap<Integer, Node>> mapList = new HashMap<Integer, HashMap<Integer, Node>>();

	public class Node {

		Node parent;

		mxCell cell;

		ArrayList<Node> children = new ArrayList<Node>();

		public int nodeNumber;

		public String label = "qqq";

		@Override
		public String toString() {
			return label;
		}
	}

	private final void fillTreeNodes() {
		final ArrayList<Database.Entry> entries = Simulator.getDatabase().getAllEntries();

		for (Database.Entry entry : entries) {
			final Database.EntryType type = Database.EntryType.values()[entry.getHeader().get(
					TypeSize.Internal.ENTRY_TYPE_OFFSET)];
			switch (type) {
			case SEARCH:
				
				final ByteBuffer header = prepareBufferForReading(entry.getHeader());
				final ByteBuffer data = prepareBufferForReading(entry.getData());

				Node treeNode = new Node();
				skipPart(header, TypeSize.BYTE);
				final Database.SearchEntryType entryType = Database.SearchEntryType.values()[header.get()];
				switch (entryType) {
				case BEGIN: {
					skipPart(data, TypeSize.DOUBLE);
					final int dptNumber = data.getInt();
					currentDptNumber = dptNumber;
					solutionMap.put(currentDptNumber, new ArrayList<Integer>());
					mapList.put(currentDptNumber, new HashMap<Integer, Node>());
					treeNode.parent = null;
					treeNode.nodeNumber = 0;
					mapList.get(currentDptNumber).put(treeNode.nodeNumber, null);
					treeNode.label = Integer.toString(treeNode.nodeNumber);
					mapList.get(currentDptNumber).put(treeNode.nodeNumber, treeNode);
					break;
				}
				case END: {
					final DecisionPointSearch.StopCode endStatus = DecisionPointSearch.StopCode.values()[data.get()];
					infoMap.put(currentDptNumber, new GraphInfo());
					switch (endStatus) {
					case SUCCESS: {
						skipPart(data, TypeSize.DOUBLE + TypeSize.LONG * 2);
						final double finalCost = data.getDouble();
						final int totalOpened = data.getInt();
						final int totalNodes = data.getInt();
						infoMap.get(currentDptNumber).solutionCost = finalCost;
						infoMap.get(currentDptNumber).numOpened = totalOpened;
						infoMap.get(currentDptNumber).numNodes = totalNodes;
						break;
					}
					case FAIL: {
						skipPart(data, TypeSize.DOUBLE + TypeSize.LONG * 2 + TypeSize.DOUBLE);
						final int totalOpened = data.getInt();
						final int totalNodes = data.getInt();
						infoMap.get(currentDptNumber).solutionCost = 0;
						infoMap.get(currentDptNumber).numOpened = totalOpened;
						infoMap.get(currentDptNumber).numNodes = totalNodes;
						break;
					}
					default:
						break;
					}
					currentDptNumber = -1;
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
						treeNode.parent = mapList.get(currentDptNumber).get(parentNumber);
						mapList.get(currentDptNumber).get(parentNumber).children.add(treeNode);
						treeNode.nodeNumber = nodeNumber;
						treeNode.label = Integer.toString(treeNode.nodeNumber);
						mapList.get(currentDptNumber).put(nodeNumber, treeNode);
						break;
					}
					case WORSE: {
						break;
					}
					case BETTER: {
						final int nodeNumber = data.getInt();
						final int parentNumber = data.getInt();
						treeNode.parent = mapList.get(currentDptNumber).get(parentNumber);
						mapList.get(currentDptNumber).get(parentNumber).children.add(treeNode);
						treeNode.nodeNumber = nodeNumber;
						treeNode.label = Integer.toString(treeNode.nodeNumber);
						mapList.get(currentDptNumber).put(nodeNumber, treeNode);
						break;
					}
					}
					break;
				}
				case DECISION: {
					final int number = data.getInt();
					solutionMap.get(currentDptNumber).add(number);
					break;
				}
				}
				break;
			default:
				break;
			}
		}
	}

	final ByteBuffer prepareBufferForReading(final ByteBuffer buffer) {
		return (ByteBuffer) buffer.duplicate().rewind();
	}

	final void skipPart(final ByteBuffer buffer, final int size) {
		for (int i = 0; i < size; i++)
			buffer.get();
	}

	public HashMap<Integer, ArrayList<Integer>> solutionMap = new HashMap<Integer, ArrayList<Integer>>();

	public class GraphInfo {
		double solutionCost;
		int numOpened;
		int numNodes;
	}

	public HashMap<Integer, GraphInfo> infoMap = new HashMap<Integer, GraphInfo>();

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

	private int currentDptNumber = -1;

	private void buildTree(mxGraph graph, HashMap<Integer, Node> map, Node parent) {
		mxCell child = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, parent, 385, 100, 30, 30,
				"fontColor=000000;strokeColor=000000");
		parent.cell = child;
		if (parent.parent != null)
			graph.insertEdge(graph.getDefaultParent(), null, null, parent.parent.cell, child, strokeColor);
		if (parent.children.size() != 0)
			for (int i = 0; i < parent.children.size(); i++)
				buildTree(graph, map, map.get(parent.children.get(i).nodeNumber));
	}

	final String solutionColor = "fillColor=32CD32;";
	final String strokeColor = "strokeColor=000000;";
	final String fontColor = "fontColor=000000;";

	public void colorNodes(Map<Integer, Node> map, ArrayList<Integer> solution) {
		if (!solution.isEmpty()) {
			int lastNodeNumber = solution.get(solution.size() - 1);
			map.get(lastNodeNumber).cell.setStyle(solutionColor + strokeColor + fontColor);
			int flag = map.get(lastNodeNumber).nodeNumber;
			do {
				map.get(flag).parent.cell.setStyle(solutionColor + strokeColor + fontColor);
				flag = map.get(flag).parent.nodeNumber;
			} while (map.get(flag).parent != null);
		}
	}

	public TreeGrapher() {
		fillTreeNodes();
	}

	public class Graph extends JFrame {

		private static final long serialVersionUID = 1L;

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
}