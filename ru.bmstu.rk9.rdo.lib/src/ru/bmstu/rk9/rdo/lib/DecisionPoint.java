package ru.bmstu.rk9.rdo.lib;

import java.util.Comparator;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

public class DecisionPoint
{
	private String name;

	public static abstract class Priority
	{
		abstract public void calculate();

		protected double priority;

		public double get()
		{
			return priority;
		}
	}

	protected final Priority priority;

	protected static Comparator<DecisionPoint> prioritizer = new Comparator<DecisionPoint>()
	{
		@Override
		public int compare(DecisionPoint x, DecisionPoint y)
		{
			if (x.priority == null)
				return 1;

			if (y.priority == null)
				return -1;

			if (x.priority.get() > y.priority.get())
				return -1;

			if (x.priority.get() <= y.priority.get())
				return 1;

			return 0;
		}
	};

	public static interface Condition
	{
		public boolean check();
	}

	protected Condition condition;

	public DecisionPoint(String name, Priority priority, Condition condition)
	{
		this.name = name;
		this.priority = priority;
		this.condition = condition;
	}

	public String getName()
	{
		return name;
	}

	protected ArrayList<DecisionPoint> children = new ArrayList<DecisionPoint>();

	public void addChild(DecisionPoint child)
	{
		children.add(child);
	}

	public static abstract class Activity
	{
		private String name;

		public Activity(String name)
		{
			this.name = name;
		}

		public String getName()
		{
			return name;
		}

		public abstract boolean checkActivity();
		public abstract Pattern executeActivity();
	}

	private List<Activity> activities = new LinkedList<Activity>();

	public void addActivity(Activity a)
	{
		activities.add(a);
	}

	public boolean check()
	{
		if (!children.isEmpty())
			for (DecisionPoint c : children)
				if (c.check())
					return true;

		if (condition != null && !condition.check())
			return false;

		return checkActivities();
	}

	private boolean checkActivities()
	{
		for (Activity a : activities)
			if (a.checkActivity())
			{
				a.executeActivity();
				return true;
			}

		return false;
	}
}
