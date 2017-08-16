package ru.bmstu.rk9.rao.ui.gef.font;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;

public final class Font implements Serializable {

	private static final long serialVersionUID = 1;

	private String name;
	private int height;
	private int style;
	private transient org.eclipse.swt.graphics.Font font;

	public Font(org.eclipse.swt.graphics.Font font) {
		this(font.getFontData());
	}

	public Font(FontData[] fontData) {
		this(fontData[0]);
	}

	public Font(FontData fontData) {
		this(fontData.getName(), fontData.getHeight(), fontData.getStyle());
	}

	private Font(String name, int height, int style) {
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
		if (style == SWT.NORMAL)
			return "Normal";

		List<String> list = new ArrayList<>();

		if ((SWT.BOLD & style) == SWT.BOLD) {
			list.add("Bold");
		}
		if ((SWT.ITALIC & style) == SWT.ITALIC) {
			list.add("Italic");
		}
		if ((1 << 5 & style) == 1 << 5) {
			list.add("Oblique");
		}
		return String.join(" ", list);
	}

	public FontData getFontData() {
		return new FontData(getName(), getHeight(), getStyle());
	}

	public org.eclipse.swt.graphics.Font getSwtFont() {
		if (font == null)
			font = new org.eclipse.swt.graphics.Font(null, getFontData());

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
		Font other = (Font) obj;
		if (height != other.height)
			return false;
		if (name != null && !name.equals(other.name))
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
