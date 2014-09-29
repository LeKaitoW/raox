package ru.bmstu.rk9.rdo.lib;

import java.util.Comparator;

import java.util.List;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class DecisionPointSearch<T extends ModelState<T>> extends DecisionPoint
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

	private class GraphNode
	{
		public GraphNode parent = null;

		public LinkedList<GraphNode> children;

		public double g;
		public double h;

		public T state;
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

	static volatile boolean allowSearch = true;

	@Override
	public boolean check()
	{
		if (terminate.check() && !allowSearch)
			return false;

		nodesOpen.clear();
		nodesClosed.clear();

		if (condition != null && !condition.check())
			return false;

		GraphNode head = new GraphNode();
		head.state = retriever.get();
		nodesOpen.add(head);

		while (nodesOpen.size() > 0)
		{
			if (!allowSearch)
				return false;

			GraphNode current = nodesOpen.poll();
			nodesClosed.add(current);
			current.state.deploy();

			if (terminate.check())
				return true;

			current.children = spawnChildren(current);
			nodesOpen.addAll(current.children);
		}
		head.state.deploy();
		return false;
	}

	private LinkedList<GraphNode> spawnChildren(GraphNode parent)
	{
		LinkedList<GraphNode> children = new LinkedList<GraphNode>();

		for (Activity a : activities)
		{
			double value = 0;

			if (a.checkActivity())
			{
				GraphNode newChild = new GraphNode();
				newChild.parent = parent;

				if (a.applyMoment == Activity.ApplyMoment.before)
					value = a.calculateValue();

				newChild.state = parent.state.copy();
				newChild.state.deploy();
				a.executeActivity();

				if (a.applyMoment == Activity.ApplyMoment.after)
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
									break compare_tops;
								}
								else
									break add_child;

						for (GraphNode closed : nodesClosed)
							if (newChild.state.checkEqual(closed.state))
								if (newChild.g < closed.g)
								{
									nodesClosed.remove(closed);
									break compare_tops;
								}
								else
									break add_child;
					}
					children.add(newChild);
				}
				parent.state.deploy();
			}
		}
		return children;
	}
}
