package ru.bmstu.rk9.rao.ui.gef;

import org.eclipse.swt.widgets.Display;

import ru.bmstu.rk9.rao.ui.gef.font.SerializableFontData;

public final class DefaultFonts {

	/**
	 * Экземпляры запрещены
	 */
	private DefaultFonts() {
	}

	public static final SerializableFontData DEFAULT_FONT = new SerializableFontData(
			Display.getDefault().getSystemFont());
}
