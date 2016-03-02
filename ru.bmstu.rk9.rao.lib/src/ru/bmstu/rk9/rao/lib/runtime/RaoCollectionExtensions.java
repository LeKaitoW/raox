package ru.bmstu.rk9.rao.lib.runtime;

import java.util.ArrayList;

import org.eclipse.xtext.xbase.lib.CollectionLiterals;

public class RaoCollectionExtensions {
	@SafeVarargs
	public static <T> ArrayList<T> array(T... initial) {
		return CollectionLiterals.newArrayList(initial);
	}

	public static <T> T any(Iterable<T> iterable) {
		if (!iterable.iterator().hasNext())
			return null;

		return iterable.iterator().next();
	}
}
