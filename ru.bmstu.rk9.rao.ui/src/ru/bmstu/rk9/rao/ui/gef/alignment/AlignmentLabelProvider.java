package ru.bmstu.rk9.rao.ui.gef.alignment;

import org.eclipse.jface.viewers.LabelProvider;

public class AlignmentLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		return ((Alignment) element).getDescription();
	}
}
