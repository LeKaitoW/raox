package ru.bmstu.rk9.rao.lib.sequence;

import java.util.List;

public class ListSequence<T> extends GeneratorSequence<T> {
	public ListSequence(List<T> enumerationList) {
		super(new ListGenerator<T>(enumerationList));
	}
}
