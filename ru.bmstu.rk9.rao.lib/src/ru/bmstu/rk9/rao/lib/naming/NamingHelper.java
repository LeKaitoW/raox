package ru.bmstu.rk9.rao.lib.naming;

import java.lang.reflect.Member;
import java.lang.reflect.Method;

//TODO bring order
public class NamingHelper {
	public final static String getModelProjectName(final String name) {
		return name.substring(name.indexOf('/') + 1, name.lastIndexOf('/'));
	}

	public final static String stripFirstPart(final String name) {
		return name.substring(name.indexOf('.') + 1);
	}

	public final static String getRelativeModelName(final String name) {
		return name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));
	}

	public final static String getLastPart(final String name) {
		return name.substring(name.lastIndexOf('.') + 1);
	}

	public final static String getFirstPart(final String name) {
		return name.substring(0, name.indexOf('.'));
	}

	public final static String createFullName(final String modelResourceName, final String nameGeneric) {
		return getModelProjectName(modelResourceName) + "." + getRelativeModelName(modelResourceName) + "."
				+ nameGeneric;
	}

	public final static String changeDollarToDot(final String name) {
		return name.replace('$', '.');
	}

	public final static String createFullNameForMember(final Member member) {
		return changeDollarToDot(member.getDeclaringClass().getName() + "." + member.getName());
	}

	public final static String createFullNameFromInitializeMethod(final Method method) {
		String originalName = method.getName().substring("initialize".length());
		originalName = Character.toLowerCase(originalName.charAt(0)) + originalName.substring(1);

		return changeDollarToDot(method.getDeclaringClass().getName() + "." + originalName);
	}
}
