package ru.bmstu.rk9.rao.lib.resource;

import java.util.Collection;
import java.util.Collections;

import java.util.Iterator;

import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class ResourceManager<T extends ComparableResource<T>> {
	private SortedMap<Integer, T> listResources;

	private Integer resourceNumber;

	public void addResource(T res) {
		Integer number = resourceNumber++;

		res.setNumber(number);
		listResources.put(number, res);
	}

	public void eraseResource(T res) {
		Integer number = res.getNumber();

		listResources.remove(number);
	}

	public T getResource(int number) {
		return listResources.get(number);
	}

	public Collection<T> getAll() {
		return Collections.unmodifiableCollection(listResources.values());
	}

	public ResourceManager() {
		this.listResources = new ConcurrentSkipListMap<Integer, T>();
		this.resourceNumber = 0;
	}

	private ResourceManager(ResourceManager<T> source) {
		this.listResources = new ConcurrentSkipListMap<Integer, T>(source.listResources);
		this.resourceNumber = source.resourceNumber;
	}

	public ResourceManager<T> copy() {
		return new ResourceManager<T>(this);
	}

	public boolean checkEqual(ResourceManager<T> other) {
		if (this.listResources.size() != other.listResources.size())
			return false;

		Iterator<T> itThis = this.listResources.values().iterator();
		Iterator<T> itOther = other.listResources.values().iterator();

		for (int i = 0; i < this.listResources.size(); i++) {
			T resThis = itThis.next();
			T resOther = itOther.next();

			if (resThis != resOther && !resThis.checkEqual(resOther))
				return false;
		}
		return true;
	}
}
