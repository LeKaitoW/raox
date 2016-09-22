package ru.bmstu.rk9.rao;

import java.util.List;

import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.scoping.batch.ImplicitlyImportedFeatures;

import com.google.common.collect.Lists;

import ru.bmstu.rk9.rao.lib.runtime.RaoCollectionExtensions;
import ru.bmstu.rk9.rao.lib.runtime.RaoFactory;
import ru.bmstu.rk9.rao.lib.runtime.RaoObjectExtensions;
import ru.bmstu.rk9.rao.lib.runtime.RaoRuntime;

@SuppressWarnings("restriction")
public class RaoImplicitlyImportedFeatures extends ImplicitlyImportedFeatures {
	@Override
	protected List<Class<?>> getExtensionClasses() {
		List<Class<?>> originalClasses = super.getExtensionClasses();
		/* NB: see comment to RaoObjectExtensions class for explanation */
		originalClasses.remove(ObjectExtensions.class);
		originalClasses.add(RaoObjectExtensions.class);
		List<Class<?>> classes = getRaoExtensionClasses();

		classes.addAll(originalClasses);
		return classes;
	}

	@Override
	protected List<Class<?>> getStaticImportClasses() {
		List<Class<?>> classes = getRaoExtensionClasses();

		classes.addAll(super.getStaticImportClasses());
		return classes;
	}

	private List<Class<?>> getRaoExtensionClasses() {
		return Lists.<Class<?>> newArrayList(RaoRuntime.class, RaoCollectionExtensions.class, RaoFactory.class);
	}
}
