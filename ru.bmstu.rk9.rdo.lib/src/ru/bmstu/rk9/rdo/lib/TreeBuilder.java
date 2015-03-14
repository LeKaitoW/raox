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
		final Database.Entry entry = Simulator.getDatabase().allEntries.get(entryNumber++);
		parseEntry(entry);
		notifyGUIPart();
	}
	
	private Subscriber GUISubscriber = null;
	
	public final void notifyGUIPart()
	{
		GUISubscriber.fireChange();
	}
	
	public final void setGUISubscriber(Subscriber subscriber) {
		this.GUISubscriber = subscriber;
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

	public HashMap<Integer, ArrayList<Node>> listMap = new HashMap<Integer, ArrayList<Node>>();
	
	public final void buildTree() {
		final ArrayList<Database.Entry> entries = Simulator.getDatabase().allEntries;

		int size = entries.size();
		while (entryNumber < size) {
			parseEntry(entries.get(entryNumber++));
		}
	}

	private void parseEntry(Database.Entry entry) {
		final Database.EntryType type = Database.EntryType.values()[entry.getHeader().get(
				TypeSize.Internal.ENTRY_TYPE_OFFSET)];
		switch (type) {
		case SEARCH:

			final ByteBuffer header = Tracer.prepareBufferForReading(entry.getHeader());
			final ByteBuffer data = Tracer.prepareBufferForReading(entry.getData());

			Tracer.skipPart(header, TypeSize.BYTE);
			final Database.SearchEntryType entryType = Database.SearchEntryType.values()[header.get()];
			switch (entryType) {
			case BEGIN: {
				Node treeNode = new Node();
				Tracer.skipPart(data, TypeSize.DOUBLE);
				final int dptNumber = data.getInt();
				currentDptNumber = dptNumber;
				solutionMap.put(currentDptNumber, new ArrayList<Node>());
				listMap.put(currentDptNumber, new ArrayList<Node>());
				treeNode.parent = null;
				treeNode.index = 0;
				treeNode.label = Integer.toString(treeNode.index);
				listMap.get(currentDptNumber).add(treeNode);
				GraphControl.setDptNumOfLastAddedVertex(currentDptNumber);
				GraphControl.setLastAddedVertexIndex(0);
				System.out.println("treeB last node index = " + treeNode.index);
				System.out.println("treeB last node parent index = null(root)");
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
					Node treeNode = new Node();
					final int nodeNumber = data.getInt();
					final int parentNumber = data.getInt();
					final double g = data.getDouble();
					final double h = data.getDouble();
					final int ruleNum = data.getInt();
					ActivityInfo activity = Simulator.getTracer().decisionPointsInfo.get(currentDptNumber).activitiesInfo
							.get(ruleNum);
					final int patternNumber = activity.patternNumber;
					final double ruleCost = data.getDouble();
					treeNode.parent = listMap.get(currentDptNumber).get(parentNumber);
					listMap.get(currentDptNumber).get(parentNumber).children.add(treeNode);

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
					listMap.get(currentDptNumber).add(treeNode);
//					lastAddedNode.put(currentDptNumber, treeNode);
					System.out.println("treeB last node index = " + treeNode.index);
					System.out.println("treeB last node parent index = " + treeNode.parent.index);
					break;
				case WORSE:
					break;
				}
				break;
			}
			case DECISION: {
				final int number = data.getInt();
				ArrayList<Node> currentSolutionList = solutionMap.get(currentDptNumber);
				ArrayList<Node> currentNodeList = listMap.get(currentDptNumber);
				Node solutionNode = currentNodeList.get(number);
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
	
	public int getCurrentDptNumber() {
		return currentDptNumber;
	}
	
	private int entryNumber = 0;
	
	public int getEntryNumber() {
		return entryNumber;
	}
}