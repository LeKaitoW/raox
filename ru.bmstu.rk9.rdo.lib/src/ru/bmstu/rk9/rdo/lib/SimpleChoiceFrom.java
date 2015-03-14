package ru.bmstu.rk9.rdo.lib;

import java.util.Collection;
import java.util.Iterator;

import java.util.Queue;
import java.util.LinkedList;

import java.util.PriorityQueue;
import java.util.Comparator;

public class SimpleChoiceFrom<P, T, PT> {
	public static interface Checker<P, T, PT> {
		public boolean check(P resources, T res, PT parameters);
	}

	public static abstract class ChoiceMethod<P, T, PT> implements
			Comparator<T> {
		protected P resources;
		protected PT parameters;

		private void setPattern(P resources, PT parameters) {
			this.resources = resources;
			this.parameters = parameters;
		}
	}

	private Checker<P, T, PT> checker;
	private ChoiceMethod<P, T, PT> comparator;

	public SimpleChoiceFrom(Checker<P, T, PT> checker,
			ChoiceMethod<P, T, PT> comparator) {
		this.checker = checker;
		if (comparator != null) {
			this.comparator = comparator;
			matchingList = new PriorityQueue<T>(1, comparator);
		} else
			matchingList = new LinkedList<T>();
	}

	private Queue<T> matchingList;

	public Collection<T> findAll(P resources, Collection<T> reslist,
			PT parameters) {
		matchingList.clear();
		if (comparator != null)
			comparator.setPattern(resources, parameters);

		T res;
		for (Iterator<T> iterator = reslist.iterator(); iterator.hasNext();) {
			res = iterator.next();
			if (checker.check(resources, res, parameters))
				matchingList.add(res);
		}

		return matchingList;
	}

	public T find(P resources, Collection<T> reslist, PT parameters) {
		matchingList.clear();
		if (comparator != null)
			comparator.setPattern(resources, parameters);

		T res;
		for (Iterator<T> iterator = reslist.iterator(); iterator.hasNext();) {
			res = iterator.next();
			if (res != null && checker.check(resources, res, parameters))
				if (matchingList instanceof LinkedList)
					return res;
				else
					matchingList.add(res);
		}

		return matchingList.poll();
	}
}
