package ru.bmstu.rk9.rao.ui.graph;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import ru.bmstu.rk9.rao.lib.database.Database;
import ru.bmstu.rk9.rao.lib.database.Database.Entry;
import ru.bmstu.rk9.rao.lib.database.Database.EntryType;
import ru.bmstu.rk9.rao.lib.database.Database.TypeSize;
import ru.bmstu.rk9.rao.lib.dpt.DecisionPointSearch;
import ru.bmstu.rk9.rao.lib.modelStructure.ActivityCache;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.ui.trace.StringJoiner;
import ru.bmstu.rk9.rao.ui.trace.Tracer;
import ru.bmstu.rk9.rao.ui.trace.StringJoiner.StringFormat;

public class TreeBuilder {

	public final void updateTree() {
		List<Entry> entries = Simulator.getDatabase().getAllEntries();
		while (entryNumber < entries.size()) {
			final Database.Entry entry = entries.get(entryNumber);
			parseEntry(entry);
			entryNumber++;
		}
	}

	public ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);

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

	public final Map<Integer, List<Node>> listMap = new HashMap<Integer, List<Node>>();

	private boolean parseEntry(Entry entry) {
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

		switch (searchEntryType) {
		case BEGIN: {
			Node treeNode = new Node();
			solutionMap.put(dptNumber, new ArrayList<Node>());
			listMap.put(dptNumber, new ArrayList<Node>());
			dptSimulationInfoMap.put(dptNumber, new DPTSimulationInfo());
			treeNode.parent = null;
			treeNode.index = 0;
			treeNode.label = Integer.toString(treeNode.index);
			rwLock.writeLock().lock();
			try {
				listMap.get(dptNumber).add(treeNode);
				lastAddedNodeIndexMap.put(dptNumber, treeNode.index);
			} finally {
				rwLock.writeLock().unlock();
			}
			break;
		}
		case END: {
			final ByteBuffer data = Tracer.prepareBufferForReading(entry
					.getData());
			infoMap.put(dptNumber, new GraphInfo());
			Tracer.skipPart(data, TypeSize.BYTE + TypeSize.LONG * 2);

			final double finalCost = data.getDouble();
			final int totalOpened = data.getInt();
			final int totalNodes = data.getInt();

			rwLock.writeLock().lock();
			try {
				infoMap.get(dptNumber).solutionCost = finalCost;
				infoMap.get(dptNumber).numOpened = totalOpened;
				infoMap.get(dptNumber).numNodes = totalNodes;
				dptSimulationInfoMap.get(dptNumber).isFinished = true;
			} finally {
				rwLock.writeLock().unlock();
			}
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
				ActivityCache activity = Simulator.getModelStructureCache()
						.getDecisionPointsInfo().get(dptNumber)
						.getActivitiesInfo().get(ruleNum);
				final int patternNumber = activity.getPatternNumber();
				final double ruleCost = data.getDouble();
				treeNode.parent = listMap.get(dptNumber).get(parentNumber);
				listMap.get(dptNumber).get(parentNumber).children.add(treeNode);

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
				rwLock.writeLock().lock();
				try {
					listMap.get(dptNumber).add(treeNode);
					lastAddedNodeIndexMap.put(dptNumber, treeNode.index);
				} finally {
					rwLock.writeLock().unlock();
				}
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
			List<Node> currentSolutionList = solutionMap.get(dptNumber);
			List<Node> currentNodeList = listMap.get(dptNumber);
			Node solutionNode = currentNodeList.get(number);
			currentSolutionList.add(solutionNode);
			break;
		}
		}
		return true;
	}

	public final Map<Integer, List<Node>> solutionMap = new HashMap<Integer, List<Node>>();

	public class GraphInfo {
		public double solutionCost;
		public int numOpened;
		public int numNodes;
	}

	public final Map<Integer, GraphInfo> infoMap = new HashMap<Integer, GraphInfo>();

	public class DPTSimulationInfo {
		public boolean isFinished = false;
	}

	public final Map<Integer, DPTSimulationInfo> dptSimulationInfoMap = new HashMap<Integer, DPTSimulationInfo>();
	private int entryNumber = 0;
	private final Map<Integer, Integer> lastAddedNodeIndexMap = new HashMap<Integer, Integer>();

	public Map<Integer, Integer> getLastAddedNodeIndexMap() {
		return lastAddedNodeIndexMap;
	}

	public int getEntryNumber() {
		return entryNumber;
	}
}
