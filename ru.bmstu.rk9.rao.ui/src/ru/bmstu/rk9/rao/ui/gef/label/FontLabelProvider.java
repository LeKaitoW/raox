package ru.bmstu.rk9.rao.ui.gef.label;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

public class FontLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		Font font = (Font) element;
		return formatFont(font);
	}

	public static String formatFont(Font font) {
		FontData[] fontData = font.getFontData();
		String text = "";
		for (FontData data : fontData) {
			String bitStyle = Integer.toBinaryString(data.getStyle());
			text += String.format("%s %s %s ", data.getName(), data.getHeight(), bitStyle);
		}
		return text;
	}

}
