package ru.bmstu.rk9.rao.lib.resource;

import java.util.Collection;
import java.util.Collections;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

import ru.bmstu.rk9.rao.lib.exception.RaoLibException;

public class ResourceManager<T extends ComparableResource<T>> {
	private SortedMap<Integer, T> resources = new ConcurrentSkipListMap<Integer, T>();
	private Integer numberOfResources = 0;

	public ResourceManager() {
		this.numberOfResources = 0;
	}

	public void addResource(T res) {
		Integer number = numberOfResources++;

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

	// TODO do we need more efficient way to get by name?
	public T getResource(String name) {
		Collection<T> resources = getAll().stream().filter(r -> r.getName().equals(name)).collect(Collectors.toList());
		if (resources.size() != 1)
			throw new RaoLibException(
					"Exactly one resource with name \"" + name + "\" should exist, instead found " + resources.size());

		return resources.iterator().next();
	}

	public Collection<T> getAll() {
		return Collections.unmodifiableCollection(resources.values());
	}

	public Collection<T> getAccessible() {
		return Collections.unmodifiableCollection(
				resources.values().stream().filter((res) -> res.isAccessible()).collect(Collectors.toList()));
	}

	public T copyOnWrite(T resource) {
		resource.isShallowCopy = false;
		T copy = resource.deepCopy();
		resources.put(resource.number, copy);
		return copy;
	}

	public ResourceManager<T> deepCopy() {
		return copy(false);
	}

	public ResourceManager<T> shallowCopy() {
		return copy(true);
	}

	private ResourceManager<T> copy(boolean isShallow) {
		ResourceManager<T> copy = new ResourceManager<>();

		copy.numberOfResources = this.numberOfResources;
		for (Entry<Integer, T> entry : this.resources.entrySet()) {
			copy.resources.put(entry.getKey(),
					isShallow ? entry.getValue().shallowCopy() : entry.getValue().deepCopy());
		}

		return copy;
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
