package ru.bmstu.rk9.rao.ui.gef.alignment;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class AlignmentDialogCellEditor extends DialogCellEditor {

	protected AlignmentDialogCellEditor(Composite parent) {
		super(parent);
	}

	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		AlignmentDialog alignmentDialog = new AlignmentDialog(cellEditorWindow.getShell());

		if (getValue() != null) {
			Alignment alignment = (Alignment) getValue();
			alignmentDialog.setAlignment(alignment);
		}

		if (alignmentDialog.open() == Window.OK) {
			return alignmentDialog.getAlignment();
		}
		return getValue();
	}

	@Override
	protected void updateContents(Object value) {
		if (getDefaultLabel() == null) {
			return;
		}
		if (value != null) {
			getDefaultLabel().setText(((Alignment) value).getDescription());
		}
	}

}
