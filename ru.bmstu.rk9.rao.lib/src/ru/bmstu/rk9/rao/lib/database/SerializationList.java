package ru.bmstu.rk9.rao.lib.database;

import java.util.Collections;
import java.util.List;

public class SerializationList {
	static final List<String> getNames() {
		return Collections.unmodifiableList(names);
	}

	public static final void setNames(List<String> names) {
		SerializationList.names = names;
	}

	private static List<String> names;
}