package ru.bmstu.rk9.rao.lib.sequence;

import java.util.List;

public class Values<T> extends InfiniteSequence<T> {
	public Values(List<T> enumerationList) {
		super(enumerationList);
	}
}
