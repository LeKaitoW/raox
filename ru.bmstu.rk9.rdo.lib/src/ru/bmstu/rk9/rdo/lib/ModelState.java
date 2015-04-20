package ru.bmstu.rk9.rdo.lib;

public interface ModelState<T> {
	public void deploy();

	public T copy();

	public boolean checkEqual(T other);
}
