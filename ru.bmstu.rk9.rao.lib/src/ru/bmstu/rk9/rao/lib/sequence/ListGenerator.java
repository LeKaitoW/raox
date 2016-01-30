package ru.bmstu.rk9.rao.lib.sequence;

import java.util.List;

public class ListGenerator<T> extends InfiniteGenerator<T> {
	private final List<T> elements;

	public ListGenerator(List<T> elements) {
		this.elements = elements;
	}

	@Override
	protected void generate() throws InterruptedException {
		for (T element : elements)
			yield(element);
	}
}
