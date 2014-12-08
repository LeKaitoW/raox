package ru.bmstu.rk9.rdo.lib;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Comparator;

public class DecisionPointSearch<T extends ModelState<T>> extends DecisionPoint implements Subscriber
{
	private DecisionPoint.Condition terminate;

	private DatabaseRetriever<T> retriever;

	private boolean compareTops;

	private EvaluateBy evaluateBy;

	public DecisionPointSearch
	(
		String name,
		Condition condition,
		Condition terminate,
		EvaluateBy evaluateBy,
		boolean compareTops,
		DatabaseRetriever<T> retriever
	)
	{
		super(name, null, condition);
		this.terminate = terminate;
		this.evaluateBy = evaluateBy;
		this.retriever = retriever;
		this.compareTops = compareTops;

		Simulator
			.getNotifier()
			.getSubscription("ExecutionAborted")
			.addSubscriber(this);
	}

	public static interface EvaluateBy
	{
		public double get();
	}

	public static abstract class Activity extends DecisionPoint.Activity
	{
		public enum ApplyMoment { before, after }

		public Activity(String name, ApplyMoment applyMoment)
		{
			super(name);
			this.applyMoment = applyMoment;
		}

		public abstract double calculateValue();

		public final ApplyMoment applyMoment;
	}

	private List<Activity> activities = new LinkedList<Activity>();

	public void addActivity(Activity a)
	{
		activities.add(a);
	}

	public static interface DatabaseRetriever<T extends ModelState<T>>
	{
		public T get();
	}

	public class ActivityInfo
	{
		private ActivityInfo(int number, Rule rule)
		{
			this.number = number;
			this.rule = rule;
		}

		public final int number;
		public final Rule rule;
	}

	private class GraphNode
	{
		private GraphNode(int number, GraphNode parent)
		{
			this.number = number;
			this.parent = parent;
		}

		final int number;
		final GraphNode parent;

		ActivityInfo activityInfo;

		LinkedList<GraphNode> children;

		double g;
		double h;

		T state;
	}

	private Comparator<GraphNode> nodeComparator = new Comparator<GraphNode>()
	{
		@Override
		public int compare(GraphNode x, GraphNode y)
		{
			if (x.g + x.h < y.g + y.h)
				return -1;
			if (x.g + x.h > y.g + y.h)
				return 1;
			return 0;
		}
	};

	private PriorityQueue<GraphNode> nodesOpen = new PriorityQueue<GraphNode>(1, nodeComparator);
	private LinkedList<GraphNode> nodesClosed = new LinkedList<GraphNode>();

	private volatile boolean allowSearch = true;

	@Override
	public void fireChange()
	{
		this.allowSearch = false;
	}

	private GraphNode head;
	private GraphNode current; 

	private long memory;
	private long time;

	private int totalOpened;
	private int totalSpawned;
	private int totalAdded;

	@Override
	public boolean check()
	{
		Database database = Simulator.getDatabase();

		time = System.currentTimeMillis();
		memory = Runtime.getRuntime().freeMemory();

		totalOpened = 0;
		totalSpawned = 0;
		totalAdded = 0;

		if (enoughSensitivity(SerializationLevel.START_STOP))
		{
			Simulator.getDatabase().addSearchEntry(
				this, Database.SearchEntryType.BEGIN, null);
		}

		if(!allowSearch)
			return stop(StopCode.ABORTED);

		if (condition != null && !condition.check() || terminate.check())
			return stop(StopCode.CONDITION);

		nodesOpen.clear();
		nodesClosed.clear();

		head = new GraphNode(totalAdded++, null);
		head.state = retriever.get();
		head.children = spawnChildren(head);

		nodesOpen.addAll(head.children);

		while(nodesOpen.size() > 0)
		{
			if(!allowSearch)
				return stop(StopCode.ABORTED);

			current = nodesOpen.poll();
			nodesClosed.add(current);
			current.state.deploy();

			totalOpened++;

			ByteBuffer data = ByteBuffer.allocate
			(
				Database.TypeSize.INTEGER * 2 +
				Database.TypeSize.DOUBLE * 2
			);

			data
				.putInt(current.number)
				.putInt(current.parent.number)
				.putDouble(current.g)
				.putDouble(current.h);

			if (enoughSensitivity(SerializationLevel.TOPS))
			{
				database.addSearchEntry(
					this, Database.SearchEntryType.OPEN, data);
			}

			if (terminate.check())
				return stop(StopCode.SUCCESS);

			current.children = spawnChildren(current);
			nodesOpen.addAll(current.children);
		}
		head.state.deploy();
		return stop(StopCode.FAIL);
	}

	public static enum SpawnStatus
	{
		NEW, WORSE, BETTER
	}

