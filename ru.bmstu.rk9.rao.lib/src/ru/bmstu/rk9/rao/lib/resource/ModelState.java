package ru.bmstu.rk9.rao.lib.resource;

public interface ModelState<T> {
	public void deploy();

	public T copy();

	public boolean checkEqual(T other);
}
