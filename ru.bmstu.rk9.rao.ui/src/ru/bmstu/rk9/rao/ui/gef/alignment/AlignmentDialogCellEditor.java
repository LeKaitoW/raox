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
		AlignmentDialog alDialog = new AlignmentDialog(cellEditorWindow.getShell());

		if (getValue() != null) {
			Alignment alignment = (Alignment) getValue();
			alDialog.setAlignment(alignment);
		}

		if (alDialog.open() == Window.OK) {
			return alDialog.getAlignment();
		}
		return getValue();
	}

	@Override
	protected void updateContents(Object value) {
		if (getDefaultLabel() == null) {
			return;
		}

		String text = "";
		if (value != null) {
			text = ((Alignment) value).getDescription();
		}
		getDefaultLabel().setText(text);
	}

}
