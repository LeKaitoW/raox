package ru.bmstu.rk9.rao.lib.dpt;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Supplier;

import ru.bmstu.rk9.rao.lib.database.Database;
import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.notification.Subscription.SubscriptionType;
import ru.bmstu.rk9.rao.lib.pattern.Pattern;
import ru.bmstu.rk9.rao.lib.pattern.Rule;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.ExecutionState;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.SimulatorState;
import ru.bmstu.rk9.rao.lib.simulator.ModelState;

public abstract class Search extends AbstractDecisionPoint {
	public Search() {
		CurrentSimulator.getSimulatorStateNotifier().addSubscriber(simulatorInitializedListener,
				SimulatorState.INITIALIZED, EnumSet.of(SubscriptionType.IGNORE_ACCUMULATED, SubscriptionType.ONE_SHOT));
		initializeEdges();
		init();
	}

	protected abstract void initializeEdges();

	protected boolean compareTops;
	protected Supplier<Double> priority;
	protected Supplier<Boolean> startCondition;
	protected Supplier<Double> heuristic;
	protected Supplier<Boolean> terminateCondition;
	private Logic parent;

	public Logic getParent() {
		return parent;
	}

	protected void setParent(Logic parent) {
		this.parent = parent;
		parent.addChild(this);
	}

	private final Subscriber simulatorInitializedListener = new Subscriber() {
		@Override
		public void fireChange() {
			CurrentSimulator.getExecutionStateNotifier().addSubscriber(executionAbortedListener,
					ExecutionState.EXECUTION_ABORTED, EnumSet.of(SubscriptionType.ONE_SHOT));
			CurrentSimulator.getExecutionStateNotifier().addSubscriber(executionStartedListener,
					ExecutionState.EXECUTION_STARTED, EnumSet.of(SubscriptionType.ONE_SHOT));
		}
	};

	private final Subscriber executionStartedListener = new Subscriber() {
		@Override
		public void fireChange() {
			allowSearch = true;
		}
	};

	private final Subscriber executionAbortedListener = new Subscriber() {
		@Override
		public void fireChange() {
			allowSearch = false;
		}
	};

	private volatile boolean allowSearch = false;

	private List<Edge> edges = new LinkedList<Edge>();

	public void addActivity(Edge a) {
		edges.add(a);
	}

	public class EdgeInfo {
		private EdgeInfo(int number, Rule rule) {
			this.number = number;
			this.rule = rule;
		}

		public final int number;
		public final Rule rule;
	}

	private class GraphNode {
		private GraphNode(int number, GraphNode parent) {
			this.number = number;
			this.parent = parent;
		}

		final int number;
		GraphNode parent;
		final List<GraphNode> children = new ArrayList<GraphNode>();

		EdgeInfo edgeInfo;

		double g;
		double h;

		ModelState state;
	}

	private Comparator<GraphNode> nodeComparator = new Comparator<GraphNode>() {
		@Override
		public int compare(GraphNode x, GraphNode y) {
			if (x.g + x.h < y.g + y.h)
				return -1;
			if (x.g + x.h > y.g + y.h)
				return 1;

			return x.number - y.number;
		}
	};

	private PriorityQueue<GraphNode> nodesOpen = new PriorityQueue<GraphNode>(1, nodeComparator);
	private LinkedList<GraphNode> nodesClosed = new LinkedList<GraphNode>();

	private GraphNode head;
	private GraphNode current;

	private long memory;
	private long time;

	private int nodeIndex;
	private int countSpawned;

	@Override
	public boolean check() {
		if (startCondition != null && !startCondition.get())
			return false;

		time = System.currentTimeMillis();
		memory = Runtime.getRuntime().freeMemory();

		nodeIndex = 0;
		countSpawned = 0;

		serializeStart();

		if (!allowSearch)
			return stop(StopCode.ABORTED);

		if (terminateCondition.get())
			return stop(StopCode.CONDITION);

		nodesOpen.clear();
		nodesClosed.clear();

		head = new GraphNode(nodeIndex++, null);
		head.state = CurrentSimulator.getModelState();
		nodesOpen.add(head);

		while (!nodesOpen.isEmpty()) {
			if (!allowSearch)
				return stop(StopCode.ABORTED);

			current = nodesOpen.poll();
			nodesClosed.add(current);
			current.state.deploy();

			serializeOpen(current);

			if (terminateCondition.get())
				return stop(StopCode.SUCCESS);

			nodesOpen.addAll(spawnChildren(current));
		}

		head.state.deploy();
		return stop(StopCode.FAIL);
	}

