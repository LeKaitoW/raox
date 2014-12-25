package ru.bmstu.rk9.rdo.ui.graph;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import ru.bmstu.rk9.rdo.lib.Database;
import ru.bmstu.rk9.rdo.lib.DecisionPointSearch;
import ru.bmstu.rk9.rdo.lib.Simulator;
import ru.bmstu.rk9.rdo.lib.Database.TypeSize;

import com.mxgraph.model.mxCell;

//TODO rename class
public class TreeGrapher {

	public HashMap<Integer, HashMap<Integer, Node>> mapList = new HashMap<Integer, HashMap<Integer, Node>>();

	public class Node {

		Node parent;

		mxCell cell;

		ArrayList<Node> children = new ArrayList<Node>();

		public int index;

		public String label;

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
					treeNode.index = 0;
					mapList.get(currentDptNumber).put(treeNode.index, null);
					treeNode.label = Integer.toString(treeNode.index);
					mapList.get(currentDptNumber).put(treeNode.index, treeNode);
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
						treeNode.index = nodeNumber;
						treeNode.label = Integer.toString(treeNode.index);
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
						treeNode.index = nodeNumber;
						treeNode.label = Integer.toString(treeNode.index);
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

	private int currentDptNumber = -1;

	public TreeGrapher() {
		fillTreeNodes();
	}
}