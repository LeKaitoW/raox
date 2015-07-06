package ru.bmstu.rk9.rao.lib.naming;

public class NamingHelper {
	public static final String getRelativeModelName(final String name) {
		return name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));
	}

	public static final String getRelativeElementName(final String name) {
		return name.substring(name.lastIndexOf('.') + 1);
	}

	public static final String getNameOfElementModel(final String name) {
		return name.substring(0, name.indexOf('.'));
	}
}