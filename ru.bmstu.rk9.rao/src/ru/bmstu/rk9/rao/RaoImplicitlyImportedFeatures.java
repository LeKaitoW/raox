package ru.bmstu.rk9.rao;

import java.util.List;

import org.eclipse.xtext.xbase.scoping.batch.ImplicitlyImportedFeatures;

import com.google.common.collect.Lists;

import ru.bmstu.rk9.rao.lib.runtime.RaoLibraryFeatures;

@SuppressWarnings("restriction")
public class RaoImplicitlyImportedFeatures extends ImplicitlyImportedFeatures {
	@Override
	protected List<Class<?>> getExtensionClasses() {
		List<Class<?>> classes = getRaoExtensionClasses();

		classes.addAll(super.getExtensionClasses());
		return classes;
	}

	@Override
	protected List<Class<?>> getStaticImportClasses() {
		List<Class<?>> classes = getRaoExtensionClasses();

		classes.addAll(super.getStaticImportClasses());
		return classes;
	}

	private List<Class<?>> getRaoExtensionClasses() {
		return Lists.<Class<?>> newArrayList(RaoLibraryFeatures.class);
	}
}
