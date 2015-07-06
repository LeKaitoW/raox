package ru.bmstu.rk9.rao.lib.database;

import java.util.Collections;
import java.util.List;

public class SerializationObjectsNames {
	static final List<String> get() {
		return Collections.unmodifiableList(names);
	}

	public static final void set(List<String> names) {
		SerializationObjectsNames.names = names;
	}

	private static List<String> names;
}