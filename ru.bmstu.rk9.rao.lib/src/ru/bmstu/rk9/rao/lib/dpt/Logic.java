package ru.bmstu.rk9.rao.lib.dpt;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;

import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public abstract class Logic extends AbstractDecisionPoint {
	public Logic() {
		initializeActivities();
		init();
	}

	protected abstract void initializeActivities();

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

	private Supplier<Double> priority = () -> 0.0;
	private Supplier<Boolean> condition = () -> true;
	private Logic parent = null;

	private List<AbstractDecisionPoint> children = new ArrayList<AbstractDecisionPoint>();

	protected void addChild(AbstractDecisionPoint child) {
		children.add(child);
	}

	private SortedSet<Activity> activities = new TreeSet<Activity>(comparator);

	@Override
	public boolean check() {
		SortedSet<AbstractDecisionPoint> all = new TreeSet<AbstractDecisionPoint>(DPTManager.prioritizer);

		all.addAll(children);
		all.add(this);

		for (AbstractDecisionPoint decisionPoint : all)
			if (decisionPoint == this) {
				if (checkCurrent())
					return true;
			} else {
				if (decisionPoint.check())
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
		for (Activity activity : activities)
			if (activity.execute()) {
				Simulator.getDatabase().addDecisionEntry(this, activity);
				activity.getPattern().addResourceEntriesToDatabase(null, null);

				return true;
			}

		return false;
	}

	/* API available to user */

	protected void setParent(Logic parent) {
		this.parent = parent;
		parent.addChild(this);
	}

	protected Logic getParent() {
		return parent;
	}

	protected void setPriority(Supplier<Double> priority) {
		this.priority = priority;
	}

	@Override
	public Supplier<Double> getPriority() {
		return priority;
	}

	protected void setCondition(Supplier<Boolean> condition) {
		this.condition = condition;
	}

	protected Supplier<Boolean> getCondition() {
		return condition;
	}

	protected void addActivity(Activity a) {
		a.setNumber(activities.size());
		activities.add(a);
	}
}
