package ru.bmstu.rk9.rao.lib.dpt;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;

public class Logic extends AbstractDecisionPoint {
	public Logic(Supplier<Double> priority, Supplier<Boolean> condition) {
		this.priority = priority;
		this.condition = condition;
	}

	private static Comparator<Activity> comparator = new Comparator<Activity>() {
		@Override
		public int compare(Activity x, Activity y) {
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

	protected Supplier<Double> priority;
	protected Supplier<Boolean> condition;
	protected Logic parent;

	private List<AbstractDecisionPoint> children = new ArrayList<AbstractDecisionPoint>();

	void addChild(AbstractDecisionPoint child) {
		children.add(child);
	}

	protected SortedSet<Activity> activities = new TreeSet<Activity>(comparator);

	@Override
	public boolean check() {
		SortedSet<AbstractDecisionPoint> all = new TreeSet<AbstractDecisionPoint>(DPTManager.prioritizer);

		all.addAll(children);
		all.add(this);

		for (AbstractDecisionPoint d : all)
			if (d == this) {
				if (checkCurrent())
					return true;
			} else {
				if (d.check())
					return true;
			}
		return false;
	}

	private boolean checkCurrent() {
		if (condition != null && !condition.get())
			return false;

		return checkActivities();
	}

	private boolean checkActivities() {
		for (Activity a : activities)
			if (a.execute())
				return true;

		return false;
	}

	/* API available to user */

	public void setParent(Logic parent) {
		this.parent = parent;
		parent.addChild(this);
	}

	public Logic getParent() {
		return parent;
	}

	public void setPriority(Supplier<Double> priority) {
		this.priority = priority;
	}

	@Override
	public Supplier<Double> getPriority() {
		return priority;
	}

	public void setCondition(Supplier<Boolean> condition) {
		this.condition = condition;
	}

	public Supplier<Boolean> getCondition() {
		return condition;
	}

	public void addActivity(Activity a) {
		activities.add(a);
	}

	public SortedSet<Activity> getActivities() {
		return activities;
	}
}
