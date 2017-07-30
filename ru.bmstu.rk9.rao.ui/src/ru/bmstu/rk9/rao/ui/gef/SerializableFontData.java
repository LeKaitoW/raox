package ru.bmstu.rk9.rao.ui.gef;

import java.io.Serializable;

import org.eclipse.swt.graphics.FontData;

public final class SerializableFontData implements Serializable {

	private static final long serialVersionUID = 1;

	private String name;
	private int height;
	private int style;

	public SerializableFontData(String name, int height, int style) {
		this.name = name;
		this.height = height;
		this.style = style;
	}

	public SerializableFontData(FontData fontData) {
		this(fontData.getName(), fontData.getHeight(), fontData.getStyle());
	}

	public String getName() {
		return name;
	}

	public int getHeight() {
		return height;
	}

	public int getStyle() {
		return style;
	}

	public FontData getFontData() {
		return new FontData(getName(), getHeight(), getStyle());
	}

}
