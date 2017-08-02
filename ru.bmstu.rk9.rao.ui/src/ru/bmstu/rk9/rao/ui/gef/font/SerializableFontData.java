package ru.bmstu.rk9.rao.ui.gef.font;

import java.io.Serializable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

public final class SerializableFontData implements Serializable {

	private static final long serialVersionUID = 1;

	private String name;
	private int height;
	private int style;
	private transient Font font;

	public SerializableFontData(Font font) {
		this(font.getFontData());
	}

	public SerializableFontData(FontData[] fontData) {
		this(fontData[0]);
	}

	public SerializableFontData(FontData fontData) {
		this(fontData.getName(), fontData.getHeight(), fontData.getStyle());
	}

	public SerializableFontData(String name, int height, int style) {
		this.name = name;
		this.height = height;
		this.style = style;
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

	public String getTextStyle() {
		String text = "";
		if ((SWT.BOLD & style) == SWT.BOLD) {
			text += "Bold ";
		}
		if ((SWT.ITALIC & style) == SWT.ITALIC) {
			text += "Italic ";
		}
		if ((1 << 5 & style) == 1 << 5) {
			text += "Oblique ";
		}
		return text.trim();
	}

	public FontData getFontData() {
		return new FontData(getName(), getHeight(), getStyle());
	}

	public Font getFont() {
		if (font == null) {
			font = new Font(null, getFontData());
		}
		return font;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + height;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + style;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SerializableFontData other = (SerializableFontData) obj;
		if (height != other.height)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (style != other.style)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return name + " " + height + " " + getTextStyle();
	}
}
