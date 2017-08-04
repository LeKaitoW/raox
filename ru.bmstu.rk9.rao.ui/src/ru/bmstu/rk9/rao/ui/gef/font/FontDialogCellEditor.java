package ru.bmstu.rk9.rao.ui.gef.font;

import org.eclipse.jface.viewers.DialogCellEditor;
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
			SerializableFontData font = (SerializableFontData) getValue();
			FontData[] fontData = new FontData[] { font.getFontData() };
			ftDialog.setFontList(fontData);
		}
		FontData fData = ftDialog.open();

		if (fData != null) {
			SerializableFontData font = new SerializableFontData(fData);
			return font;
		}
		return getValue();
	}

}
