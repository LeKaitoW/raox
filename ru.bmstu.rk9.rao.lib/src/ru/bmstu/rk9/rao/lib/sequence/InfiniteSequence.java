package ru.bmstu.rk9.rao.lib.sequence;

import java.util.Iterator;

public abstract class InfiniteSequence<T> implements ArbitraryTypeSequence<T> {
	InfiniteSequence(Iterable<T> iterable) {
		this.iterable = iterable;
		restartIterator();
	}

	@Override
	public T next() {
		if (!iterator.hasNext())
			restartIterator();

		return iterator.next();
	}

	protected void restartIterator() {
		this.iterator = iterable.iterator();
	}

	protected Iterator<T> iterator;
	protected Iterable<T> iterable;
}
