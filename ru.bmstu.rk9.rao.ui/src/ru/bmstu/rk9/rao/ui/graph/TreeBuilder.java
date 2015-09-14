package ru.bmstu.rk9.rao.ui.graph;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		public Node(Node parent, int index) {
			this.parent = parent;
			this.index = index;
			this.label = String.valueOf(index);
		}

		public final Node parent;
		public final List<Node> children = new ArrayList<Node>();
		public final int index;

		public int depthLevel;
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
			Node treeNode = new Node(null, 0);
			treeNode.depthLevel = 1;
			graphInfo.depth = 1;
			widthByLevel.put(1, 1);
			nodeByNumber.add(treeNode);
			lastAddedNodeIndex = treeNode.index;
			break;
		}
		case END: {
			final ByteBuffer data = Tracer.prepareBufferForReading(entry
					.getData());
			Tracer.skipPart(data, TypeSize.BYTE + TypeSize.LONG * 2);

			final double finalCost = data.getDouble();
			final int totalOpened = data.getInt();
			final int totalNodes = data.getInt();

			graphInfo.solutionCost = finalCost;
			graphInfo.numOpened = totalOpened;
			graphInfo.numNodes = totalNodes;
			graphInfo.width = calculateTreeWidth();

			if (solutionFound)
				solutionList.add(0, nodeByNumber.get(0));
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
			switch (spawnStatus) {
			case NEW:
			case BETTER: {
				final int nodeNumber = data.getInt();
				final int parentNumber = data.getInt();
				final double g = data.getDouble();
				final double h = data.getDouble();
				final int ruleNum = data.getInt();
				ActivityCache activity = decisionPointCache.getActivitiesInfo()
						.get(ruleNum);
				final int patternNumber = activity.getPatternNumber();
				final double ruleCost = data.getDouble();

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

				Node parentNode = nodeByNumber.get(parentNumber);
				Node treeNode = new Node(parentNode, nodeNumber);
				parentNode.children.add(treeNode);
				treeNode.g = g;
				treeNode.h = h;
				treeNode.ruleNumber = ruleNum;
				treeNode.ruleDesсription = activity.getName()
						+ relResStringJoiner.getString();
				treeNode.ruleCost = ruleCost;

				treeNode.depthLevel = treeNode.parent.depthLevel + 1;
				if (widthByLevel.containsKey(treeNode.depthLevel))
					widthByLevel.put(treeNode.depthLevel,
							widthByLevel.get(treeNode.depthLevel) + 1);
				else
					widthByLevel.put(treeNode.depthLevel, 1);

				if (treeNode.depthLevel > graphInfo.depth)
					graphInfo.depth = treeNode.depthLevel;

				nodeByNumber.add(treeNode);
				lastAddedNodeIndex = treeNode.index;
				break;
			}
			case WORSE:
				break;
			}
			break;
		}
		case DECISION: {
			final ByteBuffer data = Tracer.prepareBufferForReading(entry
					.getData());
			final int number = data.getInt();
			Node solutionNode = nodeByNumber.get(number);
			solutionList.add(solutionNode);
			solutionFound = true;
			break;
		}
		}

		return isFinished;
	}

	private final int calculateTreeWidth() {
		int maxWidth = 0;
		for (Integer width : widthByLevel.values()) {
			if (width > maxWidth)
				maxWidth = width;
		}

		return maxWidth;
	}

	final Map<Integer, Integer> widthByLevel = new HashMap<>();
	final List<Node> nodeByNumber = new ArrayList<Node>();
	final List<Node> solutionList = new ArrayList<Node>();
	final GraphInfo graphInfo = new GraphInfo();
	int lastAddedNodeIndex = -1;
	private boolean solutionFound = false;

	private int entryNumber = 0;
	private final int dptNumber;
	private final DecisionPointCache decisionPointCache;

	public class GraphInfo {
		public double solutionCost;
		public int numOpened;
		public int numNodes;
		public int depth;
		public int width;
	}

	public int getEntryNumber() {
		return entryNumber;
	}
}