	public static enum SpawnStatus {
		NEW, WORSE, BETTER
	}

	private LinkedList<GraphNode> spawnChildren(GraphNode parent) {
		LinkedList<GraphNode> children = new LinkedList<GraphNode>();

		for (int edgeNumber = 0; edgeNumber < edges.size(); edgeNumber++) {
			Edge edge = edges.get(edgeNumber);
			double value = 0;

			ModelState parentStateCopy = parent.state.shallowCopy();
			parentStateCopy.deploy();

			if (!edge.check())
				continue;

			GraphNode newChild = new GraphNode(nodeIndex, parent);

			countSpawned++;

			SpawnStatus spawnStatus = SpawnStatus.NEW;

			if (edge.applyOrder == ApplyOrder.BEFORE)
				value = edge.calculateValue();

			newChild.state = parentStateCopy;
			newChild.state.deploy();

			Rule executed = edge.execute();
			newChild.edgeInfo = new EdgeInfo(edgeNumber, executed);

			if (edge.applyOrder == ApplyOrder.AFTER)
				value = edge.calculateValue();

			newChild.g = parent.g + value;
			newChild.h = heuristic.get();

			add_child: {
				if (compareTops) {
					for (Collection<GraphNode> nodesList : Arrays.asList(nodesOpen, nodesClosed)) {
						for (GraphNode node : nodesList) {
							if (!newChild.state.checkEqual(node.state))
								continue;

							if (newChild.g < node.g) {
								if (node.parent != null)
									node.parent.children.remove(node);
								node.parent = newChild.parent;
								node.edgeInfo = newChild.edgeInfo;
								reduceCost(node, node.g - newChild.g);
								newChild = node;
								spawnStatus = SpawnStatus.BETTER;
								break add_child;
							} else {
								spawnStatus = SpawnStatus.WORSE;
								break add_child;
							}
						}
					}
				}

				children.add(newChild);
				nodeIndex++;
			}

			serializeTops(newChild, spawnStatus, value);

			CurrentSimulator.getExecutionStateNotifier().notifySubscribers(ExecutionState.SEARCH_STEP);
			parent.state.deploy();
		}

		return children;

	}

	private final void reduceCost(GraphNode node, double delta) {
		node.g -= delta;
		for (final GraphNode child : node.children) {
			reduceCost(child, delta);
		}
	}

	public static enum StopCode {
		ABORTED, CONDITION, SUCCESS, FAIL
	}

	private boolean stop(StopCode code) {
		double finalCost;
		boolean result = false;

		switch (code) {
		case ABORTED:
			if (head != null)
				head.state.deploy();
			if (current != null)
				finalCost = current.g;
			else
				finalCost = 0;
			break;
		case CONDITION:
			finalCost = 0;
			break;
		case SUCCESS:
			databaseAddDecision();
			result = true;
			finalCost = current.g;
			break;
		case FAIL:
		default:
			finalCost = 0;
			break;
		}

		serializeStop(code, finalCost);

		return result;
	}

	private void databaseAddDecision() {
		LinkedList<GraphNode> decision = new LinkedList<GraphNode>();
		GraphNode node = current;

		while (node != head) {
			decision.add(node);
			node = node.parent;
		}

		serializeDecision(decision);
	}

	public enum SerializationLevel implements Comparable<SerializationLevel> {
		ALL("all", 4), TOPS("tops", 2), DECISION("decision", 1), START_STOP("start/stop", 0);

		private SerializationLevel(String name, int comparisonValue) {
			this.name = name;
			this.comparisonValue = comparisonValue;
		}

		private final String name;
		private final int comparisonValue;

		@Override
		public String toString() {
			return name;
		}
	}

	private final Comparator<SerializationLevel> serializationLevelComparator = new Comparator<SerializationLevel>() {
		@Override
		public int compare(SerializationLevel o1, SerializationLevel o2) {
			return o1.comparisonValue - o2.comparisonValue;
		}
	};

