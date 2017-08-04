package ru.bmstu.rk9.rao.ui.gef.alignment;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class AlignmentPropertyDescriptor extends PropertyDescriptor {

	public AlignmentPropertyDescriptor(Object id, String displayName) {
		super(id, displayName);
		setLabelProvider(new AlignmentLabelProvider());
	}

	@Override
	public CellEditor createPropertyEditor(Composite parent) {
		CellEditor editor = new AlignmentDialogCellEditor(parent);
		if (getValidator() != null)
			editor.setValidator(getValidator());
		return editor;
	}
}
