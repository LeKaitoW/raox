package ru.bmstu.rk9.rao.ui.gef.label;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FontDialog;

import ru.bmstu.rk9.rao.ui.gef.SerializableFont;

public class FontDialogCellEditor extends DialogCellEditor {

	protected FontDialogCellEditor(Composite parent) {
		super(parent);
	}

	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		FontDialog ftDialog = new FontDialog(cellEditorWindow.getShell());
		// String value = (String) getValue();

		if (getValue() != null) {
			SerializableFont font = (SerializableFont) getValue();
			FontData[] fontData = font.getFontData();
			ftDialog.setFontList(fontData);
		}
		FontData fData = ftDialog.open();

		if (fData != null) {
			SerializableFont font = new SerializableFont(fData);
			return font;
		}
		return getValue();
	}

}