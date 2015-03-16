package ru.bmstu.rk9.rdo.lib;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ru.bmstu.rk9.rdo.lib.Database;
import ru.bmstu.rk9.rdo.lib.Database.Entry;
import ru.bmstu.rk9.rdo.lib.Database.EntryType;
import ru.bmstu.rk9.rdo.lib.DecisionPointSearch;
import ru.bmstu.rk9.rdo.lib.Simulator;
import ru.bmstu.rk9.rdo.lib.Database.TypeSize;
import ru.bmstu.rk9.rdo.lib.RDOLibStringJoiner.StringFormat;

public class TreeBuilder implements Subscriber {
	
	@Override
	public void fireChange() {
		final Database.Entry entry = Simulator.getDatabase().getAllEntries().get(entryNumber++);
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
		final List<Database.Entry> entries = Simulator.getDatabase().getAllEntries();

		int size = entries.size();
		while (entryNumber < size) {
			parseEntry(entries.get(entryNumber++));
		}
	}

	private void parseEntry(Entry entry) {
		final EntryType type = EntryType.values()[entry.header.get(
				TypeSize.Internal.ENTRY_TYPE_OFFSET)];
		switch (type) {
		case SEARCH:

			final ByteBuffer header = Tracer.prepareBufferForReading(entry.header);

			Tracer.skipPart(header, TypeSize.BYTE);
			final Database.SearchEntryType entryType = Database.SearchEntryType.values()[header.get()];
			Tracer.skipPart(header, TypeSize.DOUBLE);
			final int dptNumber = header.getInt();
			
			switch (entryType) {
			case BEGIN: {
				Node treeNode = new Node();
				solutionMap.put(dptNumber, new ArrayList<Node>());
				listMap.put(dptNumber, new ArrayList<Node>());
				treeNode.parent = null;
				treeNode.index = 0;
				treeNode.label = Integer.toString(treeNode.index);
				listMap.get(dptNumber).add(treeNode);
				System.out.println("treeB last node index = " + treeNode.index);
				System.out.println("treeB last node parent index = null(root)");
				break;
			}
			case END: {
				final ByteBuffer data = Tracer.prepareBufferForReading(entry.data);
				final DecisionPointSearch.StopCode endStatus = DecisionPointSearch.StopCode.values()[data.get()];
				infoMap.put(dptNumber, new GraphInfo());
				Tracer.skipPart(data, TypeSize.LONG * 2);
				//TODO delete switch?
				switch (endStatus) {
				case SUCCESS: {
					final double finalCost = data.getDouble();
					final int totalOpened = data.getInt();
					final int totalNodes = data.getInt();
					infoMap.get(dptNumber).solutionCost = finalCost;
					infoMap.get(dptNumber).numOpened = totalOpened;
					infoMap.get(dptNumber).numNodes = totalNodes;
					break;
				}
				case FAIL: {
					final double finalCost = data.getDouble();
					final int totalOpened = data.getInt();
					final int totalNodes = data.getInt();
					infoMap.get(dptNumber).solutionCost = finalCost;
					infoMap.get(dptNumber).numOpened = totalOpened;
					infoMap.get(dptNumber).numNodes = totalNodes;
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
				final ByteBuffer data = Tracer.prepareBufferForReading(entry.data);
				final DecisionPointSearch.SpawnStatus spawnStatus = DecisionPointSearch.SpawnStatus.values()[data.get()];
				//TODO delete spawn
				switch (spawnStatus) {
				case NEW:
				case BETTER:
					Node treeNode = new Node();
					final int nodeNumber = data.getInt();
					final int parentNumber = data.getInt();
					final double g = data.getDouble();
					final double h = data.getDouble();
					final int ruleNum = data.getInt();
					ActivityCache activity = Simulator.getModelStructureCache().decisionPointsInfo.get(dptNumber).activitiesInfo
							.get(ruleNum);
					final int patternNumber = activity.patternNumber;
					final double ruleCost = data.getDouble();
					treeNode.parent = listMap.get(dptNumber).get(parentNumber);
					listMap.get(dptNumber).get(parentNumber).children.add(treeNode);

					final int numberOfRelevantResources = Simulator.getModelStructureCache().patternsInfo.get(patternNumber).relResTypes
							.size();

					RDOLibStringJoiner relResStringJoiner = new RDOLibStringJoiner(StringFormat.FUNCTION);

					for (int num = 0; num < numberOfRelevantResources; num++) {
						final int resNum = data.getInt();
						final int typeNum = Simulator.getModelStructureCache().patternsInfo.get(patternNumber).relResTypes.get(num);
						final String typeName = Simulator.getModelStructureCache().resourceTypesInfo.get(typeNum).name;

						final String name = Simulator.getModelStructureCache().resourceNames.get(typeNum).get(resNum);
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
					listMap.get(dptNumber).add(treeNode);
					System.out.println("treeB last node index = " + treeNode.index);
					System.out.println("treeB last node parent index = " + treeNode.parent.index);
					break;
				case WORSE:
					break;
				}
				break;
			}
			case DECISION: {
				final ByteBuffer data = Tracer.prepareBufferForReading(entry.data);
				final int number = data.getInt();
				ArrayList<Node> currentSolutionList = solutionMap.get(dptNumber);
				ArrayList<Node> currentNodeList = listMap.get(dptNumber);
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

	
	private int entryNumber = 0;
	
	public int getEntryNumber() {
		return entryNumber;
	}
}