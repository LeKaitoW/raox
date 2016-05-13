package ru.bmstu.rk9.rao.ui.gef;

import java.util.Arrays;
import org.eclipse.emf.common.ui.celleditor.ExtendedComboBoxCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class EnumComboBoxPropertyDescriptor<T extends Enum<T>> extends PropertyDescriptor {

	private final Class<T> enumClass;
	private final ILabelProvider labelProvider = new LabelProvider();

	public EnumComboBoxPropertyDescriptor(Object id, String displayName, Class<T> enumClass) {
		super(id, displayName);
		this.enumClass = enumClass;
	}

	@Override
	public CellEditor createPropertyEditor(Composite parent) {
		CellEditor editor = new ExtendedComboBoxCellEditor(parent, Arrays.asList(enumClass.getEnumConstants()),
				labelProvider, SWT.READ_ONLY);
		if (getValidator() != null) {
			editor.setValidator(getValidator());
		}
		return editor;
	}

	@Override
	public ILabelProvider getLabelProvider() {
		return labelProvider;
	}
}
