package ru.bmstu.rk9.rao.lib.pattern;

import java.util.Collection;

public class Select {
	public static interface Checker<T> {
		public boolean check(T res);
	}

	public static <T> boolean exist(Collection<T> resources, Checker<T> checker) {
		for (T res : resources)
			if (checker.check(res))
				return true;
		return false;
	}

	public static <T> boolean notExist(Collection<T> resources, Checker<T> checker) {
		for (T res : resources)
			if (checker.check(res))
				return false;
		return true;
	}

	public static <T> boolean forAll(Collection<T> resources, Checker<T> checker) {
		for (T res : resources)
			if (!checker.check(res))
				return false;
		return true;
	}

	public static <T> boolean except(Collection<T> resources, Checker<T> checker) {
		for (T res : resources)
			if (!checker.check(res))
				return true;
		return false;
	}

	public static <T> boolean empty(Collection<T> resources, Checker<T> checker) {
		return notExist(resources, checker);
	}

	public static <T> int size(Collection<T> resources, Checker<T> checker) {
		int count = 0;
		for (T res : resources)
			if (checker.check(res))
				count++;
		return count;
	}
}
