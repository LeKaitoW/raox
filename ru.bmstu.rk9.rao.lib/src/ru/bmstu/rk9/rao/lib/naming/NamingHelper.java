package ru.bmstu.rk9.rao.lib.naming;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

//TODO bring order
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

	public final static String createFullNameFromInitializeMethod(final Method method) {
		String originalName = method.getName().substring("initialize".length());
		originalName = Character.toLowerCase(originalName.charAt(0)) + originalName.substring(1);

		return changeDollarToDot(method.getDeclaringClass().getName() + "." + originalName);
	}
}
