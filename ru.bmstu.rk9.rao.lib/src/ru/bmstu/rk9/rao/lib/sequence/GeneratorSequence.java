package ru.bmstu.rk9.rao.lib.sequence;

import java.util.Iterator;

import ru.bmstu.rk9.rao.lib.thirdparty.Generator;

public class GeneratorSequence<T> implements ArbitraryTypeSequence<T> {
	private final Iterator<T> iterator;

	public GeneratorSequence(Generator<T> generator) {
		this.iterator = generator.iterator();
	}

	@Override
	public T next() {
		return iterator.next();
	}
}
