package ru.bmstu.rk9.rao.ui.graph;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import ru.bmstu.rk9.rao.lib.database.Database;
import ru.bmstu.rk9.rao.lib.database.Database.Entry;
import ru.bmstu.rk9.rao.lib.database.Database.EntryType;
import ru.bmstu.rk9.rao.lib.database.Database.TypeSize;
import ru.bmstu.rk9.rao.lib.dpt.DecisionPointSearch;
import ru.bmstu.rk9.rao.lib.modelStructure.ActivityCache;
import ru.bmstu.rk9.rao.lib.modelStructure.DecisionPointCache;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.ui.trace.StringJoiner;
import ru.bmstu.rk9.rao.ui.trace.StringJoiner.StringFormat;
import ru.bmstu.rk9.rao.ui.trace.Tracer;

public class TreeBuilder {
	TreeBuilder(int dptNumber) {
		this.dptNumber = dptNumber;
		this.decisionPointCache = Simulator.getModelStructureCache()
				.getDecisionPointsInfo().get(dptNumber);
	}

	public final boolean updateTree() {
		List<Entry> entries = Simulator.getDatabase().getAllEntries();
		while (entryNumber < entries.size()) {
			final Database.Entry entry = entries.get(entryNumber);
			if (parseEntry(entry))
				return true;
			entryNumber++;
		}
		return false;
	}

	public class Node {
		public Node parent;
		public List<Node> children = new ArrayList<Node>();
		public int index;
		public double g;
		public double h;
		public int ruleNumber;
		public String ruleDesсription;
		public double ruleCost;
		public String label;

		@Override
		public String toString() {
			return label;
		}
	}

	private boolean parseEntry(Entry entry) {
		boolean isFinished = false;

		final EntryType entryType = EntryType.values()[entry.getHeader().get(
				TypeSize.Internal.ENTRY_TYPE_OFFSET)];
		if (entryType != EntryType.SEARCH)
			return false;

		final ByteBuffer header = Tracer.prepareBufferForReading(entry
				.getHeader());

		Tracer.skipPart(header, TypeSize.BYTE);
		final Database.SearchEntryType searchEntryType = Database.SearchEntryType
				.values()[header.get()];
		Tracer.skipPart(header, TypeSize.DOUBLE);
		final int dptNumber = header.getInt();

		if (dptNumber != this.dptNumber)
			return false;

		switch (searchEntryType) {
		case BEGIN: {
			Node treeNode = new Node();
			treeNode.parent = null;
			treeNode.index = 0;
			treeNode.label = Integer.toString(treeNode.index);
			nodeList.add(treeNode);
			lastAddedNodeIndex = treeNode.index;
			break;
		}
		case END: {
			final ByteBuffer data = Tracer.prepareBufferForReading(entry
					.getData());
			graphInfo = new GraphInfo();
			Tracer.skipPart(data, TypeSize.BYTE + TypeSize.LONG * 2);

			final double finalCost = data.getDouble();
			final int totalOpened = data.getInt();
			final int totalNodes = data.getInt();

			graphInfo.solutionCost = finalCost;
			graphInfo.numOpened = totalOpened;
			graphInfo.numNodes = totalNodes;
			isFinished = true;
			break;
		}
		case OPEN:
			break;
		case SPAWN: {
			final ByteBuffer data = Tracer.prepareBufferForReading(entry
					.getData());
			final DecisionPointSearch.SpawnStatus spawnStatus = DecisionPointSearch.SpawnStatus
					.values()[data.get()];
			// TODO delete spawn
			switch (spawnStatus) {
			case NEW:
			case BETTER:
				Node treeNode = new Node();
				final int nodeNumber = data.getInt();
				final int parentNumber = data.getInt();
				final double g = data.getDouble();
				final double h = data.getDouble();
				final int ruleNum = data.getInt();
				ActivityCache activity = decisionPointCache.getActivitiesInfo()
						.get(ruleNum);
				final int patternNumber = activity.getPatternNumber();
				final double ruleCost = data.getDouble();
				treeNode.parent = nodeList.get(parentNumber);
				nodeList.get(parentNumber).children.add(treeNode);

				final int numberOfRelevantResources = Simulator
						.getModelStructureCache().getPatternsInfo()
						.get(patternNumber).getRelResTypes().size();

				StringJoiner relResStringJoiner = new StringJoiner(
						StringFormat.FUNCTION);

				for (int num = 0; num < numberOfRelevantResources; num++) {
					final int resNum = data.getInt();
					final int typeNum = Simulator.getModelStructureCache()
							.getPatternsInfo().get(patternNumber)
							.getRelResTypes().get(num);
					final String typeName = Simulator.getModelStructureCache()
							.getResourceTypesInfo().get(typeNum).getName();

					final String name = Simulator.getModelStructureCache()
							.getResourceNames().get(typeNum).get(resNum);
					final String resourceName = name != null ? name : typeName
							+ Tracer.encloseIndex(resNum);

					relResStringJoiner.add(resourceName);
				}

				treeNode.g = g;
				treeNode.h = h;
				treeNode.ruleNumber = ruleNum;
				treeNode.ruleDesсription = activity.getName()
						+ relResStringJoiner.getString();
				treeNode.ruleCost = ruleCost;
				treeNode.index = nodeNumber;
				treeNode.label = Integer.toString(treeNode.index);
				nodeList.add(treeNode);
				lastAddedNodeIndex = treeNode.index;
				break;
			case WORSE:
				break;
			}
			break;
		}
		case DECISION: {
			final ByteBuffer data = Tracer.prepareBufferForReading(entry
					.getData());
			final int number = data.getInt();
			Node solutionNode = nodeList.get(number);
			solutionList.add(solutionNode);
			break;
		}
		}

		return isFinished;
	}

	public final List<Node> nodeList = new ArrayList<Node>();
	List<Node> solutionList = new ArrayList<Node>();
	GraphInfo graphInfo = new GraphInfo();
	int lastAddedNodeIndex = 0;
	private int entryNumber = 0;
	private final int dptNumber;
	private final DecisionPointCache decisionPointCache;

	public class GraphInfo {
		public double solutionCost;
		public int numOpened;
		public int numNodes;
	}

	public int getEntryNumber() {
		return entryNumber;
	}
}