	private LinkedList<GraphNode> spawnChildren(GraphNode parent)
	{
		LinkedList<GraphNode> children = new LinkedList<GraphNode>();

		for(int i = 0; i < activities.size(); i++)
		{
			if(!allowSearch)
				return children;

			Activity a = activities.get(i);
			double value = 0;

			if(a.checkActivity())
			{
				GraphNode newChild = new GraphNode(totalAdded, parent);

				totalSpawned++;

				SpawnStatus spawnStatus = SpawnStatus.NEW;

				if(a.applyMoment == Activity.ApplyMoment.before)
					value = a.calculateValue();

				newChild.state = parent.state.copy();
				newChild.state.deploy();

				Rule executed = a.executeActivity();
				newChild.activityInfo = new ActivityInfo(i, executed);

				if(a.applyMoment == Activity.ApplyMoment.after)
					value = a.calculateValue();

				newChild.g = parent.g + value;
				newChild.h = evaluateBy.get();

				add_child:
				{
					compare_tops:
					if (compareTops)
					{
						for (GraphNode open : nodesOpen)
							if (newChild.state.checkEqual(open.state))
								if (newChild.g < open.g)
								{
									nodesOpen.remove(open);
									spawnStatus = SpawnStatus.BETTER;
									break compare_tops;
								}
								else
								{
									spawnStatus = SpawnStatus.WORSE;
									break add_child;
								}

						for (GraphNode closed : nodesClosed)
							if (newChild.state.checkEqual(closed.state))
								if (newChild.g < closed.g)
								{
									nodesClosed.remove(closed);
									spawnStatus = SpawnStatus.BETTER;
									break compare_tops;
								}
								else
								{
									spawnStatus = SpawnStatus.WORSE;
									break add_child;
								}
					}
					children.add(newChild);
					totalAdded++;
				}
				parent.state.deploy();

				int[] relevantResources = newChild.activityInfo.rule.getRelevantInfo();

				if (enoughSensitivity(SerializationLevel.TOPS))
				{
					ByteBuffer data = ByteBuffer.allocate
					(
						Database.TypeSize.BYTE + Database.TypeSize.DOUBLE * 3 +
						Database.TypeSize.INTEGER * (3 + relevantResources.length)
					);

					data
						.put((byte)spawnStatus.ordinal())
						.putInt(newChild.number)
						.putInt(parent.number)
						.putDouble(newChild.g)
						.putDouble(newChild.h)
						.putInt(i)
						.putDouble(value);

					for(int relres : relevantResources)
						data.putInt(relres);

					Simulator.getDatabase().addSearchEntry(
						this, Database.SearchEntryType.SPAWN, data);
				}

				if (enoughSensitivity(SerializationLevel.ALL))
				{
					executed.addResourceEntriesToDatabase(
						Pattern.ExecutedFrom.SEARCH);
				}
			}
		}
		return children;
	}

	public static enum StopCode
	{
		ABORTED, CONDITION, SUCCESS, FAIL
	}

	private boolean stop(StopCode code)
	{
		boolean decisionCode = false;

		ByteBuffer data = ByteBuffer.allocate
		(
			Database.TypeSize.BYTE + Database.TypeSize.DOUBLE * 2 + 
			Database.TypeSize.INTEGER * 4 + Database.TypeSize.LONG * 2
		);

		double finalCost;

		switch(code)
		{
			case ABORTED:
				if(head != null)
					head.state.deploy();
				if(current != null)
					finalCost = current.g;
				else
					finalCost = 0;
			break;
			case CONDITION:
				finalCost = 0;
			break;
			case SUCCESS:
				databaseAddDecision();
			default:
				finalCost = current.g;
			break;
		}

		if (enoughSensitivity(SerializationLevel.START_STOP))
		{
			data
				.put((byte)code.ordinal())
				.putDouble(Simulator.getTime())
				.putLong(System.currentTimeMillis() - time)
				.putLong(memory - Runtime.getRuntime().freeMemory())
				.putDouble(finalCost)
				.putInt(totalOpened)
				.putInt(nodesOpen.size() + nodesClosed.size())
				.putInt(totalAdded)
				.putInt(totalSpawned);

			Simulator.getDatabase().addSearchEntry(
				this, Database.SearchEntryType.END, data);
		}

		return decisionCode;
	}

	private void databaseAddDecision()
	{
		Database database = Simulator.getDatabase();

		LinkedList<GraphNode> decision = new LinkedList<GraphNode>();

		GraphNode node = current;
		while(node != head)
		{
			decision.add(node);
			node = node.parent;
		}

		if (enoughSensitivity(SerializationLevel.DECISION))
		{
			for(Iterator<GraphNode> it = decision.descendingIterator(); it.hasNext();)
			{
				node = it.next();

				Rule rule = node.activityInfo.rule;
				int[] relevantResources = rule.getRelevantInfo();

				ByteBuffer data = ByteBuffer.allocate(
					Database.TypeSize.INTEGER * (2 + relevantResources.length));

				data
					.putInt(node.number)
					.putInt(node.activityInfo.number);

				for(int relres : relevantResources)
					data.putInt(relres);

				database.addSearchEntry(
					this, Database.SearchEntryType.DECISION, data);

				if (enoughSensitivity(SerializationLevel.ALL))
				{
					rule.addResourceEntriesToDatabase(
						Pattern.ExecutedFrom.SOLUTION);
				}
			}
		}
	}

	public enum SerializationLevel implements Comparable<SerializationLevel>
	{
		ALL("all", 4),
		TOPS("tops", 2),
		DECISION("decision", 1),
		START_STOP("start/stop", 0);

		private SerializationLevel(String name, int comparisonValue)
		{
			this.name = name;
			this.comparisonValue = comparisonValue;
		}

		private final String name;
		private final int comparisonValue;

		@Override
		public String toString()
		{
			return name;
		}
	}

	private class SerializationTypeComparator implements Comparator<SerializationLevel>
	{
		@Override
		public int compare(SerializationLevel o1, SerializationLevel o2)
		{
			return o1.comparisonValue - o2.comparisonValue;
		}
	}

	private final boolean enoughSensitivity(SerializationLevel checkedType)
	{
		SerializationTypeComparator comparator =
			new SerializationTypeComparator();

		for (SerializationLevel type : SerializationLevel.values())
		{
			if (Simulator.getDatabase().sensitiveTo(
					getName() + "." + type.toString()))
				if (comparator.compare(type, checkedType) >= 0)
					return true;
		}

		return false;
	}
}
