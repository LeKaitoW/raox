package ru.bmstu.rk9.rao;

import org.eclipse.xtext.xbase.compiler.XbaseCompiler;
import org.eclipse.xtext.xbase.scoping.batch.ImplicitlyImportedFeatures;
import org.eclipse.xtext.xbase.typesystem.computation.XbaseTypeComputer;

import com.google.inject.name.Names;

import ru.bmstu.rk9.rao.compiler.RaoCompiler;
import ru.bmstu.rk9.rao.typesystem.RaoTypeComputer;

@SuppressWarnings("restriction")
public class RaoRuntimeModule extends ru.bmstu.rk9.rao.AbstractRaoRuntimeModule {

	@Override
	public void configureIScopeProviderDelegate(com.google.inject.Binder binder) {
		binder.bind(org.eclipse.xtext.scoping.IScopeProvider.class)
				.annotatedWith(
						Names.named(org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider.NAMED_DELEGATE))
				.to(RaoXImportSectionNamespaceScopeProvider.class);
	}

	public Class<? extends XbaseCompiler> bindXbaseCompiler() {
		return RaoCompiler.class;
	}

	public Class<? extends XbaseTypeComputer> bindXbaseTypeComputer() {
		return RaoTypeComputer.class;
	}

	public Class<? extends ImplicitlyImportedFeatures> bindImplicitlyImportedFeatures() {
		return RaoImplicitlyImportedFeatures.class;
	}
}
