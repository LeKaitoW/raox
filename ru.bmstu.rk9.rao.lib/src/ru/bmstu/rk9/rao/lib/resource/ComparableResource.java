package ru.bmstu.rk9.rao.lib.resource;

public abstract class ComparableResource<T> extends Resource {
	public abstract boolean checkEqual(T other);
}
