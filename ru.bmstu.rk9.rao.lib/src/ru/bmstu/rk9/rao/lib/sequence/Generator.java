package ru.bmstu.rk9.rao.lib.sequence;

import java.util.Iterator;

import ru.bmstu.rk9.rao.lib.thirdparty.AbstractGenerator;

public abstract class Generator<T> extends AbstractGenerator<T> implements ArbitraryTypeSequence<T> {
	private final Iterator<T> iterator;

	public Generator() {
		this.iterator = iterator();
	}

	@Override
	public T next() {
		return iterator.next();
	}
}
