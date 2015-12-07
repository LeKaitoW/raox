package ru.bmstu.rk9.rao;

import java.util.List;

import org.eclipse.xtext.xbase.scoping.batch.ImplicitlyImportedFeatures;

import com.google.common.collect.Lists;

import ru.bmstu.rk9.rao.lib.runtime.Model;

@SuppressWarnings("restriction")
public class RaoImplicitlyImportedFeatures extends ImplicitlyImportedFeatures {
	@Override
	protected List<Class<?>> getExtensionClasses() {
		List<Class<?>> classes = Lists.<Class<?>> newArrayList(Model.class);

		classes.addAll(super.getExtensionClasses());
		return classes;
	}

	@Override
	protected List<Class<?>> getStaticImportClasses() {
		List<Class<?>> classes = Lists.<Class<?>> newArrayList(Model.class);

		classes.addAll(super.getStaticImportClasses());
		return classes;
	}
}
