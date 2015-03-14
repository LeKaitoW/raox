package ru.bmstu.rk9.rdo.lib;

import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

public class DecisionPointPrior extends DecisionPoint
{
	public DecisionPointPrior(String name, Priority priority, Condition condition)
	{
		super(name, priority, condition);
	}

	public static abstract class Activity extends DecisionPoint.Activity
	{
		public Activity(String name, DecisionPoint.Priority priority)
		{
			super(name);
			this.priority = priority;
		}

		public final DecisionPoint.Priority priority;
	}

	private static Comparator<Activity> comparator = new Comparator<Activity>()
	{
		@Override
		public int compare(Activity x, Activity y)
		{
			if(x.priority == null)
				return 1;

			if(y.priority == null)
				return -1;

			if(x.priority.get() > y.priority.get())
				return -1;

			if(x.priority.get() <= y.priority.get())
				return 1;

			return 0;
		}
	};

	private List<Activity> activities = new ArrayList<Activity>();

	public void addActivity(Activity a)
	{
		activities.add(a);
	}

	@Override
	public boolean check()
	{
		List<DecisionPoint> all = new ArrayList<DecisionPoint>(children);

		all.add(this);

		for(DecisionPoint d : all)
			if(d.priority != null)
				d.priority.calculate();

		Collections.sort(all, prioritizer);

		for(DecisionPoint d : all)
			if(d == this)
			{
				if(checkCurrent())
					return true;
			}
			else
			{
				if(d.check())
					return true;
			}
				return false;
	}

	private boolean checkCurrent()
	{
		if(condition != null && !condition.check())
			return false;

		for(Activity a : activities)
			if(a.priority != null)
				a.priority.calculate();

		Collections.sort(activities, comparator);

		return checkActivities();
	}

	private boolean checkActivities()
	{
		for(Activity a : activities)
			if(a.checkActivity())
			{
				a.executeActivity();
				return true;
			}

		return false;
	}
}