	private final boolean enoughSensitivity(SerializationLevel checkedType) {
		for (SerializationLevel type : SerializationLevel.values()) {
			if (CurrentSimulator.getDatabase().sensitiveTo(getTypeName() + "." + type.toString()))
				if (serializationLevelComparator.compare(type, checkedType) >= 0)
					return true;
		}

		return false;
	}

	private final void serializeStart() {
		if (!enoughSensitivity(SerializationLevel.START_STOP))
			return;

		CurrentSimulator.getDatabase().addSearchEntry(this, Database.SearchEntryType.BEGIN, null);
	}

	private final void serializeStop(StopCode code, double finalCost) {
		if (!enoughSensitivity(SerializationLevel.START_STOP))
			return;

		ByteBuffer data = ByteBuffer.allocate(Database.TypeSize.BYTE + Database.TypeSize.DOUBLE
				+ Database.TypeSize.INT * 4 + Database.TypeSize.LONG * 2);

		data.put((byte) code.ordinal()).putLong(System.currentTimeMillis() - time)
				.putLong(memory - Runtime.getRuntime().freeMemory()).putDouble(finalCost).putInt(nodesClosed.size())
				.putInt(nodesOpen.size()).putInt(countSpawned);

		CurrentSimulator.getDatabase().addSearchEntry(this, Database.SearchEntryType.END, data);
	}

	private final void serializeOpen(GraphNode node) {
		if (node != head && enoughSensitivity(SerializationLevel.TOPS)) {
			ByteBuffer data = ByteBuffer.allocate(Database.TypeSize.INT * 2 + Database.TypeSize.DOUBLE * 2);

			data.putInt(node.number).putInt(node.parent.number).putDouble(node.g).putDouble(node.h);

			CurrentSimulator.getDatabase().addSearchEntry(this, Database.SearchEntryType.OPEN, data);
		}
	}

	private final void serializeTops(GraphNode node, SpawnStatus spawnStatus, double value) {
		Rule rule = node.edgeInfo.rule;
		int edgeNumber = node.edgeInfo.number;

		if (enoughSensitivity(SerializationLevel.TOPS)) {
			List<Integer> relevantResourcesNumbers = rule.getRelevantResourcesNumbers();

			ByteBuffer data = ByteBuffer.allocate(Database.TypeSize.BYTE + Database.TypeSize.DOUBLE * 3
					+ Database.TypeSize.INT * (4 + relevantResourcesNumbers.size()));

			data.put((byte) spawnStatus.ordinal()).putInt(node.number).putInt(node.parent.number).putDouble(node.g)
					.putDouble(node.h).putInt(edgeNumber)
					.putInt(CurrentSimulator.getStaticModelData().getPatternNumber(rule.getTypeName()))
					.putDouble(value);

			for (int num : relevantResourcesNumbers)
				data.putInt(num);

			CurrentSimulator.getDatabase().addSearchEntry(this, Database.SearchEntryType.SPAWN, data);
		}

		if (enoughSensitivity(SerializationLevel.ALL)) {
			rule.addResourceEntriesToDatabase(Pattern.ExecutedFrom.SEARCH, this.getTypeName());
		}
	}

	private final void serializeDecision(LinkedList<GraphNode> decision) {
		if (!enoughSensitivity(SerializationLevel.DECISION))
			return;

		GraphNode node;
		for (Iterator<GraphNode> it = decision.descendingIterator(); it.hasNext();) {
			node = it.next();

			Rule rule = node.edgeInfo.rule;
			List<Integer> relevantResourcesNumbers = rule.getRelevantResourcesNumbers();

			ByteBuffer data = ByteBuffer.allocate(Database.TypeSize.INT * (3 + relevantResourcesNumbers.size()));

			data.putInt(node.number).putInt(node.edgeInfo.number)
					.putInt(CurrentSimulator.getStaticModelData().getPatternNumber(node.edgeInfo.rule.getTypeName()));

			for (int num : relevantResourcesNumbers)
				data.putInt(num);

			CurrentSimulator.getDatabase().addSearchEntry(this, Database.SearchEntryType.DECISION, data);

			if (enoughSensitivity(SerializationLevel.ALL)) {
				rule.addResourceEntriesToDatabase(Pattern.ExecutedFrom.SOLUTION, this.getTypeName());
			}
		}
	}

	@Override
	public Supplier<Double> getPriority() {
		return () -> 0.0;
	}
}
