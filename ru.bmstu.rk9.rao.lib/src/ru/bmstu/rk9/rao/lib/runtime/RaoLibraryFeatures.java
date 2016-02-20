package ru.bmstu.rk9.rao.lib.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

import ru.bmstu.rk9.rao.lib.dpt.Logic;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class RaoLibraryFeatures {
	public static final double getCurrentTime() {
		return Simulator.getTime();
	}

	@SafeVarargs
	public static <T> ArrayList<T> array(T... initial) {
		return CollectionLiterals.newArrayList(initial);
	}

	public static <T> T min(List<T> list, Comparator<? super T> c) {
		if (list.isEmpty())
			return null;

		Collections.sort(list, c);
		return list.get(0);
	}

	public static <T> T max(List<T> list, Comparator<? super T> c) {
		if (list.isEmpty())
			return null;

		Collections.sort(list, c);
		return list.get(list.size() - 1);
	}

	public static <T> T any(List<T> list) {
		if (list.isEmpty())
			return null;

		return list.get(0);
	}

	public static Logic newLogic(Procedure1<? super Logic> initializer) {
		Logic logic = new Logic();

		if (initializer != null) {
			try {
				initializer.apply(logic);
			} catch (Exception e) {
				// TODO something
			}
		}

		return logic;
	}
}
