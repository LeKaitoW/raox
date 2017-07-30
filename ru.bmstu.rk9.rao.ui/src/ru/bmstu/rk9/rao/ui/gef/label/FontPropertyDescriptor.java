package ru.bmstu.rk9.rao.ui.gef.label;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class FontPropertyDescriptor extends PropertyDescriptor {

	public FontPropertyDescriptor(Object id, String displayName) {
		super(id, displayName);
		setLabelProvider(new FontLabelProvider());
	}

	@Override
	public CellEditor createPropertyEditor(Composite parent) {
		CellEditor editor = new FontDialogCellEditor(parent);
		if (getValidator() != null)
			editor.setValidator(getValidator());
		return editor;
	}
}