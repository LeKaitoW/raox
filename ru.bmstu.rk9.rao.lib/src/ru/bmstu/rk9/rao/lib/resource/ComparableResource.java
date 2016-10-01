package ru.bmstu.rk9.rao.lib.resource;

import org.eclipse.xtext.xbase.lib.Functions.Function1;

public abstract class ComparableResource<T> extends Resource {
	public abstract boolean checkEqual(T other);

	public abstract T deepCopy();

	@SuppressWarnings("unchecked")
	public final T onlyif(final Function1<? super T, Boolean> predicate) {
		if (predicate.apply((T) this))
			return (T) this;

		return null;
	}

	@SuppressWarnings("unchecked")
	public T shallowCopy() {
		isShallowCopy = true;
		return (T) this;
	}
}
