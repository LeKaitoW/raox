package ru.bmstu.rk9.rao.lib.resource;

import java.util.Collection;
import java.util.Collections;

import java.util.Iterator;

import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

public class ResourceManager<T extends ComparableResource<T>> {
	private SortedMap<Integer, T> resources;

	private Integer resourceNumber;

	public void addResource(T res) {
		Integer number = resourceNumber++;

		res.setNumber(number);
		resources.put(number, res);
	}

	public void eraseResource(T res) {
		Integer number = res.getNumber();

		resources.remove(number);
	}

	public T getResource(int number) {
		return resources.get(number);
	}

	public Collection<T> getAll() {
		return Collections.unmodifiableCollection(resources.values());
	}

	public Collection<T> getAccessible() {
		return Collections.unmodifiableCollection(
				resources.values().stream().filter((res) -> res.isAccessible()).collect(Collectors.toList()));
	}

	public ResourceManager() {
		this.resources = new ConcurrentSkipListMap<Integer, T>();
		this.resourceNumber = 0;
	}

	private ResourceManager(ResourceManager<T> source) {
		this.resources = new ConcurrentSkipListMap<Integer, T>(source.resources);
		this.resourceNumber = source.resourceNumber;
	}

	public ResourceManager<T> copy() {
		return new ResourceManager<T>(this);
	}

	public boolean checkEqual(ResourceManager<T> other) {
		if (this.resources.size() != other.resources.size())
			return false;

		Iterator<T> itThis = this.resources.values().iterator();
		Iterator<T> itOther = other.resources.values().iterator();

		for (int i = 0; i < this.resources.size(); i++) {
			T resThis = itThis.next();
			T resOther = itOther.next();

			if (resThis != resOther && !resThis.checkEqual(resOther))
				return false;
		}
		return true;
	}
}
