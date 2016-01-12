package ru.bmstu.rk9.rao.ui.labeling

import com.google.inject.Inject

class RaoLabelProvider extends org.eclipse.xtext.xbase.ui.labeling.XbaseLabelProvider {
	@Inject
	new(org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider delegate) {
		super(delegate);
	}
}