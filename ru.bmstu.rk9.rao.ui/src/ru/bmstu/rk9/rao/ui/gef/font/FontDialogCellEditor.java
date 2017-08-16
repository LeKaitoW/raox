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
		FontDialog fontDialog = new FontDialog(cellEditorWindow.getShell());
		// String value = (String) getValue();

		if (getValue() != null) {
			Font serializableFontData = (Font) getValue();
			final FontData[] fontDatas = new FontData[] { serializableFontData.getFontData() };
			fontDialog.setFontList(fontDatas);
		}
		final FontData fontData = fontDialog.open();
		return fontData == null ? getValue() : new Font(fontData);
	}

}
