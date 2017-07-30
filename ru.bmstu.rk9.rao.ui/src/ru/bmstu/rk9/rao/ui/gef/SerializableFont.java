package ru.bmstu.rk9.rao.ui.gef;

import java.io.Serializable;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

public final class SerializableFont implements Serializable {

	private static final long serialVersionUID = 1;

	private SerializableFontData[] serializableFontData;
	private transient Font font;

	public SerializableFont(Font font) {
		this(font.getFontData());
	}

	public SerializableFont(FontData[] fontData) {
		this.serializableFontData = new SerializableFontData[fontData.length];
		for (int i = 0; i < fontData.length; i++) {
			serializableFontData[i] = new SerializableFontData(fontData[i]);
		}
	}

	public SerializableFont(SerializableFontData[] fontData) {
		serializableFontData = fontData;
	}

	public SerializableFont(SerializableFontData fontData) {
		serializableFontData = new SerializableFontData[] { fontData };
	}

	public Font getFont() {
		if (font == null) {
			font = new Font(null, getFontData());
		}
		return font;
	}

	public FontData[] getFontData() {
		FontData[] fontDataArray = new FontData[serializableFontData.length];
		for (int i = 0; i < serializableFontData.length; i++) {
			fontDataArray[i] = serializableFontData[i].getFontData();
		}
		return fontDataArray;

	}
}
