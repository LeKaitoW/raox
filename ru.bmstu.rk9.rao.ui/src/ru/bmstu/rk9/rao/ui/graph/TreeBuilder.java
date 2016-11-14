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
import ru.bmstu.rk9.rao.lib.dpt.Search;
import ru.bmstu.rk9.rao.lib.naming.NamingHelper;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
import ru.bmstu.rk9.rao.ui.trace.StringJoiner;
import ru.bmstu.rk9.rao.ui.trace.StringJoiner.StringFormat;
import ru.bmstu.rk9.rao.ui.trace.Tracer;

public class TreeBuilder {
	TreeBuilder(int dptNumber, boolean useShortNames) {
		this.dptNumber = dptNumber;
		this.useShortNames = useShortNames;
	}

	private final boolean useShortNames;

	public final boolean updateTree() {
		List<Entry> entries = CurrentSimulator.getDatabase().getAllEntries();
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

		public Node parent;
		public final List<Node> children = new ArrayList<Node>();
		public final int index;

		public int depth;
		public double g;
		public double h;
		public int ruleNumber;
		public String ruleName;
		public double ruleCost;
		public String relevantResources;

		public String label;

		@Override
		public String toString() {
			return label;
		}
	}

	private boolean parseEntry(final Entry entry) {
		boolean isFinished = false;

		final EntryType entryType = EntryType.values()[entry.getHeader().get(TypeSize.Internal.ENTRY_TYPE_OFFSET)];
		if (entryType != EntryType.SEARCH)
			return isFinished;

		final ByteBuffer header = Tracer.prepareBufferForReading(entry.getHeader());

		Tracer.skipPart(header, TypeSize.BYTE);
		final Database.SearchEntryType searchEntryType = Database.SearchEntryType.values()[header.get()];
		Tracer.skipPart(header, TypeSize.DOUBLE);
		final int dptNumber = header.getInt();

		if (dptNumber != this.dptNumber)
			return isFinished;

		switch (searchEntryType) {
		case BEGIN: {
			final Node node = new Node(null, 0);
			node.depth = 1;
			graphInfo.depth = node.depth;
			widthByLevel.put(node.depth, 1);
			nodeByNumber.add(node);
			lastAddedNodeIndex = node.index;
			break;
		}

		case END: {
			final ByteBuffer data = Tracer.prepareBufferForReading(entry.getData());
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
			final ByteBuffer data = Tracer.prepareBufferForReading(entry.getData());
			final Search.SpawnStatus spawnStatus = Search.SpawnStatus.values()[data.get()];
			switch (spawnStatus) {
			case NEW: {
				final int nodeNumber = data.getInt();
				final int parentNumber = data.getInt();
				final double g = data.getDouble();
				final double h = data.getDouble();
				final int ruleNumber = data.getInt();
				final int patternNumber = data.getInt();
				final double ruleCost = data.getDouble();

				final Node parentNode = nodeByNumber.get(parentNumber);
				final Node node = new Node(parentNode, nodeNumber);

				parentNode.children.add(node);
				node.g = g;
				node.h = h;
				node.ruleNumber = ruleNumber;
				node.ruleName = CurrentSimulator.getStaticModelData().getEdgeName(dptNumber, ruleNumber);
				node.relevantResources = getRelevantResources(data, patternNumber);
				node.ruleCost = ruleCost;
				node.depth = node.parent.depth + 1;

				updateGraphInfo(node);

				nodeByNumber.add(node);
				lastAddedNodeIndex = node.index;
				break;
			}

			case BETTER: {
				final int nodeNumber = data.getInt();
				final int parentNumber = data.getInt();
				final double g = data.getDouble();
				data.getDouble();
				final int ruleNumber = data.getInt();
				final int patternNumber = data.getInt();
				data.getDouble();

				final Node node = nodeByNumber.get(nodeNumber);
				final Node parentNode = nodeByNumber.get(parentNumber);

				parentChanges.add(new ParentChange(node, node.parent, parentNode));

				if (widthByLevel.containsKey(node.depth))
					widthByLevel.put(node.depth, widthByLevel.get(node.depth) - 1);

				node.parent.children.remove(node);
				node.parent = parentNode;
				parentNode.children.add(node);
				node.g = g;
				node.ruleNumber = ruleNumber;
				node.ruleName = CurrentSimulator.getStaticModelData().getEdgeName(dptNumber, ruleNumber);
				node.relevantResources = getRelevantResources(data, patternNumber);
				node.depth = node.parent.depth + 1;

				updateGraphInfo(node);
				break;
			}

			case WORSE:
				break;
			}
			break;
		}

		case DECISION: {
			final ByteBuffer data = Tracer.prepareBufferForReading(entry.getData());
			final int number = data.getInt();
			Node solutionNode = nodeByNumber.get(number);
			solutionList.add(solutionNode);
			solutionFound = true;
			break;
		}
		}

		return isFinished;
	}

	private final String getRelevantResources(final ByteBuffer data, final int patternNumber) {
		final int numberOfRelevantResources = CurrentSimulator.getStaticModelData()
				.getNumberOfRelevantResources(patternNumber);

		final StringJoiner relResStringJoiner = new StringJoiner(StringFormat.ENUMERATION);

		for (int num = 0; num < numberOfRelevantResources; num++) {
			final int resNum = data.getInt();
			final String typeName = CurrentSimulator.getStaticModelData().getRelevantResourceTypeName(patternNumber,
					num);
			final int typeNum = CurrentSimulator.getStaticModelData().getResourceTypeNumber(typeName);
			final String name = CurrentSimulator.getStaticModelData().getResourceName(typeNum, resNum);
			final String resourceName = name != null ? name : typeName + Tracer.encloseIndex(resNum);

			relResStringJoiner.add(NamingHelper.convertName(resourceName, useShortNames));
		}
		return relResStringJoiner.getString();
	}

	private final void updateGraphInfo(final Node node) {
		if (widthByLevel.containsKey(node.depth))
			widthByLevel.put(node.depth, widthByLevel.get(node.depth) + 1);
		else
			widthByLevel.put(node.depth, 1);

		if (graphInfo.depth < node.depth)
			graphInfo.depth = node.depth;
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

	class ParentChange {
		Node node;
		Node worseParent;
		Node betterParent;

		ParentChange(final Node node, final Node worseParent, final Node betterParent) {
			this.node = node;
			this.worseParent = worseParent;
			this.betterParent = betterParent;
		}
	}

	final List<ParentChange> parentChanges = new ArrayList<ParentChange>();

	private int entryNumber = 0;
	private final int dptNumber;

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
