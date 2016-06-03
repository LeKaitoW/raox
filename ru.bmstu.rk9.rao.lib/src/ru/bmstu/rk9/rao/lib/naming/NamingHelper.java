package ru.bmstu.rk9.rao.lib.naming;

import java.lang.reflect.Field;

public class NamingHelper {
	public final static String getModelProjectName(final String name) {
		return name.substring(name.indexOf('/') + 1, name.lastIndexOf('/'));
	}

	public final static String getNameWithoutProject(final String name) {
		return name.substring(name.indexOf('.') + 1);
	}

	public final static String getRelativeModelName(final String name) {
		return name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));
	}

	public final static String getRelativeElementName(final String name) {
		return name.substring(name.lastIndexOf('.') + 1);
	}

	public final static String getNameOfElementModel(final String name) {
		return name.substring(0, name.indexOf('.'));
	}

	public final static String createFullName(final String modelResourceName, final String nameGeneric) {
		return getModelProjectName(modelResourceName) + "." + getRelativeModelName(modelResourceName) + "."
				+ nameGeneric;
	}

	public final static String changeDollarToDot(final String name) {
		return name.replace('$', '.');
	}

	public final static String createFullNameForField(final Field field) {
		return changeDollarToDot(field.getDeclaringClass().getName() + "." + field.getName());
	}
}