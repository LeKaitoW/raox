package ru.bmstu.rk9.rao.lib.sequence;

import java.util.List;

import ru.bmstu.rk9.rao.lib.thirdparty.Generator;

public class ListGenerator<T> extends Generator<T> {
	private final List<T> elements;

	public ListGenerator(List<T> elements) {
		this.elements = elements;
	}

	protected void run() throws InterruptedException {
		while (true)
			for (T element : elements)
				yield(element);
	}
}

