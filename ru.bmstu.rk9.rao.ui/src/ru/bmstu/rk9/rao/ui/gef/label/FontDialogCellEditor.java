package ru.bmstu.rk9.rao.ui.gef.label;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FontDialog;

public class FontDialogCellEditor extends DialogCellEditor {

	protected FontDialogCellEditor(Composite parent) {
		super(parent);
	}

	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		FontDialog ftDialog = new FontDialog(cellEditorWindow.getShell());
		// String value = (String) getValue();

		if (getValue() != null) {
			Font font = (Font) getValue();
			FontData[] fontData = font.getFontData();
			ftDialog.setFontList(fontData);
		}
		FontData fData = ftDialog.open();

		if (fData != null) {
			// TODO первое поле - Device, можно брать из старого Font, но так
			// пока работает
			Font font = new Font(null, fData);
			return font;
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
			Font font = (Font) value;
			text = FontLabelProvider.formatFont(font);
		}
		getDefaultLabel().setText(text);
	}

}