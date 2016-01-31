package ru.bmstu.rk9.rao.lib.runtime;

import java.util.ArrayList;

import org.eclipse.xtext.xbase.lib.CollectionLiterals;

import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class Model {
	public static final double getCurrentTime() {
		return Simulator.getTime();
	}

	@SafeVarargs
	public static <T> ArrayList<T> array(T... initial) {
		return CollectionLiterals.newArrayList(initial);
	}
}
