package ru.bmstu.rk9.rdo.lib;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import ru.bmstu.rk9.rdo.lib.Database;
import ru.bmstu.rk9.rdo.lib.DecisionPointSearch;
import ru.bmstu.rk9.rdo.lib.Simulator;
import ru.bmstu.rk9.rdo.lib.Database.TypeSize;
import ru.bmstu.rk9.rdo.lib.RDOLibStringJoiner.StringFormat;

public class TreeBuilder implements Subscriber {

	@Override
	public void fireChange() {

	}

	public class Node {

		public Node parent;

		public ArrayList<Node> children = new ArrayList<Node>();

		public int index;

		public double g;

		public double h;

		public int ruleNumber;

		public String ruleName;

		public double ruleCost;

		public String label;

		@Override
		public String toString() {
			return label;
		}
	}

	public HashMap<Integer, HashMap<Integer, Node>> mapList = new HashMap<Integer, HashMap<Integer, Node>>();

	public final void buildTree() {
		final ArrayList<Database.Entry> entries = Simulator.getDatabase().getAllEntries();

		for (Database.Entry entry : entries) {
			parseEntry(entry);
		}
	}

	private void parseEntry(Database.Entry entry) {
		final Database.EntryType type = Database.EntryType.values()[entry.getHeader().get(
				TypeSize.Internal.ENTRY_TYPE_OFFSET)];
		switch (type) {
		case SEARCH:

			final ByteBuffer header = Tracer.prepareBufferForReading(entry.getHeader());
			final ByteBuffer data = Tracer.prepareBufferForReading(entry.getData());

			Node treeNode = new Node();
			Tracer.skipPart(header, TypeSize.BYTE);
			final Database.SearchEntryType entryType = Database.SearchEntryType.values()[header.get()];
			switch (entryType) {
			case BEGIN: {
				Tracer.skipPart(data, TypeSize.DOUBLE);
				final int dptNumber = data.getInt();
				currentDptNumber = dptNumber;
				solutionMap.put(currentDptNumber, new ArrayList<Node>());
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
					Tracer.skipPart(data, TypeSize.DOUBLE + TypeSize.LONG * 2);
					final double finalCost = data.getDouble();
					final int totalOpened = data.getInt();
					final int totalNodes = data.getInt();
					infoMap.get(currentDptNumber).solutionCost = finalCost;
					infoMap.get(currentDptNumber).numOpened = totalOpened;
					infoMap.get(currentDptNumber).numNodes = totalNodes;
					break;
				}
				case FAIL: {
					Tracer.skipPart(data, TypeSize.DOUBLE + TypeSize.LONG * 2 + TypeSize.DOUBLE);
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
				case NEW:
				case BETTER:
					final int nodeNumber = data.getInt();
					final int parentNumber = data.getInt();
					final double g = data.getDouble();
					final double h = data.getDouble();
					final int ruleNum = data.getInt();
					ActivityInfo activity = Simulator.getTracer().decisionPointsInfo.get(currentDptNumber).activitiesInfo
							.get(ruleNum);
					final int patternNumber = activity.patternNumber;
					final double ruleCost = data.getDouble();
					treeNode.parent = mapList.get(currentDptNumber).get(parentNumber);
					mapList.get(currentDptNumber).get(parentNumber).children.add(treeNode);

					final int numberOfRelevantResources = Simulator.getTracer().patternsInfo.get(patternNumber).relResTypes
							.size();

					RDOLibStringJoiner relResStringJoiner = new RDOLibStringJoiner(StringFormat.FUNCTION);

					for (int num = 0; num < numberOfRelevantResources; num++) {
						final int resNum = data.getInt();
						final int typeNum = Simulator.getTracer().patternsInfo.get(patternNumber).relResTypes.get(num);
						final String typeName = Simulator.getTracer().resourceTypesInfo.get(typeNum).name;

						final String name = Simulator.getTracer().resourceNames.get(typeNum).get(resNum);
						final String resourceName = name != null ? name : typeName + Tracer.encloseIndex(resNum);

						relResStringJoiner.add(resourceName);
					}

					treeNode.g = g;
					treeNode.h = h;
					treeNode.ruleNumber = ruleNum;
					treeNode.ruleName = activity.name + relResStringJoiner.getString();
					treeNode.ruleCost = ruleCost;
					treeNode.index = nodeNumber;
					treeNode.label = Integer.toString(treeNode.index);
					mapList.get(currentDptNumber).put(nodeNumber, treeNode);
					break;
				case WORSE:
					break;
				}
				break;
			}
			case DECISION: {
				final int number = data.getInt();
				ArrayList<Node> currentSolutionList = solutionMap.get(currentDptNumber);
				HashMap<Integer, Node> currentNodeMap = mapList.get(currentDptNumber);
				Node solutionNode = currentNodeMap.get(number);
				currentSolutionList.add(solutionNode);
				break;
			}
			}
			break;
		default:
			break;
		}
	}

	public HashMap<Integer, ArrayList<Node>> solutionMap = new HashMap<Integer, ArrayList<Node>>();

	public class GraphInfo {
		public double solutionCost;
		public int numOpened;
		public int numNodes;
	}

	public HashMap<Integer, GraphInfo> infoMap = new HashMap<Integer, GraphInfo>();

	private int currentDptNumber = -1;
}