package ru.bmstu.rk9.rao.lib.runtime;

import java.util.ArrayList;

import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;

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

	public static <T, C extends Comparable<? super C>> T minBySafe(final Iterable<T> iterable,
			final Function1<? super T, C> compareBy) {
		if (!iterable.iterator().hasNext())
			return null;

		return IteratorExtensions.minBy(iterable.iterator(), compareBy);
	}

	public static <T, C extends Comparable<? super C>> T maxBySafe(final Iterable<T> iterable,
			final Function1<? super T, C> compareBy) {
		if (!iterable.iterator().hasNext())
			return null;

		return IteratorExtensions.maxBy(iterable.iterator(), compareBy);
	}
}
