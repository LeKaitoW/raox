package ru.bmstu.rk9.rao.ui.gef.alignment;

import org.eclipse.jface.viewers.LabelProvider;

public class AlignmentLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		if (!(element instanceof Alignment)) {
			throw new IllegalArgumentException("Supported only " + Alignment.class.getCanonicalName()
					+ " class, but received " + element.getClass().getCanonicalName());
		}
		return ((Alignment) element).getDescription();
	}
}
