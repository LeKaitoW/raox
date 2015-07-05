package ru.bmstu.rk9.rao.lib;

import java.util.List;
import java.util.ArrayList;

import java.util.Collection;
import java.util.Iterator;

import java.util.PriorityQueue;

public class CombinationalChoiceFrom<R, PT> {
	private R set;
	private RelevantResourcesManager<R> setManager;
	private PriorityQueue<R> matchingList;

	public CombinationalChoiceFrom(R set,
			SimpleChoiceFrom.ChoiceMethod<R, R, PT> comparator,
			RelevantResourcesManager<R> setManager) {
		this.set = set;
		this.setManager = setManager;
		matchingList = new PriorityQueue<R>(1, comparator);
	}

	public static abstract class Setter<R, T> {
		public abstract void set(R set, T resource);
	}

	public static interface Retriever<T> {
		public Collection<T> getResources();
	}

	public static abstract class RelevantResourcesManager<R> {
		public abstract R create(R set);

		public abstract void apply(R origin, R set);
	}

	public static class Finder<R, T, PT> {
		private Retriever<T> retriever;
		private SimpleChoiceFrom<R, T, PT> choice;
		private Setter<R, T> setter;

		public Finder(Retriever<T> retriever,
				SimpleChoiceFrom<R, T, PT> choice, Setter<R, T> setter) {
			this.retriever = retriever;
			this.choice = choice;
			this.setter = setter;
		}

		public boolean find(R set, Iterator<Finder<R, ?, PT>> finder,
				PriorityQueue<R> matchingList,
				RelevantResourcesManager<R> setManager, PT parameters) {
			Collection<T> all = choice.findAll(set, retriever.getResources(),
					parameters);
			Iterator<T> iterator = all.iterator();
			if (finder.hasNext()) {
				Finder<R, ?, PT> currentFinder = finder.next();
				while (iterator.hasNext()) {
					setter.set(set, iterator.next());
					if (currentFinder.find(set, finder, matchingList,
							setManager, parameters))
						if (matchingList.iterator() == null)
							return true;
				}
				return false;
			} else {
				if (!all.isEmpty())
					if (matchingList.comparator() == null) {
						setter.set(set, all.iterator().next());
						return true;
					} else {
						for (T a : all) {
							setter.set(set, a);
							matchingList.add(setManager.create(set));
						}
						return true;
					}
				else
					return false;
			}
		}
	}

	private List<Finder<R, ?, PT>> finders = new ArrayList<Finder<R, ?, PT>>();

	public void addFinder(Finder<R, ?, PT> finder) {
		finders.add(finder);
	}

	public boolean find(PT parameters) {
		matchingList.clear();

		if (finders.isEmpty())
			return true;

		Iterator<Finder<R, ?, PT>> finder = finders.iterator();

		if (finder.next().find(set, finder, matchingList, setManager,
				parameters)
				&& matchingList.comparator() == null)
			return true;

		if (!matchingList.isEmpty()) {
			setManager.apply(set, matchingList.poll());
			return true;
		} else
			return false;
	}
}
